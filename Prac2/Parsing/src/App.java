import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        // ask for file name
        System.out.print("Enter file name: ");
        try (Scanner scanner = new Scanner(System.in)) {
            String fileName = scanner.nextLine();
            Parser parser = new Parser(fileName);
            parser.parse();
        }
    }
}
