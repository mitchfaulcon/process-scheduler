package se306.scheduler.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import se306.scheduler.graph.PartialSchedule;

/**
 * Branch and Bound DFS implementation to find the optimal schedule for a list
 * of tasks. Much like DFS, but creates a lower-bound estimate on each branch
 * before exploring it in order to gauge whether it is worth exploring.
 */
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
			this.threadNum = threadNum;
		}

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

//        bestSchedule = greedySchedule();
//        addedScheduleIDs.put(bestSchedule.toString(), null);

        // use a greedy algorithm to find a decent initial bound
//        bestMakespan = bestSchedule.getMakespan();
//        updateSchedule(bestSchedule);



        setLowerBounds();

		Deque<PartialSchedule> temp = new ArrayDeque<>();
		temp.add(new PartialSchedule(graph));
		while (temp.size() < nThreads) {
			bnb(temp.pollFirst(), temp);
		}

		ArrayList<Deque<PartialSchedule>> stacks = new ArrayList<>();
		for (int i = 0; i < nThreads; i++) {
			stacks.add(new ConcurrentLinkedDeque<>());
		}
		for (int i = 0; !temp.isEmpty(); i++) {
			stacks.get(i % nThreads).addLast(temp.pollFirst());
		}

		System.out.println("Creating threads");
		Thread[] threads = new Thread[nThreads];
		for (int i = 0; i < nThreads; i++) {
			Thread thread = new Thread(new BNBTask(i, stacks), "BNB Thread " + i);
			threads[i] = thread;
		}

		try {
			for (Thread thread : threads) {
				thread.start();
			}
			System.out.println("Waiting for threads");
			for (Thread thread : threads) {
				thread.join();
			}
			System.out.println("Completed");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		completed(bestSchedule);
	}
}
