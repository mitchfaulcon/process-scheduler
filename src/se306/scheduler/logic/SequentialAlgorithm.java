package se306.scheduler.logic;

import java.util.ArrayList;
import java.util.List;

import se306.scheduler.graph.Node;
import se306.scheduler.graph.OutputGraph;

/**
 * Finds a valid schedule by scheduling all tasks on the same processor.
 * This schedule is unlikely to be optimal, but it can be found quickly.
 */
public class SequentialAlgorithm extends Algorithm {

    @Override
    public void schedule() {
        // As nodes are reached in main loop they will be removed from here
        List<Node> unreached = new ArrayList<>(graph);
        boolean parentsFound;
        int currentTime = 0;
        int i = 0;

        // finding some node with no parents to set as root
        while (!graph.get(i).getParents().isEmpty()) {
            i++;
        }
        unreached.remove(graph.get(i));
        graph.get(i).setStartTime(currentTime);

        //Add node to the output schedule graph
        OutputGraph.getOutputGraph().addNode(graph.get(i).getName(), currentTime);
        String lastNode = graph.get(i).getName();

        currentTime += graph.get(i).getWeight();

        // iterates until all nodes reached - have had their startTime updated
        while (!unreached.isEmpty()) {
            int j;
            for (j = 0; j < unreached.size(); j++) {
                parentsFound = true;
                // finding if all parents are reached (nodes can only run if all parents reached)
                for (Node parent: unreached.get(j).getParents()) {
                    if (unreached.contains(parent)) {
                        parentsFound = false;
                        break;
                    }
                }
                if (parentsFound) {
                    // if all parents met then node can start at current time and processor will be occupied for
                    // however long the node's weight is.
                    unreached.get(j).setStartTime(currentTime);

                    //Add the processed node and an edge to it from the previously processed node
                    OutputGraph.getOutputGraph().addNode(unreached.get(j).getName(), currentTime);
                    OutputGraph.getOutputGraph().addEdge(lastNode, lastNode=unreached.get(j).getName(), 0);

                    currentTime += unreached.get(j).getWeight();
                    break;
                }
            }
            unreached.remove(j);
        }
        
        completed(graph);
    }

}
