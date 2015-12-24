package intopass.bmoe;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Created by fuhaiwei on 15/12/20.
 */
public class TempCount {
    public static void main(String[] args) {
        Stream.of(new File("output/benzhan/").listFiles())
                .filter(f -> f.isDirectory())
                .filter(f -> filter_date(f, "12-25"))
                .map(f -> Stream.of(f.listFiles()).collect(toList()))
                .reduce(reduce_files())
                .ifPresent((files) -> {
                    System.out.println("投票数据");
                    print_files(files, "01", s -> s.contains("远坂凛"));
//                    print_files(files, "01", s -> s.contains("佐仓千代"));
//                    print_files(files, "01", s -> s.contains("Archer"));
//                    print_files(files, "01", s -> s.contains("坂田银时"));

                    System.out.println("票差数据");
                    print_files(files, "02", s -> s.contains("亚丝娜") || s.contains("时崎狂三"));
                    print_files(files, "02", s -> s.contains("远坂凛") || s.contains("土间埋"));
                    print_files(files, "02", s -> s.contains("牧濑红莉栖") || s.contains("白"));
                    print_files(files, "02", s -> s.contains("加藤惠") || s.contains("友利奈绪"));
                    print_files(files, "02", s -> s.contains("卫宫切嗣") || s.contains("杀老师"));
                    print_files(files, "02", s -> s.contains("路飞") || s.contains("Lancer"));
                    print_files(files, "02", s -> s.contains("鲁路修") || s.contains("土间太平"));
                    print_files(files, "02", s -> s.contains("利威尔") || s.contains("音无结弦"));
                });
    }

    private static boolean filter_date(File f, String... dates) {
        return dates.length == 0 || Arrays.asList(dates).contains(f.getName());
    }

    private static void print_files(List<File> files, String type, Predicate<String> predicate) {
        Map<String, List<File>> collect = files.stream()
                .filter(f -> f.getName().startsWith(type))
                .collect(groupingBy(f -> f.getName().substring(10, 15)));
        collect.keySet()
                .stream()
                .sorted()
                .forEach(time -> collect.get(time).forEach(file -> {
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
                }));
        System.out.println();
        System.out.println();
    }

    private static String format(String type, String line) {
        if (type.equals("01")) {
            return line.substring(3);
        } else if (type.equals("02")) {
            return line.substring(8);
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