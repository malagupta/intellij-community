// "Move 'return' to computation of the value of 's'" "true"
import java.io.*;

class T {
    private static String getString() throws IOException {
        String s;
        try (BufferedReader reader = open()) {
            while (true) {
                s = reader.readLine();
                if (s == null || s.startsWith("$")) {
                    return s;
                }
            }
        }
    }
    private static BufferedReader open() throws FileNotFoundException {
        return null;
    }
}
