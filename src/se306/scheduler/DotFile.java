package se306.scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se306.scheduler.graph.Node;
import se306.scheduler.logic.Scheduler;

public class DotFile {

    private File file;
    private List<String> lines;

    private static String NAME_REGEX = "\t\\w+\t";
    private static String WEIGHT_REGEX = "=\\d+\\]";
    private static String PARENT_NODE_REGEX = "\\w+\\s*->";
    private static String CHILD_NODE_REGEX = "->\\s*\\w+";
    
    private static String LS = System.lineSeparator();

    /**
     * Converts fileName into file
     *
     * @param fileName name of .dot file
     * @throws FileNotFoundException to be caught in {@link ProcessScheduler}
     *                               and displayed on command line as input error
     */
    DotFile(String fileName) {
        lines = new ArrayList<>();
        this.file = new File(fileName);
    }

    /**
     * Reads lines within the .dot file
     *
     * @throws FileNotFoundException if file entered by user does not exist
     */
    public void read() throws FileNotFoundException {
        Scanner sc = new Scanner(this.file);

        while (sc.hasNextLine()) {
            lines.add(sc.nextLine());
        }
        
        sc.close();

        for (String line : lines) {
            if (!(line.contains("{") && line.contains("}"))) {
                // Only get lines that create node dependencies
                if (line.contains("->")) {
                    addDependency(line);
                } else {
                    addNode(line);
                }
            }
        }
    }

    private void addNode(String s) {
        String name = findName(s);
        int weight = findWeight(s);

        if (name != null) {
            Scheduler.getScheduler().addNode(new Node(name, weight));
        }
    }

    private void addDependency(String s) {
        String parent = regex(regex(s, PARENT_NODE_REGEX), "\\w+");
        String child = regex(regex(s, CHILD_NODE_REGEX), "\\w+");
        int weight = findWeight(s);

        if (parent != null && child != null) {
            Scheduler.getScheduler().addChild(parent, child, weight);
        }
    }

    private String findName(String s) {
        String tempName = regex(s, NAME_REGEX);
        if (tempName != null) {
            return regex(tempName, "\\w+");
        }
        return null;
    }

    private int findWeight(String s) {
        String tempWeight = regex(s, WEIGHT_REGEX);
        if (tempWeight != null) {
            return Integer.parseInt(regex(tempWeight, "\\d+"));
        }
        return -1;
    }

    private String regex(String s, String regex) {
        String out = null;

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            out = matcher.group(0);
        }

        return out;
    }
    
    /**
     * Writes a graph to a file using the DOT format.
     * All tasks are written (along with the values of the schedule we found), then all dependencies.
     * 
     * @param fileName Where the output will be written
     * @param nodes The list of input nodes
     * @throws IOException if the file cannot be written to
     */
    public void write(String fileName, List<Node> nodes) throws IOException {
        String output = "digraph \"outputExample\" {" + LS;

        // write all tasks
        for (Node node: nodes) {
            output += String.format("\t%s\t[Weight=%d,Start=%d,Processor=%d];" + LS, node.getName(), node.getWeight(),
                    node.getStartTime(), node.getProcessor());
        }
        
        // write all dependencies
        for (Node node: nodes) {
            Map<Node, Integer> children = node.getChildren();
            for (Node child: children.keySet()) {
                output += String.format("\t%s -> %s\t[Weight=%d];" + LS, node.getName(), child.getName(),
                        children.get(child));
            }
        }
        
        output += "}" + LS;
        
        OutputStream fos = new FileOutputStream(new File(fileName));
        fos.write(output.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

}
