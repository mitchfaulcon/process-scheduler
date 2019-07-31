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
            if (!line.contains("{") || line.contains("}")) {
                addNode(line);
            }
        }
    }

    private void addNode(String s) {
        String name = findName(s);
        int weight = findWeight(s);

        if (name != null) {
            //TODO add nodes to list of existing nodes
        }

        System.out.println(name + weight); //TODO Find node dependencies and weights
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
