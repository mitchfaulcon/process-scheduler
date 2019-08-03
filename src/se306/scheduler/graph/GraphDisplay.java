package se306.scheduler.graph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.util.Random;

public class GraphDisplay {

    private static final GraphDisplay graphDisplay = new GraphDisplay();

    private Graph graph = new SingleGraph("graphDisplay", false, true);
    private String title = "Graph";
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
     * Display the graph in a JFrame window
     */
    public void displayGraph(){
        //Displays the nodes & edges with correct formatting
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        JFrame frame = new JFrame(title);

        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        frame.add(viewer.addDefaultView(false));

        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    /**
     * Method to add a node to the graph display
     * @param nodeName the label displayed on the node in the graph
     */
    public void addNode(String nodeName){
        graph.addNode(nodeName);
        Node node = graph.getNode(nodeName);

        //Set Label & Style
        node.addAttribute("ui.label", nodeName);
        node.addAttribute("ui.style", "shape:circle;fill-color: white;size: 50px;" +
                "stroke-color: black;stroke-mode: plain;stroke-width: 1px;" +
                "text-alignment: center;text-size: 30px;");

        //Set node coordinate to a 'good enough' initial location (can be moved by user in pop-up window later)
        Random random = new Random();
        double xCoord = (2 * random.nextDouble() - 1) * yCoord;
        node.addAttribute("xy", xCoord, yCoord++);
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
                "text-background-mode: plain;text-size: 30px;" +
                "arrow-size: 10px;");
    }
}
