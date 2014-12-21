package org.haedus.machines;

import java.util.Collection;

/**
 * Created by Samantha F M McCabe on 12/21/14.
 */
public interface Node<T> {

    /**
     * Checks if the object
     *
     * @return
     */
    boolean isEmpty();

    boolean matches(T target);

    /**
     * @param node
     * @return
     */
    void add(Node<T> node);

    void add(T arcValue, Node<T> node);

    boolean hasArc(T arcValue);

    Collection<Node<T>> getNodes(T arcValue);

    Collection<T> getKeys();

    boolean isAccepting();

    int getId();

    void setAccepting(boolean b);
}
