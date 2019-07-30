package se306.scheduler.logic;

import se306.scheduler.graph.Node;
import se306.scheduler.logic.Scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * This class tests various methods within the Logic.Scheduler class
 */
class SchedulerClassTest {

    private Scheduler scheduler = Scheduler.getScheduler();

    /**
     * Initialise graph structure before each test
     *
     *        A   B
     *        ↓ ↘ ↓
     *        C   D
     *         ↘ ↙
     *          E
     *
     * (With each edge having weight 1)
     */
    @BeforeEach
    void graphSetup(){
        Node nodeA = new Node("a", 2);
        Node nodeB = new Node("b", 3);
        Node nodeC = new Node("c", 1);
        Node nodeD = new Node("d", 2);
        Node nodeE = new Node("e", 1);

        scheduler.addNode(nodeA);
        scheduler.addNode(nodeB);
        scheduler.addNode(nodeC);
        scheduler.addNode(nodeD);
        scheduler.addNode(nodeE);

        scheduler.addChild(nodeA, nodeC, 1);
        scheduler.addChild(nodeA, nodeD, 1);
        scheduler.addChild(nodeB, nodeD, 1);
        scheduler.addChild(nodeC, nodeE, 1);
        scheduler.addChild(nodeD, nodeE, 1);
    }

    /**
     * Test if a child node can be successfully added to another node through the Scheduler class
     */
    @Test
    void testChildAdded(){

    }

    @Test
    void testSchedule(){
        ArrayList<Node> graph = scheduler.schedule();

        for (Node n: graph) {
            System.out.println(n.getStartTime());
        }
    }
}
