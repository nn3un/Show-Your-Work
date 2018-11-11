import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.util.TextRange;


import java.io.IOException;

public class CopyPasteListener implements AnActionListener {
    private SelectionModel selectionModel;
    private CaretModel caretModel;
    private String CSVFilePath;
    private int oldOffset;
    private Document document;
    CopyPasteListener(SelectionModel selectionModel, CaretModel caretModel, String CSVFilePath, Document document) {
        this.selectionModel = selectionModel;
        this.caretModel = caretModel;
        this.CSVFilePath = CSVFilePath;
        this.document = document;
        this.oldOffset = 0;
    }
    @Override
    public void beforeActionPerformed(AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent) {
        oldOffset = caretModel.getOffset();
        if (anAction instanceof com.intellij.openapi.editor.actions.CutAction || anAction instanceof com.intellij.ide.actions.CutAction || anAction instanceof com.intellij.openapi.editor.actions.CopyAction || anAction instanceof com.intellij.ide.actions.CopyAction){
            //If we have a copy or cut action we must add this as a copy and let the offset be the start of the current selection, and the text being copied to clipboard be the selected text
            try {
                CSVFileWriter.appendToCsv(CSVFilePath, System.currentTimeMillis(), "copy", selectionModel.getSelectionStart(), selectionModel.getSelectedText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterActionPerformed(AnAction anAction, DataContext dataContext, AnActionEvent event) {
        int newOffset = caretModel.getOffset();
        if (anAction instanceof com.intellij.openapi.editor.actions.PasteAction || anAction instanceof com.intellij.ide.actions.PasteAction){
            //If we have a paste action we must add this as a paste and let the offset be the start of the current caret position, and the text being pasted be the substring of the document positioned by the oldOffset/the caret position before the paste and the newOffset/the caret position after the paste
            try {
                CSVFileWriter.appendToCsv(CSVFilePath, System.currentTimeMillis(), "paste", oldOffset, document.getText(TextRange.create(oldOffset, newOffset)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
