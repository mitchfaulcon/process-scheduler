package se306.scheduler.visualisation;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Creates a graph during the parsing of the dot file.
 * All the nodes and edges are converted to be displayable
 * via the GraphStream library.
 *
 * <a href="http://graphstream-project.org"/>
 */
public class GraphDisplay {
    //Singleton object
    private static final GraphDisplay graphDisplay = new GraphDisplay();

    private Graph graph = new SingleGraph("graphDisplay", false, true);
    private Map<String, String> nodeColours;

    private GraphDisplay() {

    }

    public static GraphDisplay getGraphDisplay() {
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
     *
     * @param inputNode The Node representation of the node
     * @param yCoOrd    The y co-ordinate for this node to be drawn at
     * @param xCoOrd    The x co-ordinate for this node to be drawn at
     */
    private void addNode(Node inputNode, int xCoOrd, int yCoOrd) {
        String nodeName = inputNode.getName();
        graph.addNode(nodeName);
        org.graphstream.graph.Node node = graph.getNode(nodeName);

        //Add extra label for node weight
        SpriteManager spriteManager = new SpriteManager(graph);
        Sprite sprite = spriteManager.addSprite(nodeName, Sprite.class);
        sprite.attachToNode(nodeName);
        sprite.setAttribute("ui.label", inputNode.getWeight());
        sprite.setAttribute("ui.style", "text-size: 15px;fill-color: rgba(255,255,255,0);text-font: Consolas;");
        sprite.setPosition(StyleConstants.Units.PX, 40, 0, 0);

        //Set Label, Style and Position
        String colour = String.format(nodeColours.get(nodeName), "127");
        node.setAttribute("ui.label", nodeName);
        node.setAttribute("ui.style", "shape:circle;fill-color: " + colour + ";size: 50px;" +
                "stroke-mode: none;" +
                "text-alignment: left;text-size: 30px;text-font: Consolas;");
        node.setAttribute("xy", xCoOrd, yCoOrd);

        //Add invisible 'buddy' node
        String invisNodeName = "invisible" + inputNode.getName();
        graph.addNode(invisNodeName);
        org.graphstream.graph.Node invisNode = graph.getNode(invisNodeName);
        invisNode.setAttribute("ui.style", "shape:circle;fill-color: rgba(0,0,0,0);size: 50px;");
        invisNode.setAttribute("xy", xCoOrd+10, yCoOrd);
    }

    /**
     * Method to add an edge to the graph display
     *
     * @param parent     The parent node
     * @param child      The child node
     * @param edgeWeight The weight of the edge to be added
     */
    private void addEdge(Node parent, Node child, int edgeWeight) {
        String edgeID = parent.getName() + child.getName();
        graph.addEdge(edgeID, parent.getName(), child.getName(), true);
        Edge edge = graph.getEdge(edgeID);

        //Add weight label and set edge label & arrow style
        edge.setAttribute("ui.label", edgeWeight);
        edge.setAttribute("ui.style", "fill-color: rgb(100,100,100);" +
                "text-background-mode: plain;text-size: 20px;" +
                "arrow-size: 10px;text-font: Consolas;");
    }

    /**
     * Adds all nodes and their edges to the graph so that it looks like a tree
     *
     * @param schedule The schedule to base the graph on
     */
    public void addNodes(PartialSchedule schedule) {
        // this represents each "layer" - a node is added to the layer one lower than the lowest of its parents
        List<List<Node>> layers = new ArrayList<>();
        List<Node> added = new ArrayList<>();
        layers.add(new ArrayList<>());
        // adding all the entry nodes to the first layer
        for (Node node : schedule.getNodes()) {
            if (node.getIncomingEdges().size() == 0) {
                layers.get(0).add(node);
                added.add(node);
            }
        }
        // spacing between layers, and between nodes on each layer, is 2*scale
        int scale = 10;
        // set to values that make adds all the entry nodes evenly spaced at y = 0
        int xCoOrd = scale - scale * layers.get(0).size();
        int yCoOrd = 0;
        for (Node node : layers.get(0)) {
            addNode(node, xCoOrd, yCoOrd);
            xCoOrd += 2 * scale;
        }

        while (added.size() < schedule.getNodes().size()) {
            layers.add(new ArrayList<>());
            int maxChildren = 0;
            for (Node node : layers.get(layers.size() - 2)) {
                // each node has a certain number (possibly 0) of children on the next layer. The next layer will be
                // spaced as though each node has the same number of children as the node with the most children
                int childrenOnNextLayer = 0;
                for (Node child : node.getChildren()) {
                    // if this child has not been added yet, but all its parents have, it is added to the next layer
                    if (!added.contains(child)) {
                        boolean parentsAdded = true;
                        for (Node parent : child.getIncomingEdges().keySet()) {
                            // if one of the child's parents is on this layer then don't add to this, add to next
                            if (!added.contains(parent) || layers.get(layers.size() - 1).contains(parent)) {
                                parentsAdded = false;
                            }
                        }
                        if (parentsAdded) {
                            childrenOnNextLayer++;
                            layers.get(layers.size() - 1).add(child);
                            added.add(child);
                        }
                    }
                }
                if (childrenOnNextLayer > maxChildren) {
                    maxChildren = childrenOnNextLayer;
                }
            }
            // starting xCoOrd is based on how wide child layer would be if all nodes had same number of children
            xCoOrd = scale - scale * layers.get(layers.size() - 2).size() * maxChildren;
            yCoOrd = 0 - 2 * scale * (layers.size() - 1);
            // painting all the children and their incoming edges.
            for (Node node : layers.get(layers.size() - 1)) {
                addNode(node, xCoOrd, yCoOrd);
                for (Node parent : node.getIncomingEdges().keySet()) {
                    addEdge(parent, node, node.getIncomingEdges().get(parent));
                }
                xCoOrd += scale * 2;
            }
        }
    }
}