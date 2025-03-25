package dev.franpelirrojo.nvimdrive;

public class Main {
    public static void main(String... args) {
        if (args[0].equals("-d")) {
            SocketRPC.getSocket("/run/user/1000//nvim." + args[1] + ".0");
        } else {
            SocketRPC.getSocket(args[0]);
        }

        DriveService.getInstance();
        new ServerRPC().start();
    }
}
