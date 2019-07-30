package se306.scheduler.graph;

import org.junit.jupiter.api.Test;
import se306.scheduler.graph.Node;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests various methods within the Graph.Node class
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

        assertEquals(1, nodeA.getChildren().size());
        assertEquals(0, nodeA.getParents().size());
        assertTrue(nodeA.getChildren().containsKey(nodeB));
        assertEquals(1, (int)nodeA.getChildren().get(nodeB));
        assertEquals(0, nodeB.getChildren().size());
        assertEquals(1, nodeB.getParents().size());
        assertTrue(nodeB.getParents().contains(nodeA));
    }

    /**
     * Test case to check if node start time is set correctly
     */
    @Test
    void testNodeStartTime(){
        Node nodeA = new Node("a", 2);
        nodeA.setStartTime(8);

        assertEquals(8, nodeA.getStartTime());
    }
}
