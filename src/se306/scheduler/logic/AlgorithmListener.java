package se306.scheduler.logic;

import java.util.List;

import se306.scheduler.graph.Node;

public interface AlgorithmListener {
    public void algorithmCompleted(List<Node> schedule);
}
