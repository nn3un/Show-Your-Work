
import java.io.*;
import java.util.LinkedList;

public class diffGenerator {
    /**
     * Helper class method that updates the log file to remove discrepancies with the original
     * @param CSVFilePath The path for the log file
     * @param originalFileContent The contents of the original .py file
     */
    public static void updateLog(String CSVFilePath, String originalFileContent){
        //Use the CSVFileReader class to replicate the original from the CSV
        String logVersionOfFile = CSVFileReader.generateFileFromCsv(CSVFilePath);
        //Get a list of the differences using diff-match-patch
        diff_match_patch dmp = new diff_match_patch();
        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(logVersionOfFile, originalFileContent, false);
        try {
        File logFile = new File(CSVFilePath);
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
        FileWriter fw = new FileWriter(logFile.getAbsoluteFile(), true);
        BufferedWriter bw = new BufferedWriter(fw);
        //the offset starts at 0
        int offset = 0;
        for(diff_match_patch.Diff d: diffs){
            //for the part of the text that are the same, we can just move our caret forward
            if (d.operation == diff_match_patch.Operation.EQUAL){
                offset += d.text.length();
            }
            //If the original file has something the log version of the file doesn't, we have to add that to the log as an add, and move the cursor forward.
            else if (d.operation == diff_match_patch.Operation.INSERT) {
                CSVFileWriter.appendToCsv(CSVFilePath, System.currentTimeMillis(),"add", offset, d.text);
                offset += d.text.length();
            }
            //If the original file is missing something the log version of the file has,we have to add that to the csv log as a sub. No need to move the caret
            else {
                CSVFileWriter.appendToCsv(CSVFilePath, System.currentTimeMillis(),"sub", offset, d.text);
            }
        }
        } catch (IOException e) {
            //Todo: Better Error handling
            e.printStackTrace();
        }
    }
}