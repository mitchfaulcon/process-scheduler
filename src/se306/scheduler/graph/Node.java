package se306.scheduler.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
    private String name;
    private Map<Node, Integer> parents = new HashMap<>();
    private List<Node> children;
    private int BLWeight;
    private int weight;
    private int startTime;
    private int processor;

    public Node(String name, int weight) {
        // set processor to -1 by default, meaning the node has not been assigned a processor.
        this.name = name;
        this.weight = weight;
        this.processor = -1;
        this.children = new ArrayList<>();
        this.BLWeight = -1;
    }
    
    /**
     * Creates a new node using the properties of the supplied one, including parents.
     */
    public Node(Node node) {
        this.name = node.name;
        this.weight = node.weight;
        this.processor = node.processor;
        this.startTime = node.startTime;
        this.parents = new HashMap<Node, Integer>();
        for (Map.Entry<Node, Integer> entry: node.getParents().entrySet()) {
            parents.put(entry.getKey(), entry.getValue());
        }
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
        parents.put(parent, edgeWeight);
        parent.children.add(this);
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }
    
    public void setProcessor(int processor) {
        this.processor = processor;
    }
    
    public boolean isVisited() {
        return processor != -1;
    }

    public int getWeight() {
        return this.weight;
    }

    public String getName() {
        return this.name;
    }
    
    public List<Node> getChildren() {
        return children;
    }

    public Map<Node, Integer> getParents() {
        return parents;
    }

    public int getStartTime() {
        return this.startTime;
    }
    
    public int getFinishTime() {
        return startTime + weight;
    }
    
    public int getProcessor() {
        return this.processor;
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
        return String.format("(Name: %s  Processor: %d  Start Time: %d  Finish Time: %d)", getName(), getProcessor(), getStartTime(), getFinishTime());
    }
}
