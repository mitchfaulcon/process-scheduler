import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        try {
            DotFile dot = new DotFile(args[0]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Input Error: File not found");
        }
    }
}
