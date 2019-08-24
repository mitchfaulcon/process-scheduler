package se306.scheduler.controller;


import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.graphstream.ui.fx_viewer.FxDefaultView;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.fx_viewer.util.FxMouseManager;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.stylesheet.Selector;
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

import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;

public class HomeController implements Initializable, AlgorithmListener {

    @FXML AnchorPane anchorPane, headerPane1, headerPane2, numProcPane, numThreadsPane, bestTimePane, checkedPane;
    @FXML Rectangle greyRectangle;
    @FXML Button startButton;
    @FXML Pane graphPane;
    @FXML ScrollPane scrollPane;
    @FXML Pane bottomPane;
    @FXML Pane timerboxPane;
    @FXML Label timeDisplay, filenameLabel, numProcLabel, numThreadsLabel,
                bestTimeLabel, checkedLabel, timeTitleLabel, headerLabel;

    private final static double MAX_TEXT_WIDTH = 197;
    private final static double DEFAULT_FONT_SIZE = 50;
    final double FILE_LABEL_SIZE = 20;

    private final static Font DEFAULT_FONT = Font.font("Consolas", FontWeight.BOLD, DEFAULT_FONT_SIZE);
    private final static Paint DEFAULT_COLOR = Paint.valueOf("#1b274e");

    private GraphDisplay graphDisplay;
    private OutputSchedule outputSchedule;
    private Timer timer = Timer.getInstance(true);
    private Scheduler scheduler;
    private Map<String, String> nodeColours;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Setup action events
        startButton.setOnAction(event -> start());
        startButton.setOnMouseEntered(event -> startButton.getStyleClass().add("button-hover"));
        startButton.setOnMouseExited(event -> startButton.getStyleClass().remove("button-hover"));
        anchorPane.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                startButton.fire();
            }
        });

        setTextProperty(numProcLabel);
        setTextProperty(numThreadsLabel);
        setTextProperty(bestTimeLabel);
        setTextProperty(checkedLabel);

        setStatsAnimation(numProcPane);
        setStatsAnimation(numThreadsPane);
        setStatsAnimation(bestTimePane);
        setStatsAnimation(checkedPane);

        setTextProperty(Font.font("Consolas", FILE_LABEL_SIZE), FILE_LABEL_SIZE, 673, Paint.valueOf("#000000"), filenameLabel);

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

        // Display input graph
        graphDisplay = GraphDisplay.getGraphDisplay();
        graphDisplay.setNodeColours(nodeColours);
        graphDisplay.addNodes(new PartialSchedule(nodes));


        //Display output schedule
        NumberAxis xAxis = new NumberAxis();
        CategoryAxis yAxis = new CategoryAxis();
        int numProcessors = ProcessScheduler.getNumProcessors();
        outputSchedule = new OutputSchedule<>(xAxis,yAxis, numProcessors, scrollPane.getPrefHeight(), nodeColours);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(outputSchedule);

        int numNodes = scheduler.getNodes().size();

        timer.setMaxSchedules(BigInteger.valueOf(numProcessors).pow(numNodes).multiply(Algorithm.factorial(numNodes)));

        //Set initial timer label
        timeDisplay.setText(timer.getSspTime().get());

        //Listener for when ssp time changes in timer
        timer.getSspTime().addListener(observable -> {
            //Update label in application thread
            Platform.runLater(() -> {
                timeDisplay.setText(timer.getSspTime().get());
                checkedLabel.setText(timer.getSchedulesRemaining());
            });
        });
    }

    private void start() {
        greyRectangle.setVisible(false);
        anchorPane.getChildren().remove(startButton);
        filenameLabel.setText("Input file: " + ProcessScheduler.getFileName());
        numThreadsLabel.setText(String.valueOf(ProcessScheduler.getNumThreads()));
        numProcLabel.setText(String.valueOf(ProcessScheduler.getNumProcessors()));

        //Display graph
        org.graphstream.graph.Graph graph = graphDisplay.getGraph();
        FxViewer fxViewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        GraphRenderer renderer = new FxGraphRenderer();
        FxDefaultView view = (FxDefaultView) fxViewer.addView(FxViewer.DEFAULT_VIEW_ID, renderer);
        view.setPrefSize(graphPane.getPrefWidth(), graphPane.getPrefHeight());

        view.setMouseManager(new FxMouseManager(){
            @Override
            protected void elementMoving(GraphicElement element, MouseEvent event) {
                //Only move if the weight labels aren't clicked (i.e. only the nodes can be moved)
                if(!element.getSelectorType().equals(Selector.Type.SPRITE)){
                    view.moveElementAtPx(element, event.getX(), event.getY());
                }
            }
        });

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
            headerPane1.getStyleClass().add("header-done");
            headerPane2.getStyleClass().add("header-done");
            headerLabel.setText("Best Output Schedule");
            timeTitleLabel.setText("Completion time");
            checkedLabel.setText("0");
            endAnimation();
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

    private void setTextProperty(Font font, double size, double maxWidth, Paint paint, Label label) {
        label.setFont(font);
        label.setTextFill(paint);

        label.textProperty().addListener(((observable, oldValue, newValue) -> {
            Text tmpText = new Text(newValue);
            tmpText.setFont(font);

            double textWidth = tmpText.getLayoutBounds().getWidth();

            //check if text width is smaller than maximum width allowed
            if (textWidth <= maxWidth) {
                label.setFont(font);
            } else {
                //and if it isn't, calculate new font size,
                // so that label text width matches MAX_TEXT_WIDTH
                double newFontSize = size * maxWidth / textWidth;
                label.setFont(Font.font(font.getFamily(), newFontSize));
            }
        }));
    }

    private void setTextProperty(Label label) {
        setTextProperty(DEFAULT_FONT, DEFAULT_FONT_SIZE, MAX_TEXT_WIDTH, DEFAULT_COLOR, label);
    }

    private TranslateTransition animation(javafx.scene.Node node, double x, double y, Duration duration) {
        TranslateTransition transition = new TranslateTransition(duration, node);
        transition.setToX(x);
        transition.setToY(y);
        transition.setAutoReverse(true);

        return transition;
    }

    private TranslateTransition paneUpAnimation(AnchorPane pane, Duration duration) {
        TranslateTransition transition = animation(pane, 0, -20, duration);
        transition.play();

        return transition;
    }

    private TranslateTransition paneDownAnimation(AnchorPane pane, Duration duration) {
        TranslateTransition transition = animation(pane, 0, 0, duration);
        transition.play();

        return transition;
    }

    private void setStatsAnimation(AnchorPane pane) {
        final Duration DURATION = Duration.millis(100);

        pane.setOnMouseEntered(e -> paneUpAnimation(pane, DURATION));
        pane.setOnMouseExited(e -> paneDownAnimation(pane, DURATION));
    }

    private void endAnimation() {
        final Duration DURATION = Duration.millis(100);

        TranslateTransition up1 = paneUpAnimation(numProcPane, DURATION);
        TranslateTransition up2 = paneUpAnimation(numThreadsPane, DURATION);
        TranslateTransition up3 = paneUpAnimation(bestTimePane, DURATION);
        TranslateTransition up4 = paneUpAnimation(checkedPane, DURATION);
        TranslateTransition down1 = paneDownAnimation(numProcPane, DURATION);
        TranslateTransition down2 = paneDownAnimation(numThreadsPane, DURATION);
        TranslateTransition down3 = paneDownAnimation(bestTimePane, DURATION);
        TranslateTransition down4 = paneDownAnimation(checkedPane, DURATION);
        PauseTransition pt = new PauseTransition(Duration.millis(100));

        SequentialTransition upSequence = new SequentialTransition(up1, up2, up3, up4);
        SequentialTransition downSequence = new SequentialTransition(pt, down1, down2, down3, down4);
        ParallelTransition animation = new ParallelTransition(upSequence, downSequence);
        animation.play();


    }

}
