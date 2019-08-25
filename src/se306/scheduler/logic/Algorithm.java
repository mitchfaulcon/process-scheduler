package se306.scheduler.logic;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;

public abstract class Algorithm {
    protected List<AlgorithmListener> listeners;
    protected List<Node> graph;
    protected static volatile BigInteger schedulesChecked;
    protected int numProcessors;
    
    public Algorithm(int numProcessors) {
        listeners = new ArrayList<AlgorithmListener>();
        graph = new ArrayList<Node>();
        schedulesChecked = BigInteger.valueOf(0);
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
    protected synchronized void updateBranchCut(int nodesRemaining, int factor) {
        schedulesChecked = schedulesChecked.add(BigInteger.valueOf(this.numProcessors).pow(nodesRemaining).multiply(factorial(nodesRemaining)).multiply(BigInteger.valueOf(factor)));
    }

    public static BigInteger getBigSchedulesChecked(){
        return schedulesChecked;
    }

    /**
     * Simple method to be used in updateBranchCut that finds the factorial of a given input
     * @return result of number
     */
    public static BigInteger factorial(int number) {
        BigInteger output = BigInteger.valueOf(1);
        for (int n = 2; n <= number; n++) {
            output = output.multiply(BigInteger.valueOf(n));
        }
        return output;
    }
}
