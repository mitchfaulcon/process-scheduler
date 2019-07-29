package Logic;

import Graph.Node;

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

    public void addNode(String name, int weight){
        graph.add(new Node(name, weight));
    }

}
