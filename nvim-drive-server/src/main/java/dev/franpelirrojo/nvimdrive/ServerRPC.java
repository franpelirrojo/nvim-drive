package dev.franpelirrojo.nvimdrive;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import com.google.api.services.drive.model.File;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.ImmutableArrayValue;
import org.msgpack.value.ImmutableValue;
import org.msgpack.value.Value;

public class ServerRPC {
    private class UnpackMessage {
        private int type;
        private int msgid;
        private String metodo;
        private List<Value> opts;
    }

    private final UnpackMessage unpackmsg = new UnpackMessage();
    private final MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
    private final SocketRPC cliente = SocketRPC.getSocket();
    private final ServiceProvider serviceprovider = DriveService.getInstance();
    private boolean end = false;
    private byte[] responseBytes;
    private ArrayValue arrayValue;

    public ServerRPC() {
    }

    public void start() {
        try {
            ByteBuffer msgBytes;
            MessageUnpacker unpacker;
            while (!end) {
                msgBytes = cliente.recive();
                unpacker = MessagePack.newDefaultUnpacker(msgBytes);
                ImmutableValue msg = unpacker.unpackValue();
                unpackMessage(msg);
                handle();
                unpacker.close();
            }

            packer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle() throws IOException {
        switch (unpackmsg.metodo) {
            case "get_files":
                List<File> archivos = serviceprovider.getFiles();
                packer.packArrayHeader(4);
                packer.packInt(1);
                packer.packInt(unpackmsg.msgid);
                packer.packNil();
                packer.packArrayHeader(archivos.size());
                for (int i = 0; i < archivos.size(); i++) {
                    packer.packMapHeader(3);
                    packer.packString("id");
                    packer.packString(archivos.get(i).getId());
                    packer.packString("name");
                    packer.packString(archivos.get(i).getName());
                    packer.packString("mime");
                    packer.packString(archivos.get(i).getMimeType());
                }

                responseBytes = packer.toByteArray();
                cliente.send(responseBytes);
                break;
            case "file_content":
                packer.packArrayHeader(4);
                packer.packInt(1);
                packer.packInt(unpackmsg.msgid);
                packer.packNil();
                byte[] filecontent = serviceprovider.getFileContent(unpackmsg.opts.getFirst().asStringValue().toString());
                packer.packBinaryHeader(filecontent.length);
                packer.writePayload(filecontent);

                responseBytes = packer.toByteArray();
                cliente.send(responseBytes);
                break;
            case "update_content":
                arrayValue = unpackmsg.opts.getFirst().asArrayValue();
                serviceprovider.updateContent(
                        arrayValue.get(0).asStringValue().toString(),
                        arrayValue.get(1).asStringValue().toString());
                break;
            case "create_file":
                arrayValue = unpackmsg.opts.getFirst().asArrayValue();
                serviceprovider.createFile(
                        arrayValue.get(0).asStringValue().toString(),
                        arrayValue.get(1).asStringValue().toString());
                break;
            case "delete_file":
                arrayValue = unpackmsg.opts.getFirst().asArrayValue();
                serviceprovider.deleteFile(
                        arrayValue.get(0).asStringValue().toString());
                break;
            case "nvim_error_event":
                for (int i = 0; i < unpackmsg.opts.size(); i++) {
                    Nvim.getInstance().showInFloating("bad", "Error", true, unpackmsg.opts.get(i).toString());
                }
                break;
            case "stop":
                end = true;
                break;
            default:
        }

        packer.clear();
    }

    private UnpackMessage unpackMessage(ImmutableValue msg) {
        if (msg.getValueType().isArrayType()) {
            ImmutableArrayValue array = msg.asArrayValue();
            unpackmsg.type = array.get(0).asIntegerValue().asInt();
            if (unpackmsg.type == 2) {
                unpackmsg.metodo = array.get(1).asStringValue().toString();
                unpackmsg.opts = array.get(2).asArrayValue().list();
            } else {
                unpackmsg.msgid = array.get(1).asIntegerValue().asInt();
                unpackmsg.metodo = array.get(2).asStringValue().asString();
                unpackmsg.opts = array.get(3).asArrayValue().list();
            }
        }

        return unpackmsg;
    }
}
