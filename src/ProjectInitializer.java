import com.intellij.notification.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;

public class ProjectInitializer implements ProjectComponent {
    static HashMap<Document, DocumentListener> hasDocumentListener;
    static HashMap<Document, Notification> notificationOpen;
    static HashMap<Document, CopyPasteListener> hasCopyPasteListener;

    /**
     * This would be invoked when a new project is started
     */

    public void initComponent(){
        Logger logger = LogManager.getLogger("ProjectInitializer");
        //Todo come up with better way to prevent multiple document listener for one file
        //I couldn't think of anything else but making the variable static which means there can be 1 hasDocuementListener for multiple open projects, so we have to act accordingly
        if(hasDocumentListener==null) {
            hasDocumentListener = new HashMap<>();
        }
        if(notificationOpen==null) {
            notificationOpen = new HashMap<>();
        }
        if(hasCopyPasteListener==null) {
            hasCopyPasteListener = new HashMap<>();
        }
        //This class helps with tracking the editors in the project
        EditorFactory editorFactory = EditorFactory.getInstance();
        editorFactory.addEditorFactoryListener(new EditorFactoryListener() {
            @Override
            //Will be evoked after a tab is created
            public void editorCreated(@NotNull EditorFactoryEvent editorFactoryEvent) {
                Editor editor = editorFactoryEvent.getEditor();
                if (!editor.getEditorKind().equals(EditorKind.UNTYPED)) {
                    //There are different kind of editors in Intellij, we only want to track edits that happen in the UNTYPED one
                    return;
                }
                Document document = editor.getDocument();
                VirtualFile file = FileDocumentManager.getInstance().getFile(document);
                if (file == null || !file.getName().endsWith(".py") || ProjectInitializer.hasDocumentListener.containsKey(document) || ProjectInitializer.notificationOpen.containsKey(document)) {
                    //if it's not a .py file, we're not interested in tracking its changes. If it already has a listener or a notification, we don't want to add a second one, as that will lead to double logging
                    logger.info("The file correct file doesn't exist, or it's somehow already in one of the maps");
                    return;
                }
                //Creating a notification to remind the user to start logging activity
                //Documentation: https://github.com/JetBrains/intellij-community/blob/306d705e1829bd3c74afc2489bfb7ed59d686b84/platform/platform-api/src/com/intellij/notification/NotificationGroup.java
                NotificationGroup showYourWork = new NotificationGroup("Show Your Work", NotificationDisplayType.STICKY_BALLOON, false);
                //Documentation: https://github.com/JetBrains/intellij-community/blob/306d705e1829bd3c74afc2489bfb7ed59d686b84/platform/platform-api/src/com/intellij/notification/Notification.java
                Notification notification = showYourWork.createNotification("Start Logging Edits for file " + file.getName() + "?", NotificationType.INFORMATION);
                notificationOpen.put(document, notification);
                notification.addAction(new NotificationAction("Start") {
                    @Override
                    //The action that happens when the user presses the 'Start' button on the notification, essentially the same thing the TrackerLog class does
                    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                        //when the start button is pressed, remove notification and also its place in the notificationOpen map
                        notification.expire();
                        notificationOpen.remove(document);
                        //this is the same as the trackerLog.java class
                        //The file ends with .py and has no listener, so we are going to add a listener to it
                        String path = file.getCanonicalPath();
                        if(path == null || !path.endsWith(".py")){
                            logger.error("The correct path does not exist");
                            return;
                        }
                        String logFilePath = path.substring(0, path.length()-2) + "csv";
                        //get the path for the currently viewing tab, and then feed that, along with the original file into the diff generator to see if there's any discrepancy, and correct the log accordingly
                        diffGenerator.updateLog(logFilePath, document.getText());
                        //add a document listener to the currently active tab
                        DocumentListenerImpl documentListener = new DocumentListenerImpl(path);
                        document.addDocumentListener(documentListener);
                        //Since we just added a listener, it should be included in the map hasDocumentListener
                        ProjectInitializer.hasDocumentListener.put(document, documentListener);
                        //Get the action manager that will help with the copy paste logging
                        ActionManager actionManager = ActionManager.getInstance();
                        String CCPFilePath = path.substring(0, path.length()-2) + "csv";
                        //Add the listener that will log copy-paste
                        CopyPasteListener copyPasteListener = new CopyPasteListener(editor, CCPFilePath);
                        actionManager.addAnActionListener(copyPasteListener);
                        hasCopyPasteListener.put(document, copyPasteListener);
                    }
                });
                notification.notify(editor.getProject());
            }
            @Override
            //Will be invoked after a tab is closed
            public void editorReleased(@NotNull EditorFactoryEvent editorFactoryEvent) {
                Editor editor = editorFactoryEvent.getEditor();
                //If it's not the proper kind of editor, we don't care about what its listeners
                if (!editor.getEditorKind().equals(EditorKind.UNTYPED)){
                    return;
                }
                //When a tab is closed, we want to remove its documentListener as well as remove it from hasDocumentListener
                Document documentClosed = editorFactoryEvent.getEditor().getDocument();
                if (hasDocumentListener.containsKey(documentClosed)){
                    documentClosed.removeDocumentListener(hasDocumentListener.get(documentClosed));
                    hasDocumentListener.remove(documentClosed);
                }
                if (notificationOpen.containsKey(documentClosed)){
                    //if there's a notification open, when the tab is closed, it should be removed
                    ProjectInitializer.notificationOpen.get(documentClosed).expire();
                    ProjectInitializer.notificationOpen.remove(documentClosed);
                }
                if (hasCopyPasteListener.containsKey(documentClosed)){
                    ActionManager.getInstance().removeAnActionListener(hasCopyPasteListener.get(documentClosed));
                    hasCopyPasteListener.remove(documentClosed);
                }
            }
        }, () -> {

        });

    }
}
