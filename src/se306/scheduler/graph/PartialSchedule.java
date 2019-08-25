package se306.scheduler.graph;

import java.util.*;

public class PartialSchedule {
    private List<Node> nodes; // all nodes in the graph
    private List<Node> visited; // all scheduled tasks
    private List<Node> unvisited; // all unscheduled tasks
    private Map<Node, Integer> processorMap; // the processors that scheduled tasks are assigned to
    private Map<Node, Integer> startTimes; // the start times of all scheduled tasks
    private Set<Integer> traversedProcessors; // all processors that at least one task has been placed on
    private String ID;

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
        updateID();
    }

    /**
     * Updates teh ID field, which is returned by PartialSchedule.toString(). Partial schedules that are practically the
     * same will have the same ID. The ID is made by concatenating all the node names in their scheduled order for each
     * processor, then sorting these strings to make the ID the same for schedules that are effectively the same but with
     * a processor switched. The ID is the concatenation of all these strings.
     */
    private void updateID() {
        String[] processorIDs = new String[processorMap.values().size()];
        // so that there is a string to concatenate on to.
        Arrays.fill(processorIDs, "");
        for (Node node : visited) {
            processorIDs[processorMap.get(node) - 1] += node.getName();
        }
        Arrays.sort(processorIDs);
        ID = Arrays.toString(processorIDs);
    }

    /**
     * Checks if all nodes in the schedule have been visited, in which case the schedule is complete.
     */
    public boolean allVisited() {
        return unvisited.isEmpty();
    }

    /**
     * Returns a list of all unvisited nodes.
     */
    public List<Node> getUnvisitedNodes() {
        return new ArrayList<>(unvisited);
    }

    /**
     * Returns a list of all unvisited node names.
     */
    private List<Node> getUnvisited() {
        return unvisited;
    }
    
    /**
     * Returns a list of all visited node names.
     */
    public List<Node> getVisited() {
        return visited;
    }

    /**
     * Returns the map of scheduled nodes to their processors.
     */
    public Map<Node, Integer> getProcessorMap() {
        return processorMap;
    }

    /**
     * Returns the map of scheduled nodes to their start times.
     */
    public Map<Node, Integer> getStartTimes() {
        return startTimes;
    }

    /**
     * Return the processor a given `node` is scheduled on.
     */
    public int getProcessor(Node node) {
        return processorMap.get(node);
    }

    /**
     * Returns the start time of a given `node` if it has been assigned to a processor.
     */
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
        updateID();

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
                int newStartTime = startTimes.get(parent) + parent.getWeight() + edgeCost;
                if (newStartTime > bestStartTime) {
                    bestStartTime = newStartTime;
                }
            }
        }
        
        return bestStartTime;
    }
    
    /**
     * Returns true if no tasks have been scheduled on `processor`.
     */
    public boolean isProcessorEmpty(int processor) {
    	return !traversedProcessors.contains(processor);
    }
    
    /**
     * Returns all the nodes in the graph.
     */
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
    
    /**
     * Returns a string representation of this schedule.
     */
    @Override
    public String toString() {
        return ID;
    }
}
