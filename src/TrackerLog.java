import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;

public class TrackerLog extends AnAction {
    /**
     * Allows the logging activity of a document
     * @param anActionEvent Occurs when the 'Start logging edit' button is pressed
     */
    public void actionPerformed(@NotNull final AnActionEvent anActionEvent) {
        //Get the editor for the current document tab, and from that get the document
        Editor editor = (Editor)anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        Document d = editor.getDocument();

        //get the path for the currently viewing tab, and then feed that, along with the original file into the diff generator to see if there's any discrepancy
        //TODO: use regular expression to change .py to .csv
        String logFilePath = ((VirtualFile)anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE)).getCanonicalPath().replace(".py", ".csv");
        diffGenerator.updateLog(logFilePath, d.getText());

        //add a document listener to the currently active tab (documentation: https://github.com/JetBrains/intellij-community/blob/4ea2f6de15ff5c57a0664477dd673c3748d3c5cb/platform/core-api/src/com/intellij/openapi/editor/event/DocumentListener.java)
        d.addDocumentListener(new DocumentListener() {
            String path = ((VirtualFile)anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE)).getCanonicalPath();
            public void documentChanged(DocumentEvent event){
                if (!("" + event.getNewFragment()).equals("")) {
                    //if a non-empty string is attached to the document that means an add action happened, so the necessary information is passed to the CSVfilewriter
                    //this code is included in the documentChanged portion, since we need the new offset position.
                    try {
                        CSVFileWriter.appendToCsv(path, "add", event.getOffset(), "" + event.getNewFragment());
                    }
                    catch (IOException exception) {
                        //TODO: Better Error Handling
                        exception.printStackTrace();
                    }
                }
            }
            public void beforeDocumentChange(DocumentEvent event) {
                if (!("" + event.getOldFragment()).equals("")) {
                    //if a non-empty string is deleted from the  document that means a sub action happened, so the necessary information is passed to the CSVfilewriter
                    //this code is included in the beforeDocumentChange portion, since we need the old offset position.
                    try {
                        CSVFileWriter.appendToCsv(path, "sub", event.getOffset(), "" + event.getOldFragment());
                    } catch (IOException exception) {
                        //TODO: Better Error Handling
                        exception.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Updates the view of the plugin button
     * @param e The button press for "Start Logging Edit"
     */
    //TODO Find a way to disable the button after its pressed for an editor tab, without disabling it for the other tabs, because pressing it multiple times adds multiple listener which causes multiple logging of the same event.
    public void update(AnActionEvent e) {
        Project project = (Project)e.getData(CommonDataKeys.PROJECT);
        Editor editor = (Editor)e.getData(CommonDataKeys.EDITOR);
        //The button should be on if the there's a project and an editor tab open
        e.getPresentation().setVisible(project!=null && editor!=null);
    }
}