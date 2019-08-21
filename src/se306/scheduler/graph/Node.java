package se306.scheduler.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
    private String name;
    private List<IncomingEdge> incomingEdges = new ArrayList<>();
    private List<Node> children = new ArrayList<>();
    private int BLWeight;
    private int weight;

    public Node(String name, int weight) {
        // set processor to -1 by default, meaning the node has not been assigned a processor.
        this.name = name;
        this.weight = weight;
        this.BLWeight = -1;
    }
    
    /**
     * Creates a new node using the properties of the supplied one, including parents.
     */
    public Node(Node node) {
        this.name = node.name;
        this.weight = node.weight;
        this.incomingEdges = new ArrayList<>();
        this.incomingEdges.addAll(node.getIncomingEdges());
        this.children = new ArrayList<>();
        this.children.addAll(node.children);
        this.BLWeight = node.BLWeight;
    }

    /**
     * Method to add another node as a parent of this one.
     * @param parent The parent node to be added
     * @param edgeWeight the weight of the link between the parent node and this node
     */
    public void addParent(Node parent, int edgeWeight){
        this.incomingEdges.add(new IncomingEdge(parent, edgeWeight));
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

    public List<IncomingEdge> getIncomingEdges() {
        return this.incomingEdges;
    }

    public int getBLWeight() {
        return this.BLWeight;
    }

    public void setBLWeight(int BLWeight) {
        this.BLWeight = BLWeight;
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

    public class IncomingEdge {
        private Node parent;
        private int weight;
        private IncomingEdge(Node parent, int weight) {
            this.parent = parent;
            this.weight = weight;
        }
        public int getWeight() {
            return this.weight;
        }
        public Node getParent() {
            return this.parent;
        }
    }
}
