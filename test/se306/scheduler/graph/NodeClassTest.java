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
        assertEquals(0, nodeA.getChildren().size(), "Node should have no children");
    }

    /**
     * Test case to check that a node can be correctly added to another node as its child
     */
    @Test
    void testNodeWithChild(){
        Node nodeA = new Node("a", 2);
        Node nodeB = new Node("b", 3);
        nodeA.addChild(nodeB, 1);

        assertEquals(1, nodeA.getChildren().size(), "Node A should have 1 child");
        assertEquals(0, nodeA.getParents().size(), "Node A should have 0 parents");
        assertTrue(nodeA.getChildren().containsKey(nodeB), "Node A should have Node B as its child");
        assertEquals(new Integer(1), nodeA.getChildren().get(nodeB),
                "The edge weight between Node A and Node B should be 1");
        assertEquals(0, nodeB.getChildren().size(), "Node B should have 0 children");
        assertEquals(1, nodeB.getParents().size(), "Node B should have 1 parent");
        assertTrue(nodeB.getParents().contains(nodeA), "Node B should have Node A as its parent");
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
