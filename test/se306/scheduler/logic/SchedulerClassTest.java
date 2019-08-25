package se306.scheduler.logic;

import se306.scheduler.graph.Node;
import se306.scheduler.graph.PartialSchedule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This class tests various methods within {@link Scheduler}
 */
class SchedulerClassTest {

    private Scheduler scheduler;

    private Node nodeA = new Node("a", 2);
    private Node nodeB = new Node("b", 3);
    private Node nodeC = new Node("c", 1);
    private Node nodeD = new Node("d", 2);
    private Node nodeE = new Node("e", 1);
    
    private CompletableFuture<PartialSchedule> outputSchedule;
    
    /**
     * Initialise graph structure before each test case
     *
     *        A(2)  B(3)
     *        ↓   ↘  ↓
     *        C(1)  D(2)
     *          ↘   ↙
     *           E(1)
     *
     * (With each edge having weight 1)
     */
    @BeforeEach
    void graphSetup(){
        Algorithm algorithm = new SequentialAlgorithm();
        scheduler = new Scheduler(algorithm);
        
        outputSchedule = new CompletableFuture<PartialSchedule>();
        algorithm.addListener(new AlgorithmListener() {

			@Override
			public void algorithmCompleted(PartialSchedule schedule) {
				outputSchedule.complete(schedule);
			}

			@Override
			public void newOptimalFound(PartialSchedule schedule) {
				
			}
        	
        });
        
        scheduler.addNode(nodeA);
        scheduler.addNode(nodeB);
        scheduler.addNode(nodeC);
        scheduler.addNode(nodeD);
        scheduler.addNode(nodeE);

        scheduler.addChild(nodeA.getName(), nodeC.getName(), 1);
        scheduler.addChild(nodeA.getName(), nodeD.getName(), 1);
        scheduler.addChild(nodeB.getName(), nodeD.getName(), 1);
        scheduler.addChild(nodeC.getName(), nodeE.getName(), 1);
        scheduler.addChild(nodeD.getName(), nodeE.getName(), 1);

        scheduler.start();
    }

    /**
     * Reset the graph after each test case so that it can be repopulated in the {@link #graphSetup()} method
     */
    @AfterEach
    void graphReset(){
        scheduler.clearGraph();
    }

    /**
     * Test if a child node can be successfully added to another node through the Scheduler class
     */
    @Test
    void testChildAdded(){
        assertEquals(0, nodeA.getIncomingEdges().size(), "Node A should have 0 parents");
        assertEquals(0, nodeB.getIncomingEdges().size(), "Node B should have 0 parents");
        assertEquals(1, nodeC.getIncomingEdges().size(), "Node C should have 1 parent");
        assertTrue(nodeC.getIncomingEdges().containsKey(nodeA), "Node C should have Node A as its parent");
        assertEquals(2, nodeD.getIncomingEdges().size(), "Node D should have 2 parents");
        assertTrue(nodeD.getIncomingEdges().containsKey(nodeA), "Node D should have Node A as its parent");
        assertTrue(nodeD.getIncomingEdges().containsKey(nodeB), "Node D should have Node B as its parent");
        assertEquals(2, nodeE.getIncomingEdges().size(), "Node E should have 2 parents");
        assertTrue(nodeE.getIncomingEdges().containsKey(nodeC), "Node E should have Node C as its parent");
        assertTrue(nodeE.getIncomingEdges().containsKey(nodeD), "Node E should have Node D as its parent");
    }

    /**
     * Tests that the schedule method produces a valid output
     */
    @Test
    void testFirstMilestoneSchedule(){
    	PartialSchedule schedule = null;
		try {
			schedule = outputSchedule.get();
		} catch (InterruptedException | ExecutionException e) {
			fail(":(");
			e.printStackTrace();
		}
        assertNotEquals(schedule.getStartTime(nodeA), schedule.getStartTime(nodeB),
                "Nodes A & B should have different start times");
        assertTrue(schedule.getStartTime(nodeA) < schedule.getStartTime(nodeC),
                "Node A should be scheduled before Node C");
        assertTrue(schedule.getStartTime(nodeA) < schedule.getStartTime(nodeD),
                "Node A should be scheduled before Node D");

        assertNotEquals(schedule.getStartTime(nodeC), schedule.getStartTime(nodeD));
        assertTrue(schedule.getStartTime(nodeB) < schedule.getStartTime(nodeD),
                "Node B should be scheduled before Node D");

        assertTrue(schedule.getStartTime(nodeC) < schedule.getStartTime(nodeE),
                "Node C should be scheduled before Node E");
        assertTrue(schedule.getStartTime(nodeD) < schedule.getStartTime(nodeE),
                "Node D should be scheduled before Node E");
    }

    @Test
    void testInvalidChild(){
        try {
            scheduler.addChild("a", "z", 5);
            fail("NullPointerException should have been thrown");
        }
        catch (NullPointerException ignored){

        }
    }
}
