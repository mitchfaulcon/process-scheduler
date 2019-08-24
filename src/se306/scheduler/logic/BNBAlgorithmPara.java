package se306.scheduler.logic;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;

/**
 * Branch and Bound DFS implementation to find the optimal schedule for a list
 * of tasks. Much like DFS, but creates a lower-bound estimate on each branch
 * before exploring it in order to gauge whether it is worth exploring.
 */
public class BNBAlgorithmPara extends Algorithm {

	public BNBAlgorithmPara(int numProcessors, int nThreads) {
		super(numProcessors);
		this.nThreads = nThreads;
	}

	private int nThreads;
	private PartialSchedule bestSchedule = null;
	private volatile int bestMakespan = Integer.MAX_VALUE;
	private volatile boolean earlyStop = false;
//	private AtomicInteger total = new AtomicInteger(); // remove or add as stat

	class BNBTask implements Runnable {
		ConcurrentLinkedDeque<PartialSchedule> stack;
		ArrayList<ConcurrentLinkedDeque<PartialSchedule>> stacks;
		int threadNum;

		public BNBTask(int threadNum, ArrayList<ConcurrentLinkedDeque<PartialSchedule>> stacks) {
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
			for(ConcurrentLinkedDeque<PartialSchedule> other: stacks) {
				if(other == stack || other.size() <= 1) continue;
				PartialSchedule stolen = other.pollLast();
				if(stolen != null) {
					bnb(stolen, stack);
//					System.out.println(threadNum + " Stole");
					return true;
				}
			}
//			System.out.println(threadNum + " Quitting");
			return false;
		}

	}

	@Override
	public void schedule() {
		setLowerBounds();

		ArrayDeque<PartialSchedule> temp = new ArrayDeque<>();
		temp.add(new PartialSchedule(graph));
		while (temp.size() < nThreads) {
			bnb(temp.pollFirst(), temp);
		}

		ArrayList<ConcurrentLinkedDeque<PartialSchedule>> stacks = new ArrayList<>();
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

//		System.out.println("Visited " + total.get());
		completed(bestSchedule);
	}

	private boolean bnb(PartialSchedule state, Deque<PartialSchedule> stack) {
//		total.incrementAndGet();
		// all nodes have been assigned to a processor
		if (state.allVisited()) {
			int makespan = state.getMakespan();

			// check if the current solution is better than the best one found so far
			synchronized (this) {
				if (makespan < bestMakespan) {
					System.out.println(Thread.currentThread().getName() + ": New best " + makespan + " Stack " + stack.size());//+ " After: " + total);

					bestMakespan = makespan;
					bestSchedule = state;

					// Update listener with new schedule
					updateSchedule(state);
				}
			}
			// regardless of whether the schedule is best, one complete schedule will be removed
			updateBranchCut(0, 1);

			// if the makespan is the critical path from the first node to the last then it
			// is an optimal solution so break while loop
			return makespan == state.getVisited().get(0).getLBWeight();
		}
		for (Node node : state.getUnvisitedNodes()) {
			// check if the node's parents have all been scheduled
			if (state.dependenciesSatisfied(node)) {
				// create new states by adding the new node to every processor
				processorLoop:
				for (int p = 1; p <= numProcessors; p++) {
					// find the earliest time the new node can be added on this processor
					int bestStart = state.findBestStartTime(node, p);
					List<Node> otherNodes = new ArrayList<>(state.getUnvisitedNodes());
					otherNodes.remove(node);
					// this loop finds the lower bound on all the other dependency-met nodes after
					// the node is scheduled to see if any of lower bounds are higher than the
					// current max
					for (Node node2 : otherNodes) {
						// tests if scheduling on this processor would be too slow
						if (state.dependenciesSatisfied(node2) && bestStart + node.getWeight() + node2.getLBWeight() > bestMakespan) {
							int bestEndTime = Integer.MAX_VALUE;
							for (int processor = 1; processor <= numProcessors; processor++) {
								// tests for all the other processors
								if (processor != p) {
									int endTime = state.findBestStartTime(node2, processor) + node2.getLBWeight();
									if (endTime < bestEndTime) {
										bestEndTime = endTime;
									}
								}
							}
							if (bestEndTime >= bestMakespan) {
								// if the lower bound is too high, then the new state is not made and we move to
								// the next processor
								updateBranchCut(state.getUnvisitedNodes().size() - 1, 1);
								continue processorLoop;
							}
						}
					}
					PartialSchedule newState = new PartialSchedule(state);
					boolean isFirstOnProcessor = newState.scheduleTask(node, p, bestStart);
					// if the lower bound for scheduling this node here on this processor is worse
					// than best so far
					// then partial schedule is not added to stack
					if (bestStart + node.getLBWeight() < bestMakespan) {
						// add the node at this time

						stack.addFirst(newState);
						// if this task is placed as the first task on a processor then trying to place
						// the
						// task on any subsequent processor will create an effectively identical
						// schedule
						if (isFirstOnProcessor) {
								updateBranchCut(newState.getUnvisitedNodes().size(), numProcessors - p);
							break;
						}
					} else {
						updateBranchCut(newState.getUnvisitedNodes().size(), 1);
					}
				}
			} else {
					updateBranchCut(state.getUnvisitedNodes().size() - 1, numProcessors);
			}
		}
		return false;
	}

	/**
	 * This method sets the lower bound weights for all the nodes in the graph to be
	 * scheduled. The lower bound weight for any node is found by comparing each way
	 * to schedule its children, given their lower bounds. e.g. If node a has
	 * children b and c, then the lower bound will be found by min(max(b.LBWeight,
	 * c.edgecost(a) + c.LBWeight), max(c.LBWeight, b.edgecost(a) + b.LBWeight))
	 */
	private void setLowerBounds() {

		List<Node> unvisited = new ArrayList<>(graph);
		for (Node node : graph) {
			if (node.getChildren().isEmpty()) {
				// nodes with no children are the only node on their critical path
				node.setLBWeight(node.getWeight());
				unvisited.remove(node);
			}
		}
		while (unvisited.size() > 0) {
			for (int i = 0; i < unvisited.size(); i++) {
				int maxWeight = 0;
				for (Node child : unvisited.get(i).getChildren()) {
					if (child.getLBWeight() < 0) {
						// this indicates that a child has not had its BLW calculated, so loop breaks.
						maxWeight = -1;
						break;
					} else if (child.getLBWeight() > maxWeight) {
						maxWeight = child.getLBWeight();
					}
				}
				if (maxWeight > 0) {
					unvisited.get(i).setLBWeight(unvisited.get(i).getWeight() + maxWeight);
					unvisited.remove(i);
					break;
				}
			}
		}
	}
}
