
import jdk.nashorn.internal.runtime.ErrorManager;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.LinkedList;

import static java.lang.System.exit;

class diffGenerator {
    private static Logger logger = LogManager.getLogger("diffGenerator");

    /**
     * Helper class method that updates the log file to remove discrepancies with the original
     * @param CSVFilePath The path for the log file
     * @param originalFileContent The contents of the original .py file
     */
    static void updateLog(String CSVFilePath, String originalFileContent){
        //Use the CSVFileReader class to replicate the original from the CSV
        String logVersionOfFile = CSVFileReader.generateFileFromCsv(CSVFilePath);
        //Get a list of the differences using diff-match-patch
        diff_match_patch dmp = new diff_match_patch();
        LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(logVersionOfFile, originalFileContent, false);
        try {
        File logFile = new File(CSVFilePath);
        if (!logFile.exists()) {
            if(!logFile.createNewFile()){
                logger.error("Failed to create new CSV file");
                return;
            };
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
            //TODO: If memory becomes an issue, consider only saving the length of the substring when the type is "sub" since the actual deleted string is useless
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