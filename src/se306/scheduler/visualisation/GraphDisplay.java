package se306.scheduler.visualisation;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import se306.scheduler.graph.PartialSchedule;

import java.util.*;

public class GraphDisplay {

    private static final GraphDisplay graphDisplay = new GraphDisplay();

    private Graph graph = new SingleGraph("graphDisplay", false, true);
    private int yCoord = 0;
    private Map<String, String> nodeColours;

    private GraphDisplay() {

    }

    public static GraphDisplay getGraphDisplay(){
        return graphDisplay;
    }
    
    public void setNodeColours(Map<String, String> nodeColours) {
        this.nodeColours = nodeColours;
    }

    public Graph getGraph() {
        return graph;
    }

    /**
     * Method to add a node and its weight to the graph display
     * @param nodeName the name of the node in the graph
     * @param weight the processing weight of the node
     */
    public void addNode(String nodeName, int weight){
        graph.addNode(nodeName);
        Node node = graph.getNode(nodeName);

        //Add extra label for node weight
        SpriteManager spriteManager = new SpriteManager(graph);
        Sprite sprite = spriteManager.addSprite(nodeName, Sprite.class);
        sprite.attachToNode(nodeName);
        sprite.setAttribute("ui.label", weight);
        sprite.setAttribute("ui.style", "text-size: 15px;fill-color: rgba(255,255,255,0);");
        sprite.setPosition(StyleConstants.Units.PX, 40,0,0);
        
        //Set Label & Style
        String colour = String.format(nodeColours.get(nodeName), "127");
        node.setAttribute("ui.label", nodeName);
        node.setAttribute("ui.style", "shape:circle;fill-color: " + colour + ";size: 50px;" +
                "stroke-mode: none;" +
                "text-alignment: left;text-size: 30px;");

        //Set node coordinate to a 'good enough' initial location (can be moved by user in pop-up window later)
        Random random = new Random();
        double xCoord = (2 * random.nextDouble() - 1) * yCoord;
        node.setAttribute("xy", xCoord, yCoord--);
    }

    /**
     * Method to add an edge to the graph display
     * @param node1 The parent node
     * @param node2 The child node
     * @param edgeWeight The weight of the edge to be added
     */
    public void addEdge(String node1, String node2, int edgeWeight){
        String edgeID = node1 + node2;
        graph.addEdge(edgeID, node1, node2, true);
        Edge edge = graph.getEdge(edgeID);

        //Add weight label and set edge label & arrow style
        edge.setAttribute("ui.label", edgeWeight);
        edge.setAttribute("ui.style", "fill-color: rgb(100,100,100);" +
                "text-background-mode: plain;text-size: 20px;" +
                "arrow-size: 10px;");
    }

    /**
     * Adds all nodes and their edges to the graph so that it looks like a tree
     * @param schedule The schedule to base the graph on
     */
    public void addNodes(PartialSchedule schedule) {
        // adding all the nodes and edges to the graph with inital values
        for (se306.scheduler.graph.Node node: schedule.getNodes()) {
            addNode(node.getName(), node.getWeight());
            for (se306.scheduler.graph.Node parent: node.getIncomingEdges().keySet()) {
                addEdge(parent.getName(), node.getName(), node.getIncomingEdges().get(parent));
            }
        }
        // this represents each "layer" - a node is added to the layer one lower than the lowest of its parents
        List<List<se306.scheduler.graph.Node>> layers = new ArrayList<>();
        List<se306.scheduler.graph.Node> unAdded = new ArrayList<>(schedule.getNodes());
        int added = 0;
        // adding all the entry nodes to the first layer
        for (se306.scheduler.graph.Node node: schedule.getVisited()) {
            layers.add(new ArrayList<>());
            if (node.getIncomingEdges().size() == 0) {
                layers.get(0).add(node);
                unAdded.remove(node);
                added++;
            }
        }
        while (added < schedule.getNodes().size()) {
            int i = 0;
            // goes through the unAdded nodes and adds them to a layer if all their parents are added
            nodeLoop:
            for (i = 0; i < unAdded.size(); i++) {
                se306.scheduler.graph.Node node = unAdded.get(i);
                int newLayer = 0;
                for (se306.scheduler.graph.Node parent: node.getIncomingEdges().keySet()) {
                    int parentLayer = -1;
                    for (List<se306.scheduler.graph.Node> layer: layers) {
                        if (layer.contains(parent)) {
                            parentLayer = layers.indexOf(layer);
                        }
                    }
                    if (parentLayer == -1) {
                        // this indicates that not all parents are added yet, so move on to next node
                        continue nodeLoop;
                    } else if (parentLayer >= newLayer) {
                        newLayer = parentLayer + 1;
                    }
                }
                // if this node's layer is not added yet, add it
                if (newLayer >= layers.size()) {
                    layers.add(new ArrayList<>());
                }
                layers.get(newLayer).add(node);
                break;
            }
            unAdded.remove(i);
            added++;
        }
        // this determines spacing between nodes - set to 10 because it looks nice
        int scale = 10;
        // set to values that make adds all the entry nodes evenly spaced at y = 0
        int xCoOrd = scale - scale*layers.get(0).size();
        int yCoOrd = 0;
        for (se306.scheduler.graph.Node node: layers.get(0)) {
            graph.getNode(node.getName()).setAttribute("xy", xCoOrd, yCoOrd);
            xCoOrd += 2*scale;
        }
        // goes through each layer except the last and adds each node's children underneath its position
        for (int i = 0; i < layers.size() - 1; i++) {
            List<se306.scheduler.graph.Node> layer = layers.get(i);
            // so that there is 2*scale distance between layers
            yCoOrd = 0 - 2*scale*(i + 1);
            int maxChildSize = 0;
            for (se306.scheduler.graph.Node node: layer) {
                if (node.getChildren().size() > maxChildSize) {
                    maxChildSize = node.getChildren().size();
                }
            }
            // this means there is space under each node to have the same number of children - looks less cramped more
            // like a tree
            xCoOrd = scale - scale*layer.size()*maxChildSize;
            List<se306.scheduler.graph.Node> addedChildren = new ArrayList<>();
            for (se306.scheduler.graph.Node node: layer) {
                // child nodes are added together so siblings are next to each other
                for (se306.scheduler.graph.Node child: node.getChildren()) {
                    if (!addedChildren.contains(child)) {
                        graph.getNode(child.getName()).setAttribute("xy", xCoOrd, yCoOrd);
                        // so that there is 2*scale distance between nodes on same layer
                        xCoOrd += 2 * scale;
                        addedChildren.add(child);
                    }
                }
            }
        }
    }
}