package com.data.stateDescription;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;

public class StateMap_test{
    static final String testXML =
        "<MessageDB>" +
            "<Message name=\"TESTA\" path=\"/test\">"+
                "Hello"+
            "</Message>"+
            "<Message name=\"TESTB\" path=\"/exam\">"+
                "&lt;b&gt;"+
            "</Message>"+
        "</MessageDB>";
    static final Description TESTA = new Description("TESTA", "/test", "Hello");
    static final Description TESTB = new Description("TESTB", "/exam", "<b>");

    //test escaped < charecters etc.

    @Test
    public void simpleParse() {
        StateMap sm = null;
        try{
            Reader is = new StringReader(testXML);
            sm = StateMap.read(is);
        } catch (Exception e) {
            fail();
        }
        assertThat(sm.getFullDescription(TESTA.getName()).get(),
                    equalTo(TESTA));
        assertThat(sm.getFullDescription(TESTB.getName()).get(),
                    equalTo(TESTB));
        assertThat(sm.getFullDescription("Nope"), equalTo(Optional.empty()));
    }
}
