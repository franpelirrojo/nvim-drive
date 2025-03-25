package dev.franpelirrojo.nvimdrive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

public class DriveService extends ServiceProvider{
    private static DriveService INSTANCE;
    private Drive service;
    private static final String FILEQ = "nextPageToken," +
            "files(id, name, ownedByMe, parents, mimeType, shortcutDetails)";

    private List<File> files;

    private DriveService() {
        try {
            service = new GoogleService().getDriveService();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DriveService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DriveService();
            new Thread(() -> INSTANCE.getFiles()).start();
        }

        return INSTANCE;
    }

    @Override
    public List<File> getFiles() {
        if (files != null) {
            return files;
        }

        String pageToken = null;
        files = new LinkedList<>();

        try {
            do {
                FileList result;
                result = service.files().list()
                        .setQ("trashed = false and mimeType = 'application/vnd.google-apps.document'")
                        .setCorpora("user")
                        .setFields(FILEQ)
                        .setPageToken(pageToken)
                        .setPageSize(250)
                        .execute();
                pageToken = result.getNextPageToken();
                files.addAll(result.getFiles());
            } while (pageToken != null);
        } catch (IOException e) {
            System.err.println("Error en la recepción de la respuesta de Google Drive. Para los ficheros");
            e.printStackTrace();
        }

        return files;
    }
    
    @Override
    public String sendFile(String name, String content){
        File newfile = null;
        File filemetadata = new File();
        filemetadata.setName(name);
        filemetadata.setMimeType("application/vnd.google-apps.document");
        try {
            newfile = service.files().create(filemetadata, new ByteArrayContent("text/markdown", content.getBytes(StandardCharsets.UTF_8)))
                .setFields("id")
                .set("uploadType", "media").execute();
        } catch (IOException e) {
            e.printStackTrace();
        } 

        return newfile.getId();
    }

    @Override
    public byte[] getFileContent(String fileId) {
        byte[] content = null;
        
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            service.files().export(fileId, "text/markdown").executeMediaAndDownloadTo(out);

            content = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        };
            
        return content;
    }

    @Override
    public void updateContent(String fileId, String content) {
        ByteArrayContent byteBuffer = new ByteArrayContent("text/markdown", content.getBytes(StandardCharsets.UTF_8));
        try {
            service.files().update(fileId, new File(), byteBuffer).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFile(String fileId) {
        try {
            service.files().delete(fileId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
