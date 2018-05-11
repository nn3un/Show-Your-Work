

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GenerateZip extends AnAction {
    public GenerateZip() {
    }

    public void actionPerformed(AnActionEvent e) {
        String path = ((VirtualFile)e.getData(PlatformDataKeys.VIRTUAL_FILE)).getCanonicalPath();
        String fileName = ((VirtualFile)e.getData(PlatformDataKeys.VIRTUAL_FILE)).getName();

        try {
            int BUFFER = 2048;
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(path.replace(".java", "_log.zip"));
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte[] data_byte = new byte[BUFFER];
            FileInputStream fi = new FileInputStream(path.replace(".java", ".csv"));
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry csv_entry = new ZipEntry(fileName.replace(".java", ".csv"));
            out.putNextEntry(csv_entry);

            int count;
            while((count = origin.read(data_byte, 0, BUFFER)) != -1) {
                out.write(data_byte, 0, count);
            }

            byte[] data_byte_file = new byte[BUFFER];
            fi = new FileInputStream(new File(path));
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry document_entry = new ZipEntry(fileName);
            out.putNextEntry(document_entry);

            while((count = origin.read(data_byte_file, 0, BUFFER)) != -1) {
                out.write(data_byte_file, 0, count);
            }

            origin.close();
            out.close();
        } catch (Exception var14) {
            var14.printStackTrace();
        }

    }

    public void update(AnActionEvent e) {
        Project project = (Project)e.getData(CommonDataKeys.PROJECT);
        Editor editor = (Editor)e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setVisible(project != null && editor != null);
    }
}
