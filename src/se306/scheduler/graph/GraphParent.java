package se306.scheduler.graph;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.stylesheet.Selector;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.util.DefaultMouseManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class GraphParent implements se306.scheduler.graph.Graph {

    Graph graph = new SingleGraph("graphDisplay", false, true);
    String title = "Graph";
    JFrame frame;
    boolean isOutputSchedule = false;

    /**
     * Display the graph in a JFrame window
     */
    public void displayGraph(){
        //Displays the nodes & edges with correct formatting
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        frame = new JFrame(title);
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        frame.add(viewer.addDefaultView(false));
        viewer.getDefaultView().setMouseManager(new DefaultMouseManager(){
            @Override
            protected void elementMoving(GraphicElement element, MouseEvent event) {
                //Only move if the weight labels aren't clicked (i.e. only the nodes can be moved)
                //Also can't move anything in the output schedule
                if(!element.getSelectorType().equals(Selector.Type.SPRITE) && !isOutputSchedule){
                    view.moveElementAtPx(element, event.getX(), event.getY());
                }
            }
        });

        frame.setLocationRelativeTo(null);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) screenSize.getWidth()/2,(int) screenSize.getHeight() - 50);
        setFrameLocation();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    public void setFrameLocation() {
        frame.setLocation(0,0);
    }
}
