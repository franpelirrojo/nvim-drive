package dev.franpelirrojo.nvimdrive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

/**
 * GoogleService
 */
public class GoogleService {
    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.dir") + "/tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String APPLICATION_NAME = "nvim-drive";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    // If modifying these scopes, delete your previously saved tokens/ folder.
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    public GoogleService() {
    }

    public Drive getDriveService() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        return service;
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = Main.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        /*
         * El código a aprtir de aquí es un añadido para
         * que la autorización se muestre en nvim
         */
        final int[] windowid = new int[1];
        final boolean[] windowopen = new boolean[] { false };

        AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow, receiver) {
            @Override
            protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) {
                windowid[0] = Nvim.getInstance().showInFloating(null,
                        "Autorización Google Drive",
                        false,
                        "Por favor, autorice la aplicación visitando la siguiente URL: ",
                        authorizationUrl.build());
                windowopen[0] = true;
            }
        };

        Credential credential = app.authorize("user");
        // Esto cierra la ventana emergente en el editor y muestra
        // un mensaje temporal con el estado de la utenticación
        if (windowopen[0] && windowid[0] != -1) {
            Nvim.getInstance().closeWindow(windowid[0]);
        }

        if (credential != null) {
            Nvim.getInstance().showInFloating("good",
                "Exito en la autenticación",
                true,
                "La autenticación se ha completado con exito.",
                "Conectado a Drive.");
        } else {
            Nvim.getInstance().showInFloating("bad",
                "Fracaso en la autenticación",
                true,
                "La autenticación ha fracasado.",
                "No se pudo conectar a Drive");
        }

        return credential;
    }
}
