package com.util;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * A simple WeakList container for transparently keeping track of a group of
 * references using WeakReferences only.
 * Does not permit null elements.
 */
public class WeakList<T> implements Iterable<T> {
    private final Collection<WeakReference<T>> bag
            = new LinkedList<WeakReference<T>>();

    /**
     * Create a new WeakList.
     * The WeakList object stores its contains in WeakReferences and
     * transparently removes them from the collection when their contents
     * get garbage collected.
     */
    public WeakList() {
    }

    /**
     * Add an element `obj` to the list.
     * This collection does not accept nulls; calling with a null argument
     * will have no effect.
     */
    void add(T obj) {
        if (obj != null) {
            bag.add(new WeakReference<T>(obj));
        }
    }

    /**
     * Remove all elements from the list
     */
    void clear() {
        bag.clear();
    }

    /**
     * Remove all elements e in the collection such that e == `obj`
     * e.g. by reference equality
     */
    void remove(T obj) {
        for (Iterator<WeakReference<T>> i = bag.iterator(); i.hasNext(); ) {
            T r = i.next().get();
            if (r == null || r == obj) {
                i.remove();
            }
        }
    }

    /**
     * Return an iterator for the items still present in the collection.
     * All references returned by `next()` will be non-null and valid.
     */
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            final Iterator<WeakReference<T>> itr = bag.iterator();
            T next = null;

            public boolean hasNext() {
                while (itr.hasNext() && next == null) {
                    next = itr.next().get();
                    // If the reference has been GC'd, remove it
                    if (next == null) {
                        itr.remove();
                    }
                }
                return next != null;
            }

            public T next() throws NoSuchElementException {
                if (hasNext()) {
                    T rtn = next;
                    next = null;
                    return rtn;
                }
                throw new NoSuchElementException();
            }

            public void remove() throws IllegalStateException {
                itr.remove();
            }
        };
    }
}
