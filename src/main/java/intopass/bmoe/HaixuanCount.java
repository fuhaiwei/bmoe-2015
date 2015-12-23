package intopass.bmoe;

import intopass.bmoe.spdier.Person;
import intopass.bmoe.spdier.Spider;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

/**
 * Created by fuhaiwei on 15/12/23.
 */
public class HaixuanCount {
    public static void main(String[] args) {
        List<Person> haixuan = Spider.get_persons(LocalDate.of(2015, 10, 31), LocalDate.of(2015, 12, 6));
        List<Person> benzhan_32 = Spider.get_persons(LocalDate.of(2015, 12, 20), LocalDate.of(2015, 12, 24));
        List<Person> benzhan_16 = benzhan_32.stream().filter(p -> p.rank == 1).collect(toList());

        List<Person> haixuan_32 = benzhan_32.stream()
                .map(p -> find_by_rid(haixuan, p.rid))
                .collect(toList());
        List<Person> haixuan_16 = benzhan_16.stream()
                .map(p -> find_by_rid(haixuan, p.rid))
                .collect(toList());

        print_date("海选晋级32强日期分布情况 女子组", haixuan_32, 0);
        print_date("海选晋级32强日期分布情况 男子组", haixuan_32, 1);

        print_date("海选晋级16强日期分布情况 女子组", haixuan_16, 0);
        print_date("海选晋级16强日期分布情况 男子组", haixuan_16, 1);
    }

    private static void print_date(String title, List<Person> persons, int sex) {
        System.out.println(title);
        Map<String, List<Person>> collect = persons.stream()
                .filter(p -> p.sex == sex)
                .collect(groupingBy(p -> p.date));
        collect.keySet().stream()
                .sorted()
                .forEach(date -> {
                    List<Person> dates = collect.get(date);
                    String names = dates.stream()
                            .sorted()
                            .map(p -> p.name + "(" + p.rank + ")")
                            .collect(joining(" "));
                    System.out.printf("%s: (%d人) %s%n",
                            date.substring(5), dates.size(), names);
                });
    }

    private static Person find_by_rid(List<Person> haixuan, int rid) {
        return haixuan.stream().filter(p -> p.rid == rid).findFirst().get();
    }
}
