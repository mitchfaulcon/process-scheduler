package se306.scheduler.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import se306.scheduler.graph.PartialSchedule;

public class BNBAlgorithmPara extends BNBAlgorithm {

	public BNBAlgorithmPara(int numProcessors, int nThreads) {
		super(numProcessors);
		this.nThreads = nThreads;
	}

	private int nThreads;
	private volatile boolean earlyStop = false;

	class BNBTask implements Runnable {
		Deque<PartialSchedule> stack;
		ArrayList<Deque<PartialSchedule>> stacks;
		int threadNum;

		public BNBTask(int threadNum, ArrayList<Deque<PartialSchedule>> stacks) {
			this.stack = stacks.get(threadNum);
			this.stacks = stacks;
		}

		/**
		 * Same as sequential method
		 * @see BNBAlgorithm#schedule();
		 */
		@Override
		public void run() {
			do {
				while (!stack.isEmpty() && !earlyStop) {
					PartialSchedule ps = stack.pollFirst();
					if(ps != null) {
						if(bnb(ps, stack)) {
							earlyStop = true;
						}
					}
				}
			} while (findMore());
		}

		/**
		 * Steals schedules from other threads if we're done
		 * @return true if more work was found, else false
		 */
		private boolean findMore() {
			for(Deque<PartialSchedule> other: stacks) {
				if(other == stack || other.size() <= 1) continue;
				PartialSchedule stolen = other.pollLast();
				if(stolen != null) {
					bnb(stolen, stack);
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public void schedule() {
	    addedScheduleIDs = new ConcurrentHashMap<>();

        setLowerBounds();

        
        // Generate enough schedules to split across the threads.
		Deque<PartialSchedule> temp = new ArrayDeque<>();
		temp.add(new PartialSchedule(graph));
		while (temp.size() < nThreads) {
			bnb(temp.pollFirst(), temp);
		}

		// Assign schedules
		ArrayList<Deque<PartialSchedule>> stacks = new ArrayList<>();
		for (int i = 0; i < nThreads; i++) {
			stacks.add(new ConcurrentLinkedDeque<>());
		}
		for (int i = 0; !temp.isEmpty(); i++) {
			stacks.get(i % nThreads).addLast(temp.pollFirst());
		}

		// Create worker threads
		Thread[] threads = new Thread[nThreads];
		for (int i = 0; i < nThreads; i++) {
			Thread thread = new Thread(new BNBTask(i, stacks), "BNB Thread " + i);
			threads[i] = thread;
		}

		try {
			for (Thread thread : threads) {
				thread.start();
			}
			for (Thread thread : threads) {
				thread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		completed(bestSchedule);
	}
}
