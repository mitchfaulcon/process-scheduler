package se306.scheduler.logic;

import se306.scheduler.graph.Node;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
        /*assertEquals(0, nodeA.getIncomingEdges().size(), "Node A should have 0 parents");
        assertEquals(0, nodeB.getIncomingEdges().size(), "Node B should have 0 parents");
        assertEquals(1, nodeC.getIncomingEdges().size(), "Node C should have 1 parent");
        assertTrue(nodeC.getIncomingEdges().contains(Node.IncomingEdge(nodeA, 1)), "Node C should have Node A as its parent");
        assertEquals(2, nodeD.getIncomingEdges().size(), "Node D should have 2 parents");
        assertTrue(nodeD.getIncomingEdges().contains(new Node.IncomingEdge(nodeA, 1)), "Node D should have Node A as its parent");
        assertTrue(nodeD.getIncomingEdges().containsKey("b"), "Node D should have Node B as its parent");
        assertEquals(2, nodeE.getIncomingEdges().size(), "Node E should have 2 parents");
        assertTrue(nodeE.getIncomingEdges().containsKey("c"), "Node E should have Node C as its parent");
        assertTrue(nodeE.getIncomingEdges().containsKey("d"), "Node E should have Node D as its parent");*/
    }

    /**
     * Tests that the schedule method produces a valid output
     */
    /*@Test
    void testFirstMilestoneSchedule(){
        assertNotEquals(nodeA.getStartTime(), nodeB.getStartTime(),
                "Nodes A & B should have different start times");
        assertTrue(nodeA.getStartTime() < nodeC.getStartTime(),
                "Node A should be scheduled before Node C");
        assertTrue(nodeA.getStartTime() < nodeD.getStartTime(),
                "Node A should be scheduled before Node D");

        assertNotEquals(nodeC.getStartTime(), nodeD.getStartTime());
        assertTrue(nodeB.getStartTime() < nodeD.getStartTime(),
                "Node B should be scheduled before Node D");

        assertTrue(nodeC.getStartTime() < nodeE.getStartTime(),
                "Node C should be scheduled before Node E");
        assertTrue(nodeD.getStartTime() < nodeE.getStartTime(),
                "Node D should be scheduled before Node E");
    }
*/
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
