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
    void testNodeCreation() {
        Node nodeA = new Node("a", 2);

        assertEquals("a", nodeA.getName(), "Node name should be 'a'");
        assertEquals(2, nodeA.getWeight(), "Node weight should be 2");
        assertEquals(0, nodeA.getIncomingEdges().size(), "Node should have no parents");
    }

    /**
     * Test case to check that a node can be correctly added to another node as its child
     */
    @Test
    void testNodeWithChild() {
        Node nodeA = new Node("a", 2);
        Node nodeB = new Node("b", 3);
        nodeB.addParent(nodeA, 1);

        assertEquals(0, nodeA.getIncomingEdges().size(), "Node A should have 0 parents");
        assertEquals(1, nodeB.getIncomingEdges().size(), "Node B should have 1 parent");
        assertTrue(nodeB.getIncomingEdges().containsKey(nodeA), "Node B should have Node A as its parent");
        assertEquals(new Integer(1), nodeB.getIncomingEdges().get(nodeA),
                "The edge weight between Node A and Node B should be 1");
    }
}
