package intopass.bmoe;

import intopass.bmoe.spdier.Person;
import intopass.bmoe.spdier.Spider;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by fuhaiwei on 15/12/27.
 */
public class StaticCount {

    public static void main(String[] args) {
        String date = "2015-12-28";
        File path = new File("cached/json_text2/" + date);
        List<Person> persons = Spider.get_persons(date);
        print_static_persons(path, persons, 0);
    }

    public static Predicate<File> filter_time() {
        return file -> {
//            String time = file.getName().substring(11, 19);
//            if (time.compareTo("00:30:00") > 0) {
//                return false;
//            }
            return true;
        };
    }

    public static void print_static_persons(File path, List<Person> persons, int sex) {
        Stream.of(path.listFiles())
                .filter(f -> f.getName().endsWith(".txt"))
                .filter(filter_time())
//                .skip(0)
//                .limit(10)
                .forEach(f -> {
                    List<Person> static_persons = Spider.get_static_persons(f, persons);
                    System.out.printf("%s %s%n", sex(sex), f.getName().substring(11, 19));
                    print_static_persons(static_persons.stream()
                            .filter(filter_sex(sex))
                            .sorted()
                            .collect(toList()));
                });
    }

    public static void print_static_persons(List<Person> persons) {
        int vote_0 = persons.get(0).vote;
        int vote_1 = persons.get(1).vote;
        int vote_sub = vote_0 - vote_1;
        int vote_sum = persons.stream().mapToInt(p -> p.vote).sum();
        System.out.printf("(总票: %d, 票差: %d, 比例: %4.1f%%)%n",
                vote_sum, vote_sub, per(vote_sub, vote_1));
        AtomicInteger c = new AtomicInteger(1);
        persons.stream().forEach(p -> System.out.printf("%d: [ %05d | %4.1f%% ] %s%n",
                c.getAndIncrement(), p.vote, per(p.vote, vote_sum), p.name));
        System.out.println();
    }

    private static float per(int a, int b) {
        if (b == 0) {
            return 0f;
        } else {
            return a * 100f / b;
        }
    }

    private static String sex(int sex) {
        return sex == 0 ? "女子组" : sex == 1 ? "男子组" : "不分性别";
    }

    private static Predicate<Person> filter_sex(int sex) {
        return p -> sex == 2 || p.sex == sex;
    }

}
