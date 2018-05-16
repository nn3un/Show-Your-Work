import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
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
     *
     * @param anActionEvent Occurs when the 'Start logging edit' button is pressed
     */
    public void actionPerformed(@NotNull final AnActionEvent anActionEvent) {
        //Get the editor for the current document tab, and from that get the document
        Editor editor = (Editor) anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (!file.getName().endsWith(".py") || ProjectInitializer.hasDocumentListener.containsKey(document)) {
            //if it's not a .py file, we're not interested in tracking its changes. If it already has a listener, we don't want to add a second one, as that will lead to double logging
            return;
        }
        //The file ends with .py and has no listener, so we are going to add a listener to it
        String logFilePath = file.getCanonicalPath().replace(".py", ".csv");
        //get the path for the currently viewing tab, and then feed that, along with the original file into the diff generator to see if there's any discrepancy, and correct the log accordingly
        diffGenerator.updateLog(logFilePath, document.getText());
        //add a document listener to the currently active tab
        String path = file.getCanonicalPath();
        DocumentListenerImpl documentListener = new DocumentListenerImpl(path);
        document.addDocumentListener(documentListener);
        //Since we just added a listener, it should be included in the map hasDocumentListener
        ProjectInitializer.hasDocumentListener.put(document, documentListener);
    }

    /**
     * Updates the view of the plugin button
     *
     * @param e The button press for "Start Logging Edit"
     */
    //TODO Find a way to disable the button after its pressed for an editor tab, without disabling it for the other tabs, because pressing it multiple times adds multiple listener which causes multiple logging of the same event.
    public void update(AnActionEvent e) {
        Project project = (Project) e.getData(CommonDataKeys.PROJECT);
        Editor editor = (Editor) e.getData(CommonDataKeys.EDITOR);
        Document document = editor.getDocument();
        if (project!= null && editor!=null){
            VirtualFile file = FileDocumentManager.getInstance().getFile(document);
            if (file.getName().endsWith(".py") && !ProjectInitializer.hasDocumentListener.containsKey(document)) {
                //The button would be available only when there is no listener in the document already and when it's a .py file
                e.getPresentation().setVisible(true);
                return;
            }
        }
        e.getPresentation().setVisible(false);
    }
}