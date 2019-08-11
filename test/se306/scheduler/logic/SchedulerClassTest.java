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
        assertEquals(2, nodeA.getChildren().size(), "Node A should have 2 children");
        assertTrue(nodeA.getChildCosts().containsKey("c") && nodeA.getChildCosts().containsKey("d"),
                "Node A should have Nodes C & D as its children");
        assertEquals(1, nodeB.getChildren().size(), "Node B should have 1 child");
        assertTrue(nodeB.getChildCosts().containsKey("d"), "Node B should have Node D as its child");
        assertEquals(1, nodeC.getChildren().size(), "Node C should have 1 child");
        assertTrue(nodeC.getChildCosts().containsKey("e"), "Node C should have Node E as its child");
        assertEquals(1, nodeD.getChildren().size(), "Node D should have 1 child");
        assertTrue(nodeD.getChildCosts().containsKey("e"), "Node D should have Node E as its child");
        assertEquals(0, nodeE.getChildren().size(), "Node E should have 0 children");
    }

    /**
     * Tests that the schedule method produces a valid output
     */
    @Test
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
