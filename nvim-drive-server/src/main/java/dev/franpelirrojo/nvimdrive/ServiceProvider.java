package dev.franpelirrojo.nvimdrive;

import java.util.List;

import com.google.api.services.drive.model.File;

public abstract class ServiceProvider {
    public final void createFile(String name, String content) {
        String fileId = sendFile(name, content);
        Nvim.getInstance().sendIdNewBuffer(fileId);
    }

    abstract List<File> getFiles();
    abstract String sendFile(String name, String content);
    abstract byte[] getFileContent(String fileId);
    abstract void updateContent(String fileId, String content); 
    abstract void deleteFile(String fileId);
}
