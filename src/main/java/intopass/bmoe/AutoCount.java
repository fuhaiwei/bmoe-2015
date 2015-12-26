package intopass.bmoe;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import static intopass.bmoe.spdier.Spider.save_static_json;

/**
 * Created by fuhaiwei on 15/12/20.
 */
public class AutoCount {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("Auto Count Starting");
        do_count();
        new Timer().schedule(new TimerTask() {
            public void run() {
                save_static_json();
                try_count();
            }
        }, 1000, 15 * 1000);
    }

    private static void try_count() {
        if (is_count(LocalTime.now())) {
            do_count();
        }
    }

    private static void do_count() {
        try {
            MainCount.do_count();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            exec_command("say something wrong");
            return;
        }
        exec_command("say data updated");
        System.out.println(FORMATTER.format(LocalDateTime.now()));
    }

    private static boolean is_count(LocalTime now) {
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();

        if (hour == 0 && minute < 30) {
            return false;
        }
        if (minute == 0 || minute == 30) {
            return second > 20;
        }
        if (minute == 1 || minute == 31) {
            return second > 40;
        }
        return false;
    }

    private static void exec_command(String command) {
        try {
            System.out.println(command);
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
