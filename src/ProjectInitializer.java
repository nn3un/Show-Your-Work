import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ProjectInitializer implements ProjectComponent {
    public static HashMap<Document, DocumentListener> hasDocumentListener;
    /**
     * This would be invoked when a new project is started
     */
    public void initComponent(){
        //I couldn't think of anything else but making the variable static which means there will be 1 hasDocuementListener for multiple open projects, so we have to act accordingly
        if(hasDocumentListener==null) {
            hasDocumentListener = new HashMap<Document, DocumentListener>();
        }
        //This class helps with tracking the editors in the project
        EditorFactory editorFactory = EditorFactory.getInstance();
        editorFactory.addEditorFactoryListener(new EditorFactoryListener() {
            @Override
            /**
             * Is evoked before a new editor tab is opened
             */
            public void editorCreated(@NotNull EditorFactoryEvent editorFactoryEvent) {
            }
            @Override
            /**
             * Is evoked after a tab is closed.
             */
            public void editorReleased(@NotNull EditorFactoryEvent editorFactoryEvent) {
                //When a tab is closed, we want to remove its documentListener as well as remove it from the map hasDocumentListener
                Document TabClosed = editorFactoryEvent.getEditor().getDocument();
                if (hasDocumentListener.containsKey(TabClosed)){
                    TabClosed.removeDocumentListener(hasDocumentListener.get(TabClosed));
                    hasDocumentListener.remove(TabClosed);
                }
            }
        }, new Disposable() {
            @Override
            //Todo Figure out what this should do
            public void dispose() {

            }
        });

    }
}
