package se306.scheduler.logic;

import java.util.Stack;

import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;

/**
 * Simple implementation of DFS to find the optimal schedule for a list of tasks.
 * This involves trying every possible combination of nodes on every processor, so it will be very slow on large inputs.
 */
public class DFSAlgorithm extends Algorithm {

    public DFSAlgorithm(int numProcessors) {
        super(numProcessors);
    }

    @Override
    public void schedule() {
        Stack<PartialSchedule> stack = new Stack<PartialSchedule>();

        // add initial state
        stack.push(new PartialSchedule(graph));

        int bestMakespan = Integer.MAX_VALUE;
        PartialSchedule bestSchedule = null;

        while (!stack.isEmpty()) {
            PartialSchedule state = stack.pop();

            // all nodes have been assigned to a processor
            if (state.allVisited()) {
                // check if the current solution is better than the best one found so far
                int makespan = state.getMakespan();
                if (makespan < bestMakespan) {
                    bestMakespan = makespan;
                    bestSchedule = state;

                    //Update listener with new schedule
                    updateSchedule(bestSchedule);
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
                        PartialSchedule newState = new PartialSchedule(state);
                        boolean isFirstOnProcessor = newState.scheduleTask(node, p, bestStart);

                        stack.push(newState);

                        // if this task is placed as the first task on a processor then trying to place the
                        // task on any subsequent processor will create an effectively identical schedule
                        if (isFirstOnProcessor) {
                            break;
                        }
                    }
                }
            }
        }

        System.out.println(bestSchedule);
        completed(bestSchedule);
    }

}
