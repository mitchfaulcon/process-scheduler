package Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {

    private String name;
    private List<Node> parents = new ArrayList<>();
    private HashMap<Node, Integer> children = new HashMap<>();
    private int weight;
    private int startTime;
    private int processor;

    public Node(String name, int weight){
        this.name = name;
        this.weight = weight;
        // set as 1 by default.
        processor = 1;
    }

    /**
     * Method to add another node as a child to this one.
     * @param child The child node to be added
     * @param edgeWeight the weight of the link between the child node and this node
     */
    public void addChild(Node child, int edgeWeight){
        this.children.put(child, edgeWeight);

        //Add this node to the predecessor list of the child node
        child.parents.add(this);
    }

    public List<Node> getParents(){
        return this.parents;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getWeight() {
        return this.weight;
    }

}
