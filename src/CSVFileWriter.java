/**@Author: Zach Struble*/
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVFileWriter {
    public static void appendToCsv(String path, Long ms, String type, int offset, String Content) throws IOException {
        String filename = path.replace(".py", ".csv");
        FileWriter fw = null;
        BufferedWriter bw = null;
        String content = Content.replace(',', '`');
        String data = ms + "," + type + "," + offset + "," + content + ",\n";
        File file = new File(filename);
        if (!file.exists()) {
            file.createNewFile();
        }

        fw = new FileWriter(file.getAbsoluteFile(), true);
        bw = new BufferedWriter(fw);
        bw.write(data);
        bw.close();
    }
}
