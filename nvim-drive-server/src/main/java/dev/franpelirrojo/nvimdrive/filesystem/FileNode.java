package dev.franpelirrojo.nvimdrive.filesystem;

import java.util.Objects;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.api.services.drive.model.File;

/**
 * Representa un nodo del arbol de ficheros. Implementa la interfaz
 * {@link GTNode}
 * No existen nodos nulos, es decir, todo nodo tiene un elemento file.
 */
public class FileNode implements GTNode<File> {
    private FileNode parent;
    private FileNode leftChild;
    private FileNode rightSib;
    private File file;

    private boolean FOLDER;
    private boolean OWN;

    public FileNode(File file) {
        this.file = file; //TODO: implementar estos dos sin errores
        // FOLDER = Utils.isFolder(file);
        // OWN = file.getOwnedByMe();
    }

    /**
     * Busca si el nodo target se encuentra en el subarbol representado a partir de
     * este nodo.
     * Si lo encuentra retorna un optional con una referencia al nodo que forma
     * parte del arbol,
     * si no hay cocincidencias, retorna un opcional vacío.
     */
    Optional<FileNode> find(FileNode target) {
        if (target.equals(this)) return Optional.of(this);

        if (!isLeaf()) {
            FileNode child = (FileNode) leftmostChild();
            while (child != null) {
                Optional<FileNode> optional = child.find(target);
                if (optional.isPresent()) return optional;

                child = (FileNode) child.rightSibling();
            }
        }

        return Optional.empty();
    }

    public String print() {
        return print(0);
    }

    String print(int indent) {
        String texto = String.format("\t".repeat(indent) + "| %s%n", file.getName());
        if (!isLeaf()) {
            FileNode child = (FileNode) leftmostChild();
            while (child != null) {
                child.print(++indent);
                indent--;
                child = (FileNode) child.rightSibling();
            }
        }

        return texto;
    }

    XMLStreamWriter writeXML(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("Elemento");
        writer.writeCharacters(file.getName());

        if (!isLeaf()) {
            FileNode child = (FileNode) leftmostChild();
            while (child != null) {
                child.writeXML(writer);
                child = (FileNode) child.rightSibling();
            }
        }

        writer.writeEndElement();
        return writer;
    }

    @Override
    public File value() {
        return file;
    }

    @Override
    public boolean isLeaf() {
        return leftChild == null;
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public GTNode<File> parent() {
        return parent;
    }

    @Override
    public GTNode<File> leftmostChild() {
        return leftChild;
    }

    @Override
    public GTNode<File> rightSibling() {
        return rightSib;
    }

    @Override
    public void setValue(File value) {
        if (value == null) throw new NullPointerException(); // TODO: Excepción propia
        file = value;
    }

    @Override
    public void setParent(GTNode<File> par) {
        parent = (FileNode) par;
    }

    @Override
    public void insertFirst(GTNode<File> n) {
        leftChild = (FileNode) n;
    }

    @Override
    public void insertNext(GTNode<File> n) {
        rightSib = (FileNode) n;
    }

    @Override
    public void removeFirst() {
        leftChild = null;
    }

    @Override
    public void removeNext() {
        rightSib = null;
    }

    public boolean isFOLDER() {
        return FOLDER;
    }

    public void setFOLDER(boolean folder) {
        FOLDER = folder;
    }

    public boolean isOWN() {
        return OWN;
    }

    public void setOWN(boolean own) {
        OWN = own;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        FileNode nodo = (FileNode) obj;
        return file.getId().equals(nodo.file.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(file.getId());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(file.getName() + "\n");
        
        int counter = 1;
        if (!isLeaf()) {
            FileNode child = (FileNode) leftmostChild();
            while (child != null) {
                sb.append("\t" + counter++ + ". " + child.value().getName() + "\n");
                child = (FileNode) child.rightSibling();
            }
        }

        return sb.toString();
    }
}
