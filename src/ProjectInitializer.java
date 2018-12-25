import com.intellij.notification.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ProjectInitializer implements ProjectComponent {
    static HashMap<Document, DocumentListener> hasDocumentListener;
    static HashMap<Document, Notification> notificationOpen;
    static HashMap<Document, CopyPasteListener> hasCopyPasteListener;
    private Logger logger = LogManager.getLogger("ProjectInitializer");
    /**
     * This would be invoked when a new project is started
     */

    public void initComponent(){
        //Todo come up with better way to prevent multiple document listener for one file
        //I couldn't think of anything else but making the variable static which means there can be 1 hasDocuementListener for multiple open projects, so we have to act accordingly
        hasDocumentListener = hasDocumentListener == null? new HashMap<>() : hasDocumentListener;
        notificationOpen = notificationOpen == null? new HashMap<>() : notificationOpen;
        hasCopyPasteListener = hasCopyPasteListener == null? new HashMap<>() : hasCopyPasteListener;

        //Updating the status bar whenever editor tab is changed with the help of a listener
        addEditorTabChangedListener();

        //Add listener to editor factory, so we know when to create new notifications, delete old ones as editors are created and closed
        addEditorFactoryListener();
    }


    /**
     * Adds listener to editorFactory for notifications
     */
    private void addEditorFactoryListener(){
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
                if (file == null || !file.getName().endsWith(".py") || ProjectInitializer.notificationOpen.containsKey(document) ||ProjectInitializer.hasDocumentListener.containsKey(document)) {
                    //if it's not a .py file, we're not interested in tracking its changes. If it already has a listener or a notification, we don't want to add a second one, as that will lead to double logging
                    logger.info("The correct file doesn't exist, or it's somehow already in one of the maps");
                    return;
                }

                //Create the notification
                addNotification(editor);
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
                //ActionManager.getInstance().removeAnActionListener(hasCopyPasteListener.get(documentClosed));
                hasCopyPasteListener.remove(documentClosed);
            }

        }, () -> {

        });
    }

    /**
     * adds Notification for the newest editor created
     * @param editor the new editor
     */
    private void addNotification(Editor editor){
        Document document = editor.getDocument();
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        assert file!=null;
        NotificationGroup showYourWork = new NotificationGroup("Show Your Work", NotificationDisplayType.STICKY_BALLOON, false);
        Notification notification = showYourWork.createNotification("Start Logging Edits for file " + file.getName() + "?", NotificationType.INFORMATION);
        notificationOpen.put(document, notification);
        notification.addAction(new NotificationAction("Start") {
            @Override
            //The action that happens when the user presses the 'Start' button on the notification, essentially the same thing the TrackerLog class does
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                //when the start button is pressed, remove notification and also its place in the notificationOpen map
                notification.expire();
                notificationOpen.remove(document);
                initializeListeners(editor);
            }
        });
        notification.notify(editor.getProject());
    }

    /**
     * Helpter method that adds listener to assist with status bar updates
     */
    private void addEditorTabChangedListener(){
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        MessageBusConnection connection = bus.connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                if(event.getNewFile() == null){
                    return;
                }
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(ProjectManager.getInstance().getOpenProjects()[0]);
                if(statusBar == null){
                    return;
                }
                //First remove all the other widgets
                statusBar.removeWidget("Show-your-work");
                if(!event.getNewFile().getName().endsWith(".py")){
                    //If it's not a .py file then move on
                    return;
                }
                MyStatusBarWidget myStatusBarWidget = new MyStatusBarWidget();
                myStatusBarWidget.setText("PLUGIN NOT RUNNING");
                for (Document document: hasDocumentListener.keySet()){
                    //Check if the document already has a listener
                    if (event.getNewFile().equals(FileDocumentManager.getInstance().getFile(document))){
                        //if it does then change the text and move on.
                        myStatusBarWidget.setText("Document Tracking: ON");
                        break;
                    }
                }
                statusBar.addWidget(myStatusBarWidget, "before Position");
                statusBar.updateWidget("Show-your-work");
            }
        });
    }

    /**
     * Helper method to initialize listeners for CSV file
     * @param editor the editor that needs tracking
     */
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
        ProjectInitializer.hasDocumentListener.put(document, documentListener);


        String CCPFilePath = originalPath.substring(0, originalPath.length()-2)+ "csv";
        //Add the listener that will log copy-paste
        CopyPasteListener copyPasteListener = new CopyPasteListener(editor, CCPFilePath);
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        MessageBusConnection connection = bus.connect();
        connection.subscribe(AnActionListener.TOPIC, copyPasteListener);
        //Add it to the hasCopyPasteListener map
        ProjectInitializer.hasCopyPasteListener.put(document, copyPasteListener);

        //Changing the widget text
        IdeFrame frame = WindowManager.getInstance().getIdeFrame(editor.getProject());
        StatusBar statusBar = frame.getStatusBar();
        //First remove all the other widgets
        statusBar.removeWidget("Show-your-work");
        MyStatusBarWidget myStatusBarWidget = new MyStatusBarWidget();
        myStatusBarWidget.setText("Document Tracking: ON");
        statusBar.addWidget(myStatusBarWidget, "before Position");
        statusBar.updateWidget("Show-your-work");
    }
}
