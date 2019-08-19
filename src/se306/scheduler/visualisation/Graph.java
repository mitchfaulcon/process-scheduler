package se306.scheduler.visualisation;

public interface Graph {
    public void addNode(String nodeName, int weight);
    
    public void addEdge(String node1, String node2, int weight);
}
