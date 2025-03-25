package dev.franpelirrojo.nvimdrive.filesystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeIterator<E> implements Iterator<GTNode<E>> {
    private List<GTNode<E>> arr = new ArrayList<>();
    private int current = 0;

    public TreeIterator(GTree<E> tree) {
        copiarArbol(tree.root());
    }

    @Override
    public boolean hasNext() {
        return current < arr.size();
    }

    @Override
    public GTNode<E> next() {
        return arr.get(current++);
    }

    public void copiarArbol(GTNode<E> nodo) {
        arr.add(nodo);
        if (!nodo.isLeaf()) {
            GTNode<E> child = nodo.leftmostChild();
            while (child != null) {
                copiarArbol(child);
                child = child.rightSibling();
            }
        }
    }
}
