package Logic;

import Graph.Node;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {

    private static final Scheduler scheduler = new Scheduler();

    private List<Node> graph = new ArrayList<>();

    public static Scheduler getScheduler(){
        return scheduler;
    }

    private Scheduler(){

    }

    public void addNode(String name, int weight){
        graph.add(new Node(name, weight));
    }

    /**
     * Edits the nodes in List<Node> graph to have the correct start time based on a valid schedule running on a single
     * processor.
     */
    public void schedule(){
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

        // iterates until all nodes reached - have had their startTime updated
        while (!unreached.isEmpty()) {
            for (Node node: unreached) {
                parentsFound = true;
                // finding if all parents are reached (nodes can only run if all parents reached)
                for (Node parent: node.getParents()) {
                    if (unreached.contains(parent)) {
                        parentsFound = false;
                        break;
                    }
                }
                if (parentsFound) {
                    // if all parents met then node can start at current time and processor will be occupied for
                    // however long the node's weight is.
                    unreached.remove(node);
                    node.setStartTime(currentTime);
                    currentTime += node.getWeight();
                }
            }
        }
    }

}
