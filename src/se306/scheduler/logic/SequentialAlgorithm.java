package se306.scheduler.logic;

import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds a valid schedule by scheduling all tasks on the same processor.
 * This schedule is unlikely to be optimal, but it can be found quickly.
 */
public class SequentialAlgorithm extends Algorithm {

    public SequentialAlgorithm() {
        super(1);
    }

    @Override
    public void schedule() {
        // As nodes are reached in main loop they will be removed from here
        List<Node> unreached = new ArrayList<>(graph);
        PartialSchedule schedule = new PartialSchedule(graph);
        // this is necessary after some changes made to Node, but not going to rework the whole algorithm
        Map<String, Node> nodeMap = new HashMap<String, Node>();
        for (Node node: unreached) {
            nodeMap.put(node.getName(), node);
        }
        
        boolean parentsFound;
        int currentTime = 0;
        int i = 0;

        // finding some node with no parents to set as root
        while (!schedule.getNodes().get(i).getIncomingEdges().isEmpty()) {
            i++;
        }
        unreached.remove(schedule.getNodes().get(i));
        schedule.scheduleTask(schedule.getNodes().get(i), 1, currentTime);

        currentTime += graph.get(i).getWeight();

        // iterates until all nodes reached - have had their startTime updated
        while (!unreached.isEmpty()) {
            int j;
            for (j = 0; j < unreached.size(); j++) {
                parentsFound = true;
                // finding if all parents are reached (nodes can only run if all parents reached)
                for (Node.IncomingEdge edge: unreached.get(j).getIncomingEdges()) {
                    if (unreached.contains(edge.getParent())) {
                        parentsFound = false;
                        break;
                    }
                }
                if (parentsFound) {
                    // if all parents met then node can start at current time and processor will be occupied for
                    // however long the node's weight is.
                    schedule.scheduleTask(schedule.getNodes().get(j), 1, currentTime);

                    currentTime += unreached.get(j).getWeight();
                    break;
                }
            }
            unreached.remove(j);
        }
        
        completed(schedule);
    }

}
