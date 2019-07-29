package Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {

    private String name;
    private List<Node> parents = new ArrayList<>();
    private HashMap<Node, Integer> children = new HashMap<>();
    private int weight;

    public Node(String name, int weight){
        this.name = name;
        this.weight = weight;
    }

    /**
     * Method to add another node as a child to this one.
     * @param child The child node to be added
     */
    public void addChild(Node child){
        this.children.put(child, child.weight);

        //Add this node to the predecessor list of the child node
        child.parents.add(this);
    }

}
