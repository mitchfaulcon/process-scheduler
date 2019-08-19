package se306.scheduler.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NodeList {
    private Map<String, Node> nodes;
    private Set<String> visited;
    private Set<String> unvisited;
    
    public NodeList() {
        nodes = new HashMap<String, Node>();
        visited = new HashSet<String>();
        unvisited = new HashSet<String>();
    }
    
    public NodeList(List<Node> nodes) {
        this.nodes = new HashMap<String, Node>();
        for (Node node: nodes) {
            this.nodes.put(node.getName(), node);
        }

        visited = new HashSet<String>();
        unvisited = new HashSet<String>(this.nodes.keySet());
    }
    
    public NodeList(Map<String, Node> nodes) {
        this.nodes = nodes;

        visited = new HashSet<String>();
        unvisited = new HashSet<String>(this.nodes.keySet());
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
    }
    
    /**
     * Checks if all nodes in the schedule have been visited, in which case the schedule is complete.
     */
    public boolean allVisited() {
        for (Node node: nodes.values()) {
            if (!node.isVisited()) {
                return false;
            }
        }
        return true;
    }
    
    public Set<Node> getUnvisitedNodes() {
        Set<Node> unvisitedNodes = new HashSet<Node>();
        for (String nodeName: unvisited) {
            unvisitedNodes.add(nodes.get(nodeName));
        }
        return unvisitedNodes;
    }

    /**
     * Returns a set of all unvisited nodes.
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

    public void visitNode(String nodeName) {
        visited.add(nodeName);
        unvisited.remove(nodeName);
    }
    
    /**
     * Checks if all of a node's dependencies have already been assigned to processors.
     */
    public boolean dependenciesSatisfied(Node node) {
        for (String parentName: node.getParents().keySet()) {
            Node parent = nodes.get(parentName);
            if (!parent.isVisited()) {
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
        for (Node node: nodes.values()) {
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
        for (String nodeName: getVisited()) {
            Node node = nodes.get(nodeName);
            if (node.getProcessor() == processor) {
                int finishTime = node.getFinishTime();
                if (finishTime > bestStartTime) {
                    bestStartTime = finishTime;
                }
            }
        }
        
        // account for dependency 'edge costs'
        for (Map.Entry<String, Integer> parentEntry: newNode.getParents().entrySet()) {
            Node parent = nodes.get(parentEntry.getKey());
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
        for (Node node: nodes.values()) {
            if (node.isVisited()) {
                continue;
            }
            strings.add(node.toString());
        }
        return String.join(", ", strings);
    }
}
