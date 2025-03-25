package dev.franpelirrojo.nvimdrive;

import java.io.IOException;
import java.net.BindException;
import java.net.SocketAddress;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketRPC {
    private static SocketRPC instance;
    private SocketChannel socketChannel;
    private static int msgid = 1;
    private static int premsgid = 0;

    private SocketRPC(String unixSocket) {
        SocketAddress address = UnixDomainSocketAddress.of(unixSocket);
        System.out.println("Conexión RPC al socket Unix: " + unixSocket);
        try {
            socketChannel = SocketChannel.open(address);
        } catch (BindException e) { // TODO: Esto no tiene sentido
            Nvim.getInstance().showInFloating("bad", "Error en el socket", true, "El SocketUnix está en uso.", "No se pudo conectar.");
        } catch (IOException e) {
            Nvim.getInstance().showInFloating("bad", "Error en el socket", true, "No se puedo completar la conexión al socket unix: " + unixSocket);
        }
    }
 
    /**
     * Singleton para inicializar el socket.
     */
    public static SocketRPC getSocket(String unixSocket) {
        if (instance == null) {
            instance = new SocketRPC(unixSocket);
        }

        return instance;
    }

    /**
     * Devuelve la instancia si se ha inicializado el socket, null si no.
     */
    public static SocketRPC getSocket() {
        if (instance == null) {
            return null;
        }

        return instance;
    }

    /**
     * Devuelve los bytes recividos por el socket en un buffer listo para lectura
     */
    public ByteBuffer recive() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        int bytesRead;

        bytesRead = socketChannel.read(buffer);

        while (bytesRead > 0 && buffer.remaining() == 0) {
            ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;

            bytesRead = socketChannel.read(buffer);
        }

        buffer.flip();
        return buffer;
    }

    public void send(byte[] requestBytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(requestBytes);
        socketChannel.write(buffer);
    }

    public int newmsgid() {
        premsgid = msgid;
        return msgid++;
    }

    public int peekmsgid() {
        return premsgid;
    }
}
