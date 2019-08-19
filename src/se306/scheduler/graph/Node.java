package se306.scheduler.graph;

import java.util.HashMap;
import java.util.Map;

public class Node {
    private String name;
    private Map<String, Integer> parents = new HashMap<>();
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
     * Creates a new node using the properties of the supplied one, including parents and children.
     */
    public Node(Node node) {
        this.name = node.name;
        this.weight = node.weight;
        this.processor = node.processor;
        this.startTime = node.startTime;
        
        this.parents = new HashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry: node.getParents().entrySet()) {
            parents.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Method to add another node as a parent of this one.
     * @param parent The name of the parent node to be added
     * @param edgeWeight the weight of the link between the parent node and this node
     */
    public void addParent(String parent, int edgeWeight){
        parents.put(parent, edgeWeight);
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
    
    /*public Set<String> getChildren() {
        return children;
    }*/

    public Map<String, Integer> getParents() {
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
    
    @Override
    public String toString() {
        return String.format("(%s %d %d)", getName(), getProcessor(), getFinishTime());
    }
}
