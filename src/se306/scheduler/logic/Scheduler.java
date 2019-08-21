package se306.scheduler.logic;

import java.util.ArrayList;
import java.util.List;

import se306.scheduler.graph.Node;

public class Scheduler {

    private List<Node> graph = new ArrayList<>();
    
    private Algorithm algorithm;

    public Scheduler(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void start() {
        algorithm.setGraph(graph);
        algorithm.schedule();
    }

    public void addNode(Node node){
        graph.add(node);
    }

    public void addChild(String parent, String child, int edgeWeight) throws NullPointerException {
        Node parentNode = getNode(parent);
        getNode(child).addParent(parentNode, edgeWeight);
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
