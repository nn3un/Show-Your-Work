
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jetbrains.annotations.NotNull;

public class TrackerLog extends AnAction {
    String file_content = "";
    String file_name = "";

    public TrackerLog() {
    }

    public void app(String path, String type, int offset, String Content) throws IOException {
        String filename = path.replace(".java", ".csv");
        FileWriter fw = null;
        BufferedWriter bw = null;
        String content = Content.replace(',', '`');
        String data = type + "," + offset + "," + content + ",\n";
        File file = new File(filename);
        if (!file.exists()) {
            file.createNewFile();
        }

        fw = new FileWriter(file.getAbsoluteFile(), true);
        bw = new BufferedWriter(fw);
        bw.write(data);
        bw.close();
    }

    public void actionPerformed(@NotNull final AnActionEvent anActionEvent) {
        Editor editor = (Editor)anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        Document d = editor.getDocument();
        this.file_content = d.getText();
        this.file_name = ((VirtualFile)anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE)).getName();
        d.addDocumentListener(new DocumentListener() {
            public void beforeDocumentChange(DocumentEvent event) {
                int offset = event.getOffset();
                String path = ((VirtualFile)anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE)).getCanonicalPath();
                System.out.println(path);
                (new StringBuilder()).append("").append(event.getNewFragment()).toString();
                int oldSize = event.getOldLength();
                int newSize = event.getNewLength();
                String changed_content;
                String type;
                if (!("" + event.getNewFragment()).equals("")) {
                    type = "add";
                    changed_content = "" + event.getNewFragment();

                    try {
                        TrackerLog.this.app(path, type, offset, changed_content);
                    } catch (IOException var10) {
                        ;
                    }
                }

                if (!("" + event.getOldFragment()).equals("")) {
                    type = "sub";
                    changed_content = "" + event.getOldFragment();

                    try {
                        TrackerLog.this.app(path, type, offset, changed_content);
                    } catch (IOException var9) {
                        ;
                    }
                }

            }
        });
    }

    public void update(AnActionEvent e) {
        Project project = (Project)e.getData(CommonDataKeys.PROJECT);
        Editor editor = (Editor)e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setVisible(project != null && editor != null);
    }
}
