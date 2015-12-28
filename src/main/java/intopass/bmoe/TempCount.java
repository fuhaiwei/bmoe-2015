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
                .filter(f -> filter_date(f, "12-29"))
                .map(f -> Stream.of(f.listFiles()).collect(toList()))
                .reduce(reduce_files())
                .ifPresent((files) -> {
                    System.out.println("投票数据");
                    print_files(files, "01", s -> s.contains("Saber"));
                    print_files(files, "01", s -> s.contains("牧濑红莉栖"));
                    print_files(files, "01", s -> s.contains("小智"));
                    print_files(files, "01", s -> s.contains("路飞"));

                    System.out.println("票差数据");
                    print_files(files, "02", s -> s.contains("Saber") || s.contains("牧濑红莉栖"));
                    print_files(files, "02", s -> s.contains("小智") || s.contains("路飞"));
                });
    }

    public static Predicate<String> filter_time() {
        return t -> {
//            if (t.compareTo("02:00") > 0 && t.compareTo("6:00") < 0) {
//                return false;
//            }
//            if (t.endsWith(":30")) {
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
                    int curr = parseInt(time.substring(0, 2)) * 60 + parseInt(time.substring(3));
                    if (curr - prev.getAndSet(curr) > 60) {
                        System.out.println("......");
                    }
                    collect.get(time).forEach(file -> {
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
                    });
                });
        System.out.println();
    }

    private static String format(String type, String line) {
        if (type.equals("01")) {
            return line.substring(3);
        } else if (type.equals("02")) {
            return line.substring(11);
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