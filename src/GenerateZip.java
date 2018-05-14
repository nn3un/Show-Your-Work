import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenerateZip extends AnAction {
    /**This method allows us to zip up the files
     * @param e Occurs when "Generate Zip for Submission" is pressed
     */
    //Todo: Allow the action only if there already exists a CSV file and .py file to work with
    public void actionPerformed(AnActionEvent e) {
        //Get the file path and file name of the file associated with the currently active tab
        String path = ((VirtualFile)e.getData(PlatformDataKeys.VIRTUAL_FILE)).getCanonicalPath();
        String fileName = ((VirtualFile)e.getData(PlatformDataKeys.VIRTUAL_FILE)).getName();
        try {
            int BUFFER = 2048;
            BufferedInputStream origin = null;
            //This is the new zip folder, if the original is called "test.py" the zip file would be located in the same directory and would be called "test_log.zip")
            //Todo: Use regex to replace .py with _log.zip
            FileOutputStream dest = new FileOutputStream(path.replace(".py", "_log.zip"));
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            int count;
            //This creates a copy of the csv file for the zip file
            byte[] data_byte = new byte[BUFFER];
            //Todo: Use regex to replace .py with .csv
            FileInputStream fi = new FileInputStream(path.replace(".py", ".csv"));
            origin = new BufferedInputStream(fi, BUFFER);
            //Todo: Use regex to replace .py with .csv
            ZipEntry csv_entry = new ZipEntry(fileName.replace(".py", ".csv"));
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
        //The button should be active if the there's a project and an editor tab open
        e.getPresentation().setVisible(project != null && editor != null);
    }
}
