package com.telemetry;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;

public class TelemetryManager_test {
    @Test public void instance(){
        TelemetryManager tm = new TelemetryManager();
    }

    @Test public void retrieveTest(){
        TelemetryManager tm = new TelemetryManager();

        tm.update(0, 10.0);
        tm.update(10, 12.0);

        assertThat(tm.get(0), is(10.0));
        assertThat(tm.get(10), is(12.0));

        tm.update(10, 10.0);
        assertThat(tm.get(10), is(10.0));
    }

    @Test public void registerListener(){
        TelemetryManager tm = new TelemetryManager();
        TelemetryListener tl = mock(TelemetryListener.class);

        tm.registerListener(2, tl);
        tm.update(2, 10.0);
        tm.update(3, 15.0);
        verify(tl).update(10.0);
    }

    @Test public void removeListener(){
        TelemetryManager tm = new TelemetryManager();
        TelemetryListener tl = mock(TelemetryListener.class);

        tm.registerListener(2, tl);
        tm.removeListener(tl);
        tm.update(2, 10.0);
        verify(tl, never()).update(10.0);
    }

    @Test public void maxIndex(){
        TelemetryManager tm = new TelemetryManager();
        assertThat(tm.maxIndex(), is(0));
        tm.update(0, 2.0);
        assertThat(tm.maxIndex(), is(1));
        tm.update(9, 2.0);
        assertThat(tm.maxIndex(), is(10));
    }

    @Test public void changeIndex(){
        TelemetryManager tm = new TelemetryManager();
        int index = tm.changeIndex();
        tm.update(2, 2.0);
        int newIndex = tm.changeIndex();
        assertThat(newIndex, greaterThan(index));

        index = newIndex;
        tm.update(100, 2.0);
        newIndex = tm.changeIndex();
        assertThat(newIndex, greaterThan(index));
    }
}
