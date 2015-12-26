package intopass.bmoe;

import intopass.bmoe.spdier.Person;
import intopass.bmoe.spdier.Spider;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by fuhaiwei on 15/12/21.
 */
public class LostCount {

    public static void main(String[] args) {
        LocalDate start = LocalDate.of(2015, 12, 12);
        LocalDate end = LocalDate.of(2015, 12, 26);
        List<Person> persons = Spider.get_persons(start, end);

        count_vote(persons);
    }

    public static void count_vote(List<Person> persons) {
        System.out.println("注意这里的弃票率是相对弃票率，因为没有准确的票仓数据。");
        System.out.println("所以我用得票最高的小组总票数，作为基准票仓数据。");
        System.out.println("所以弃票率为 0.0%，不是说弃票真的是零，只是说明弃票最少。");
        System.out.println("小组弃票率即为「该小组总票数」与「当天最高小组总票数」之差的比例");
        System.out.println();

        persons.stream()
                .map(p -> p.date)
                .distinct()
                .sorted()
                .forEach(date_name -> {
                    Map<String, List<Person>> collect = persons.stream()
                            .filter(p -> p.date.equals(date_name))
                            .collect(groupingBy(p -> format(p)));
                    AtomicInteger vote_max = new AtomicInteger(0);
                    Map<String, Object[]> vote_sums = new LinkedHashMap<>();
                    count_vote(collect, vote_max, vote_sums);

                    AtomicInteger count = new AtomicInteger(1);
                    System.out.println("分组: [ 小组总票数 | 小组弃票率 | 角色被投率 ] 角色名称");
                    vote_sums.forEach((group_name, array) -> {
                        int vote_sum = (int) array[0];
                        String name = (String) array[1];
                        float scale = (float) array[2];
                        float scale2 = (vote_max.intValue() - vote_sum) * 100f / vote_max.intValue();
                        System.out.printf("%s [ %05d | %3.1f%% | %.1f%% ] %s%n",
                                group_name, vote_sum, scale2, scale, name);
                        if (count.getAndIncrement() % 8 == 0) {
                            System.out.println();
                        }
                    });
                });
    }

    private static void count_vote(Map<String, List<Person>> collect, AtomicInteger vote_max,
                                   Map<String, Object[]> vote_sums) {
        collect.keySet().stream()
                .sorted()
                .forEach(group_name -> {
                    int vote_sum = collect.get(group_name).stream()
                            .mapToInt(p -> p.vote)
                            .sum();
                    Person person = collect.get(group_name).stream()
                            .sorted()
                            .findFirst()
                            .get();
                    float scale = person.vote * 100f / vote_sum;
                    vote_sums.put(group_name, new Object[]{vote_sum, person.name, scale});
                    if (vote_max.intValue() < vote_sum) {
                        vote_max.set(vote_sum);
                    }
                });
    }

    private static String format(Person p) {
        return p.date.substring(5) + " " + sex(p) + " " + p.group;
    }

    private static String sex(Person p) {
        return p.sex == 0 ? "女" : "男";
    }
}
