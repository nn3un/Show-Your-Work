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
import com.intellij.openapi.fileEditor.FileDocumentManager;
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
import java.nio.file.Paths;
import java.util.Objects;
import java.util.zip.*;

import static com.intellij.openapi.util.Disposer.newDisposable;

public class TrackerLog extends AnAction {

    String file_content = "";
    String file_name = "";

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

        // The following try/catch archives the log.csv and the edited document into a log.zip file
        try {
            int BUFFER = 2048;
            BufferedInputStream origin = null;
            FileOutputStream dest = new
                    FileOutputStream(System.getProperty("user.dir") + "/log.zip");
            ZipOutputStream out = new ZipOutputStream(new
                    BufferedOutputStream(dest));

            byte data_byte[] = new byte[BUFFER];

            FileInputStream fi = new
                    FileInputStream(file);

            origin = new
                    BufferedInputStream(fi, BUFFER);

            ZipEntry csv_entry = new ZipEntry(file.getName());
            out.putNextEntry(csv_entry);

            int count;
            while((count = origin.read(data_byte, 0,
                    BUFFER)) != -1) {
                out.write(data_byte, 0, count);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file_name));
            writer.write(file_content);
            writer.close();

            byte data_byte_file[] = new byte[BUFFER];

            fi = new
                    FileInputStream(file_name);

            origin = new
                    BufferedInputStream(fi, BUFFER);

            ZipEntry document_entry = new ZipEntry(file_name);
            out.putNextEntry(document_entry);

            while((count = origin.read(data_byte_file, 0,
                    BUFFER)) != -1) {
                out.write(data_byte_file, 0, count);
            }

            origin.close();
            out.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);

        Document d = editor.getDocument();
        file_content = d.getText();
        file_name = anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE).getName();

        //this class allows us to track document events
        d.addDocumentListener(new DocumentListener() {
            @Override
            public void beforeDocumentChange(DocumentEvent event) {
                int offset = event.getOffset();
                String changed_content;
                String type;
                String path = System.getProperty("user.dir") + "/log.csv";
                changed_content = "" + event.getNewFragment();
                int oldSize = event.getOldLength();
                int newSize = event.getNewLength();
                if (!(("" + event.getNewFragment()).equals(""))) {
                    type = "add";
                    changed_content = "" + event.getNewFragment() ;
                    try {
                        app(path, type, offset, changed_content);
                    } catch (IOException e) {
                        // TODO: 4/6/2018 better error handling
                    }
                }
                if (!(("" + event.getOldFragment()).equals(""))) {
                    type = "sub";
                    changed_content = "" + event.getOldFragment() ;
                    try {
                        app(path, type, offset, changed_content);
                    } catch (IOException e) {
                        // TODO: 4/6/2018 better error handling
                    }
                }
            }
        });

    }

    public void update(final AnActionEvent e) {

        //Get required data keys

        final Project project = e.getData(CommonDataKeys.PROJECT);

        final Editor editor = e.getData(CommonDataKeys.EDITOR);

        //Set visibility only in case of existing project and editor and if some text in the editor is selected

        e.getPresentation().setVisible((project != null && editor != null));


    }
}