import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;

//This is the class that creates a group for the actions in the VCS tab under the name -"Show Your Work Plugin".
public class DocumentActionLogger extends DefaultActionGroup {
    /**
     * Decides the visibility for the button "Show Your Work Plugin Tools"
     * @param event
     */
    public void update(AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        //Should only be visible if a editor is open
        event.getPresentation().setVisible(editor != null);
        event.getPresentation().setEnabled(editor != null);
        event.getPresentation().setIcon(AllIcons.General.Error);
    }
}
