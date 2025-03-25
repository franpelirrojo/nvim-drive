package dev.franpelirrojo.nvimdrive.filesystem;

/**
 * ADT del nodo de un arbol general basado en la implementación hijo-hermano.
 */
public interface GTNode<E> {
    public E value();

    public boolean isLeaf();

    public boolean isRoot();

    public GTNode<E> parent();

    public GTNode<E> leftmostChild();

    public GTNode<E> rightSibling();

    public void setValue(E value);

    public void setParent(GTNode<E> par);

    public void insertFirst(GTNode<E> n);

    public void insertNext(GTNode<E> n);

    public void removeFirst();

    public void removeNext();
}
