package dev.gigaherz.enderrift.graph3;

import dev.gigaherz.enderrift.graph3.Graph;
import dev.gigaherz.enderrift.graph3.Mergeable;

public interface GraphObject<T extends Mergeable<T>>
{
    Graph<T> getGraph();

    void setGraph(Graph<T> g);
}
