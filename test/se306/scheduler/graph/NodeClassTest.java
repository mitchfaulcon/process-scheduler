package se306.scheduler.graph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests various methods within {@link Node}
 */
class NodeClassTest {

    /**
     * This test case checks that a node can be created successfully with the intended fields
     */
    @Test
    void testNodeCreation(){
        Node nodeA = new Node("a", 2);

        assertEquals("a", nodeA.getName(), "Node name should be 'a'");
        assertEquals(2, nodeA.getWeight(), "Node weight should be 2");
        assertEquals(0, nodeA.getParents().size(), "Node should have no parents");
    }

    /**
     * Test case to check that a node can be correctly added to another node as its child
     */
    @Test
    void testNodeWithChild(){
        Node nodeA = new Node("a", 2);
        Node nodeB = new Node("b", 3);
        nodeB.addParent("a", 1);

        assertEquals(0, nodeA.getParents().size(), "Node A should have 0 parents");
        assertEquals(1, nodeB.getParents().size(), "Node B should have 1 parent");
        assertTrue(nodeB.getParents().containsKey("a"), "Node B should have Node A as its parent");
        assertEquals(new Integer(1), nodeB.getParents().get("a"),
                "The edge weight between Node A and Node B should be 1");
    }

    /**
     * Test case to check if node start time is set correctly
     */
    @Test
    void testNodeStartTime(){
        Node nodeA = new Node("a", 2);
        nodeA.setStartTime(8);

        assertEquals(8, nodeA.getStartTime(), "Node A should have a start time of 8");
    }
}
