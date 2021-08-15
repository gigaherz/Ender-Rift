package dev.gigaherz.enderrift.graph3;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentGraph<T extends Mergeable<T>> extends Graph<T>
{
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    @PublicApi
    public static <T extends Mergeable<T>> void connect(GraphObject<T> object1, GraphObject<T> object2)
    {
        connect(object1, object2, null);
    }

    @PublicApi
    public static <T extends Mergeable<T>> void connect(GraphObject<T> object1, GraphObject<T> object2, @Nullable ContextDataFactory<T> contextDataFactory)
    {
        connect(object1, object2, dev.gigaherz.enderrift.graph3.ConcurrentGraph::new, contextDataFactory);
    }

    /**
     * Adds the object to a graph. It will reuse the neighbours' graph,
     * or create a new one if none found.
     * @param object The object to add into a graph
     * @param neighbours The neighbours it will connect to (directed)
     */
    @PublicApi
    public static <T extends Mergeable<T>> void integrate(GraphObject<T> object, List<GraphObject<T>> neighbours)
    {
        integrate(object, neighbours, null);
    }

    /**
     * Adds the object to a graph. It will reuse the neighbours' graph,
     * or create a new one if none found.
     * @param object The object to add into a graph
     * @param neighbours The neighbours it will connect to (directed)
     * @param contextDataFactory A provider for the shared data object contained in the graph
     */
    @PublicApi
    public static <T extends Mergeable<T>> void integrate(GraphObject<T> object, List<GraphObject<T>> neighbours, @Nullable ContextDataFactory<T> contextDataFactory)
    {
        integrate(object, neighbours, dev.gigaherz.enderrift.graph3.ConcurrentGraph::new, contextDataFactory);
    }

    /**
     * Returns the assigned context object.
     */
    @Override
    public T getContextData()
    {
        readLock.lock();
        try
        {
            return super.getContextData();
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public void setContextData(T contextData)
    {
        writeLock.lock();
        try
        {
            super.setContextData(contextData);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void addNodeAndEdges(GraphObject<T> object, Iterable<GraphObject<T>> neighbours)
    {
        writeLock.lock();
        try
        {
            super.addNodeAndEdges(object, neighbours);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void addDirectedEdges(GraphObject<T> object, Iterable<GraphObject<T>> neighbours)
    {
        writeLock.lock();
        try
        {
            super.addDirectedEdges(object, neighbours);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void addSingleEdge(GraphObject<T> object, GraphObject<T> neighbour)
    {
        writeLock.lock();
        try
        {
            super.addSingleEdge(object, neighbour);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void removeSingleEdge(GraphObject<T> object, GraphObject<T> neighbour)
    {
        writeLock.lock();
        try
        {
            super.removeSingleEdge(object, neighbour);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    public void remove(GraphObject<T> object)
    {
        writeLock.lock();
        try
        {
            super.remove(object);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Deprecated
    @Override
    public Collection<GraphObject<T>> getObjects()
    {
        return super.getObjects();
    }

    public Collection<GraphObject<T>> acquireObjects()
    {
        readLock.lock();
        return super.getObjects();
    }

    public void releaseObjects()
    {
        readLock.unlock();
    }

    @Override
    public Collection<GraphObject<T>> getNeighbours(GraphObject<T> object)
    {
        readLock.lock();
        try
        {
            return super.getNeighbours(object);
        }
        finally
        {
            readLock.unlock();
        }
    }

    @Override
    public boolean contains(GraphObject<T> object)
    {
        readLock.lock();
        try
        {
            return super.contains(object);
        }
        finally
        {
            readLock.unlock();
        }
    }
}
