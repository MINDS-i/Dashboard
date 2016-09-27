package com.util;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.lang.ref.WeakReference;

public class WeakList_test {
    @Test public void objectRetrieval(){
        Object strong = new Object();
        WeakList<Object> wl = new WeakList<Object>();
        wl.add(strong);
        int callCount = 0;
        for(Object o : wl){
            assertThat(o, is(strong));
            callCount++;
        }
        assertThat(callCount, is(1));
    }

    @Test public void collectedObjectRemoved(){
        Object strong = new Object();
        WeakReference<Object> weak = new WeakReference<Object>(strong);
        WeakList<Object> wl = new WeakList<Object>();
        wl.add(strong);

        strong = null;
        // wait for strong to be collected
        while(weak.get() != null) System.gc();

        for(Object o : wl){
            fail("Collection had an object despite contents being collected");
        }
    }

    @Test public void addingNullNoEffect(){
        WeakList<Object> wl = new WeakList<Object>();
        wl.add(null);

        for(Object o : wl){
            fail("WeakList can't contain nulls");
        }
    }

    @Test public void removeOnlyElement(){
        Object strong = new Object();
        WeakList<Object> wl = new WeakList<Object>();
        wl.add(strong);

        wl.remove(strong);

        for(Object o : wl){
            fail("WeakList should have had its only element removed");
        }
    }

    @Test public void clearElements(){
        Object strong = new Object();
        Object strong2 = new Object();
        WeakList<Object> wl = new WeakList<Object>();
        wl.add(strong);
        wl.add(strong2);
        wl.clear();
        for(Object o : wl){
            fail("WeakList should have no remaining elements");
        }
    }

    @Test public void iterateMultiple(){
        Object[] objs = new Object[10];
        for(int i=0; i<objs.length; i++) objs[i] = new Object();

        WeakList<Object> wl = new WeakList<Object>();
        for(Object o : objs) wl.add(o);

        int callCount = 0;
        for(Object o : wl){
            callCount++;
        }
        assertThat(callCount, is(10));
    }

    @Test public void iterateSome(){
        Object[] objs = new Object[10];
        for(int i=0; i<objs.length; i++) objs[i] = new Object();
        WeakList<Object> wl = new WeakList<Object>();
        for(Object o : objs) wl.add(o);

        WeakReference<Object> weak = new WeakReference<Object>(objs[0]);
        for(int i=0; i<objs.length; i+=2) objs[i] = null;
        // wait for confirmation of a GC pass
        while(weak.get() != null) System.gc();

        int callCount = 0;
        for(Object o : wl){
            callCount++;
        }
        assertThat(callCount, is(5));
    }
}
