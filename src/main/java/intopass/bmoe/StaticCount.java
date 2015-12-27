package intopass.bmoe;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Created by fuhaiwei on 15/12/27.
 */
public class StaticCount {
    public static void main(String[] args) {
        File path = new File("cached/json_text2");
        AtomicInteger prev = new AtomicInteger(0);
        Stream.of(path.listFiles())
                .filter(f -> f.getName().endsWith(".txt"))
                .forEach(f -> {
                    int hashcode = read_file(f);
                    if (prev.getAndSet(hashcode) != hashcode) {
                        System.out.println(f);
                    }
                });
    }

    private static int read_file(File f) {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            return br.lines().mapToInt(line -> line.hashCode()).sum();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }
}
