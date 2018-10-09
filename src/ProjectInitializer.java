import com.intellij.notification.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.HashMap;
public class ProjectInitializer implements ProjectComponent {
    public static HashMap<Document, DocumentListener> hasDocumentListener;
    public static HashMap<Document, Notification> notificationOpen;
    /**
     * This would be invoked when a new project is started
     */
    public void initComponent(){
        //Todo come up with better way to prevent multiple document listener for one file
        //I couldn't think of anything else but making the variable static which means there can be 1 hasDocuementListener for multiple open projects, so we have to act accordingly
        if(hasDocumentListener==null) {
            hasDocumentListener = new HashMap<Document, DocumentListener>();
        }
        if(notificationOpen==null) {
            notificationOpen = new HashMap<Document, Notification>();
        }
        //This class helps with tracking the editors in the project
        EditorFactory editorFactory = EditorFactory.getInstance();
        editorFactory.addEditorFactoryListener(new EditorFactoryListener() {
            @Override
            /**
             * Is evoked before a new editor tab is opened
             */
            public void editorCreated(@NotNull EditorFactoryEvent editorFactoryEvent) {
                Editor editor = editorFactoryEvent.getEditor();
                Document document = editor.getDocument();
                VirtualFile file = FileDocumentManager.getInstance().getFile(document);
                if (file == null || !file.getName().endsWith(".py") || ProjectInitializer.hasDocumentListener.containsKey(document)) {
                    //if it's not a .py file, we're not interested in tracking its changes. If it already has a listener, we don't want to add a second one, as that will lead to double logging
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
                    /**
                     * What is done if the user presses the start on the notification
                     * @param e the event of clicking start on the notification button
                     * @param notification the notification being clicked on
                     */
                    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                        //when the start button is pressed, remove notification and also its place in the notificationOpen map
                        notification.expire();
                        notificationOpen.remove(document);
                        //this is the same as the trackerLog.java class
                        //The file ends with .py and has no listener, so we are going to add a listener to it
                        String path = file.getCanonicalPath();
                        String logFilePath = path.substring(0, path.length()-2) + "csv";
                        //get the path for the currently viewing tab, and then feed that, along with the original file into the diff generator to see if there's any discrepancy, and correct the log accordingly
                        diffGenerator.updateLog(logFilePath, document.getText());
                        //add a document listener to the currently active tab
                        DocumentListenerImpl documentListener = new DocumentListenerImpl(path);
                        document.addDocumentListener(documentListener);
                        //Since we just added a listener, it should be included in the map hasDocumentListener
                        ProjectInitializer.hasDocumentListener.put(document, documentListener);
                    }
                });
                notification.notify(editorFactoryEvent.getEditor().getProject());
            }
            @Override
            /**
             * Is evoked after a tab is closed.
             */
            public void editorReleased(@NotNull EditorFactoryEvent editorFactoryEvent) {
                //When a tab is closed, we want to remove its documentListener as well as remove it from hasDocumentListener
                Document TabClosed = editorFactoryEvent.getEditor().getDocument();
                if (hasDocumentListener.containsKey(TabClosed)){
                    TabClosed.removeDocumentListener(hasDocumentListener.get(TabClosed));
                    hasDocumentListener.remove(TabClosed);
                }
                if (notificationOpen.containsKey(TabClosed)){
                    //if there's a notification open, when the tab is closed, it should be removed
                    ProjectInitializer.notificationOpen.get(TabClosed).expire();
                    ProjectInitializer.notificationOpen.remove(TabClosed);
                }
            }
        }, () -> {

        });

    }
}
