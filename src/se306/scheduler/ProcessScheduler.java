package se306.scheduler;

import com.martiansoftware.jsap.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import se306.scheduler.exception.InvalidFileFormatException;
import se306.scheduler.graph.GraphDisplay;
import se306.scheduler.graph.Node;
import se306.scheduler.graph.OutputGraph;
import se306.scheduler.logic.Algorithm;
import se306.scheduler.logic.AlgorithmListener;
import se306.scheduler.logic.DFSAlgorithm;
import se306.scheduler.logic.Scheduler;
import se306.scheduler.logic.SequentialAlgorithm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class ProcessScheduler extends Application implements AlgorithmListener {

    private JSAPResult config;
    private DotFile dot;
    private Scheduler scheduler;
    
	public static void main(String[] args) {
	    ProcessScheduler processScheduler = new ProcessScheduler();
	    processScheduler.parse(args);
	    processScheduler.schedule(args);
	}
	
	public ProcessScheduler() {
	    
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		//set event handler for when user presses 'x' button on stage.
		primaryStage.setOnCloseRequest(evt -> {
			// prevent window from closing
			evt.consume();

			// execute own shutdown procedure
			shutdown();
		});

		//Change to home screen
		Parent root = FXMLLoader.load(getClass().getResource("view/Home.fxml"));
		primaryStage.setTitle("Process Scheduler");
		primaryStage.setScene(new Scene(root, 1420, 800));
		primaryStage.show();
		primaryStage.setResizable(false);
	}

	public void parse(String[] args) {
        System.err.close();  // Workaround to stop help being printed twice
        SimpleJSAP jsap = buildParser();
        config = jsap.parse(args);
        if (!config.success()) {
            System.out.println("Usage: java -jar scheduler.jar "  + jsap.getUsage() + "\n");
            System.out.println(jsap.getHelp(JSAP.DEFAULT_SCREENWIDTH, ""));
            System.exit(1);
        }
        
        // Call methods with these values
//      System.out.println("Input file: " + config.getString("INPUT"));
//      System.out.println("N Processors: " + config.getInt("P"));
//      System.out.println("Cores to use: " + config.getInt("N"));
//      System.out.println("Visualise: " + config.getBoolean("V"));
//      System.out.println("Output file: " + config.getString("OUTPUT", config.getString("INPUT") + "-output.dot"));
	}
	
	public void schedule(String[] args) {
        //Algorithm algorithm = new SequentialAlgorithm();
        Algorithm algorithm = new DFSAlgorithm(config.getInt("P"));
        scheduler = new Scheduler(algorithm);
        
        algorithm.addListener(this);

		try {
			// attempt to load the input file
			dot = new DotFile(config.getString("INPUT"));
			dot.read(scheduler);

			// set up graphs if -v flag specified
			if(config.getBoolean("V")) {
				launch(args);
			}

			//Calculate the schedule
			scheduler.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Input Error: File not found");
        } catch (InvalidFileFormatException e) {
			e.printStackTrace();
			System.out.println("Invalid File format: Does not end in \".dot\"");
		}

	}

	static SimpleJSAP buildParser() {
		Parameter input = new UnflaggedOption("INPUT", JSAP.STRING_PARSER, JSAP.REQUIRED,
				"A task graph with integer weights in dot format");

		Parameter nProcessors = new UnflaggedOption("P", JSAP.INTEGER_PARSER, JSAP.REQUIRED,
				"Number of processors to schedule the INPUT graph on");

		Parameter nCores = new FlaggedOption("N", JSAP.INTEGER_PARSER, "1", JSAP.NOT_REQUIRED, 'p', null,
				"How many parallel threads to use");
		
		Parameter vis = new Switch("V", 'v', null,
				"Visualise the search");
		
		Parameter output = new FlaggedOption("OUTPUT", JSAP.STRING_PARSER, null, JSAP.NOT_REQUIRED, 'o', null,
				"Set output filename (default is INPUT-output.dot)");

		try {
			return new SimpleJSAP("scheduler.jar", "Finds optimal schedule for given tasks",
					new Parameter[] { input, nProcessors, nCores, vis, output });
		} catch (JSAPException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

    // once a schedule has been found, write the output to a file
    @Override
    public void algorithmCompleted(List<Node> schedule) {
        try {
            dot.write(config.getString("OUTPUT"), schedule);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Output Error: File could not be written to");
        }
        
        if(config.getBoolean("V")) {
//            GraphDisplay.getGraphDisplay().displayGraph();
//            OutputGraph.getOutputGraph().displayGraph();
        }
    }

	private void shutdown(){
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?", ButtonType.YES, ButtonType.NO);
		if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
			//Quit
			System.exit(1);
		}
	}
}
