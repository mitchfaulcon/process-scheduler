package se306.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se306.scheduler.exception.InvalidFileFormatException;
import se306.scheduler.graph.Node;
import se306.scheduler.logic.Scheduler;
import se306.scheduler.logic.SequentialAlgorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests various methods within {@link DotFile}
 */
public class DotFileTest {

    private Scheduler scheduler;

    private DotFile dot;
    
    @BeforeEach
    void graphSetup() {
        scheduler = new Scheduler(new SequentialAlgorithm());
    }

    @AfterEach
    void graphReset(){
        scheduler.clearGraph();
    }
    
    /**
     * Test that a graph can be successfully loaded from a file
     */
    @Test
    void testLoadGraph() {
        try {
            dot = new DotFile("test_data/test1.dot");
            dot.read(scheduler);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail("File not found");
        } catch (InvalidFileFormatException e) {
            e.printStackTrace();
            fail("Invalid file format");
        }

        List<Node> nodes = scheduler.getNodes();
        Node nodeA = nodes.get(0);
        Node nodeB = nodes.get(1);
        Node nodeC = nodes.get(2);
        Node nodeD = nodes.get(3);
        
        assertEquals(2, nodeA.getChildren().size(), "Node A should have 2 children");
        assertTrue(nodeA.getChildCosts().containsKey("b") && nodeA.getChildCosts().containsKey("c"),
                "Node A should have Nodes B & C as its children");
        assertEquals(1, nodeB.getChildren().size(), "Node B should have 1 child");
        assertTrue(nodeB.getChildCosts().containsKey("d"), "Node B should have Node D as its child");
        assertEquals(1, nodeC.getChildren().size(), "Node C should have 1 child");
        assertTrue(nodeC.getChildCosts().containsKey("d"), "Node C should have Node D as its child");
        assertEquals(0, nodeD.getChildren().size(), "Node D should have 0 children");
    }
    
    /**
     * Test that a graph can be successfully saved to a file
     * 
     * TODO: don't use hashmap because we don't know the key ordering
     */
    @Test
    void testWriteGraph() {
        // load a graph
        try {
            dot = new DotFile("test_data/test1.dot");
            dot.read(scheduler);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail("File not found");
        } catch (InvalidFileFormatException e) {
            e.printStackTrace();
            fail("Invalid file format");
        }

        // find a valid schedule (which is saved in the nodes)
        scheduler.start();
        
        // write the nodes + schedule to a file
        String outFile = "test_data/test1_out.dot";
        String outFileValid = "test_data/test1_out_valid.dot";
        try {
            dot.write(outFile, scheduler.getNodes());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Could not save to test_data/test1_out.dot");
        }

        // check that the output we produced is the same as the known correct output (test1_out_valid.dot)
        // https://stackoverflow.com/a/3403112
        try {
            Scanner outScanner = new Scanner(new File(outFile));
            Scanner outScannerValid = new Scanner(new File(outFileValid));
            
            String outContent = outScanner.useDelimiter("\\Z").next();
            String outContentValid = outScannerValid.useDelimiter("\\Z").next();
            assertEquals(outContentValid, outContent);

            outScanner.close();
            outScannerValid.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fail("Shouldn't happen");
        }
    }

    @Test
    void invalidFileFormat() {
        try {
            dot = new DotFile("invalid");
            fail("Should have thrown InvalidFileFormatException");
        } catch (InvalidFileFormatException ignored) {

        }
    }
}
