package com.legendapl.lightning.adhoc.dao.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *　データベースのテーブルの結合のサブグラフ
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class TableSubGraph {
    private final int vertices;
    private int edges;
    private List<LinkedList<Integer>> adj;

    public TableSubGraph(int vertices) {
        if(vertices < 0)
            throw new IllegalArgumentException("Number of vertices must be nonnegative");
        this.vertices = vertices;
        this.edges = 0;
        adj = new ArrayList<LinkedList<Integer>>(vertices);
        for(int i = 0; i < vertices; i++) {
            adj.add(new LinkedList<Integer>());
        }
    }

    public void addEdge(int v, int w) {
        if(v < 0 || v >= vertices)
            throw new IndexOutOfBoundsException();
        if(w < 0 || w >= vertices)
            throw new IndexOutOfBoundsException();
        edges++;
        adj.get(v).add(w);
        adj.get(w).add(v);
    }

    public int getVertices() {
        return vertices;
    }

    public int getEdges() {
        return edges;
    }

    public Iterable<Integer> adj(int v) {
        if(v < 0 || v >= vertices)
            throw new IndexOutOfBoundsException();
        return adj.get(v);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        String NEWLINE = System.getProperty("line.separator");
        s.append(vertices + " vertices, " + edges + " edges " + NEWLINE);
        for(int v = 0; v < vertices; v++) {
            s.append(v + ": ");
            for(int w : adj.get(v)) {
                s.append(w + " ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }
}

