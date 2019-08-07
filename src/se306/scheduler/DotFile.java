package se306.scheduler;

import se306.scheduler.exception.InvalidFileFormatException;
import se306.scheduler.graph.Node;
import se306.scheduler.logic.Scheduler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DotFile {

    private File file;
    private String fileName;
    private List<String> lines;
    private List<LineRecord> lineRecords = new ArrayList<>();

    private static String NAME_REGEX = "\t\\w+\t";
    private static String WEIGHT_REGEX = "=\\d+\\]";
    private static String PARENT_NODE_REGEX = "\\w+\\s*->";
    private static String CHILD_NODE_REGEX = "->\\s*\\w+";
    private static String LS = System.lineSeparator();
    
    /**
     * Struct to represent a line in a DOT file, so we can keep track of their order
     */
    public class LineRecord {
        public boolean isDependency; // each record will be either a dependency or a task
        public String taskName1;
        public String taskName2; // will be empty if line is a task
        
        public LineRecord(boolean isDependency, String taskName1) {
            this(isDependency, taskName1, "");
        }
        
        public LineRecord(boolean isDependency, String taskName1, String taskName2) {
            this.isDependency = isDependency;
            this.taskName1 = taskName1;
            this.taskName2 = taskName2;
        }
    }

    public DotFile(String fileName) throws InvalidFileFormatException {
        if (fileName.endsWith(".dot")) {
            lines = new ArrayList<>();
            this.fileName = fileName;
            this.file = new File(fileName);
        } else {
            throw new InvalidFileFormatException();
        }
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

        // Iterate through each line and add nodes or dependencies
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

    /**
     * Add a new node to the graph
     *
     * @param s line read from .dot file
     */
    private void addNode(String s) {
        String name = findName(s);
        int weight = findWeight(s);

        if (name != null) {
            Scheduler.getScheduler().addNode(new Node(name, weight));
            lineRecords.add(new LineRecord(false, name));
        }
    }


    /**
     * Adds a new dependency to the graph
     *
     * @param s line read from .dot file
     */
    private void addDependency(String s) {
        String parent = regex(regex(s, PARENT_NODE_REGEX), "\\w+");
        String child = regex(regex(s, CHILD_NODE_REGEX), "\\w+");
        int weight = findWeight(s);

        if (parent != null && child != null) {
            Scheduler.getScheduler().addChild(parent, child, weight);
            lineRecords.add(new LineRecord(true, parent, child));
        }
    }

    /**
     * Extracts the name of the node from the input string
     *
     * @param s line read from .dot file
     * @return the name of the node
     */
    private String findName(String s) {
        String tempName = regex(s, NAME_REGEX);
        if (tempName != null) {
            return regex(tempName, "\\w+");
        }
        return null;
    }

    /**
     * Extract the weight of the node or dependency from
     * the input string
     *
     * @param s line read from .dot file
     * @return the weight of the node or dependency
     */
    private int findWeight(String s) {
        String tempWeight = regex(s, WEIGHT_REGEX);
        if (tempWeight != null) {
            return Integer.parseInt(regex(tempWeight, "\\d+"));
        }
        return -1;
    }

    /**
     * Find the first matching regex pattern within a string
     *
     * @param s input string to apply regex to
     * @param regex regex pattern to apply
     * @return the first match to input string
     */
    private String regex(String s, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return matcher.group(0);
        }

        return null;
    }
    
    /**
     * Writes a graph to a file using the DOT format.
     * All tasks are written (along with the values of the schedule we found), then all dependencies.
     * 
     * @param fileName Where the output will be written
     * @param nodes The list of nodes assigned to a schedule
     * @throws IOException if the file cannot be written to
     */
    public void write(String fileName, List<Node> nodes) throws IOException {
        //Create correct name for top of dot file
        // TODO use graph name read from file instead of filename
        String dotFileName = file.getName();
        String extensionRemoved;
        String capitalised = dotFileName;
        if (dotFileName.length()>=4) {
            extensionRemoved = dotFileName.substring(0, dotFileName.length() - 4);
            capitalised = extensionRemoved.substring(0, 1).toUpperCase() + extensionRemoved.substring(1);
        }

        StringBuilder output = new StringBuilder("digraph \"output" + capitalised + "\" {" + LS);

        // a map of task name to output string, which we will later access in the order specified by `lineRecords`
        Map<String, String> taskStrings = new HashMap<String, String>();
        // generate all task strings
        for (Node node: nodes) {
            taskStrings.put(node.getName(), String.format("\t%s\t[Weight=%d,Start=%d,Processor=%d];" + LS, node.getName(), node.getWeight(),
                    node.getStartTime(), node.getProcessor()));
        }

        // a map of both tasks in a dependency to output string, which we will later access in the order specified by `lineRecords`
        // the dependency task names are joined by a space
        Map<String, String> dependencyStrings = new HashMap<String, String>();
        // generate all dependency strings
        for (Node node: nodes) {
            Map<Node, Integer> children = node.getChildren();
            for (Node child: children.keySet()) {
                String dependencyKey = node.getName() + " " + child.getName();
                dependencyStrings.put(dependencyKey, String.format("\t%s -> %s\t[Weight=%d];" + LS, node.getName(), child.getName(),
                        children.get(child)));
            }
        }
        
        // write the tasks and dependencies in the order specified by `lineRecords`
        for (LineRecord record: lineRecords) {
            if (record.isDependency) {
                output.append(dependencyStrings.get(record.taskName1 + " " + record.taskName2));
            } else {
                output.append(taskStrings.get(record.taskName1));
            }
        }
        
        output.append("}").append(LS);

        //Set output filename to default if none was entered
        if (fileName == null){
            fileName = this.fileName.substring(0,this.fileName.length()-4) + "-output.dot";
        }
        OutputStream fos = new FileOutputStream(new File(fileName));
        fos.write(output.toString().getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

}
