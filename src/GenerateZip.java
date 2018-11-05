import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.System.exit;

public class GenerateZip extends AnAction {
    private Logger logger = LogManager.getLogger("GenerateZip");
    /**This method allows us to zip up the files
     * @param e Occurs when "Generate Zip for Submission" is pressed
     */
    public void actionPerformed(AnActionEvent e) {
        //Get the file path and file name of the file associated with the currently active tab
        VirtualFile vf = (VirtualFile)e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (vf == null){
            logger.error("Virtual file does not exist");
            return;
        }
        String path = vf.getCanonicalPath();
        if (path == null || !path.endsWith(".py")){
            logger.error("Correct path doesn't exist");
            return;
        }
        String fileName = vf.getName();
        try {
            int BUFFER = 2048;
            BufferedInputStream origin = null;
            //This is the new zip folder, if the original is called "test.py" the zip file would be located in the same directory and would be called "test_log.zip")
            FileOutputStream dest = new FileOutputStream(path.substring(0, path.length()-3) + "_log.zip");
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            int count;
            //This creates a copy of the csv file for the zip file
            byte[] data_byte = new byte[BUFFER];
            FileInputStream fi = new FileInputStream(path.substring(0, path.length()-2) + "csv");
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry csv_entry = new ZipEntry(fileName.substring(0, fileName.length()-2) + "csv");
            out.putNextEntry(csv_entry);
            while((count = origin.read(data_byte, 0, BUFFER)) != -1) {
                out.write(data_byte, 0, count);
            }

            //This creates a copy of the original .py file for the zip file
            byte[] data_byte_file = new byte[BUFFER];
            fi = new FileInputStream(new File(path));
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry document_entry = new ZipEntry(fileName);
            out.putNextEntry(document_entry);
            while((count = origin.read(data_byte_file, 0, BUFFER)) != -1) {
                out.write(data_byte_file, 0, count);
            }
            origin.close();
            out.close();
        }
        catch (Exception exception) {
            //Todo better error handling
            exception.printStackTrace();
        }

    }

    /**
     * updates the view of the button "Generate Log For Submission"
     * @param e
     */
    public void update(AnActionEvent e) {
        Project project = (Project)e.getData(CommonDataKeys.PROJECT);
        Editor editor = (Editor)e.getData(CommonDataKeys.EDITOR);
        if (project!= null && editor!= null){
            VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
            if (file != null && file.getName().endsWith(".py")){
                String path = file.getCanonicalPath();
                if(path != null) {
                    File logFile = new File(path.substring(0, path.length() - 2) + "csv");
                    if (logFile.exists()) {
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
