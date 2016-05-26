package com.data.stateDescription;

import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

public class Description_test{
    static final Description REF = new Description("NAME", "FILE", "TEXT");
    static final Description REFB = new Description(new String("NAME"),
                                                    new String("FILE"),
                                                    new String("TEXT") );
    static final Description DIFFNAME = new Description("NOPE", "FILE", "TEXT");
    static final Description DIFFFILE = new Description("NAME", "NOPE", "TEXT");
    static final Description DIFFTEXT = new Description("NAME", "FILE", "NOPE");
    @Test
    public void notEqualWrongClass(){
        assertFalse(REF.equals(new Object()));
    }
    @Test
    public void notEqualWhenNull() {
        assertFalse(REF.equals(null));
    }
    @Test
    public void equalWhenIdentity() {
        assertTrue(REF.equals(REF));
    }
    @Test
    public void hashCodeIdentity() {
        assertEquals(REF.hashCode(), REF.hashCode());
    }
    @Test
    public void equalWhenTrue() {
        assertTrue(REF.equals(REFB));
        assertTrue(REFB.equals(REF));
    }
    @Test
    public void hashCodeTrue() {
        assertEquals(REF.hashCode(), REFB.hashCode());
    }
    @Test
    public void notEqualMismatchedText(){
        assertFalse(REF.equals(DIFFTEXT));
        assertFalse(DIFFTEXT.equals(REF));
    }
    @Test
    public void notEqualMismatchedName(){
        assertFalse(REF.equals(DIFFNAME));
        assertFalse(DIFFNAME.equals(REF));
    }
    @Test
    public void notEqualMismatchedFile(){
        assertFalse(REF.equals(DIFFFILE));
        assertFalse(DIFFFILE.equals(REF));
    }
    @Test
    public void hashCodeMismatchedText(){
        assertFalse(REF.hashCode() == DIFFTEXT.hashCode());
    }
    @Test
    public void hashCodeMismatchedName(){
        assertFalse(REF.hashCode() == DIFFNAME.hashCode());
    }
    @Test
    public void hashCodeMismatchedFile(){
        assertFalse(REF.hashCode() == DIFFFILE.hashCode());
    }
}
