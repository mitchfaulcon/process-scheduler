package se306.scheduler.graph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

import java.awt.*;

public class OutputGraph extends GraphParent {

    private static final OutputGraph outputGraph = new OutputGraph();
    
    private int yCoord = 0;

    private OutputGraph(){
        this.title = "Output Schedule";
        this.isOutputSchedule = true;
    }

    public static OutputGraph getOutputGraph() {
        return outputGraph;
    }

    /**
     * Method to add a node and its start time to the graph
     * @param nodeName the name of the node
     * @param startTime the start time calculated for the node
     */
    public void addNode(String nodeName, int startTime) {
        graph.addNode(nodeName);
        Node node = graph.getNode(nodeName);

        //Add extra label for node start time
        SpriteManager spriteManager = new SpriteManager(graph);
        Sprite startTimeLabel = spriteManager.addSprite(nodeName);
        startTimeLabel.attachToNode(nodeName);
        startTimeLabel.addAttribute("ui.label", "Start time: " + startTime);
        startTimeLabel.addAttribute("ui.style", "text-background-mode: plain;text-size: 15px;" +
                "fill-color: white;");
        startTimeLabel.setPosition(StyleConstants.Units.PX, 75,0,0);

        //Set Label & Style
        node.addAttribute("ui.label", nodeName);
        node.addAttribute("ui.style", "shape:circle;fill-color: white;size: 50px;" +
                "stroke-color: black;stroke-mode: plain;stroke-width: 1px;" +
                "text-alignment: center;text-size: 30px;");

        node.addAttribute("xy", 0, yCoord--);
    }

    /**
     * Method to add an edge from one node to the next from the schedule
     * @param node1 A node in the graph
     * @param node2 The node with the next start time in the schedule
     */
    public void addEdge(String node1, String node2, int weight){
        String edgeID = node1 + node2;
        graph.addEdge(edgeID, node1, node2, true);
        Edge edge = graph.getEdge(edgeID);

        //Add weight label and set edge label & arrow style
        edge.addAttribute("ui.style", "fill-color: rgb(100,100,100);" +
                "text-background-mode: plain;text-size: 20px;" +
                "arrow-size: 10px;");
    }

    public void setFrameLocation() {
        //Displays the output schedule on the right of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((int) screenSize.getWidth()/2,0);
    }
}
