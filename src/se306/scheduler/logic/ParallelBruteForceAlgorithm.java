package se306.scheduler.logic;

import java.util.List;
import java.util.Stack;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import se306.scheduler.graph.Node;
import se306.scheduler.graph.NodeList;

public class ParallelBruteForceAlgorithm extends Algorithm {
    
    private BlockingDeque<NodeList> deque;
    private int threadCount;
    private AtomicInteger activeThreads;
    private int bestMakespan;
    private NodeList bestSchedule;
    private final Object lock = new Object();

    public ParallelBruteForceAlgorithm(int numProcessors) {
        super(numProcessors);
    }

    @Override
    public void schedule() {
        deque = new LinkedBlockingDeque<NodeList>();
        threadCount = 4;
        activeThreads = new AtomicInteger(0);

        // add initial state
        deque.push(new NodeList(graph));

        bestMakespan = Integer.MAX_VALUE;
        bestSchedule = null;
        
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        NodeList state = null;
                        synchronized (lock) {
                            activeThreads.incrementAndGet();
                            
                            if (deque.isEmpty()) { // there is no work for this thread to do
                                if (activeThreads.get() == 1) { // all threads have no work to do
                                    activeThreads.decrementAndGet();
                                    System.out.println(bestSchedule);
                                    completed(bestSchedule.toList());
                                    updateSchedule(bestSchedule.toList());
                                    break;
                                }
                                
                                activeThreads.decrementAndGet();
                                continue;
                            }
                            state = deque.pop();
                        }

                        // all nodes have been assigned to a processor
                        if (state.allVisited()) {
                            // check if the current solution is better than the best one found so far
                            int makespan = state.getMakespan();
                            if (makespan < bestMakespan) { 
                                bestMakespan = makespan;
                                bestSchedule = state;

                                //Update listener with new schedule
                                updateSchedule(bestSchedule.toList());
                            }
                            
                            synchronized (lock) {
                                activeThreads.decrementAndGet();
                            }
                            continue;
                        }

                        for (Node node: state.getUnvisitedNodes()) {
                            // check if the node's parents have all been scheduled
                            if (state.dependenciesSatisfied(node)) {
                                // create new states by adding the new node to every processor
                                for (int p = 1; p <= numProcessors; p++) {
                                    // find the earliest time the new node can be added on this processor
                                    int bestStart = state.findBestStartTime(node, p);

                                    // add the node at this time
                                    NodeList newState = new NodeList(state);
                                    boolean isFirstOnProcessor = newState.scheduleTask(node.getName(), p, bestStart);

                                    synchronized (lock) {
                                        deque.push(newState);
                                    }

                                    // if this task is placed as the first task on a processor then trying to place the
                                    // task on any subsequent processor will create an effectively identical schedule
                                    if (isFirstOnProcessor) {
                                        break;
                                    }
                                }
                            }
                        }
                        synchronized (lock) {
                            activeThreads.decrementAndGet();
                        }
                    }
                }
                
            });
            thread.start();
        }

    }

}
