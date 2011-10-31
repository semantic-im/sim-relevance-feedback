package rfjava;

import java.io.File;
import javax.swing.filechooser.*;

public class FileFilterExt extends FileFilter {

    String extensionValue;

    public FileFilterExt(String extValue)
    {
        extensionValue=extValue;
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
     }

    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals(extensionValue)) {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return extensionValue.toUpperCase()+" Files (*."+extensionValue.toLowerCase()+")";
    }
}