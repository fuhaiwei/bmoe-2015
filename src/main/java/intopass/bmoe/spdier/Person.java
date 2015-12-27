package intopass.bmoe.spdier;

import org.json.JSONObject;

import java.util.Comparator;

/**
 * Created by fuhaiwei on 15/12/20.
 */
public class Person implements Comparable<Person> {

    public String group; // 分组名称
    public String date; // 比赛日期
    public String time; // 当前时间
    public String bangumi; // 作品名称
    public String name; // 人物名称
    public String image; // 头像链接

    public int vote; // 投票数
    public int rank; // 小组名次
    public int rid; // 角色ID
    public int sex; // 性别, 0: 女, 1: 男

    public Person(JSONObject object, String group, String date, boolean big_image) {
        this.group = group;
        this.date = date;

        this.bangumi = object.getString("bangumi");
        this.name = object.getString("name");
        if (big_image) {
            this.image = object.getString("image_url");
        } else {
            this.image = object.getString("small_image_url");
        }

        this.vote = object.getInt("votes_count");
        this.rid = object.getInt("rid");
        this.sex = object.getInt("sex");
    }

    public Person(JSONObject object, String date, String time) {
        this.date = date;
        this.time = time;

        this.bangumi = object.getString("bangumi");
        this.name = object.getString("name");
        this.image = object.getString("image_url");

        this.vote = object.getInt("votes_count");
        this.rid = object.getInt("role_id");
        this.sex = object.getInt("sex");
    }

    public static Comparator<Person> sort_by_vote() {
        return (o1, o2) -> o2.vote - o1.vote;
    }

    public int compareTo(Person o) {
        return sort_by_vote().compare(this, o);
    }

    public String toString() {
        return String.format("%s %s (%d)", date.substring(5), name, vote);
    }
}
