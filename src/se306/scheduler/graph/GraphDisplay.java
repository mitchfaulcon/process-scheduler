package se306.scheduler.graph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

import java.util.Random;

public class GraphDisplay extends GraphParent{

    private static final GraphDisplay graphDisplay = new GraphDisplay();

    private int yCoord = 0;

    private GraphDisplay() {

    }

    public static GraphDisplay getGraphDisplay(){
        return graphDisplay;
    }

    public void setGraphTitle(String title){
        this.title = "Graph of " + title;
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
        Sprite sprite = spriteManager.addSprite(nodeName);
        sprite.attachToNode(nodeName);
        sprite.addAttribute("ui.label", weight);
        sprite.addAttribute("ui.style", "text-background-mode: plain;text-size: 15px;" +
                "fill-color: white;");
        sprite.setPosition(StyleConstants.Units.PX, 40,0,0);

        //Set Label & Style
        node.addAttribute("ui.label", nodeName);
        node.addAttribute("ui.style", "shape:circle;fill-color: white;size: 50px;" +
                "stroke-color: black;stroke-mode: plain;stroke-width: 1px;" +
                "text-alignment: center;text-size: 30px;");

        //Set node coordinate to a 'good enough' initial location (can be moved by user in pop-up window later)
        Random random = new Random();
        double xCoord = (2 * random.nextDouble() - 1) * yCoord;
        node.addAttribute("xy", xCoord, yCoord--);
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
        edge.addAttribute("ui.label", edgeWeight);
        edge.addAttribute("ui.style", "fill-color: rgb(100,100,100);" +
                "text-background-mode: plain;text-size: 20px;" +
                "arrow-size: 10px;");
    }
}
