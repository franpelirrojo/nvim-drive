package dev.franpelirrojo.nvimdrive.filesystem;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.google.api.services.drive.model.File;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Representa un arbol de ficheros navegable. Implementa {@link GTree}.
 */
public class FileTree implements GTree<File> {
    private FileNode root;

    public FileTree(FileNode root) {
        this.root = root;
    }

    @Override
    public void clear() {
        root = null;
    }

    @Override
    public GTNode<File> root() {
        return root;
    }

    @Override //TODO: revisar si es una sustitución de root o una form de añadir un arbol
    public void newroot(File value, GTNode<File> first, GTNode<File> sib) {
        root.setValue(value);
        first.setParent(root);
        root.insertFirst(first);
        sib.setParent(root);
        first.insertNext(sib);
    }

    @Override
    public void newleftchild(File value) {
        FileNode newLeft = new FileNode(value);
        newLeft.setParent(root);

        if (root.leftmostChild() != null) {
            newLeft.insertNext(root.leftmostChild());
        } 
        
        root.insertFirst(newLeft);
    }

    //TODO: reorganizar el código, volver recursivo unicamente en el sistema de archivos
    Optional<FileNode> find(FileNode target) {
        return root.find(target);
    }

    //TODO: reorganizar el código, volver recursivo unicamente en el sistema de archivos
    String print() {
        return root.print();
    }

    TreeIterator<File> iterator() {
        return new TreeIterator<File>(this);
    }

    void writeXML() {         
        try {
            StringWriter stringBuffer = new StringWriter();
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stringBuffer);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("Ficheros");

            writer = root.writeXML(writer);

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();

            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(stringBuffer.toString().getBytes()));

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");

            FileOutputStream fos = new FileOutputStream("files.xml");
            transformer.transform(new DOMSource(doc), new StreamResult(fos));
            fos.close();

        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
