import com.intellij.execution.dashboard.actions.RunAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.ide.CopyPasteManager;


import java.awt.datatransfer.DataFlavor;
import java.io.IOException;

public class CopyPasteListener implements AnActionListener {
    private Editor editor;
    private String CSVFilePath;
    private int oldOffset;
    CopyPasteListener(Editor editor, String CSVFilePath) {
        this.editor = editor;
        this.CSVFilePath = CSVFilePath;
        this.oldOffset = 0;
    }
    @Override
    public void beforeActionPerformed(AnAction anAction, DataContext dataContext, AnActionEvent anActionEvent) {
        if(editor.getSelectionModel().getSelectedText() != null) {
            //For paste you might have the case where a text range is selected and then paste happens, in that case we need to know the beginning of the selected text
            oldOffset = editor.getSelectionModel().getSelectionStart();
        }
        else{
            //If not then just knowing the caret position will tell us where the paste happened
            oldOffset = editor.getCaretModel().getOffset();
        }
    }

    @Override
    public void afterActionPerformed(AnAction anAction, DataContext dataContext, AnActionEvent event) {
        if (anAction instanceof com.intellij.openapi.editor.actions.CutAction || anAction instanceof com.intellij.ide.actions.CutAction || anAction instanceof com.intellij.openapi.editor.actions.CopyAction || anAction instanceof com.intellij.ide.actions.CopyAction){
            //If we have a copy or cut action we must add this as a copy and let the offset be the start of the current selection, and the text being copied to clipboard be the selected text
            try {
                String copiedString = CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);
                CSVFileWriter.appendToCsv(CSVFilePath, System.currentTimeMillis(), "copy", editor.getSelectionModel().getSelectionStart(), copiedString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (anAction instanceof com.intellij.openapi.editor.actions.PasteAction || anAction instanceof com.intellij.ide.actions.PasteAction){
            //If we have a paste action we must add this as a paste and let the offset be the start of the current caret position, and the text being pasted be the substring of the document positioned by the oldOffset/the caret position before the paste and the newOffset/the caret position after the paste
            try {
                String pastedString = CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor);
                CSVFileWriter.appendToCsv(CSVFilePath, System.currentTimeMillis(), "paste", oldOffset, pastedString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
