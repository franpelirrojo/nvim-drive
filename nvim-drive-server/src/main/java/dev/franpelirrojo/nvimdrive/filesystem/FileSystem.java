package dev.franpelirrojo.nvimdrive.filesystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.api.services.drive.model.File;

import dev.franpelirrojo.nvimdrive.Utils;

public class FileSystem {
    FileTree fileTree;
    FileNode miDrive;
    FileNode compartido;

    Map<String, Integer> indexIdFiles = new HashMap<>();
    Map<File, List<File>> parentsMap = new HashMap<>();
    List<File> filesHuerfanos = new ArrayList<>();

    public FileSystem() {
        File root = new File().setName("/").setId("0");
        List<String> parentRoot = new ArrayList<>();
        parentRoot.add(root.getId());
        File miDrive = new File().setName("Mi Unidad").setId("1");
        miDrive.setParents(parentRoot);
        File compartido = new File().setName("Compartido conmigo").setId("2");
        compartido.setParents(parentRoot);

        fileTree = new FileTree(new FileNode(root));
        fileTree.newleftchild(compartido);
        fileTree.newleftchild(miDrive);

        this.miDrive = (FileNode) fileTree.root().leftmostChild();
        this.compartido = (FileNode) fileTree.root().leftmostChild().rightSibling();
    }

    public FileTree makeTree(List<File> files) {
        File file;
        for (int i = 0; i < files.size(); i++) {
            file = files.get(i);
            indexIdFiles.put(file.getId(), i);

            if (Utils.isFolder(file)) {
                parentsMap.computeIfAbsent(file, k -> new ArrayList<>());
            }
        }

        Optional<File> parentFile;
        for (int i = 0; i < files.size(); i++) {
            file = files.get(i);
            parentFile = parentFile(file, files);
            if (parentFile.isPresent()) {
                parentsMap.get(parentFile.get()).add(file);
            } else {
                filesHuerfanos.add(file);
                FileNode newNode = new FileNode(file);
                if (file.getOwnedByMe()) {
                    addNodeToParent(newNode, ((FileNode) fileTree.root()).find(miDrive).get());
                } else {
                    // addNodeToParent(newNode, ((FileNode)
                    // fileTree.root()).find(compartido).get());
                }
            }
        }

        Iterator<Entry<File, List<File>>> pointer = parentsMap.entrySet().iterator();
        Entry<File, List<File>> entry;
        FileNode parentNode;
        while (pointer.hasNext()) {
            entry = pointer.next();
            parentNode = new FileNode(entry.getKey());

            List<File> children = entry.getValue();
            FileNode childNode;
            for (int i = 0; i < children.size(); i++) {
                childNode = new FileNode(children.get(i));
                addNodeToParent(childNode, parentNode);
            }

            if (entry.getKey().getOwnedByMe()) {
                addNodeToParent(parentNode, ((FileNode) fileTree.root()).find(miDrive).get());
            } else {
                // addNodeToParent(parentNode, ((FileNode)
                // fileTree.root()).find(compartido).get());
            }
        }

        TreeIterator<File> treeIterator = fileTree.iterator();
        FileNode currentNode;
        while (treeIterator.hasNext()) {
            currentNode = (FileNode) treeIterator.next();
            if (!currentNode.isRoot()) {
                if (currentNode.value().getParents() != null) {
                    if (!currentNode.parent().value().getId().equals(currentNode.value().getParents().getFirst())) {
                        Optional<File> optional = parentFile(currentNode.value(), files);
                        if (optional.isPresent()) {
                            parentNode = new FileNode(optional.get());
                            Optional<FileNode> optionalNode = fileTree.find(parentNode);
                            if (optionalNode.isPresent()) {
                                parentNode = optionalNode.get();
                                addNodeToParent(currentNode, parentNode);
                            }
                        }
                    }
                }
            }
        }

        return fileTree;
    }

    Optional<File> parentFile(File childFile, List<File> fileSet) {
        if (childFile.getParents() == null) {
            return Optional.empty();
        }

        if (indexIdFiles.get(childFile.getParents().getFirst()) == null) {
            return Optional.empty();
        }

        String parentIndex = childFile.getParents().getFirst();
        return Optional.of(fileSet.get(indexIdFiles.get(parentIndex)));
    }

    /*
     * Realiza la operación de inserción de un buevo hijo dentro del arbol
     * subyacente.
     */
    private void addNodeToParent(FileNode nodoHijo, FileNode nodoPadre) {
        if (nodoHijo.parent() != null) {
            if (!nodoHijo.equals(nodoHijo.parent().leftmostChild())) {
                FileNode child = (FileNode) nodoHijo.parent().leftmostChild();
                while (child != null) {
                    if (child.rightSibling().equals(nodoHijo)) {
                        child.insertNext(nodoHijo.rightSibling());
                        break;
                    }

                    child = (FileNode) child.rightSibling();
                }
            }
        }

        nodoHijo.setParent(nodoPadre);

        nodoHijo.insertNext(nodoPadre.leftmostChild());

        nodoPadre.insertFirst(nodoHijo);
    }

    public Menu makeMenu() {
        return new Menu((FileNode) fileTree.root());
    }

    public String print() {
        return fileTree.print();
    }

    public void toXML() {
        fileTree.writeXML();
    }
}
