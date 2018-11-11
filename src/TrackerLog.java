import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class TrackerLog extends AnAction {
    private Logger logger = LogManager.getLogger("TrackerLog");
    /**
     * Allows the logging activity of a document
     *
     * @param anActionEvent Occurs when the 'Start logging edit' button is pressed
     */
    public void actionPerformed(@NotNull final AnActionEvent anActionEvent){
        //Get the editor for the current document tab, and from that get the document
        Editor editor = (Editor) anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if(file == null){
            logger.error("The virtual file doesn't exist");
            return;
        }
        if (ProjectInitializer.notificationOpen.containsKey(document)){
            //if there's a notification open it should be removed
            ProjectInitializer.notificationOpen.get(document).expire();
            ProjectInitializer.notificationOpen.remove(document);
        }
        //The file ends with .py and has no listener, so we are going to add a listener to it
        String originalPath = file.getCanonicalPath();
        if (originalPath == null || !originalPath.endsWith(".py")){
            logger.error("The virtual file doesn't exist");
            return;
        }
        String logFilePath = originalPath.substring(0, originalPath.length()-2) + "csv";
        //get the path for the currently viewing tab, and then feed that, along with the original file into the diff generator to see if there's any discrepancy, and correct the log accordingly
        diffGenerator.updateLog(logFilePath, document.getText());
        //add a document listener to the currently active tab
        DocumentListenerImpl documentListener = new DocumentListenerImpl(originalPath);
        document.addDocumentListener(documentListener);
        //Since we just added a listener, it should be included in the map hasDocumentListener
        ProjectInitializer.hasDocumentListener.put(document, documentListener);
        //Get the action manager that will help with the copy paste logging
        ActionManager actionManager = ActionManager.getInstance();
        String CCPFilePath = originalPath.substring(0, originalPath.length()-2) + "copy_paste" + ".csv";
        //Add the listener that will log copy-paste
        CopyPasteListener copyPasteListener = new CopyPasteListener( editor.getSelectionModel(), editor.getCaretModel(), CCPFilePath, document);
        actionManager.addAnActionListener(copyPasteListener);
        //Add it to the hasCopyPasteListener map
        ProjectInitializer.hasCopyPasteListener.put(document, copyPasteListener);
    }

    /**
     * Updates the view of the 'Start logging edit' option
     *
     * @param e The button press for "Start Logging Edit"
     */
    //TODO Find a way to disable the button after its pressed for an editor tab, without disabling it for the other tabs, because pressing it multiple times adds multiple listener which causes multiple logging of the same event.
    public void update(AnActionEvent e) {
        Project project = (Project) e.getData(CommonDataKeys.PROJECT);
        Editor editor = (Editor) e.getData(CommonDataKeys.EDITOR);
        if (project!= null && editor!=null && editor.getEditorKind().equals(EditorKind.UNTYPED) && ProjectInitializer.hasDocumentListener != null){
            Document document = editor.getDocument();
            VirtualFile file = FileDocumentManager.getInstance().getFile(document);
            if (file != null && file.getName().endsWith(".py") && !ProjectInitializer.hasDocumentListener.containsKey(document)) {
                //The button would be available only when there is no listener in the document already and when it's a .py file
                e.getPresentation().setVisible(true);
                return;
            }
        }
        e.getPresentation().setVisible(false);
    }
}