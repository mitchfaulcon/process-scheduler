package se306.scheduler.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.graphstream.ui.fx_viewer.FxDefaultView;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.GraphRenderer;
import se306.scheduler.graph.GraphDisplay;
import se306.scheduler.logic.Timer;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable{

    @FXML Label timeDisplay;
    @FXML Button stopTimer;
    @FXML Pane graphPane;

    private Timer timer = new Timer();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        Platform.runLater(() -> {
//            Graph graph = GraphDisplay.getGraphDisplay().getGraph();

//        FxViewer viewer = GraphDisplay.getGraphDisplay().getGraphView();
//        viewer.enableAutoLayout();

//            viewer.getDefaultView().setMouseManager(new FxMouseManager(){
//                @Override
//                protected void elementMoving(GraphicElement element, MouseEvent event) {
//                    //Only move if the weight labels aren't clicked (i.e. only the nodes can be moved)
////                    if(!element.getSelectorType().equals(Selector.Type.SPRITE)){
////                        view.moveElementAtPx(element, event.getX(), event.getY());
////                    }
//                }
//            });

//        FxDefaultView fxDefaultView = (FxDefaultView) viewer.addDefaultView(false, new FxGraphRenderer());
//        fxDefaultView.setPrefSize(graphPane.getPrefWidth(),graphPane.getPrefHeight());
//
//
//        graphPane.getChildren().add(fxDefaultView);
//        });

        org.graphstream.graph.Graph graph = GraphDisplay.getGraphDisplay().getGraph();

        System.setProperty("org.graphstream.ui.javafx.renderer", "org.graphstream.ui.javafx.FxGraphRenderer");

        FxViewer fxViewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        GraphRenderer renderer = new FxGraphRenderer();

        FxDefaultView view = (FxDefaultView) fxViewer.addView(FxViewer.DEFAULT_VIEW_ID, renderer);

        view.setPrefSize(graphPane.getPrefWidth(), graphPane.getPrefHeight());
        graphPane.getChildren().add(view);


        //Set initial timer label
        timeDisplay.setText(timer.getSspTime().get());

        //Listener for when ssp time changes in timer
        timer.getSspTime().addListener(observable -> {
            //Update label in application thread
            Platform.runLater(() -> timeDisplay.setText(timer.getSspTime().get()));
        });

        timer.startTimer(0);
    }


    @FXML
    private void onClick(ActionEvent event){
        if (event.getSource().equals(stopTimer)){
            timer.stopTimer();
        }
    }
}
