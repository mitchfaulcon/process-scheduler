package se306.scheduler.logic;

import se306.scheduler.graph.Node;

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

    public void addNode(Node node){
        graph.add(node);
    }

    public void addChild(String parent, String child, int edgeWeight) throws NullPointerException {
        getNode(parent).addChild(getNode(child), edgeWeight);
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

                    currentTime += unreached.get(j).getWeight();
                    break;
                }
            }
            unreached.remove(j);

        }
    }

    private Node getNode(String name) {
        for (Node node : graph) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    public void clearGraph(){
        graph.clear();
    }
    
    public List<Node> getNodes() {
        return graph;
    }
}
