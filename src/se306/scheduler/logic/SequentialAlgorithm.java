package se306.scheduler.logic;

import se306.scheduler.graph.Node;

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
        
        // this is necessary after some changes made to Node, but not going to rework the whole algorithm
        Map<String, Node> nodeMap = new HashMap<String, Node>();
        for (Node node: unreached) {
            nodeMap.put(node.getName(), node);
        }
        
        boolean parentsFound;
        int currentTime = 0;
        int i = 0;

        // finding some node with no parents to set as root
        while (!graph.get(i).getParents().isEmpty()) {
            i++;
        }
        unreached.remove(graph.get(i));
        graph.get(i).setStartTime(currentTime);
        graph.get(i).setProcessor(1);

        currentTime += graph.get(i).getWeight();

        // iterates until all nodes reached - have had their startTime updated
        while (!unreached.isEmpty()) {
            int j;
            for (j = 0; j < unreached.size(); j++) {
                parentsFound = true;
                // finding if all parents are reached (nodes can only run if all parents reached)
                for (String parentName: unreached.get(j).getParents().keySet()) {
                    Node parent = nodeMap.get(parentName);
                    if (unreached.contains(parent)) {
                        parentsFound = false;
                        break;
                    }
                }
                if (parentsFound) {
                    // if all parents met then node can start at current time and processor will be occupied for
                    // however long the node's weight is.
                    unreached.get(j).setStartTime(currentTime);
                    unreached.get(j).setProcessor(1);

                    currentTime += unreached.get(j).getWeight();
                    break;
                }
            }
            unreached.remove(j);
        }
        
        completed(graph);
    }

}
