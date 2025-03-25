package dev.franpelirrojo.nvimdrive;

import com.google.api.services.drive.model.File;

public class Utils {
    public static boolean isShort(File file) {
        return file.getMimeType().equals("application/vnd.google-apps.shortcut");
    }

    public static boolean isFolder(File file) {
        String mime = file.getMimeType();

        if (mime.equals("application/vnd.google-apps.shortcut")) {
            mime = file.getShortcutDetails().getTargetMimeType();
        }

        if (mime == null) {
            return false;
        }

        return mime.equals("application/vnd.google-apps.folder");
    }
}
