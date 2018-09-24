import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileFromLogGenerator extends AnAction {
    /**
     * This produces a version of the file to be generated from the logfile. Mostly here for testing pusposes
     * @param e Occurs when the "Generate Original from Log" button is pressed
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        //Get the log file from the event, and using that get the version of the original it corresponds to using the helper CSVFileReader class
        //Todo Use regex to replace .py with .csv
        String logFilePath = ((VirtualFile)e.getData(PlatformDataKeys.VIRTUAL_FILE)).getCanonicalPath().replace(".py", ".csv");
        String logVersionText = CSVFileReader.generateFileFromCsv(logFilePath);
        try {
            //Create the file which contains the log generated version of the original. If the original log was called test.csv, this will be called "test_currentLogVersion.txt"
            File currentLogVersion = new File(logFilePath.replace(".csv", "_currentLogVersion.txt"));
            //We don't want to append to the old txt file, so we're deleting it.
            if (currentLogVersion.exists()){
                currentLogVersion.delete();
            }
            currentLogVersion.createNewFile();
            FileWriter fw = new FileWriter(currentLogVersion);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(logVersionText);
            bw.close();
        }
        catch (IOException exception){
            exception.printStackTrace();
        }
    }
    @Override
    /**
     * Updates the visibility of the "Generate Original From Log" button
     */
    public void update(AnActionEvent e) {
        Project project = (Project)e.getData(CommonDataKeys.PROJECT);
        Editor editor = (Editor)e.getData(CommonDataKeys.EDITOR);
        if (project!= null && editor!= null){
            VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
            if (file.getName().endsWith(".py")){
                //Todo Use regex to replace
                File logFile = new File(file.getCanonicalPath().replace(".py", ".csv"));
                if (logFile.exists()){
                    //The button should be active when there's a .py file involved and there's already a log file
                    e.getPresentation().setVisible(true);
                    return;
                }
            }
        }
        e.getPresentation().setVisible(false);
    }
}
