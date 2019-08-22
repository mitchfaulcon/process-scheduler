package se306.scheduler.logic;

import java.util.List;

import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;

public interface AlgorithmListener {
    void algorithmCompleted(PartialSchedule schedule);

    void newOptimalFound(PartialSchedule schedule);

//    void updateSchedulesChecked(long schedules);
}
