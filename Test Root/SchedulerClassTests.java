import Graph.Node;
import Logic.Scheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

/**
 * This class tests various methods within the Logic.Scheduler class
 */
class SchedulerClassTests {

    private Scheduler scheduler = Scheduler.getScheduler();

    private Node nodeA = new Node("a", 2);
    private Node nodeB = new Node("b", 3);
    private Node nodeC = new Node("c", 1);
    private Node nodeD = new Node("d", 2);
    private Node nodeE = new Node("e", 1);

    /**
     * Initialise graph structure before each test
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
        scheduler.addNode(nodeA);
        scheduler.addNode(nodeB);
        scheduler.addNode(nodeC);
        scheduler.addNode(nodeD);
        scheduler.addNode(nodeE);

        //TODO The setup will run differently since nodes will be firstly identified as Strings.
//        scheduler.addChild(nodeA, nodeC, 1);
//        scheduler.addChild(nodeA, nodeD, 1);
//        scheduler.addChild(nodeB, nodeD, 1);
//        scheduler.addChild(nodeC, nodeE, 1);
//        scheduler.addChild(nodeD, nodeE, 1);
    }

    /**
     * Test if a child node can be successfully added to another node through the Scheduler class
     */
    @Test
    void testChildAdded(){

    }

    @Test
    void testFirstMilestoneSchedule(){
        ArrayList<Node> graph = scheduler.schedule();

        assertNotEquals(nodeA.getStartTime(), nodeB.getStartTime());
        assertTrue(nodeA.getStartTime() < nodeC.getStartTime());
        assertTrue(nodeA.getStartTime() < nodeD.getStartTime());

        assertNotEquals(nodeC.getStartTime(), nodeD.getStartTime());


    }
}
