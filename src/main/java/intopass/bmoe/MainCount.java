package intopass.bmoe;

import intopass.bmoe.spdier.Person;
import intopass.bmoe.spdier.Spider;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static intopass.bmoe.spdier.Output.write_to_file;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Created by fuhaiwei on 15/12/20.
 */
public class MainCount {

    public static void main(String[] args) {
        do_count();
    }

    public static void do_count() {
        List<Person> today = Spider.get_persons(LocalDate.now().toString());

        new File("output/benzhan/" + get_date()).mkdirs();

        write_to_file("benzhan/" + get_date(), "01 本战比赛情况 " + get_time(),
                out -> print_benzhan(out, today));
        write_to_file("benzhan/" + get_date(), "02 本战票差情况 " + get_time(),
                out -> print_benzhan_sub(out, today));
        write_to_file("", "01 当前比赛结果",
                out -> print_current(out));
    }

    private static void print_current(PrintStream out) {
        out.println();
        out.println();
        File path = new File("output/benzhan/" + get_date());
        Stream.of(path.listFiles())
                .filter(f -> f.getName().startsWith("01 本战比赛情况"))
                .sorted(reverseOrder())
                .limit(3)
                .forEach((file) -> print_file(out, file));
        Stream.of(path.listFiles())
                .filter(f -> f.getName().startsWith("02 本战票差情况"))
                .sorted(reverseOrder())
                .limit(3)
                .forEach((file) -> print_file(out, file));
    }


    private static void print_file(PrintStream out, File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.ready()) {
                out.println(br.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.println();
    }

    private static void print_benzhan(PrintStream out, List<Person> today) {
        Map<String, List<Person>> collect = today.stream().collect(groupingBy(p -> p.group));
        print_benzhan(out, collect, 0);
        print_benzhan(out, collect, 1);
    }

    private static void print_benzhan(PrintStream out, Map<String, List<Person>> collect, int sex) {
        collect.keySet()
                .stream()
                .sorted()
                .forEach(group -> {
                    List<Person> persons = collect.get(group).stream()
                            .filter(filter_sex(sex))
                            .sorted()
                            .collect(toList());
                    int vote_sum = persons.stream().mapToInt(p -> p.vote).sum();
                    int vote_sub = persons.get(0).vote - persons.get(1).vote;
                    out.printf("%s %s (总票: %d, 票差: %d)%n", group, sex(sex), vote_sum, vote_sub);
                    AtomicInteger c = new AtomicInteger(1);
                    persons.stream().forEach(p -> out.printf("%d: [ %05d | %4.1f%% ] %s%n",
                            c.getAndIncrement(), p.vote, per(p.vote, vote_sum), p.name));
                    out.println();
                });
    }

    private static void print_benzhan_sub(PrintStream out, List<Person> today) {
        Map<String, List<Person>> collect = today.stream().collect(groupingBy(p -> p.group));
        out.printf("分组: 角色名称 (领先票数, 领先比例)%n");
        print_benzhan_sub(out, collect, 0);
        print_benzhan_sub(out, collect, 1);
    }

    private static void print_benzhan_sub(PrintStream out, Map<String, List<Person>> collect, int sex) {
        collect.keySet()
                .stream()
                .sorted()
                .forEach(group -> {
                    List<Person> persons = collect.get(group).stream()
                            .filter(filter_sex(sex))
                            .sorted()
                            .collect(toList());
                    int vote_1 = persons.get(0).vote;
                    int vote_2 = persons.get(1).vote;
                    int vote_sub = vote_1 - vote_2;
                    String first_name = persons.get(0).name;
                    out.printf("%s %s: %s (%d, %.1f%%)%n",
                            group, sex(sex), first_name, vote_sub, per(vote_sub, vote_2));
                });
    }

    private static float per(int a, int b) {
        if (b == 0) {
            return 0f;
        } else {
            return a * 100f / b;
        }
    }

    private static String get_date() {
        return LocalDate.now().toString().substring(5);
    }

    private static String get_time() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime now = LocalTime.now();
        if (now.getMinute() < 30) {
            now = now.withMinute(0);
        } else {
            now = now.withMinute(30);
        }
        return now.format(formatter);
    }

    private static String sex(int sex) {
        return sex == 0 ? "女子组" : sex == 1 ? "男子组" : "不分性别";
    }

    private static Predicate<Person> filter_sex(int sex) {
        return p -> sex == 2 || p.sex == sex;
    }

}
