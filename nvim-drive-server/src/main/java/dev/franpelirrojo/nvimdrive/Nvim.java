package dev.franpelirrojo.nvimdrive;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ImmutableArrayValue;
import org.msgpack.value.ImmutableValue;

public class Nvim {
    private static final Nvim INSTANCE = new Nvim();
    private SocketRPC socket = SocketRPC.getSocket();
    private static MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
    
    private Nvim() {}

    public static Nvim getInstance() {
        return INSTANCE;
    }

    private void executeLua(int type) throws IOException {
        if (type != 2) {
            packer.packArrayHeader(4);
        } else  {
            packer.packArrayHeader(3);
        }

        packer.packInt(type);

        if (type != 2) {
            packer.packInt(socket.newmsgid());
        }
        packer.packString("nvim_exec_lua");
    }

    public int showInFloating(String eventType, String title, boolean temporal ,String... lineas) {
        int idfloating = -1;
        try {
            packer.clear();
            executeLua(0);
            packer.packArrayHeader(2);
            packer.packString("return require\"nvimdrive.menus\".show_event(...)");
            packer.packArrayHeader(4);
            if (eventType == null) {
                packer.packNil();
            } else {
                packer.packString(eventType);
            }
            packer.packString(title);
            packer.packArrayHeader(lineas.length);
            for (int i = 0; i < lineas.length; i++) {
                packer.packString(lineas[i]);
            }
            packer.packBoolean(temporal);

            byte[] responseBytes = packer.toByteArray();
            socket.send(responseBytes);

            ByteBuffer msgBytes = socket.recive();
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(msgBytes);
            ImmutableValue msg = unpacker.unpackValue();

            if (msg.getValueType().isArrayType()) {
                ImmutableArrayValue array = msg.asArrayValue();
                int type = array.get(0).asIntegerValue().asInt();
                int msgid = array.get(1).asIntegerValue().asInt();
                if (type == 1 && msgid == socket.peekmsgid()) {
                    if (array.get(2).isNilValue()) {
                        idfloating = array.get(3).asIntegerValue().asInt();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al enviar a nvim el método showInFloating");
        }

        return idfloating;
    }

    public void sendIdNewBuffer(String fileId) {
        try {
            packer.clear();
            executeLua(2);
            packer.packArrayHeader(2);
            packer.packString("require\"nvimdrive.buffers\".set_current_id(...)");
            packer.packArrayHeader(1);
            if (fileId == null) {
                packer.packNil();
            } else {
                packer.packString(fileId);
            }

            byte[] responseBytes = packer.toByteArray();
            socket.send(responseBytes);
        } catch (IOException e) {
            System.err.println("Error al enviar a nvim el método set_current_id.");
        }
    }

    public void closeWindow(int windowid) {
        try {
            packer.clear();
            packer.packArrayHeader(3);
            packer.packInt(2);
            packer.packString("nvim_win_close");
            packer.packArrayHeader(2);
            packer.packInt(windowid);
            packer.packBoolean(false);

            byte[] responseBytes = packer.toByteArray();
            socket.send(responseBytes);

        } catch (IOException e) {
            System.err.println("Error al enviar a nvim el método showInFloating");
        }
    }
}
