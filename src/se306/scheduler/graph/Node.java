package se306.scheduler.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {
    private String name;
    private HashMap<Node, Integer> incomingEdges = new HashMap<>();
    private List<Node> children = new ArrayList<>();
    private int LBWeight;
    private int weight;

    public Node(String name, int weight) {
        // set processor to -1 by default, meaning the node has not been assigned a processor.
        this.name = name;
        this.weight = weight;
        this.LBWeight = -1;
    }
    
    /**
     * Creates a new node using the properties of the supplied one, including parents.
     */
    public Node(Node node) {
        this.name = node.name;
        this.weight = node.weight;
        this.incomingEdges = new HashMap<>();
        this.incomingEdges.putAll(node.getIncomingEdges());
        this.children = new ArrayList<>();
        this.children.addAll(node.children);
        this.LBWeight = node.LBWeight;
    }

    /**
     * Method to add another node as a parent of this one.
     * @param parent The parent node to be added
     * @param edgeWeight the weight of the link between the parent node and this node
     */
    public void addParent(Node parent, int edgeWeight){
        this.incomingEdges.put(parent, edgeWeight);
        parent.children.add(this);
    }

    public int getWeight() {
        return this.weight;
    }

    public String getName() {
        return this.name;
    }
    
    public List<Node> getChildren() {
        return this.children;
    }

    public HashMap<Node, Integer> getIncomingEdges() {
        return this.incomingEdges;
    }

    public int getLBWeight() {
        return this.LBWeight;
    }

    public void setLBWeight(int LBWeight) {
        this.LBWeight = LBWeight;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Node) {
            Node node = (Node) o;
            return (node.name.equals(this.name) && node.weight == this.weight);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "Name: " + name + ", Weight: " + weight;
    }

}
