package dev.franpelirrojo.nvimdrive.filesystem;

public class Menu {
    FileNode current;

    public Menu(FileNode root) {
        current = root;
    }

    public String show(){
        return "..\n" + current.toString();
    }

    public void next(int selection) {
        FileNode child = (FileNode) current.leftmostChild();
        for (int i = 1; i < selection; i++) {
            FileNode sibiling = (FileNode) child.rightSibling();
            if (sibiling != null) {
                child = sibiling; 
            }
        } 

        current = child;
    }
}
