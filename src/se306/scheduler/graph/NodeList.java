package se306.scheduler.graph;

import com.sun.javaws.exceptions.InvalidArgumentException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NodeList {
    private Map<String, Node> nodes; // all nodes in the graph
    private Set<String> visited; // the names of all scheduled tasks
    private Set<String> unvisited; // the names of all unscheduled tasks
    private Set<Integer> traversedProcessors; // all processors that at least one task has been placed on
    
    public NodeList() {
        nodes = new HashMap<String, Node>();
        visited = new HashSet<String>();
        unvisited = new HashSet<String>();
        traversedProcessors = new HashSet<Integer>();
    }
    
    /**
     * Create a NodeList from a list of nodes (with no nodes scheduled yet)
     */
    public NodeList(List<Node> nodes) {
        this.nodes = new HashMap<String, Node>();
        for (Node node: nodes) {
            this.nodes.put(node.getName(), node);
        }

        visited = new HashSet<String>();
        unvisited = new HashSet<String>(this.nodes.keySet());
        traversedProcessors = new HashSet<Integer>();
    }
    
    public NodeList(Map<String, Node> nodes) {
        this.nodes = nodes;

        visited = new HashSet<String>();
        unvisited = new HashSet<String>(this.nodes.keySet());
        traversedProcessors = new HashSet<Integer>();
    }
    
    /**
     * Returns a deep copy of the supplied NodeList and its contents
     */
    public NodeList(NodeList nodeList) {
        Map<String, Node> oldNodes = nodeList.getNodes();
        nodes = new HashMap<String, Node>();
        
        for (Map.Entry<String, Node> entry: oldNodes.entrySet()) {
            Node newNode = new Node(entry.getValue());
            nodes.put(entry.getKey(), newNode);
        }

        this.visited = new HashSet<String>(nodeList.getVisited());
        this.unvisited = new HashSet<String>(nodeList.getUnvisited());
        this.traversedProcessors = new HashSet<Integer>(nodeList.getTraversedProcessors());
    }

    /**
     * Checks if all nodes in the schedule have been visited, in which case the schedule is complete.
     */
    public boolean allVisited() {
        return unvisited.isEmpty();
    }
    
    public Set<Node> getUnvisitedNodes() {
        Set<Node> unvisitedNodes = new HashSet<Node>();
        for (String nodeName: unvisited) {
            unvisitedNodes.add(nodes.get(nodeName));
        }
        return unvisitedNodes;
    }

    /**
     * Returns a set of all unvisited node names.
     */
    private Set<String> getUnvisited() {
        return unvisited;
    }
    
    /**
     * Returns a set of all visited node names.
     */
    private Set<String> getVisited() {
        return visited;
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
    public boolean scheduleTask(String nodeName, int processor, int startTime) {
        Node node = nodes.get(nodeName);
        node.setProcessor(processor);
        node.setStartTime(startTime);
        
        visited.add(nodeName);
        unvisited.remove(nodeName);
        
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
        for (Node parentName: node.getParents().keySet()) {
            if (unvisited.contains(parentName)) {
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
        for (String nodeName: visited) {
            Node node = nodes.get(nodeName);
            int finishTime = node.getFinishTime();
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
        for (String nodeName: visited) {
            Node node = nodes.get(nodeName);
            if (node.getProcessor() == processor) {
                int finishTime = node.getFinishTime();
                if (finishTime > bestStartTime) {
                    bestStartTime = finishTime;
                }
            }
        }
        
        // account for dependency 'edge costs'
        for (Map.Entry<Node, Integer> parentEntry: newNode.getParents().entrySet()) {
            Node parent = parentEntry.getKey();
            int edgeCost = parentEntry.getValue();
            // edge costs only are counted if the node is on a different processor to its parent
            if (parent.getProcessor() != processor) {
                // TODO: node should have weights stored with parents not with children
                int newStartTime = parent.getFinishTime() + edgeCost;
                if (newStartTime > bestStartTime) {
                    bestStartTime = newStartTime;
                }
            }
        }
        
        return bestStartTime;
    }
    
    public Map<String, Node> getNodes() {
        return nodes;
    }
    
    /**
     * Returns the nodes using a list representation.
     */
    public List<Node> toList() {
        return new ArrayList<Node>(nodes.values());
    }
    
    /**
     * Returns the node with this name.
     */
    public Node getNode(String name) {
        return nodes.get(name);
    }
    
    @Override
    public String toString() {
        List<String> strings = new ArrayList<String>();
        for (String nodeName: visited) {
            Node node = nodes.get(nodeName);
            strings.add(node.toString());
        }
        return String.join(", ", strings);
    }
}
