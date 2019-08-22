package se306.scheduler.logic;

import java.util.ArrayList;
import java.util.List;

import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;

public abstract class Algorithm {
    protected List<AlgorithmListener> listeners;
    protected List<Node> graph;
    protected static long schedulesChecked;
    protected int numProcessors;
    
    public Algorithm(int numProcessors) {
        listeners = new ArrayList<AlgorithmListener>();
        graph = new ArrayList<Node>();
        schedulesChecked = 0;
        this.numProcessors = numProcessors;
    }
    
    public void addListener(AlgorithmListener listener) {
        listeners.add(listener);
    }
    
    public void setGraph(List<Node> graph) {
        this.graph = graph;
    }
    
    public abstract void schedule();
    
    protected void completed(PartialSchedule answer) {
        for (AlgorithmListener listener: listeners) {
            listener.algorithmCompleted(answer);
        }
//        System.out.println(schedulesChecked);
    }

    protected void updateSchedule(PartialSchedule newOptimal){
        for (AlgorithmListener listener: listeners) {
            listener.newOptimalFound(newOptimal);
        }
    }

    /**
     * This method will add to the total schedules checked by calculating the number of
     * schedules removed when a branch at some partial schedule with total nodes -
     * nodesRemaining nodes.
     * @param nodesRemaining the total nodes to schedule - number of nodes in partial schedule
     **/
    protected void updateBranchCut(int nodesRemaining) {
        this.schedulesChecked += Math.pow(this.numProcessors, nodesRemaining)*factorial(nodesRemaining);
//        for (AlgorithmListener listener: listeners) {
//            listener.updateSchedulesChecked(this.schedulesChecked);
//        }
        //System.out.println(schedulesChecked);

    }

    public static long getSchedulesChecked(){
        return schedulesChecked;
    }

    /**
     * Simple method to be used in updateBranchCut that finds the factorial of a given input
     * @return result of number!
     */
    public static long factorial(int number) {
        long output = 1;
        for (int n = 2; n <= number; n++) {
            output *= n;
        }
        return output;
    }
}
