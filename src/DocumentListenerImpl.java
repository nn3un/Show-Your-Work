import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import org.jetbrains.annotations.NotNull;


import java.io.IOException;
//helper class to construct a DocumentListener for logging actions
public class DocumentListenerImpl implements DocumentListener {
    private String path;
    DocumentListenerImpl(String path){
        this.path = path;
    }
    public void documentChanged(@NotNull DocumentEvent event){
        if (!("" + event.getNewFragment()).equals("")) {
            //if a non-empty string is attached to the document that means an add action happened, so the necessary information is passed to the CSVfilewriter
            //this code is included in the documentChanged portion, since we need the new offset position.
            try {
                CSVFileWriter.appendToCsv(path.substring(0, path.length()-2)+"csv", System.currentTimeMillis(), "add", event.getOffset(), "" + event.getNewFragment());
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
        if (!("" + event.getOldFragment()).equals("")) {
            //if a non-empty string is deleted from the  document that means a sub action happened, so the necessary information is passed to the CSVfilewriter
            //this code is included in the beforeDocumentChange portion, since we need the old offset position.
            try {
                CSVFileWriter.appendToCsv(path.substring(0, path.length()-2)+"csv", System.currentTimeMillis(), "sub", event.getOffset(), "" + event.getOldFragment());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
