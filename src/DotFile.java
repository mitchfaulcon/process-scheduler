import Graph.Node;
import Logic.Scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DotFile {

    private File file;
    private List<String> lines;

    private static String NAME_REGEX = "[^>]\\w\\s+\\[";
    private static String WEIGHT_REGEX = "=\\d+\\]";
    private static String PARENT_NODE_REGEX = "\\w+−>";
    private static String CHILD_NODE_REGEX = "−>\\w+";

    /**
     * Converts fileName into file
     *
     * @param fileName name of .dot file
     * @throws FileNotFoundException to be caught in {@link Main}
     *                               and displayed on command line as input error
     */
    DotFile(String fileName) throws FileNotFoundException {
        lines = new ArrayList<>();
        this.file = new File(fileName);
        read();
    }

    /**
     * Reads lines within the .dot file
     *
     * @throws FileNotFoundException if file entered by user does not exist
     */
    private void read() throws FileNotFoundException {
        Scanner sc = new Scanner(this.file);

        while (sc.hasNextLine()) {
            lines.add(sc.nextLine());
        }

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
        String parentString = regex(regex(s, PARENT_NODE_REGEX), "\\w+");
        String childString = regex(regex(s, CHILD_NODE_REGEX), "\\w+");
        int weight = findWeight(s);

        if (parentString != null && childString != null) {
            //Scheduler.getScheduler().addChild(new Node());
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

}
