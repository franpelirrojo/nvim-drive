package dev.franpelirrojo.nvimdrive.filesystem;

/**
 * ADT de un arbol generico basado en la implementación hijo-hermano.
 */
public interface GTree<E> {
    public void clear();

    public GTNode<E> root();

    public void newroot(E value, GTNode<E> first, GTNode<E> sib);

    public void newleftchild(E value);
}
