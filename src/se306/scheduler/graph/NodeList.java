package se306.scheduler.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NodeList {
    private List<Node> nodes;
    
    public NodeList() {
        nodes = new ArrayList<Node>();
    }
    
    public NodeList(List<Node> nodes) {
        this.nodes = nodes;
    }
    
    /**
     * Returns a deep copy of the supplied NodeList and its contents
     */
    public NodeList(NodeList nodeList) {
        List<Node> oldNodes = nodeList.getNodes();
        nodes = new ArrayList<Node>();
        
        // copy simple fields only
        for (Node oldNode: oldNodes) {
            Node newNode = new Node(oldNode);
            nodes.add(newNode);
        }
        
        // copy all parent/child relationships
        for (Node oldNode: oldNodes) {
            Node newNode = getNode(oldNode.getName());
            for (Map.Entry<String, Integer> item: oldNode.getChildCosts().entrySet()) {
                Node oldChild = nodeList.getNode(item.getKey());
                int edgeCost = item.getValue();
                Node newChildNode = getNode(oldChild.getName());
                
                newNode.addChild(newChildNode, edgeCost);
            }
        }
    }
    
    /**
     * Checks if all nodes in the schedule have been visited, in which case the schedule is complete.
     */
    public boolean allVisited() {
        for (Node node: nodes) {
            if (!node.isVisited()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a list of all unvisited nodes (not assigned to any processor).
     */
    public List<Node> getUnvisited() {
        // TODO: make this faster by storing unvisited nodes and modifying nodes through this class rather than directly
        List<Node> unvisited = new ArrayList<Node>();
        for (Node node: nodes) {
            if (!node.isVisited()) {
                unvisited.add(node);
            }
        }
        return unvisited;
    }
    
    /**
     * Returns a list of all visited nodes (assigned to a processor).
     */
    public List<Node> getVisited() {
        List<Node> visited = new ArrayList<Node>();
        for (Node node: nodes) {
            if (node.isVisited()) {
                visited.add(node);
            }
        }
        return visited;
    }

    /**
     * Checks if all of a node's dependencies have already been assigned to processors.
     */
    public boolean dependenciesSatisfied(Node node) {
        for (Node parent: node.getParents()) {
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
        for (Node node: nodes) {
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
        for (Node node: getVisited()) {
            if (node.getProcessor() == processor) {
                int finishTime = node.getFinishTime();
                if (finishTime > bestStartTime) {
                    bestStartTime = finishTime;
                }
            }
        }
        
        // account for dependency 'edge costs'
        for (Node node: newNode.getParents()) {
            // edge costs only are counted if the node is on a different processor to its parent
            if (node.getProcessor() != processor) {
                // TODO: node should have weights stored with parents not with children
                int newStartTime = node.getFinishTime() + node.getChildCosts().get(newNode.getName());
                if (newStartTime > bestStartTime) {
                    bestStartTime = newStartTime;
                }
            }
        }
        
        return bestStartTime;
    }
    
    public List<Node> getNodes() {
        return nodes;
    }
    
    /**
     * Returns the node with this name.
     */
    public Node getNode(String name) {
        for (Node node: nodes) {
            if (node.getName() == name) {
                return node;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        String output = "";
        for (int i = 0; i < nodes.size(); i++) {
            if (!nodes.get(i).isVisited()) {
                continue;
            }
            output += nodes.get(i);
            
            if (i != nodes.size() - 1) {
                output += ", ";
            }
        }
        return output;
    }
}
