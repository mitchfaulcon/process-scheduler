import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DotFile {

    private File file;
    private List<String> lines;

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
        open();
    }

    private void open() throws FileNotFoundException {
        Scanner sc = new Scanner(this.file);

        while (sc.hasNextLine()) {
            lines.add(sc.nextLine());
        }

    }

}
