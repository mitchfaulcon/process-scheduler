package se306.scheduler.logic;

import java.util.List;

import se306.scheduler.graph.Node;

public interface AlgorithmListener {
    void algorithmCompleted(List<Node> schedule);

    void newOptimalFound(List<Node> schedule);

    void updateSchedulesChecked(long schedules);
}
