package se306.scheduler;

import com.martiansoftware.jsap.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import se306.scheduler.exception.InvalidFileFormatException;
import se306.scheduler.graph.PartialSchedule;
import se306.scheduler.logic.*;
import se306.scheduler.visualisation.Timer;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ProcessScheduler extends Application implements AlgorithmListener {

    private JSAPResult config;
    private DotFile dot;
    private static Scheduler scheduler;
    private static Algorithm algorithm;
    private static int numProcessors;
    private static int numThreads;
    private static String fileName;
    private Timer timer = Timer.getInstance(false);
    
	public static void main(String[] args) {
	    ProcessScheduler processScheduler = new ProcessScheduler();
	    processScheduler.parse(args);
	    processScheduler.schedule(args);

	    System.exit(0);
	}
	
	public ProcessScheduler() {
	    
	}

	public static String getFileName() {
		return fileName;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		//set event handler for when user presses 'x' button on stage.
		primaryStage.setOnCloseRequest(evt -> {
			// prevent window from closing
			evt.consume();
            System.exit(1);
        });

		Font.loadFont(getClass().getResourceAsStream("/consola.ttf"), 30);
		Font.loadFont(getClass().getResourceAsStream("/consolab.ttf"), 30);

		//Change to home screen
		Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
		primaryStage.setTitle("Process Scheduler");
		primaryStage.setScene(new Scene(root, 1420, 800));
		primaryStage.show();
		primaryStage.setResizable(false);
	}

	public void parse(String[] args) {
        SimpleJSAP jsap = buildParser();
        config = jsap.parse(args);
        if (!config.success()) {
            System.out.println("Usage: java -jar scheduler.jar "  + jsap.getUsage() + "\n");
            System.out.println(jsap.getHelp(JSAP.DEFAULT_SCREENWIDTH, ""));
            System.exit(1);
        }

        numProcessors = config.getInt("P");
        numThreads = config.getInt("N");
	}
	
	public void schedule(String[] args) {
		if (config.getInt("P") == 1){
			//Fast algorithm for scheduling on one processor
			algorithm = new SequentialAlgorithm();
		} else if (config.getInt("N") == 1) {
			// Singlethreaded BNB
            algorithm = new BNBAlgorithm(config.getInt("P"));
        } else {
        	// Multithreaded BNB
            algorithm = new BNBAlgorithmPara(config.getInt("P"), config.getInt("N"));
        }
        scheduler = new Scheduler(algorithm);
        
        algorithm.addListener(this);

		try {
			// attempt to load the input file
			fileName = config.getString("INPUT");
			dot = new DotFile(fileName);
			dot.read(scheduler);

			System.out.println("Starting schedule calculation...");

			// set up graphs if -v flag specified
			if(config.getBoolean("V")) {
				launch(args);
			}

			//Calculate the schedule
			timer.startTimer(0);
			scheduler.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Input Error: File not found");
        } catch (InvalidFileFormatException e) {
			e.printStackTrace();
			System.out.println("Invalid File format: Does not end in \".dot\"");
		}

	}

	public static Scheduler getScheduler(){
		return scheduler;
	}

	public static Algorithm getAlgorithm(){
		return algorithm;
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
    public void algorithmCompleted(PartialSchedule schedule) {
		timer.stopTimer();
		System.out.println("Optimal schedule of " + schedule.getMakespan() + " found in " + timer.getSeconds() + " seconds");
		try {
            dot.write(config.getString("OUTPUT"), schedule);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Output Error: File could not be written to");
        }
    }

	@Override
	public void newOptimalFound(PartialSchedule schedule) {

	}

    public static int getNumProcessors(){
		return numProcessors;
	}

	public static int getNumThreads(){
		return numThreads;
	}
}
