package dev.gigaherz.enderrift.graph3;

import dev.gigaherz.enderrift.graph3.Graph;
import dev.gigaherz.enderrift.graph3.Mergeable;

@FunctionalInterface
public interface ContextDataFactory<T extends Mergeable<T>>
{
    T create(Graph<T> target);
}
