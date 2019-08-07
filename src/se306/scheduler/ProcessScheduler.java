package se306.scheduler;

import com.martiansoftware.jsap.*;
import se306.scheduler.exception.InvalidFileFormatException;
import se306.scheduler.logic.Scheduler;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ProcessScheduler {

	public static void main(String[] args) {
		System.err.close();  // Workaround to stop help being printed twice
		SimpleJSAP jsap = buildParser();
		JSAPResult config = jsap.parse(args);
		if (!config.success()) {
            System.out.println("Usage: java -jar scheduler.jar "  + jsap.getUsage() + "\n");
            System.out.println(jsap.getHelp(JSAP.DEFAULT_SCREENWIDTH, ""));
            System.exit(1);
        }
		
		// Call methods with these values
//		System.out.println("Input file: " + config.getString("INPUT"));
//		System.out.println("N Processors: " + config.getInt("P"));
//		System.out.println("Cores to use: " + config.getInt("N"));
//		System.out.println("Visualise: " + config.getBoolean("V"));
//		System.out.println("Output file: " + config.getString("OUTPUT", config.getString("INPUT") + "-output.dot"));
		
		try {
			//Read the input and display the graph generated
			DotFile dot = new DotFile(config.getString("INPUT"));
			dot.read();

			//Calculate the schedule, write it to a file, and display the schedule graph
			Scheduler.getScheduler().schedule();
			dot.write(config.getString("OUTPUT"), Scheduler.getScheduler().getNodes());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Input Error: File not found");
        } catch (IOException e){
			e.printStackTrace();
			System.out.println("Output Error: File could not be written to");
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

}
