
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVFileWriter {
    static void appendToCsv(String csvFilePath, Long ms, String type, int offset, String Content) throws IOException {
        //A CSVPrinter to print to CSV (https://commons.apache.org/proper/commons-csv/apidocs/index.html)
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(csvFilePath, true), CSVFormat.DEFAULT)) {
            printer.printRecord(ms, type, offset, Content);
        } catch (IOException ex) {
            //TODO: Better Error Handling
            ex.printStackTrace();
        }
    }
}
