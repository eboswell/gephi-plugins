package org.gephi.plugins.linkprediction.statistics;

import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.openide.util.Lookup;

import static org.gephi.plugins.linkprediction.base.LinkPredictionStatistics.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Ignore class PreferentialAttachmentStatisticsTest {
    GraphModel graphModel;

    @BeforeEach void setUp() {
        //Init project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get the default graph model
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        //Create nodes
        GraphFactory factory = graphModel.factory();
        Node a = factory.newNode("A");
        a.setLabel("Node A");
        Node b = factory.newNode("B");
        b.setLabel("Node B");
        Node c = factory.newNode("C");
        c.setLabel("Node C");
        Node d = factory.newNode("D");
        d.setLabel("Node D");
        Node e = factory.newNode("E");
        e.setLabel("Node E");
        Node f = factory.newNode("F");
        f.setLabel("Node F");
        Node g = factory.newNode("G");
        g.setLabel("Node G");
        Node h = factory.newNode("H");
        h.setLabel("Node H");
        Node i = factory.newNode("I");
        i.setLabel("Node I");

        //Create edges
        Edge e1 = factory.newEdge("E1", a, b, 1, 1, false);
        Edge e2 = factory.newEdge("E2", a, d, 1, 1, false);
        Edge e3 = factory.newEdge("E3", a, e, 1, 1, false);
        Edge e4 = factory.newEdge("E4", b, d, 1, 1, false);
        Edge e5 = factory.newEdge("E5", b, c, 1, 1, false);
        Edge e6 = factory.newEdge("E6", c, d, 1, 1, false);
        Edge e7 = factory.newEdge("E7", c, f, 1, 1, false);
        Edge e8 = factory.newEdge("E8", e, f, 1, 1, false);
        Edge e9 = factory.newEdge("E9", e, g, 1, 1, false);
        Edge e10 = factory.newEdge("E10", f, g, 1, 1, false);
        Edge e11 = factory.newEdge("E11", g, h, 1, 1, false);
        Edge e12 = factory.newEdge("E12", g, i, 1, 1, false);

        // Add nodes
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        undirectedGraph.addNode(a);
        undirectedGraph.addNode(b);
        undirectedGraph.addNode(c);
        undirectedGraph.addNode(d);
        undirectedGraph.addNode(e);
        undirectedGraph.addNode(f);
        undirectedGraph.addNode(g);
        undirectedGraph.addNode(h);
        undirectedGraph.addNode(i);
        undirectedGraph.addEdge(e1);
        undirectedGraph.addEdge(e2);
        undirectedGraph.addEdge(e3);
        undirectedGraph.addEdge(e4);
        undirectedGraph.addEdge(e5);
        undirectedGraph.addEdge(e6);
        undirectedGraph.addEdge(e7);
        undirectedGraph.addEdge(e8);
        undirectedGraph.addEdge(e9);
        undirectedGraph.addEdge(e10);
        undirectedGraph.addEdge(e11);
        undirectedGraph.addEdge(e12);
    }

    @Ignore @org.junit.jupiter.api.Test void testExecute_EdgeCount() {

        LinkPredictionStatistics statistic = new PreferentialAttachmentStatistics();
        int edgesCountOriginal = graphModel.getGraph().getEdges().toArray().length;

        statistic.execute(graphModel);
        int edgesCountNew = graphModel.getGraph().getEdges().toArray().length;

        assertEquals(edgesCountOriginal + 1, edgesCountNew);
    }

    @Ignore @org.junit.jupiter.api.Test void testGetHighestPrediction_Successfully() {

        LinkPredictionStatistics statistic = new PreferentialAttachmentStatistics();
        statistic.execute(graphModel);

        Edge max = statistic.getHighestPrediction();

        assertTrue(max.getSource().getLabel().equals("Node A"));
        assertTrue(max.getTarget().getLabel().equals("Node G"));
        assertTrue(max.getAttribute(getColLastPrediction()).equals(PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME));
        assertTrue((int) max.getAttribute(getColAddedInRun()) == 1);
        assertTrue((int) max.getAttribute(getColLastCalculatedValue()) == 12);
    }

    @Ignore @org.junit.jupiter.api.Test void testExecute_Successfully() {

        LinkPredictionStatistics statistic = new PreferentialAttachmentStatistics();
        statistic.execute(graphModel);

        Edge max = statistic.getHighestPrediction();

        assertTrue(graphModel.getGraph().contains(max));
    }

    @Ignore @org.junit.jupiter.api.Test void testGetNextIteration_Successfully() {
        LinkPredictionStatistics statistic = new PreferentialAttachmentStatistics();

        Table edgeTable = graphModel.getEdgeTable();
        statistic.initializeColumns(edgeTable);

        int firstNext = statistic.getNextIteration(graphModel.getGraph(), PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);
        assertEquals(1, firstNext);

        statistic.execute(graphModel);

        int secondNext = statistic.getNextIteration(graphModel.getGraph(), PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME);
        assertEquals(2, secondNext);
    }

}