import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
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
        //Get the editor for the current document tab
        Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        initializeListeners(editor);
    }

    /**
     * Updates the view of the 'Start logging edit' option
     *
     * @param e The button press for "Start Logging Edit"
     */
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project!= null && editor!=null && editor.getEditorKind().equals(EditorKind.UNTYPED) && IdeInitializer.hasDocumentListener != null){
            Document document = editor.getDocument();
            VirtualFile file = FileDocumentManager.getInstance().getFile(document);
            if (file != null && file.getName().endsWith(".py") && !IdeInitializer.hasDocumentListener.containsKey(document)) {
                //The button would be available only when there is no listener in the document already and when it's a .py file
                e.getPresentation().setVisible(true);
                return;
            }
        }
        e.getPresentation().setVisible(false);
    }

    /**
    Helper method to get the trackers started.
    @param editor currently active editor**/
    private void initializeListeners(Editor editor){
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if(file == null){
            logger.error("The virtual file doesn't exist");
            return;
        }
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
        IdeInitializer.hasDocumentListener.put(document, documentListener);

        //Add the listener that will log copy-paste
        ActionManager actionManager = ActionManager.getInstance();
        String CCPFilePath = originalPath.substring(0, originalPath.length()-2)+ "csv";
        CopyPasteListener copyPasteListener = new CopyPasteListener(editor, CCPFilePath);
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        MessageBusConnection connection = bus.connect();
        connection.subscribe(AnActionListener.TOPIC, copyPasteListener);
        IdeInitializer.hasCopyPasteListener.put(document, copyPasteListener);

        if (IdeInitializer.notificationOpen.containsKey(document)){
            //if there's a notification open it should be removed
            IdeInitializer.notificationOpen.get(document).expire();
            IdeInitializer.notificationOpen.remove(document);
        }

        //Changing the widget text
        IdeFrame frame = WindowManager.getInstance().getIdeFrame(editor.getProject());
        StatusBar statusBar = frame.getStatusBar();
        //First remove all the other widgets
        statusBar.removeWidget("Show-your-work");
        MyStatusBarWidget myStatusBarWidget = new MyStatusBarWidget();
        //Set the appropriate text
        myStatusBarWidget.setText("Document Tracking: ON");
        statusBar.addWidget(myStatusBarWidget, "before Position");
        statusBar.updateWidget("Show-your-work");
    }

}