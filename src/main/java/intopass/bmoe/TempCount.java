package intopass.bmoe;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.*;

/**
 * Created by fuhaiwei on 15/12/20.
 */
public class TempCount {
    public static void main(String[] args) {
        Stream.of(new File("output/benzhan/").listFiles())
                .filter(f -> f.isDirectory())
                .filter(f -> filter_date(f, "01-03"))
                .map(f -> Stream.of(f.listFiles()).collect(toList()))
                .reduce(reduce_files())
                .ifPresent((files) -> {
                    System.out.println("投票数据");
                    print_files(files, "01", s -> s.contains("Saber"));
                    print_files(files, "01", s -> s.contains("远坂凛"));
                    print_files(files, "01", s -> s.contains("杀老师"));
                    print_files(files, "01", s -> s.contains("鲁路修"));

                    System.out.println("票差数据");
                    print_files(files, "02", s -> s.contains("Saber") || s.contains("远坂凛"));
                    print_files(files, "02", s -> s.contains("杀老师") || s.contains("鲁路修"));
                });
    }

    public static Predicate<String> filter_time() {
        return t -> {
//            if (t.compareTo("01:00") > 0 && t.compareTo("07:00") < 0) {
//                return false;
//            }
//            if (!t.equals("00:30") && t.endsWith(":30")) {
//                return false;
//            }
            return true;
        };
    }

    private static boolean filter_date(File f, String... dates) {
        return dates.length == 0 || Arrays.asList(dates).contains(f.getName());
    }

    private static void print_files(List<File> files, String type, Predicate<String> predicate) {
        Map<String, List<File>> collect = files.stream()
                .filter(f -> f.getName().startsWith(type))
                .collect(groupingBy(f -> f.getName().substring(10, 15)));
        AtomicInteger prev = new AtomicInteger(0);
        collect.keySet()
                .stream()
                .filter(filter_time())
                .sorted()
                .forEach(time -> {
                    check_skip(prev, time);
                    collect.get(time).forEach(file -> print_file(file, type, time, predicate));
                });
        System.out.println();
    }

    private static void check_skip(AtomicInteger prev, String time) {
        int curr = parseInt(time.substring(0, 2)) * 60 + parseInt(time.substring(3));
        if (curr - prev.getAndSet(curr) > 60) {
            System.out.println("......");
        }
    }

    private static void print_file(File file, String type, String time, Predicate<String> predicate) {
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (predicate.test(line)) {
                    System.out.println(time + " " + format(type, line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String format(String type, String line) {
        if (type.equals("01")) {
            return line.substring(3);
        } else if (type.equals("02")) {
            return line.substring(line.indexOf(':') + 2);
        } else {
            return line;
        }
    }

    private static BinaryOperator<List<File>> reduce_files() {
        return (files, files2) -> {
            files.addAll(files2);
            return files;
        };
    }

}