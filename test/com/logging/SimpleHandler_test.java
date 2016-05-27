package com.logging;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import java.util.logging.*;

import com.logging.SimpleHandler.MessageUser;

public class SimpleHandler_test{
    Level l = Level.WARNING;
    LogRecord r = new LogRecord(l,"hi");
    MessageUser mu;
    SimpleHandler sh;

    @Before
    public void before() throws Exception {
        mu = mock(MessageUser.class);
        sh = new SimpleHandler(mu);
    }

    @Test
    public void testDefaultString(){
        sh.publish(r);
        verify(mu).use(r,"hi");
    }
    @Test
    public void testLevelPass(){
        sh.setLevel(Level.FINE);
        sh.publish(r);
        verify(mu).use(r,"hi");
    }
    @Test
    public void testLevelFail(){
        sh.setLevel(Level.OFF);
        sh.publish(r);
        verify(mu, never()).use(r,"hi");
    }
    @Test
    public void testFilterPass(){
        Filter filter = mock(Filter.class);
        when(filter.isLoggable(anyObject())).thenReturn(true);
        sh.setFilter(filter);
        sh.publish(r);
        verify(mu).use(r,"hi");
    }
    @Test
    public void testFilterFail(){
        Filter filter = mock(Filter.class);
        when(filter.isLoggable(anyObject())).thenReturn(false);
        sh.setFilter(filter);
        sh.publish(r);
        verify(mu, never()).use(r,"hi");
    }
    @Test
    public void testFormatterApplied(){
        Formatter fmt = mock(Formatter.class);
        when(fmt.format(anyObject())).thenReturn("bye");
        sh.setFormatter(fmt);
        sh.publish(r);
        verify(mu).use(r,"bye");
    }
}
