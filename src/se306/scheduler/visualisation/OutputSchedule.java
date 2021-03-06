package se306.scheduler.visualisation;

import javafx.collections.FXCollections;
import javafx.scene.chart.Axis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;

import java.util.*;

/** Class to display the current optimal schedule as a Gantt chart
 *
 * Adapted code from
 * <a href="https://stackoverflow.com/questions/27975898/gantt-chart-from-scratch"/>
 */
public class OutputSchedule<X,Y> extends XYChart<X,Y>{

    /**
     * Class to contain extra information about each bar in the graph
     */
    public static class ExtraData {

        public int length;
        public String styleClass;
        public String colour;
        public String name;

        public ExtraData(int lengthMs, String styleClass, String colour, String name) {
            super();
            this.length = lengthMs;
            this.styleClass = styleClass;
            this.colour = colour;
            this.name = name;
        }
        public double getLength() {
            return length;
        }
        
        public String getColour() {
            return colour;
        }
        
        public String getName() {
            return name;
        }
    }

    private double blockHeight;
    private String[] labels;
    private int numProcessors;
    private Map<String, String> nodeColours;

	public void update(PartialSchedule newSchedule){

	    //Remove all bars from the current graph before updtaing it
        this.getData().clear();

        //Run through each processor
        for (int processor=0; processor<labels.length; processor++){
            Series<X, Y> series = new Series<>();

            //Check if any node is scheduled in that processor
            for (Node node: newSchedule.getNodes()){
                if (newSchedule.getProcessor(node)-1==processor){
                    //Add node data to graph
                    String colour = String.format(nodeColours.get(node.getName()), "0.5");
                    ExtraData extraData = new ExtraData(node.getWeight(), "status-"+node.getName(), colour, node.getName());

                    //Add tooltip for when hovering over section
                    Data<X, Y> data = (Data<X, Y>) new Data<Integer, String>(newSchedule.getStartTime(node), labels[numProcessors - processor - 1], extraData);
                    data.setNode(new StackPane());
                    Tooltip.install(data.getNode(),new Tooltip("Node: " + node.getName() +"\n" +
                            "Start Time: " + newSchedule.getStartTime(node) +"\n" +
                            "Finish Time: " + Integer.toString(newSchedule.getStartTime(node) + node.getWeight())));

                    series.getData().add(data);
                }
            }
            this.getData().addAll(series);
        }
    }

    public OutputSchedule(Axis<X> xAxis, Axis<Y> yAxis, int numProcessors, double windowHeight, Map<String, String> nodeColours) {
        super(xAxis,yAxis);
        
        this.numProcessors = numProcessors;
        this.nodeColours = nodeColours;

        //Create correct labels for Y-axis
        labels = new String[numProcessors];
        for (int i=numProcessors - 1; i>=0; i--){
            labels[numProcessors - i - 1] = "Processor " + Integer.toString(i+1);
        }

        if (!(xAxis instanceof NumberAxis && yAxis instanceof CategoryAxis)){
            throw new IllegalArgumentException("Axis type incorrect, X should be NumberAxis and Y should be CategoryAxis");
        }
        //Apply Y-axis labels
        setData(FXCollections.observableArrayList());
        ((CategoryAxis) yAxis).setCategories(FXCollections.observableArrayList(Arrays.asList(labels)));

        //Remove minor ticks on X-axis
        ((NumberAxis) xAxis).setMinorTickCount(0);

        this.setAnimated(false);

        //Set block height based on parent window size
        blockHeight = 0.8 * windowHeight/numProcessors;

    }

    private static double getLength( Object obj) {
        return ((ExtraData) obj).getLength();
    }

    @Override protected void layoutPlotChildren() {

        for (int seriesIndex=0; seriesIndex < getData().size(); seriesIndex++) {

            Series<X,Y> series = getData().get(seriesIndex);

            Iterator<Data<X,Y>> iterator = getDisplayedDataIterator(series);
            while(iterator.hasNext()) {
                Data<X,Y> item = iterator.next();
                double x = getXAxis().getDisplayPosition(item.getXValue());
                double y = getYAxis().getDisplayPosition(item.getYValue());
                if (Double.isNaN(x) || Double.isNaN(y)) {
                    continue;
                }
                javafx.scene.Node block = item.getNode();
                Rectangle rectangle;
                if (block != null) {
                    if (block instanceof StackPane) {
                        StackPane region = (StackPane)item.getNode();
                        if (region.getShape() == null) {
                            rectangle = new Rectangle( getLength( item.getExtraValue()), getBlockHeight());
                        } else if (region.getShape() instanceof Rectangle) {
                            rectangle = (Rectangle)region.getShape();
                        } else {
                            return;
                        }
                        rectangle.setWidth( getLength( item.getExtraValue()) * ((getXAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis)getXAxis()).getScale()) : 1));
                        rectangle.setHeight(getBlockHeight() * ((getYAxis() instanceof NumberAxis) ? Math.abs(((NumberAxis)getYAxis()).getScale()) : 1));
                        y -= getBlockHeight() / 2.0;

                        region.setShape(null);
                        region.setShape(rectangle);
                        region.setScaleShape(false);
                        region.setCenterShape(false);
                        region.setCacheShape(false);
                        //Add border to boxes
                        region.setBorder(new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

                        block.setLayoutX(x);
                        block.setLayoutY(y);

                        //Add a text label for the node name on the boxes
                        Text text = new Text(((ExtraData) item.getExtraValue()).getName());
                        text.setFont(new Font("Consolas", 16));
                        region.getChildren().addAll(text);
                        text.setTranslateX(rectangle.getWidth() / 2);
                        text.setTranslateY(rectangle.getHeight() / 2);
                    }
                }
            }
        }
    }

    public double getBlockHeight() {
        return blockHeight;
    }

    @Override protected void dataItemAdded(Series<X,Y> series, int itemIndex, Data<X,Y> item) {
        javafx.scene.Node block = createContainer(item);
        getPlotChildren().add(block);
    }

    @Override protected  void dataItemRemoved(final Data<X,Y> item, final Series<X,Y> series) {
        final javafx.scene.Node block = item.getNode();
        getPlotChildren().remove(block);
        removeDataItemFromDisplay(series, item);
    }

    @Override protected void dataItemChanged(Data<X, Y> item) {
    }

    @Override protected  void seriesAdded(Series<X,Y> series, int seriesIndex) {
        for (int j=0; j<series.getData().size(); j++) {
            Data<X,Y> item = series.getData().get(j);
            javafx.scene.Node container = createContainer(item);
            getPlotChildren().add(container);
        }
    }

    @Override protected  void seriesRemoved(final Series<X,Y> series) {
        for (XYChart.Data<X,Y> d : series.getData()) {
            final javafx.scene.Node container = d.getNode();
            getPlotChildren().remove(container);
        }
        removeSeriesFromDisplay(series);

    }


    private javafx.scene.Node createContainer(final Data<X,Y> item) {

        javafx.scene.Node container = item.getNode();

        if (container == null) {
            container = new StackPane();
            item.setNode(container);
        }

        //Set colour of boxes
        String colour = ((ExtraData) item.getExtraValue()).getColour();
        container.setStyle("-fx-background-color:" + colour + ";");

        return container;
    }

    @Override protected void updateAxisRange() {
        final Axis<X> xa = getXAxis();
        final Axis<Y> ya = getYAxis();
        List<X> xData = null;
        List<Y> yData = null;
        if(xa.isAutoRanging()) xData = new ArrayList<X>();
        if(ya.isAutoRanging()) yData = new ArrayList<Y>();
        if(xData != null || yData != null) {
            for(Series<X,Y> series : getData()) {
                for(Data<X,Y> data: series.getData()) {
                    if(xData != null) {
                        xData.add(data.getXValue());
                        xData.add(xa.toRealValue(xa.toNumericValue(data.getXValue()) + getLength(data.getExtraValue())));
                    }
                    if(yData != null){
                        yData.add(data.getYValue());
                    }
                }
            }
            if(xData != null) xa.invalidateRange(xData);
            if(yData != null) ya.invalidateRange(yData);
        }
    }

}