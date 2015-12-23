package intopass.bmoe.spdier;

import java.io.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by fuhaiwei on 15/12/20.
 */
public abstract class Output {

    public static void write_to_file(String path, String name, Consumer<PrintStream> consumer) {
        try {
            File file = new File("output/" + path, name + ".txt");
            System.out.println("writing: " + file.getPath());
            PrintStream out = new PrintStream(file);
            out.printf("%s%n", name.substring(3));
            consumer.accept(out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void init_path(File path) {
        if (path.exists()) {
            delete(path);
        }
        path.mkdirs();
    }

    public static void delete(File path) {
        if (path.isDirectory()) {
            File[] filles = path.listFiles();
            if (filles != null) {
                Stream.of(filles).forEach(Output::delete);
            }
        }
        path.delete();
    }

}

