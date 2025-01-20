package com.graph;

public interface DataSource {
    /* Should provide all data between 0.0 and 1.0 on x*/
    double get(double x);

    String getName();
}
