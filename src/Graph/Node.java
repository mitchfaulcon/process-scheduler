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

    public void addParent(Node parent){

    }

    public void addChild(Node child){

    }
}
