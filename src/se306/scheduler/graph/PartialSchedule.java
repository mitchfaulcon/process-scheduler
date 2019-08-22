package se306.scheduler.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PartialSchedule {
    private List<Node> nodes; // all nodes in the graph
    private List<Node> visited; // all scheduled tasks
    private List<Node> unvisited; // all unscheduled tasks
    private HashMap<Node, Integer> processorMap;
    private HashMap<Node, Integer> startTimes;
    private Set<Integer> traversedProcessors; // all processors that at least one task has been placed on
    
    public PartialSchedule() {
        nodes = new ArrayList<>();
        visited = new ArrayList<>();
        unvisited = new ArrayList<>();
        processorMap = new HashMap<>();
        startTimes = new HashMap<>();
        traversedProcessors = new HashSet<Integer>();
    }
    
    /**
     * Create a PartialSchedule from a list of nodes (with no nodes scheduled yet)
     */
    public PartialSchedule(List<Node> nodes) {
        this.nodes = new ArrayList<>(nodes);
        visited = new ArrayList<>();
        unvisited = new ArrayList<>(nodes);
        processorMap = new HashMap<>();
        startTimes = new HashMap<>();
        traversedProcessors = new HashSet<Integer>();
    }
    
    /**
     * Returns a deep copy of the supplied PartialSchedule and its contents
     */
    public PartialSchedule(PartialSchedule paritalSchedule) {
        nodes = new ArrayList<>(paritalSchedule.getNodes());
        processorMap = new HashMap<>(paritalSchedule.getProcessorMap());
        startTimes = new HashMap<>(paritalSchedule.getStartTimes());
        this.visited = new ArrayList<>(paritalSchedule.getVisited());
        this.unvisited = new ArrayList<>(paritalSchedule.getUnvisited());
        this.traversedProcessors = new HashSet<Integer>(paritalSchedule.getTraversedProcessors());
    }

    /**
     * Checks if all nodes in the schedule have been visited, in which case the schedule is complete.
     */
    public boolean allVisited() {
        return unvisited.isEmpty();
    }

    public List<Node> getUnvisitedNodes() {
        return new ArrayList<>(unvisited);
    }

    /**
     * Returns a set of all unvisited node names.
     */
    private List<Node> getUnvisited() {
        return unvisited;
    }
    
    /**
     * Returns a set of all visited node names.
     */
    public List<Node> getVisited() {
        return visited;
    }

    public HashMap<Node, Integer> getProcessorMap() {
        return processorMap;
    }

    public HashMap<Node, Integer> getStartTimes() {
        return startTimes;
    }

    public int getProcessor(Node node) {
        return processorMap.get(node);
    }

    public int getStartTime(Node node) {
        return startTimes.get(node);
    }
    
    /**
     * Returns a set of all processors that don't have any node scheduled on them.
     */
    private Set<Integer> getTraversedProcessors() {
        return traversedProcessors;
    }

    /**
     * Schedule the node with the name `nodeName` on processor `processor`, at time `startTime`.
     * 
     * Returns true if the node is the first to be added on this processor, and false otherwise.
     */
    public boolean scheduleTask(Node node, int processor, int startTime) {
        processorMap.put(node, processor);
        startTimes.put(node,startTime);
        
        visited.add(node);
        unvisited.remove(node);
        
        if (!traversedProcessors.contains(processor)) {
            traversedProcessors.add(processor);
            return true;
        }
        return false;
    }
    
    /**
     * Checks if all of a node's dependencies have already been assigned to processors.
     */
    public boolean dependenciesSatisfied(Node node) {
        for (Node parent: node.getIncomingEdges().keySet()) {
            if (unvisited.contains(parent)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Finds the makespan of the schedule, the latest finishing time of any task.
     */
    public int getMakespan() {
        int makespan = 0;
        for (Node node: visited) {
            int finishTime = startTimes.get(node) + node.getWeight();
            if (finishTime > makespan) {
                makespan = finishTime;
            }
        }
        return makespan;
    }

    /**
     * Finds the earliest possible start time a particular node can be added to a particular processor.
     * @param newNode The node to add
     * @param processor The processor to add it to
     */
    public int findBestStartTime(Node newNode, int processor) {
        // default is 0 if no other nodes placed and it has no dependencies
        int bestStartTime = 0;
        
        // a node cannot start until all previous nodes on that processor have finished
        for (Node node: visited) {
            if (processorMap.get(node) == processor) {
                int finishTime = startTimes.get(node) + node.getWeight();
                if (finishTime > bestStartTime) {
                    bestStartTime = finishTime;
                }
            }
        }
        
        // account for dependency 'edge costs'
        for (Node parent: newNode.getIncomingEdges().keySet()) {
            int edgeCost = newNode.getIncomingEdges().get(parent);
            // edge costs only are counted if the node is on a different processor to its parent
            if (processorMap.get(parent) != processor) {
                // TODO: node should have weights stored with parents not with children
                int newStartTime = startTimes.get(parent) + parent.getWeight() + edgeCost;
                if (newStartTime > bestStartTime) {
                    bestStartTime = newStartTime;
                }
            }
        }
        
        return bestStartTime;
    }

    /**
     * This returns an underestimate on the end time of the schedule should this node be scheduled next.
     */
    public int lowerBoundEndTime(Node newNode) {
        int startTime = 0;
        for (Node parent: newNode.getIncomingEdges().keySet()) {
            if (visited.contains(parent)) {
                int time = startTimes.get(parent) + parent.getWeight();
                if (time > startTime) {
                    startTime = time;
                }
            }
        }
        return startTime + newNode.getLBWeight();
    }
    
    public List<Node> getNodes() {
        return nodes;
    }
    
    /**
     * Returns the nodes using a list representation.
     */
    public List<Node> toList() {
        return new ArrayList<Node>(nodes);
    }
    
    /**
     * Returns the node with this name.
     */
    public Node getNode(String name) {
        for (Node node: nodes) {
            if (node.getName().equals(name)) {
                return node;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        List<String> strings = new ArrayList<String>();
        for (Node node: visited) {
            strings.add(node.toString());
        }
        return String.join(", ", strings);
    }
}
