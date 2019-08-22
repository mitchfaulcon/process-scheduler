package se306.scheduler.controller;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class HomeController implements Initializable, AlgorithmListener {

    @FXML AnchorPane anchorPane;
    @FXML Rectangle greyRectangle;
    @FXML Button startButton;
    @FXML Label timeDisplay, filenameLabel, numProcLabel, numThreadsLabel, bestTimeLabel, checkedLabel, timeTitleLabel;
    @FXML Pane graphPane;
    @FXML ScrollPane scrollPane;
    @FXML Pane bottomPane;
    @FXML Pane timerboxPane;

    private static double MAX_TEXT_WIDTH = 197;

    private GraphDisplay graphDisplay;
    private OutputSchedule outputSchedule;
    private Timer timer = Timer.getInstance();
    private Scheduler scheduler;
    private Map<String, String> nodeColours;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Setup action events
        startButton.setOnAction(event -> start());
        startButton.setOnMouseEntered(event -> startButton.getStyleClass().add("button-hover"));
        startButton.setOnMouseExited(event -> startButton.getStyleClass().remove("button-hover"));

        setTextProperty(numProcLabel);
        setTextProperty(numThreadsLabel);
        setTextProperty(bestTimeLabel);
        setTextProperty(checkedLabel);

        //Get same scheduler & algorithm objects from main class
        scheduler = ProcessScheduler.getScheduler();
        Algorithm algorithm = ProcessScheduler.getAlgorithm();
        algorithm.addListener(this);
        
        // Generate node colours
        List<Node> nodes = scheduler.getNodes();
        nodeColours = new HashMap<String, String>();
        double nodeCount = nodes.size() - 1;
        double i = 0;
        for (Node node: nodes) {
            double p = i++ / nodeCount;
            int red = 64 + (int) (p * 191.0);
            int green = 63;
            int blue = 127 + (int) ((1 - p) * 127.0);
            
            // the colour format required for the two views is different, one requires 0-1 and one requires 0-255
            nodeColours.put(node.getName(), String.format("rgba(%s,%s,%s,%%s)", Integer.toString(red), Integer.toString(green), Integer.toString(blue)));
        }
        GraphDisplay.getGraphDisplay().setNodeColours(nodeColours);
        
        // Display input graph
        graphDisplay = GraphDisplay.getGraphDisplay();
        for (Node node: nodes) {
            graphDisplay.addNode(node.getName(), node.getWeight());
            
            for (Node parent: node.getIncomingEdges().keySet()) {
                graphDisplay.addEdge(parent.getName(), node.getName(), node.getIncomingEdges().get(parent));
            }
        }

        //Display output schedule
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        int numProcessors = ProcessScheduler.getNumProcessors();
        outputSchedule = new OutputSchedule<>(xAxis,yAxis, numProcessors, scrollPane.getPrefHeight(), nodeColours);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(outputSchedule);

        int numNodes = scheduler.getNodes().size();

        timer.setMaxSchedules((long)Math.pow(numProcessors, numNodes)*Algorithm.factorial(numNodes));

        //Set initial timer label
        timeDisplay.setText(timer.getSspTime().get());

        //Listener for when ssp time changes in timer
        timer.getSspTime().addListener(observable -> {
            //Update label in application thread
            Platform.runLater(() -> {
                timeDisplay.setText(timer.getSspTime().get());
                checkedLabel.setText(String.valueOf(timer.getSchedulesRemaining()));
            });
        });
    }

    private void start() {
        greyRectangle.setVisible(false);
        anchorPane.getChildren().remove(startButton);
        filenameLabel.setText(ProcessScheduler.getFileName());
        numThreadsLabel.setText(String.valueOf(ProcessScheduler.getNumThreads()));
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
    
    public void setNodeColours(Map<String, String> nodeColours) {
        this.nodeColours = nodeColours;
    }

    @Override
    public void algorithmCompleted(PartialSchedule schedule) {
        timer.stopTimer();
        Platform.runLater(() -> {
//            timeDisplay.getStyleClass().add("timer-done");
//            timeTitleLabel.getStyleClass().addAll("timer-done", "timer-done-title");
            bottomPane.getStyleClass().add("footer-done");
            timerboxPane.getStyleClass().add("timer-box-done");
            timeTitleLabel.setText("Completion time");
            checkedLabel.setText("0");
        });
    }

    @Override
    public void newOptimalFound(PartialSchedule schedule) {
        //Update output schedule in GUI thread
        Platform.runLater(() -> {
            bestTimeLabel.setText(String.valueOf(schedule.getMakespan()));
            outputSchedule.update(schedule);
        });
    }

    private void setTextProperty(Label label) {
        double defaultSize = 50;
        Font defaultFont = Font.font("Consolas", FontWeight.BOLD, defaultSize);
        label.setFont(defaultFont);
        label.setTextFill(Paint.valueOf("#1b274e"));

        label.textProperty().addListener(((observable, oldValue, newValue) -> {
            Text tmpText = new Text(newValue);
            tmpText.setFont(defaultFont);

            double textWidth = tmpText.getLayoutBounds().getWidth();

            //check if text width is smaller than maximum width allowed
            if (textWidth <= MAX_TEXT_WIDTH) {
                label.setFont(defaultFont);
            } else {
                //and if it isn't, calculate new font size,
                // so that label text width matches MAX_TEXT_WIDTH
                double newFontSize = defaultSize * MAX_TEXT_WIDTH / textWidth;
                label.setFont(Font.font(defaultFont.getFamily(), newFontSize));
            }
        }));
    }

//    @Override
//    public void updateSchedulesChecked(long schedules) {
////        new Thread(() -> {
////
////            checkedLabel.setText(String.valueOf(max - schedules));
////        }).start();
////        Platform.runLater(() -> checkedLabel.setText());
//    }
}
