import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.jupiter.api.Test;

public class DriveTest {
   
    @Test
    public void conexionRpcNvim() throws IOException{
        ProcessBuilder processBuilder = new ProcessBuilder("nvim", "--headless");
        Process process = processBuilder.start();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            writer.write(":Javado");
            writer.write(":q\n");  // Salimos de nvim para que no quede colgado
            writer.flush();
        }

        
    }
}
