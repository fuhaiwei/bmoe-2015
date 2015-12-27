package intopass.bmoe.spdier;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import static java.util.stream.Collectors.*;

/**
 * Created by fuhaiwei on 15/12/20.
 */
public abstract class Spider {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void save_static_json() {
        int retryCount = 3;
        for (int i = 0; i < retryCount; i++) {
            try {
                String json_text = Jsoup.connect("http://moe.bilibili.com/api/s/getTopEight/static.json")
                        .ignoreContentType(true)
                        .method(Connection.Method.GET)
                        .execute()
                        .body();
                File file = new File("cached/json_text2/" + datetime() + ".txt");
                file.getParentFile().mkdirs();
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println(json_text);
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException("文件写入失败: " + file.getPath(), e);
                }
            } catch (IOException e) {
                System.out.printf("连接失败: %s%n尝试重新连接中(%d/%d)%n", e.getMessage(), i + 1, retryCount);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static List<Person> get_static_persons(File file, List<Person> persons) {
        String date = file.getName().substring(5, 10);
        String time = file.getName().substring(11, 19);
        List<Integer> rids = persons.stream().map(p -> p.rid).collect(toList());
        Builder<Person> builder = Stream.builder();
        JSONObject object = new JSONObject(read_content(file));
        JSONObject data = object.getJSONObject("data");
        data.keySet().forEach(sex_name -> {
            JSONObject sex_group = data.getJSONObject(sex_name);
            sex_group.keySet().forEach(rank -> {
                JSONObject person = sex_group.getJSONObject(rank);
                if (person.has("name") && rids.contains(person.getInt("role_id"))) {
                    builder.add(new Person(person, date, time));
                }
            });
        });
        return builder.build().collect(toList());
    }

    private static String datetime() {
        return FORMATTER.format(LocalDateTime.now());
    }

    public static List<Person> get_persons(LocalDate start, LocalDate end) {
        List<Person> persons = Collections.synchronizedList(new ArrayList<>());
        Stream.iterate(start, date -> date.plusDays(1))
                .limit(end.toEpochDay() - start.toEpochDay())
                .parallel()
                .forEach(date -> persons.addAll(get_persons(date.toString())));
        System.out.printf("所需数据已全部获取完成, 数据输出中...%n%n", start, end);
        return persons;
    }

    public static List<Person> get_persons(String date) {
        JSONObject jsonObject = get_json_object(date);
        JSONObject scheduleState = jsonObject.getJSONObject("scheduleState");
        JSONObject data = jsonObject.getJSONObject("data");
        if (scheduleState.getString("sname").startsWith("本战")) {
            return get_benzhan_persons(date, data);
        } else {
            return get_haixuan_persons(date, data);
        }
    }


    public static List<Person> get_benzhan_persons(String date, JSONObject data) {
        Builder<Person> builder = Stream.builder();
        data.keys().forEachRemaining(sex -> {
            JSONArray sex_group = data.getJSONArray(sex);
            for (int i = 0; i < sex_group.length(); i++) {
                JSONObject group = sex_group.getJSONObject(i);
                String gname = group.getString("name");
                JSONArray members = group.getJSONArray("members");

                Builder<Person> temp = Stream.builder();
                for (int j = 0; j < members.length(); j++) {
                    JSONObject object = members.getJSONObject(j);
                    temp.add(new Person(object, gname, date, true));
                }
                AtomicInteger c = new AtomicInteger(1);
                temp.build().sorted().forEach(p -> {
                    p.rank = c.getAndIncrement();
                    builder.add(p);
                });
            }
        });
        return builder.build().collect(toList());
    }

    public static List<Person> get_haixuan_persons(String date, JSONObject data) {
        Builder<Person> builder = Stream.builder();
        data.keys().forEachRemaining(gname -> {
            JSONArray group = data.getJSONArray(gname);
            Builder<Person> temp = Stream.builder();
            for (int j = 0; j < group.length(); j++) {
                JSONObject object = group.getJSONObject(j);
                temp.add(new Person(object, gname, date, false));
            }
            AtomicInteger c = new AtomicInteger(1);
            temp.build().sorted().forEach(p -> {
                p.rank = c.getAndIncrement();
                builder.add(p);
            });
        });
        return builder.build().collect(toList());
    }

    public static JSONObject get_json_object(String date) {
        String json_text = get_json_text(date);
        if (json_text == null) {
            json_text = fetch_json_text(date);
        } else {
            System.out.println("从文件获取 " + date + " 数据成功");
        }
        JSONObject jsonObject = new JSONObject(json_text);
        if (jsonObject.getInt("state") != 200) {
            throw new RuntimeException("返回的state值不是200");
        }
        if (LocalDate.parse(date).isBefore(LocalDate.now())) {
            write_json_text(date, json_text);
        }
        return jsonObject;
    }

    public static String get_json_text(String date) {
        return read_content(new File("cached/json_text/" + date + ".txt"));
    }

    public static void write_json_text(String date, String json_text) {
        write_content(new File("cached/json_text/" + date + ".txt"), json_text);
    }

    public static String fetch_json_text(String date) {
        int retryCount = 5;
        for (int i = 0; i < retryCount; i++) {
            try {
                System.out.printf("尝试从网络获取 %s 数据中%n", date);
                return Jsoup.connect("http://moe.bilibili.com/api/s/getResult?date=" + date)
                        .ignoreContentType(true)
                        .method(Connection.Method.GET)
                        .execute()
                        .body();
            } catch (IOException e) {
                System.out.printf("连接失败: %s%n尝试重新连接中(%d/%d)%n", e.getMessage(), i + 1, retryCount);
            }
        }
        throw new RuntimeException(String.format("获取 %s 数据经多次重试后仍然失败, 程序异常退出%n", date));
    }

    public static String read_content(File file) {
        if (file.canRead()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return reader.lines().collect(joining());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void write_content(File file, String json_text) {
        file.getParentFile().mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(json_text);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
