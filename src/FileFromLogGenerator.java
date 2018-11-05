import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static java.lang.System.exit;

public class FileFromLogGenerator extends AnAction {
    private Logger logger = LogManager.getLogger("FileFromLogGenerator");
    /**
     * This produces a version of the file to be generated from the logfile. Mostly here for testing pusposes
     * @param e Occurs when the "Generate Original from Log" button is pressed
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        //Get the log file from the event, and using that get the version of the original it corresponds to using the helper CSVFileReader class
        VirtualFile vf = (VirtualFile)e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (vf == null){
            logger.error("The virtual file doesn't exist");
            return;
        }
        String path = vf.getCanonicalPath();
        if (path == null || !path.endsWith(".py")){
            logger.error("The correct path does not exist");
            return;
        }
        String logFilePath = path.substring(0, path.length()-2) + "csv";
        String logVersionText = CSVFileReader.generateFileFromCsv(logFilePath);
        try {
            //Create the file which contains the log generated version of the original. If the original log was called test.csv, this will be called "test_currentLogVersion.txt"
            File currentLogVersion = new File(logFilePath.substring(0, logFilePath.length()-3) + "_currentLogVersion.txt");
            //We don't want to append to the old txt file, so we're deleting it.
            if (currentLogVersion.exists()){
                if(!currentLogVersion.delete()){
                    logger.error("Could not delete old currentLogVersion.txt");
                    return;
                };
            }
            if (currentLogVersion.createNewFile()){
                FileWriter fw = new FileWriter(currentLogVersion);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(logVersionText);
                bw.close();
            }
            else{
                logger.error("Could not create new currentLogVersion.txt");
            }

        }
        catch (IOException exception){
            exception.printStackTrace();
        }
    }
    @Override
    public void update(AnActionEvent e) {
        Project project = (Project)e.getData(CommonDataKeys.PROJECT);
        Editor editor = (Editor)e.getData(CommonDataKeys.EDITOR);
        if (project!= null && editor!= null){
            VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
            if (file != null && file.getName().endsWith(".py")){
                String path = file.getCanonicalPath();
                if(path != null) {
                    File logFile = new File(path.substring(0, path.length()-2) + "csv");
                    if (logFile.exists()){
                        //The button should be active when there's a .py file involved and there's already a log file
                        e.getPresentation().setVisible(true);
                        return;
                    }
                }
            }
        }
        e.getPresentation().setVisible(false);
    }
}
