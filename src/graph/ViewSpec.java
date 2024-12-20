package com.graph;

/**
 * A ViewSpec defines a set of views at different height/widths and generates
 * instances of View for the graph to use when rendering.
 * It also defines transformations for when the view should be panned or zoomed
 */

public interface ViewSpec {
    View at(int height, int width);

    void zoom(float fac);

    void panY(int pix, int height);

    void panX(int pix, int width);

    float maxY();

    float minY();
}
