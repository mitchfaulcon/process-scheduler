package se306.scheduler.logic;

import se306.scheduler.graph.Node;
import se306.scheduler.graph.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * Branch and Bound DFS implementation to find the optimal schedule for a list of tasks.
 * Much like DFS, but creates a lower-bound estimate on each branch before exploring it in order to gauge whether it
 * is worth exploring.
 */
public class BNBAlgorithm extends Algorithm {
    private HashMap<Node, Integer> BLWeights;

    public BNBAlgorithm(int numProcessors) {
        super(numProcessors);
    }

    @Override
    public void schedule() {
        Stack<NodeList> stack = new Stack<NodeList>();

        // add initial state
        stack.push(new NodeList(graph));

        int bestMakespan = Integer.MAX_VALUE;
        NodeList bestSchedule = null;

        while (!stack.isEmpty()) {
            NodeList state = stack.pop();

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
        completed(bestSchedule.toList());
    }

    /**
     * This method sets the Bottom Level Weights for all the nodes in the graph to be scheduled.
     * Bottom level weight is the highest cost direct path to an exit node on this graph, it allows us to calculate
     * a lower bound for schedules where some node is to be scheduled next - the lower bound is the nodes start time +
     * BLW.
     */
    private void setBLWeights() {
        List<Node> unvisited = new ArrayList<>(graph);
        for (Node node : graph) {
            if (node.getChildren().isEmpty()) {
                // nodes with no children are the only node on their critical path
                node.setBLWeight(node.getWeight());
                unvisited.remove(node);
            }
        }
        while (unvisited.size() > 0) {
            for (Node node: unvisited) {
                int maxWeight = 0;
                for (Node child: node.getChildren()) {
                    if (child.getBLWeight() < 0) {
                        // this indicates that a child has not had its BLW calculated, so loop breaks.
                        maxWeight = -1;
                        break;
                    } else if (child.getBLWeight() > maxWeight) {
                        maxWeight = child.getBLWeight();
                    }
                }
                if (maxWeight > 0) {
                    node.setBLWeight(maxWeight);
                    unvisited.remove(node);
                }
            }
        }

    }

}