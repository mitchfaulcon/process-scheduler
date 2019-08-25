package se306.scheduler.logic;

import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;

import java.util.*;

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
        Stack<PartialSchedule> stack = new Stack<>();
        // add initial state
        stack.push(new PartialSchedule(graph));

        /*PartialSchedule bestSchedule =  GreedySchedule();
        int bestMakespan = bestSchedule.getMakespan();
        updateSchedule(bestSchedule);*/
        PartialSchedule bestSchedule = null;
        int bestMakespan = Integer.MAX_VALUE;

        setLowerBounds();
        while (!stack.isEmpty()) {
            PartialSchedule state = stack.pop();
            // all nodes have been assigned to a processor
            if (state.allVisited()) {
                int makespan = state.getMakespan();
                // if the makespan is the critical path from the first node to the last then it is an optimal solution
                // so break while loop
                if (makespan == state.getVisited().get(0).getLBWeight()) {
                    //Update listener with new schedule
                    updateSchedule(state);
                    break;
                }
                // check if the current solution is better than the best one found so far
                if (makespan < bestMakespan) {
                    bestMakespan = makespan;
                    bestSchedule = state;

                    //Update listener with new schedule
                    updateSchedule(bestSchedule);
                }
                // regardless of whether the schedule is best, one complete schedule will be removed
                updateBranchCut(0);
                continue;
            }

            for (Node node: state.getUnvisitedNodes()) {
                // check if the node's parents have all been scheduled
                if (state.dependenciesSatisfied(node)) {
                    // create new states by adding the new node to every processor
                    processorLoop:
                    for (int p = 1; p <= numProcessors; p++) {
                        // find the earliest time the new node can be added on this processor
                        int bestStart = state.findBestStartTime(node, p);
                        List<Node> otherNodes = new ArrayList<>(state.getUnvisitedNodes());
                        otherNodes.remove(node);
                        // this loop finds the lower bound on all the other dependency-met nodes after the node is scheduled
                        // to see if any of lower bounds are higher than the current max
                        for (Node node2: otherNodes) {
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
    									
    									// no need to check other processors as the resultant schedules would be equivalent
    									if (state.isProcessorEmpty(processor)) {
    										break;
    									}
                                    }
                                }
                                if (bestEndTime >= bestMakespan) {
                                    // if the lower bound is too high, then the new state is not made and we move to
                                    // the next processor
                                    updateBranchCut(state.getUnvisitedNodes().size() - 1);
                                    continue processorLoop;
                                }
                            }
                        }
                        PartialSchedule newState = new PartialSchedule(state);
                        boolean isFirstOnProcessor = newState.scheduleTask(node, p, bestStart);
                        // if the lower bound for scheduling this node here on this processor is worse than best so far
                        // then partial schedule is not added to stack
                        if (bestStart + node.getLBWeight() < bestMakespan) {
                            // add the node at this time

                            stack.push(newState);

                            // if this task is placed as the first task on a processor then trying to place the
                            // task on any subsequent processor will create an effectively identical schedule
                            if (isFirstOnProcessor) {
                                for (int i = p; i < numProcessors; i++) {
                                    updateBranchCut(newState.getUnvisitedNodes().size());
                                }
                                break;
                            }
                        } else {
                            updateBranchCut(newState.getUnvisitedNodes().size());
                        }
                    }
                } else {
                    for (int i = 0; i < numProcessors; i++) {
                        updateBranchCut(state.getUnvisitedNodes().size() - 1);
                    }
                }
            }
        }
        completed(bestSchedule);
    }

    /**
     * This method sets the lower bound weights for all the nodes in the graph to be scheduled.
     * The lower bound weight for any node is found by comparing each way to schedule its children, given their lower
     * bounds.
     * e.g. If node a has children b and c, then the lower bound will be found by
     * min(max(b.LBWeight, c.edgecost(a) + c.LBWeight), max(c.LBWeight, b.edgecost(a) + b.LBWeight))
     */
    private void setLowerBounds() {
//        List<Node> unvisited = new ArrayList<>(graph);
//        for (Node node : graph) {
//            if (node.getChildren().isEmpty()) {
//                // nodes with no children are the only node on their critical path
//                node.setLBWeight(node.getWeight());
//                unvisited.remove(node);
//            }
//        }
//        while (unvisited.size() > 0) {
//            for (int i = 0; i < unvisited.size(); i++) {
//                int minWeight = Integer.MAX_VALUE;
//                Node node = unvisited.get(i);
//                List<Node> otherChildren = new ArrayList<>(node.getChildren());
//                for (Node child: node.getChildren()) {
//                    if (child.getLBWeight() < 0) {
//                        minWeight = -1;
//                        break;
//                    }
//                    otherChildren.remove(child);
//                    int max = 0;
//                    for (Node otherChild: otherChildren) {
//                        int newMax = otherChild.getIncomingEdges().get(node) + otherChild.getLBWeight();
//                        if (newMax > max) {
//                            max = newMax;
//                        }
//                    }
//                    otherChildren.add(child);
//                    if (child.getLBWeight() > max) {
//                        max = child.getLBWeight();
//                    }
//                    if (max < minWeight) {
//                        minWeight = max;
//                    }
//                }
//                if (minWeight > 0) {
//                    unvisited.get(i).setLBWeight(unvisited.get(i).getWeight() + minWeight);
//                    unvisited.remove(i);
//                    break;
//                }
//            }
//        }
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
                for (Node child: unvisited.get(i).getChildren()) {
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

    /**
     * This creates a greedy schedule which is made by assigning nodes whose dependencies are met to whichever
     * processor can run them first.
     * @return A greedy schedule to be used for setting the initial best.
     */
    private PartialSchedule GreedySchedule() {
        List<Node> unreached = new ArrayList<>(graph);
        PartialSchedule schedule = new PartialSchedule(graph);
        int i = 0;
        // finding some node with no parents to set as root
        while (!schedule.getNodes().get(i).getIncomingEdges().isEmpty()) {
            i++;
        }
        unreached.remove(schedule.getNodes().get(i));
        schedule.scheduleTask(schedule.getNodes().get(i), 0, 0);
        // iterates until all nodes reached
        while (!unreached.isEmpty()) {
            int j;
            for (j = 0; j < unreached.size(); j++) {
                if (schedule.dependenciesSatisfied(unreached.get(j))) {
                    // schedules node at the processor/time that is immediately best (greedy)
                    int bestStart = Integer.MAX_VALUE;
                    int bestProcessor = 0;
                    for (int k = 1; k <= numProcessors; k++) {
                        int start = schedule.findBestStartTime(unreached.get(j), k);
                        if (start < bestStart) {
                            bestStart = start;
                            bestProcessor = k;
                        }
                    }
                    schedule.scheduleTask(unreached.get(j), bestProcessor, bestStart);
                    break;
                }
            }
            unreached.remove(j);
        }
        return schedule;
    }
}
