import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;


import java.io.IOException;
//documentation: https://github.com/JetBrains/intellij-community/blob/4ea2f6de15ff5c57a0664477dd673c3748d3c5cb/platform/core-api/src/com/intellij/openapi/editor/event/DocumentListener.java)
//helper class to construct a DocumentListener for logging actions
public class DocumentListenerImpl implements DocumentListener {
    private String path;
    public DocumentListenerImpl(String path){
        this.path = path;
    }
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
}
