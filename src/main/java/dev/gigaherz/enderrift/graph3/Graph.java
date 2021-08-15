package dev.gigaherz.enderrift.graph3;

import com.google.common.collect.*;
import dev.gigaherz.enderrift.graph3.ContextDataFactory;
import dev.gigaherz.enderrift.graph3.GraphObject;
import dev.gigaherz.enderrift.graph3.Mergeable;
import dev.gigaherz.enderrift.graph3.PublicApi;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class Graph<T extends Mergeable<T>>
{
    // Graph data
    private final Set<Node<T>> nodeList = Sets.newHashSet();
    private final Multimap<Node<T>, Node<T>> neighbours = HashMultimap.create();
    private final Multimap<Node<T>, Node<T>> reverseNeighbours = HashMultimap.create();
    private final Map<GraphObject<T>, Node<T>> objects = Maps.newHashMap();
    private T contextData;

    @PublicApi
    public static <T extends Mergeable<T>> void connect(GraphObject<T> object1, GraphObject<T> object2)
    {
        connect(object1, object2, null);
    }

    @PublicApi
    public static <T extends Mergeable<T>> void connect(GraphObject<T> object1, GraphObject<T> object2, @Nullable ContextDataFactory<T> contextDataFactory)
    {
        connect(object1, object2, dev.gigaherz.enderrift.graph3.Graph::new, contextDataFactory);
    }

    @PublicApi
    public static <T extends Mergeable<T>> void connect(GraphObject<T> object1, GraphObject<T> object2, Supplier<dev.gigaherz.enderrift.graph3.Graph<T>> graphFactory, @Nullable ContextDataFactory<T> contextDataFactory)
    {
        dev.gigaherz.enderrift.graph3.Graph<T> graph1 = object1.getGraph();
        dev.gigaherz.enderrift.graph3.Graph<T> graph2 = object2.getGraph();

        dev.gigaherz.enderrift.graph3.Graph<T> target = graph1;
        if (graph1 != null && graph2 != null)
        {
            graph1.mergeWith(graph2);
        }
        else if(graph1 != null)
        {
            graph1.addNode(object2);
        }
        else if(graph2 != null)
        {
            target = graph2;
            graph2.addNode(object1);
        }
        else
        {
            target = graphFactory.get();
            if (contextDataFactory != null)
                target.contextData = contextDataFactory.create(target);
        }

        target.addSingleEdge(object1, object2);
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
        integrate(object, neighbours, dev.gigaherz.enderrift.graph3.Graph::new, contextDataFactory);
    }

    /**
     * Adds the object to a graph. It will reuse the neighbours' graph,
     * or create a new one if none found.
     * @param object The object to add into a graph
     * @param neighbours The neighbours it will connect to (directed)
     * @param contextDataFactory A provider for the shared data object contained in the graph
     */
    @PublicApi
    public static <T extends Mergeable<T>> void integrate(GraphObject<T> object, List<GraphObject<T>> neighbours, Supplier<dev.gigaherz.enderrift.graph3.Graph<T>> graphFactory, @Nullable ContextDataFactory<T> contextDataFactory)
    {
        Set<dev.gigaherz.enderrift.graph3.Graph<T>> otherGraphs = Sets.newHashSet();

        for (GraphObject<T> neighbour : neighbours)
        {
            dev.gigaherz.enderrift.graph3.Graph<T> otherGraph = neighbour.getGraph();
            if (otherGraph != null)
                otherGraphs.add(otherGraph);
        }

        dev.gigaherz.enderrift.graph3.Graph<T> target;
        if (otherGraphs.size() > 0)
        {
            target = otherGraphs.iterator().next();
        }
        else
        {
            target = graphFactory.get();
            if (contextDataFactory != null)
                target.contextData = contextDataFactory.create(target);
        }

        target.addNodeAndEdges(object, neighbours);
    }

    /**
     * Returns the assigned context object.
     */
    @PublicApi
    public T getContextData()
    {
        return contextData;
    }

    /**
     * Assigns a context object attached to the graph.
     * Persisting this information is the responsibility of the user.
     * @param contextData The context object
     */
    @PublicApi
    public void setContextData(T contextData)
    {
        this.contextData = contextData;
    }

    /**
     * Adds an object to the graph, along with some directed edges.
     * The edges must already be part of the graph.
     * @param object The object to add.
     * @param neighbours The objects the edges point toward.
     */
    @PublicApi
    public void addNodeAndEdges(GraphObject<T> object, Iterable<GraphObject<T>> neighbours)
    {
        if (object.getGraph() != null)
            throw new IllegalArgumentException("The object is already in another graph.");

        if (objects.containsKey(object))
            throw new IllegalStateException("The object is already in this graph.");

        Node<T> node = new Node<>(this, object);

        object.setGraph(this);
        objects.put(object, node);

        nodeList.add(node);

        verify();

        addDirectedEdges(object, neighbours);
    }

    /**
     * Adds some directed edges to a node.
     * @param object The object the edge originates from.
     * @param neighbours The objects the edges point toward.
     */
    @PublicApi
    public void addDirectedEdges(GraphObject<T> object, Iterable<GraphObject<T>> neighbours)
    {
        Node<T> node = objects.get(object);
        for (GraphObject<T> neighbour : neighbours)
        {
            addSingleEdgeInternal(node, neighbour);
        }

        verify();
    }

    /**
     * Adds a single directed edge.
     * @param object The object the edge originates from.
     * @param neighbour The object the edge points toward.
     */
    @PublicApi
    public void addSingleEdge(GraphObject<T> object, GraphObject<T> neighbour)
    {
        Node<T> node = objects.get(object);

        addSingleEdgeInternal(node, neighbour);

        verify();
    }

    /**
     * Removes a single directed edge, if it exists..
     * @param object The object the edge originates from.
     * @param neighbour The object the edge points toward.
     */
    @PublicApi
    public void removeSingleEdge(GraphObject<T> object, GraphObject<T> neighbour)
    {
        Node<T> node = objects.get(object);
        Node<T> other = objects.get(neighbour);

        neighbours.remove(node, other);
        reverseNeighbours.remove(other, node);

        verify();

        splitAfterRemoval();
    }

    /**
     * Removes a node from the graph, along with all the related edges.
     * @param object The object to remove.
     */
    @PublicApi
    public void remove(GraphObject<T> object)
    {
        if (object.getGraph() != this)
            throw new IllegalArgumentException("The object is not of this graph.");

        object.setGraph(null);

        Node<T> node = objects.get(object);
        if (node == null)
            throw new IllegalStateException("The graph is broken.");

        nodeList.remove(node);

        Set<Node<T>> neighs = Sets.newHashSet(neighbours.get(node));
        neighs.addAll(reverseNeighbours.get(node));
        for (Node<T> n : neighs)
        {
            neighbours.remove(n, node);
            reverseNeighbours.remove(node, n);

            neighbours.remove(node, n);
            reverseNeighbours.remove(n, node);
        }

        objects.remove(object);

        verify();

        splitAfterRemoval();
    }

    /**
     * Obtains the list of objects representing the nodes in the graph.
     * @return The objects from the graph.
     */
    @PublicApi
    public Collection<GraphObject<T>> getObjects()
    {
        return Collections.unmodifiableSet(objects.keySet());
    }

    /**
     * Obtains the list of objects representing the nodes in the graph.
     * This version of the method is designed for concurrent graphs,
     * where it acquires the read lock.
     */
    @PublicApi
    public Collection<GraphObject<T>> acquireObjects()
    {
        return getObjects();
    }

    /**
     * Releases the read lock on the object list,
     * for concurrent graphs.
     */
    @PublicApi
    public void releaseObjects()
    {
    }

    /**
     * Obtains the neighbouring objects that the object connects to.
     * @param object The object for which to get the neighbours.
     * @return The neighbouring objects.
     */
    @PublicApi
    public Collection<GraphObject<T>> getNeighbours(GraphObject<T> object)
    {
        Set<GraphObject<T>> others = Sets.newHashSet();
        for (Node<T> n : neighbours.get(objects.get(object)))
        {
            others.add(n.getObject());
        }
        return ImmutableSet.copyOf(others);
    }

    /**
     * Checks if the given object is part of the graph.
     * @param object The object.
     * @return True if the graph contains the object as a node
     */
    @PublicApi
    public boolean contains(GraphObject<T> object)
    {
        Node<T> node = objects.get(object);
        return node != null && nodeList.contains(node);
    }

    // ##############################################################################
    // ## Private helpers

    private void addNode(GraphObject<T> object)
    {
        if (object.getGraph() != null)
            throw new IllegalArgumentException("The object is already in another graph.");

        if (objects.containsKey(object))
            throw new IllegalStateException("The object is already in this graph.");

        Node<T> node = new Node<>(this, object);

        object.setGraph(this);
        objects.put(object, node);

        nodeList.add(node);
    }

    private void addSingleEdgeInternal(Node<T> node, GraphObject<T> neighbour)
    {
        dev.gigaherz.enderrift.graph3.Graph<T> g = neighbour.getGraph();

        if (g == null)
            throw new IllegalArgumentException("The neighbour object is not in a graph.");

        if (g != this)
            mergeWith(g);

        if (neighbour.getGraph() != this)
            throw new IllegalStateException("The graph merging didn't work as expected.");

        Node<T> n = objects.get(neighbour);

        this.neighbours.put(node, n);
        reverseNeighbours.put(n, node);
    }

    private void splitAfterRemoval()
    {
        if (nodeList.size() == 0)
            return;

        Set<Node<T>> remaining = Sets.newHashSet(nodeList);
        Set<Node<T>> seen = Sets.newHashSet();
        Queue<Node<T>> succ = Queues.newArrayDeque();

        Node<T> node = remaining.iterator().next();
        succ.add(node);
        seen.add(node);
        remaining.remove(node);

        // First mark the ones that will remain in this graph
        // so that there are only new graphs created if needed
        while (succ.size() > 0)
        {
            Node<T> c = succ.poll();
            for (Node<T> n : neighbours.get(c))
            {
                if (!seen.contains(n))
                {
                    seen.add(n);
                    succ.add(n);
                    remaining.remove(n);
                }
            }
            for (Node<T> n : reverseNeighbours.get(c))
            {
                if (!seen.contains(n))
                {
                    seen.add(n);
                    succ.add(n);
                    remaining.remove(n);
                }
            }
        }

        // If anything remains unseen, it means it's on a disconnected subgraph
        while (remaining.size() > 0)
        {
            node = remaining.iterator().next();
            succ.add(node);
            seen.add(node);
            remaining.remove(node);

            dev.gigaherz.enderrift.graph3.Graph<T> newGraph = new dev.gigaherz.enderrift.graph3.Graph<>();
            if (contextData != null)
                newGraph.contextData = contextData.copy();
            while (succ.size() > 0)
            {
                Node<T> c = succ.poll();
                for (Node<T> n : neighbours.get(c))
                {
                    if (!seen.contains(n))
                    {
                        seen.add(n);
                        succ.add(n);
                        remaining.remove(n);
                    }
                }
                for (Node<T> n : reverseNeighbours.get(c))
                {
                    if (!seen.contains(n))
                    {
                        seen.add(n);
                        succ.add(n);
                        remaining.remove(n);
                    }
                }

                this.nodeList.remove(c);
                newGraph.nodeList.add(c);
                newGraph.neighbours.putAll(c, neighbours.get(c));
                newGraph.reverseNeighbours.putAll(c, reverseNeighbours.get(c));
                this.neighbours.removeAll(c);
                this.reverseNeighbours.removeAll(c);
                this.objects.remove(c.getObject());
                newGraph.objects.put(c.getObject(), c);
                c.owner = newGraph;
                c.getObject().setGraph(newGraph);
            }

            verify();
        }

        verify();
    }

    private void mergeWith(dev.gigaherz.enderrift.graph3.Graph<T> graph)
    {
        nodeList.addAll(graph.nodeList);
        objects.putAll(graph.objects);
        neighbours.putAll(graph.neighbours);
        reverseNeighbours.putAll(graph.reverseNeighbours);

        for (Node<T> n : graph.nodeList)
        { n.getObject().setGraph(this); }

        if (contextData != null && graph.contextData != null)
            contextData = contextData.mergeWith(graph.contextData);
        else if(graph.contextData != null)
            contextData = graph.contextData;

        verify();
    }

    private void verify()
    {
        for (Node<T> node : nodeList)
        {
            for (Node<T> other : neighbours.get(node))
            {
                if (!nodeList.contains(other))
                {
                    throw new IllegalStateException("Graph is broken!");
                }
            }

            if (!objects.containsKey(node.getObject()))
            {
                throw new IllegalStateException("Graph is broken!");
            }
        }

        for (Node<T> other : objects.values())
        {
            if (!nodeList.contains(other))
            {
                throw new IllegalStateException("Graph is broken!");
            }
        }
    }

    private static class Node<T extends Mergeable<T>>
    {
        private dev.gigaherz.enderrift.graph3.Graph<T> owner;

        // Object attached to this node
        private final GraphObject<T> object;

        @PublicApi
        public dev.gigaherz.enderrift.graph3.Graph<T> getOwner()
        {
            return owner;
        }

        public GraphObject<T> getObject()
        {
            return object;
        }

        public Node(dev.gigaherz.enderrift.graph3.Graph<T> owner, GraphObject<T> object)
        {
            this.owner = owner;
            this.object = object;
        }
    }
}
