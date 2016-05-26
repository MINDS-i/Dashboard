package com.logging;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import java.util.logging.*;

public class Ellipsisformatter_test{
    final Level l = Level.ALL;
    @Test
    public void testNoEllipsisNecessary(){
        EllipsisFormatter ef = new EllipsisFormatter(10);
        assertThat(ef.format(new LogRecord(l,"abcd")), equalTo("abcd"));
    }
    @Test
    public void testNoEllipsisMaxLength(){
        EllipsisFormatter ef = new EllipsisFormatter(4);
        assertThat(ef.format(new LogRecord(l,"abcd")), equalTo("abcd"));
    }
    @Test
    public void testEllipsisOneTooLong(){
        EllipsisFormatter ef = new EllipsisFormatter(4);
        assertThat(ef.format(new LogRecord(l,"abcde")), equalTo("a..."));
    }
    @Test
    public void testEllipsisVeryLong(){
        EllipsisFormatter ef = new EllipsisFormatter(4);
        assertThat(ef.format(new LogRecord(l,"abcdefghijkl")), equalTo("a..."));
    }
    @Test
    public void testFormatterPassthrough(){
        Formatter f = mock(Formatter.class);
        when(f.format(org.mockito.Mockito.any(LogRecord.class)))
            .thenReturn("abcd");
        EllipsisFormatter ef = new EllipsisFormatter(f, 4);
        assertThat(ef.format(new LogRecord(l,"nope")), equalTo("abcd"));
    }
    @Test
    public void testScrubNewlines(){
        EllipsisFormatter ef = new EllipsisFormatter(10);
        assertThat(ef.format(new LogRecord(l,"abc\ndef")),
                                     equalTo("abc def"));
    }
}
