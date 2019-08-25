package se306.scheduler.controller;


import java.math.BigInteger;
import java.net.URL;
import java.util.*;

import org.graphstream.ui.fx_viewer.FxDefaultView;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.fx_viewer.util.FxMouseManager;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.stylesheet.Selector;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.GraphRenderer;

import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.GraphicsContext;
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
import se306.scheduler.ProcessScheduler;
import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;
import se306.scheduler.logic.Algorithm;
import se306.scheduler.logic.AlgorithmListener;
import se306.scheduler.logic.Scheduler;
import se306.scheduler.visualisation.GraphDisplay;
import se306.scheduler.visualisation.OutputSchedule;
import se306.scheduler.visualisation.Timer;

/**
 * Controller for home.fxml
 */
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
    private final double FILE_LABEL_SIZE = 20;

    private final static Font DEFAULT_FONT = Font.font("Consolas", FontWeight.BOLD, DEFAULT_FONT_SIZE);
    private final static Paint DEFAULT_COLOR = Paint.valueOf("#1b274e");

    private GraphDisplay graphDisplay;
    private OutputSchedule<Number, String> outputSchedule;
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
        setTextProperty(Font.font("Consolas", FILE_LABEL_SIZE),
                FILE_LABEL_SIZE, 673, Paint.valueOf("#000000"), filenameLabel);

        setStatsAnimation(numProcPane);
        setStatsAnimation(numThreadsPane);
        setStatsAnimation(bestTimePane);
        setStatsAnimation(checkedPane);

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
        outputSchedule = new OutputSchedule<Number, String>(xAxis,yAxis, numProcessors, scrollPane.getPrefHeight(), nodeColours);
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

    /**
     * Upon starting the schedule the following occurs:
     * <ol>
     *     <li>Remove start button</li>
     *     <li>Set the values for the statistics</li>
     *     <li>Display graph elements and arrange them nicely</li>
     *     <li>Begin calculating optimal schedule and start timer</li>
     * </ol>
     */
    private void start() {
        //Remove start button and rectangle
        greyRectangle.setVisible(false);
        anchorPane.getChildren().remove(startButton);

        //Set texts for statistics
        filenameLabel.setText("Input file: " + ProcessScheduler.getFileName());
        numThreadsLabel.setText(String.valueOf(ProcessScheduler.getNumThreads()));
        numProcLabel.setText(String.valueOf(ProcessScheduler.getNumProcessors()));

        //Display graph
        org.graphstream.graph.Graph graph = graphDisplay.getGraph();
        FxViewer fxViewer = new FxViewer(graph, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        GraphRenderer<Pane, GraphicsContext> renderer = new FxGraphRenderer();
        FxDefaultView view = (FxDefaultView) fxViewer.addView(FxViewer.DEFAULT_VIEW_ID, renderer);
        view.setPrefSize(graphPane.getPrefWidth(), graphPane.getPrefHeight());
        view.setMouseManager(new FxMouseManager(){
            @Override
            protected void elementMoving(GraphicElement element, MouseEvent event) {
                //Only move if the weight labels aren't clicked (i.e. only the nodes can be moved)
                //Also cannot move the invisible buddy nodes
                if(!element.getSelectorType().equals(Selector.Type.SPRITE) && !element.getId().contains("invisible")){
                    view.moveElementAtPx(element, event.getX(), event.getY());
                    //Move invisible buddy node with normal node
                    GraphicElement graphicElement = (GraphicElement) graph.getNode("invisible" + element.getId());
                    view.moveElementAtPx(graphicElement, event.getX()+50, event.getY());
                }
            }
        });
        graphPane.getChildren().add(view);

        //Calculate optimal schedule in new thread
        timer.startTimer(0);
        new Thread(scheduler::start).start();
    }

    /**
     * Once the algorithm has completed, the following occur:
     * <ol>
     *     <li>Stop timer</li>
     *     <li>Change color scheme to green</li>
     *     <li>Play end animation</li>
     * </ol>
     */
    @Override
    public void algorithmCompleted(PartialSchedule schedule) {
        timer.stopTimer();

        Platform.runLater(() -> {
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

    /**
     * Updates the GUI whenever a new optimal schedule is found.
     * GUI updates include updating the text for best time, and updating the gantt chart to display
     * the new optimal schedule.
     *
     * @param schedule new optimal schedule
     */
    @Override
    public void newOptimalFound(PartialSchedule schedule) {
        //Update output schedule in GUI thread
        Platform.runLater(() -> {
            bestTimeLabel.setText(String.valueOf(schedule.getMakespan()));
            outputSchedule.update(schedule);
        });
    }

    /**
     * Sets the text property to have a text size that fits within the given bounds of
     * the label.
     *
     * @param font Font of the text used in the label
     * @param size Size of the font (px)
     * @param maxWidth Maximum width until text starts resizing
     * @param paint Color of the text
     * @param label Label to apply text property to
     */
    private void setTextProperty(Font font, double size, double maxWidth, Paint paint, Label label) {
        label.setFont(font);
        label.setTextFill(paint);

        //Add listener to check if text still fits within maxWidth
        label.textProperty().addListener(((observable, oldValue, newValue) -> {
            //Apply the text and font to a temporary Text object
            Text tmpText = new Text(newValue);
            tmpText.setFont(font);

            double textWidth = tmpText.getLayoutBounds().getWidth();

            //Check if the width of the temporary Text object is small enough with the new text value
            if (textWidth <= maxWidth) {
                // If it is maintain the same font
                label.setFont(font);
            } else {
                //Otherwise calculate a new font size so that the new text can fit within max width
                double newFontSize = size * maxWidth / textWidth;
                label.setFont(Font.font(font.getFamily(), newFontSize));
            }
        }));
    }

    private void setTextProperty(Label label) {
        setTextProperty(DEFAULT_FONT, DEFAULT_FONT_SIZE, MAX_TEXT_WIDTH, DEFAULT_COLOR, label);
    }

    /**
     * Apply a translation animation to the Node.
     * Moves Node towards the entered x,y value
     *
     * @param node Node to apply animation to
     * @param x value of x to move node towards
     * @param y value of y to move node towards
     * @param duration how long it takes to reach the y value.
     * @return The translation object with the applied animation
     */
    private TranslateTransition animation(javafx.scene.Node node, double x, double y, Duration duration) {
        TranslateTransition transition = new TranslateTransition(duration, node);
        transition.setToX(x);
        transition.setToY(y);
        transition.setAutoReverse(true);

        return transition;
    }

    /**
     * Makes the AnchorPane move upwards by 20 units
     *
     * @see #animation(javafx.scene.Node, double, double, Duration)
     */
    private TranslateTransition paneUpAnimation(AnchorPane pane, Duration duration) {
        TranslateTransition transition = animation(pane, 0, -20, duration);
        transition.play();

        return transition;
    }

    /**
     * Returns the pane back to its original position
     *
     * @see #animation(javafx.scene.Node, double, double, Duration)
     */
    private TranslateTransition resetAnimation(javafx.scene.Node node, Duration duration) {
        TranslateTransition transition = animation(node, 0, 0, duration);
        transition.play();

        return transition;
    }

    /**
     * Sets an animation for each statistic-related AnchorPane
     * that makes it move upwards upon mouse hover, and move
     * downwards upon mouse exit
     *
     * @param pane Pane to apply animation to
     */
    private void setStatsAnimation(AnchorPane pane) {
        final Duration DURATION = Duration.millis(100);

        pane.setOnMouseEntered(e -> paneUpAnimation(pane, DURATION));
        pane.setOnMouseExited(e -> resetAnimation(pane, DURATION));
    }

    /**
     * The end animation moves each Stat-related AnchorPane upwards
     * then downwards in a cascading fashion. Firstly moving all panes up,
     * then moving then downwards after a pause duration (which is set to
     * the duration of 2 panes moving up)
     */
    private void endAnimation() {
        final Duration DURATION = Duration.millis(100);

        // Get translation animations of all panes moving up
        TranslateTransition up1 = paneUpAnimation(numProcPane, DURATION);
        TranslateTransition up2 = paneUpAnimation(numThreadsPane, DURATION);
        TranslateTransition up3 = paneUpAnimation(bestTimePane, DURATION);
        TranslateTransition up4 = paneUpAnimation(checkedPane, DURATION);

        // Get translation animations of all panes moving down
        TranslateTransition down1 = resetAnimation(numProcPane, DURATION);
        TranslateTransition down2 = resetAnimation(numThreadsPane, DURATION);
        TranslateTransition down3 = resetAnimation(bestTimePane, DURATION);
        TranslateTransition down4 = resetAnimation(checkedPane, DURATION);

        PauseTransition pt = new PauseTransition(Duration.millis(200)); // Duration is equal to 2 up-animations

        // Combine the sequences to form the cascading animation
        SequentialTransition upSequence = new SequentialTransition(up1, up2, up3, up4);
        SequentialTransition downSequence = new SequentialTransition(pt, down1, down2, down3, down4);
        ParallelTransition animation = new ParallelTransition(upSequence, downSequence);
        animation.play();
    }

}
