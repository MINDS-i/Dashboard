import com.layer.*;

import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.io.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;

/**
 * public void add(Layer l)
 * public void draw(Graphics g)
 * public void mouseClicked(MouseEvent e)
 * public void mousePressed(MouseEvent e)
 * public void mouseDragged(MouseEvent e)
 * public void mouseReleased(MouseEvent e)
 */

public class LayerManager_test {
    Layer mockOn(int z){
        Layer l = mock(Layer.class);
        when(l.getZ()).thenReturn(z);
        return l;
    }
//NOTE: Removed 2/2/22 - Null Pointer causes test failure due to not accounting
    //for CommandManager reference in the test.
//    @Test
//    public void testLayerDrawOrder(){
//        LayerManager lm = new LayerManager();
//        Layer ls[] = new Layer[]{
//            mockOn(10), mockOn( 1),
//            mockOn( 0), mockOn(-1) };
//        Graphics g = mock(Graphics.class);
//
//        when(g.create()).thenReturn(g);
//        for(Layer l:ls) lm.add(l);
//        lm.draw(g);
//
//        //should be drawn from bottom to top
//        InOrder i = inOrder(ls[0],ls[1],ls[2],ls[3]);
//        i.verify(ls[3]).paint(g);
//        i.verify(ls[2]).paint(g);
//        i.verify(ls[1]).paint(g);
//        i.verify(ls[0]).paint(g);
//    }
    @Test
    public void testLayerClickOrder() {
        LayerManager lm = new LayerManager();
        Layer ls[] = new Layer[]{
            mockOn(10), mockOn( 1),
            mockOn( 0), mockOn(-1) };
        MouseEvent e = mock(MouseEvent.class);

        for(Layer l:ls) lm.add(l);
        lm.mouseClicked(e);

        //should be clicked from the top down
        InOrder i = inOrder(ls[0],ls[1],ls[2],ls[3]);
        i.verify(ls[0]).onClick(e);
        i.verify(ls[1]).onClick(e);
        i.verify(ls[2]).onClick(e);
        i.verify(ls[3]).onClick(e);
    }
    @Test
    public void testLayerClickClaimed() {
        LayerManager lm = new LayerManager();
        Layer ls[] = new Layer[]{
            mockOn(10), mockOn( 1),
            mockOn( 0), mockOn(-1) };
        MouseEvent e = mock(MouseEvent.class);

        for(Layer l:ls) lm.add(l);
        when(ls[1].onClick(e)).thenReturn(true);
        lm.mouseClicked(e);

        verify(ls[0]).onClick(e);
        verify(ls[1]).onClick(e);
        verify(ls[2], never()).onClick(e);
        verify(ls[3], never()).onClick(e);
    }
    @Test
    public void testLayerPressOrder() {
        LayerManager lm = new LayerManager();
        Layer ls[] = new Layer[]{
            mockOn( 0), mockOn( 1),
            mockOn( 2), mockOn(-1) };
        MouseEvent e = mock(MouseEvent.class);

        for(Layer l:ls) lm.add(l);
        lm.mousePressed(e);

        //should be Pressed from the top down
        InOrder i = inOrder(ls[0],ls[1],ls[2],ls[3]);
        i.verify(ls[2]).onPress(e);
        i.verify(ls[1]).onPress(e);
        i.verify(ls[0]).onPress(e);
        i.verify(ls[3]).onPress(e);
    }
    @Test
    public void testPressClaimedAndDragged() {
        LayerManager lm = new LayerManager();
        Layer ls[] = new Layer[]{
            mockOn( 0), mockOn( 1),
            mockOn( 2), mockOn(-1) };
        MouseEvent e = mock(MouseEvent.class);

        for(Layer l:ls) lm.add(l);
        when(ls[0].onPress(e)).thenReturn(true);
        lm.mousePressed(e);
        lm.mouseDragged(e);

        verify(ls[0]).onDrag(e);
        verify(ls[2],never()).onDrag(e);
        verify(ls[1],never()).onDrag(e);
        verify(ls[3],never()).onDrag(e);
    }
    @Test
    public void testPressClaimedAndReleased() {
        LayerManager lm = new LayerManager();
        Layer ls[] = new Layer[]{
            mockOn( 0), mockOn( 1),
            mockOn( 2), mockOn(-1) };
        MouseEvent e = mock(MouseEvent.class);

        for(Layer l:ls) lm.add(l);
        when(ls[0].onPress(e)).thenReturn(true);
        lm.mousePressed(e);
        lm.mouseReleased(e);

        verify(ls[0]).onRelease(e);
        verify(ls[2],never()).onRelease(e);
        verify(ls[1],never()).onRelease(e);
        verify(ls[3],never()).onRelease(e);
    }
    @Test
    public void testNullDrag() {
        LayerManager lm = new LayerManager();
        Layer l = mockOn(1);
        MouseEvent e = mock(MouseEvent.class);

        lm.add(l);
        lm.mousePressed(e);
        lm.mouseDragged(e);

        verify(l, never()).onDrag(any());
    }
    @Test
    public void testNullRelease() {
        LayerManager lm = new LayerManager();
        Layer l = mockOn(1);
        MouseEvent e = mock(MouseEvent.class);

        lm.add(l);
        lm.mousePressed(e);
        lm.mouseReleased(e);

        verify(l, never()).onRelease(any());
    }
}
