package se306.scheduler.controller;


import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import org.graphstream.ui.fx_viewer.FxDefaultView;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.GraphRenderer;
import se306.scheduler.ProcessScheduler;
import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;
import se306.scheduler.logic.Algorithm;
import se306.scheduler.logic.AlgorithmListener;
import se306.scheduler.logic.Scheduler;
import se306.scheduler.visualisation.GraphDisplay;
import se306.scheduler.visualisation.OutputSchedule;
import se306.scheduler.visualisation.Timer;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable, AlgorithmListener {

    @FXML ProgressBar progressBar;
    @FXML AnchorPane anchorPane;
    @FXML Rectangle greyRectangle;
    @FXML Button startButton;
    @FXML Label timeDisplay, filenameLabel, numProcLabel, progressLabel;
    @FXML Pane graphPane;
    @FXML ScrollPane scrollPane;

    private OutputSchedule outputSchedule;
    private Timer timer = new Timer();
    private Scheduler scheduler;
    private long max;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Setup action events
        startButton.setOnAction(event -> start());

        //Display output schedule
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        int numProcessors = ProcessScheduler.getNumProcessors();
        outputSchedule = new OutputSchedule<>(xAxis,yAxis, numProcessors, scrollPane.getPrefHeight());
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(outputSchedule);

        //Get same scheduler & algorithm objects from main class
        scheduler = ProcessScheduler.getScheduler();
        Algorithm algorithm = ProcessScheduler.getAlgorithm();
        algorithm.addListener(this);

        //Set initial timer label
        timeDisplay.setText(timer.getSspTime().get());

        //Set max number of branches for progress bar
        int numProc = ProcessScheduler.getNumProcessors();
        int numNodes = scheduler.getNodes().size();
        max = (long) Math.pow(numProc, numNodes)*Algorithm.factorial(numNodes);

        //Listener for when ssp time changes in timer
        timer.getSspTime().addListener(observable -> {
            //Update label in application thread
            Platform.runLater(() -> timeDisplay.setText(timer.getSspTime().get()));
        });
    }

    private void start() {
        greyRectangle.setVisible(false);
        anchorPane.getChildren().remove(startButton);
        filenameLabel.setText(ProcessScheduler.getFileName());
        numProcLabel.setText(String.valueOf(ProcessScheduler.getNumProcessors()));

        //Display graph
        org.graphstream.graph.Graph graph = GraphDisplay.getGraphDisplay().getGraph();
        FxViewer fxViewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        GraphRenderer renderer = new FxGraphRenderer();
        FxDefaultView view = (FxDefaultView) fxViewer.addView(FxViewer.DEFAULT_VIEW_ID, renderer);
        view.setPrefSize(graphPane.getPrefWidth(), graphPane.getPrefHeight());
        graphPane.getChildren().add(view);

        timer.startTimer(0);
        //Calculate optimal schedule in new thread
        new Thread(scheduler::start).start();
    }

    @Override
    public void algorithmCompleted(PartialSchedule schedule) {
        timer.stopTimer();
    }

    @Override
    public void newOptimalFound(PartialSchedule schedule) {
        //Update output schedule in GUI thread
        Platform.runLater(() -> outputSchedule.update(schedule));
    }

    @Override
    public void updateSchedulesChecked(long checked) {
        //TODO: update progress bar
    }
}
