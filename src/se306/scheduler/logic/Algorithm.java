package se306.scheduler.logic;

import java.util.ArrayList;
import java.util.List;

import se306.scheduler.graph.Node;

public abstract class Algorithm {
    protected List<AlgorithmListener> listeners;
    protected List<Node> graph;
    protected int searchedNodes;
    
    public Algorithm() {
        listeners = new ArrayList<AlgorithmListener>();
        graph = new ArrayList<Node>();
        searchedNodes = 0;
    }
    
    public void addListener(AlgorithmListener listener) {
        listeners.add(listener);
    }
    
    public void setGraph(List<Node> graph) {
        this.graph = graph;
    }
    
    public abstract void schedule();
    
    protected void completed(List<Node> answer) {
        for (AlgorithmListener listener: listeners) {
            listener.algorithmCompleted(answer);
        }
    }
}
