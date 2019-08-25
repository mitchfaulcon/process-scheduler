package se306.scheduler.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import se306.scheduler.DotFile;
import se306.scheduler.exception.InvalidFileFormatException;
import se306.scheduler.graph.PartialSchedule;

public class BNBAlgorithmParaTest {
	
	@Test
	void testBNBAlgorithmPara() {
		DotFile dot = null;
		try {
			dot = new DotFile("test_data/Nodes_7_OutTree.dot");
		} catch (InvalidFileFormatException e) {
			fail("Impossible.");
			e.printStackTrace();
		}
		
		Algorithm algorithm = new BNBAlgorithmPara(3, 4); // 3 processors, using 4 threads
		Scheduler scheduler = new Scheduler(algorithm);
		try {
			dot.read(scheduler);
		} catch (FileNotFoundException e) {
			fail("Could not find input file.");
			e.printStackTrace();
		}
		
		CompletableFuture<Integer> makespan = new CompletableFuture<>();
		algorithm.addListener(new AlgorithmListener() {

			@Override
			public void algorithmCompleted(PartialSchedule schedule) {
				makespan.complete(schedule.getMakespan());
			}

			@Override
			public void newOptimalFound(PartialSchedule schedule) {
				
			}
			
		});
        scheduler.start();
        
        int makespanResult = 0;
        try {
			makespanResult = makespan.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
        
        // since this algorithm uses multiple threads, it can get different output schedule results depending
        // on the thread execution order. so we can only make sure the resultant schedule has the correct makespan
        assertEquals(27, makespanResult);
	}
}
