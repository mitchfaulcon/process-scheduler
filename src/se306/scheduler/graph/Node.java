package se306.scheduler.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {

    private String name;
    private List<Node> parents = new ArrayList<>();
    private List<Node> children = new ArrayList<>();
    private Map<String, Integer> childCosts = new HashMap<>();
    private int weight;
    private int startTime;
    private int processor;

    public Node(String name, int weight) {
        // set processor to -1 by default, meaning the node has not been assigned a processor.
        this.name = name;
        this.weight = weight;
        this.processor = -1;
    }
    
    /**
     * Creates a new node using the properties of the supplied one, but does NOT copy parents or children.
     */
    public Node(Node node) {
        this.name = node.name;
        this.weight = node.weight;
        this.processor = node.processor;
        this.startTime = node.startTime;
    }

    /**
     * Method to add another node as a child to this one.
     * @param child The child node to be added
     * @param edgeWeight the weight of the link between the child node and this node
     */
    public void addChild(Node child, int edgeWeight){
        children.add(child);
        childCosts.put(child.getName(), edgeWeight);

        //Add this node to the predecessor list of the child node
        child.parents.add(this);
    }

    public List<Node> getParents(){
        return this.parents;
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

    public Map<String, Integer> getChildCosts() {
        return childCosts;
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
    
    @Override
    public String toString() {
        return String.format("(%s %d %d)", getName(), getProcessor(), getFinishTime());
    }
}
