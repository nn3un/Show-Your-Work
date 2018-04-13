import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.actions.DeleteAction;
import com.intellij.ide.actions.PasteAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import gherkin.lexer.Id;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Objects;

import static com.intellij.openapi.util.Disposer.newDisposable;

public class TrackerLog extends AnAction {

    //this is Zach's old code, so it probably needs updating
    public void app(String filename, String type, int offset, String Content) throws IOException{
        FileWriter fw = null;
        BufferedWriter bw = null;
        // TODO: 4/6/2018 add timestamp
        String content = Content.replace(',', '`');
        String data = type + "," + offset + "," + content + "," + "\n";

        File file = new File(filename);

        if (!file.exists()) {
            file.createNewFile();
        }

        fw = new FileWriter(file.getAbsoluteFile(), true);
        bw = new BufferedWriter(fw);
        bw.write(data);
        bw.close();
        fw.close();
    }
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        ActionManager a = ActionManager.getInstance();
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);


        Document d = editor.getDocument();

        //this class allows us to track document events
        d.addDocumentListener(new DocumentListener() {
            @Override
            public void beforeDocumentChange(DocumentEvent event) {
                int offset = event.getOffset();
                String content;
                String type;
                String path = "filename.csv";
                content = "" + event.getNewFragment();
                int oldSize = event.getOldLength();
                int newSize = event.getNewLength();
                if (!(("" + event.getNewFragment()).equals(""))) {
                    type = "add";
                    content = "" + event.getNewFragment() + ",";
                    try {
                        app(path, type, offset, content);
                    } catch (IOException e) {
                        // TODO: 4/6/2018 better error handling
                    }
                }
                if (!(("" + event.getOldFragment()).equals(""))) {
                    type = "sub";
                    content = "" + event.getOldFragment() + ",";
                    try {
                        app(path, type, offset, content);
                    } catch (IOException e) {
                        // TODO: 4/6/2018 better error handling
                    }
                }
            }
        });

    }

//This is the old approach. In case someone needs it

    /*      //tracks editor events such as copy paste delete
            CaretModel caretModel = editor.getCaretModel();
            a.addAnActionListener(new AnActionListener.Adapter() {
                int offset;
                String content;
                String type;
                String path = "C:/Users/nnuzaba47/eclipse-Workspace/Practice/Documentstest.csv";
                @Override
                public void beforeActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
                    offset = caretModel.getOffset();
                    try {
                        if (editor.getSelectionModel().hasSelection()) {
                            Document d = editor.getDocument();
                            if (a.getId(action).equals("$Delete") || a.getId(action).equals("$Cut") || a.getId(action).equals("EditorBackSpace") || a.getId(action).equals("EditorCut")){
                                SelectionModel sm = editor.getSelectionModel();
                                int start = sm.getSelectionStart();
                                int end = sm.getSelectionEnd();
                                TextRange tr = new TextRange(start, end);
                                content = d.getText(tr);
                                type = "sub";
                                offset = start;
                                app(path, type, offset, content);
                            }
                        }

                        else if (a.getId(action).equals("EditorBackSpace"))
                        {
                            Document d = editor.getDocument();
                            TextRange tr = new TextRange(offset-1, offset);
                            content = d.getText(tr);
                            type = "sub";
                            offset = caretModel.getOffset();
                            app(path, type, offset, content);
                        }
                    }
                    catch (FileNotFoundException e){

                    }
                    catch (IOException e){
                    }
                }

                @Override
                public void afterActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {

                     if (a.getId(action).equals("$Paste")|| a.getId(action).equals("EditorPaste")){
                         int nextOffset = caretModel.getOffset();
                         int start = offset;
                         int end = nextOffset;
                         Document d = editor.getDocument();
                         TextRange tr = new TextRange(start, end);
                         content = d.getText(tr);
                         type = "add";
                         try {
                             app(path, type, offset, content);
                         }
                         catch (Exception e){

                         }
                     }

                }
            });

            //Tracks keyboard and mouse event
            IdeEventQueue b = IdeEventQueue.getInstance();
            b.addPostprocessor(new IdeEventQueue.EventDispatcher() {
                @Override
                public boolean dispatch(@NotNull AWTEvent awtEvent) {
                    String path = "C:/Users/nnuzaba47/eclipse-Workspace/Practice/Documentstest.csv";
                    if (awtEvent.getID() == KeyEvent.KEY_PRESSED) {
                        try {
                            if (!((KeyEvent)awtEvent).isActionKey() && ((KeyEvent)awtEvent).getKeyCode() != KeyEvent.VK_BACK_SPACE){
                                final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
                                String content = ""+((KeyEvent) awtEvent).getKeyChar();
                                String type = "add";
                                CaretModel caretModel = editor.getCaretModel();
                                int offset = caretModel.getOffset();
                                app(path, type, offset, content);
                            }
                        }
                        catch (Exception e){

                        }

                    }
                    return false;
                }
            }, newDisposable());
        }

        @Override
    */
    public void update(final AnActionEvent e) {

        //Get required data keys

        final Project project = e.getData(CommonDataKeys.PROJECT);

        final Editor editor = e.getData(CommonDataKeys.EDITOR);

        //Set visibility only in case of existing project and editor and if some text in the editor is selected

        e.getPresentation().setVisible((project != null && editor != null));


    }
}

