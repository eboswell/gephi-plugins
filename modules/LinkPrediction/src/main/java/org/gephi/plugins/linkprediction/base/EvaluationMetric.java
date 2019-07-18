package org.gephi.plugins.linkprediction.base;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gephi.graph.api.*;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import java.io.Serializable;
import java.util.*;

/**
 * Calculates the metric to evaluate the quality of a link prediction algorithm.
 * <p>
 * This base class contains all metric-independent implementations. The
 * base class is extended by the implementations of the respective quality metrics.
 *
 * @author Marco Romanutti
 */
public abstract class EvaluationMetric implements Serializable {
    /**
     * Initial graph
     */
    protected final Graph initial;
    /**
     * Trained graph
     */
    protected Graph trained;
    /**
     * Validation graph
     */
    protected final Graph validation;
    /**
     * Validation Workspace
     */
    protected final Workspace validationWS;
    /**
     * Initial Workspace
     */
    protected final Workspace initialWS;
    /**
     * Algorithm to evaluate
     */
    protected final LinkPredictionStatistics statistic;
    /**
     * Calculated results per iteration
     */
    protected Map<Integer, Double> iterationResults = new HashMap<>();
    /**
     * Final results after all iterations
     */
    protected double finalResult;
    /**
     * Number of edges to predict
     */
    protected int diffEdgeCount;

    // Serial uid
    private static final long serialVersionUID = 3505122041350261811L;

    // Console Logger
    protected static Logger consoleLogger = LogManager.getLogger(EvaluationMetric.class);

    public EvaluationMetric(LinkPredictionStatistics statistic, Graph initial, Graph validation, Workspace initialWS,
                            Workspace validationWS) {
        this.statistic = statistic;
        this.initial = initial;
        this.validation = validation;
        this.initialWS = initialWS;
        this.validationWS = validationWS;
    }

    /**
     * Calculates respective metric for link prediction algorithm.
     *
     * @param addedEdges Number of edges added
     * @param trained    Graph on which edges will be added
     * @param validation Graph used for validation
     * @param statistics Algorithm to use for prediction
     * @return Metric value
     */
    public abstract double calculate(int addedEdges, Graph trained, Graph validation,
                                     LinkPredictionStatistics statistics);

    /**
     * Runs the calculation of prediction and evaluates initial and validation graph.
     */
    public void run() {
        consoleLogger.debug("Calcualte evaluation metric");

        //Look if the result column already exist and create it if needed
        consoleLogger.debug("Initialize columns");
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Project pr = pc.getCurrentProject();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        Table edgeTable = gc.getGraphModel().getEdgeTable();
        LinkPredictionStatistics.initializeColumns(edgeTable);

        // Determine how many edges have to be calculated and then uses the chosen algorithm
        Set<Edge> initialEdges = new HashSet<>(Arrays.asList(initial.getEdges().toArray()));
        consoleLogger.log(Level.DEBUG, () -> "Initial edges count: " + initialEdges.size());
        Set<Edge> validationEdges = new HashSet<>(Arrays.asList(validation.getEdges().toArray()));
        consoleLogger.log(Level.DEBUG, () -> "Validation edges count: " + validationEdges.size());

        // Create workspace to add predicted edges
        consoleLogger.debug("Create new workspace");
        Workspace initWorkspace = pc.getCurrentWorkspace();
        Workspace newWorkspace = pc.newWorkspace(pr);
        pc.renameWorkspace(newWorkspace, getAlgorithmName());

        // Determines current graph model and number of edges to predict
        consoleLogger.debug("Determine current graph model");
        GraphModel currentGraphModel = determineCurrentGraphModel(gc, initialEdges, validationEdges);

        // Duplicate nodes from current to new workspace
        Graph current = currentGraphModel.getGraph();
        GraphModel trainedModel = gc.getGraphModel(newWorkspace);
        consoleLogger.debug("Duplicate nodes");
        trainedModel.bridge().copyNodes(current.getNodes().toArray());
        pc.openWorkspace(newWorkspace);

        // Predict links and save metric per iteration
        consoleLogger.debug("Predict links");
        trained = trainedModel.getGraph();
        Set<Edge> trainedEdges = new HashSet<>(Arrays.asList(trained.getEdges().toArray()));
        consoleLogger.log(Level.DEBUG, () -> "Trained edges count: " + trainedEdges.size());
        predictLinks(validationEdges, trainedModel, trainedEdges);

        // Calculate final accuracy of algorithm
        finalResult = calculateCurrentResult(trainedEdges.size(), validationEdges.size());
        consoleLogger.log(Level.DEBUG, () -> "Final result :" + finalResult);

        // Get back to init workspace
        pc.openWorkspace(initWorkspace);

    }

    /**
     * Gets calculated evaluation results per iteration.
     *
     * @return Calculated metric values per iteration.
     */
    public Map<Integer, Double> getIterationResults() {
        return iterationResults;
    }

    /**
     * Gets calculated final evaluation result.
     *
     * @return Calculated final metric value.
     */
    public double getFinalResult() {
        return finalResult;
    }

    /**
     * Gets number of edges to predict.
     *
     * @return Number of edges to predict
     */
    public int getDiffEdgeCount() {
        return diffEdgeCount;
    }

    /**
     * Gets the name of the algorithm.
     *
     * @return Algorithm name
     */
    public String getAlgorithmName() {
        return statistic.getAlgorithmName();
    }

    /**
     * Gets the algorithm used.
     *
     * @return Used algorithm
     */
    public LinkPredictionStatistics getStatistic() {
        return statistic;
    }

    /**
     * Evaluates if evaluation metric has the same underlying statistic algorithm.
     *
     * @param o Object to compare
     * @return Equality of two evaluation metrics
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EvaluationMetric)) {
            return false;
        }
        return (((EvaluationMetric) o).statistic.getClass().equals(this.statistic.getClass()));
    }

    /**
     * Hashes class from statistic algorithm.
     *
     * @return Hashed statistic class
     */
    @Override
    public int hashCode() {
        return Objects.hash(statistic.getClass());
    }

    /**
     * Processes link prediction and calculation of current metric iteratively.
     *
     * @param validationEdges Edges of validation graph
     * @param trainedModel    Models to predict links on
     * @param trainedEdges    Edges of trained graph
     */
    private void predictLinks(Set<Edge> validationEdges, GraphModel trainedModel, Set<Edge> trainedEdges) {
        consoleLogger.debug("Predict links");
        // Predict i new edges
        for (int i = 1; i <= diffEdgeCount; i++) {
            statistic.execute(trainedModel);

            // Get number of edges per iteration
            if (consoleLogger.isDebugEnabled()) {
                consoleLogger.debug("Trained edges in iteration " + i + ": " + trainedEdges.size());
                consoleLogger.debug("Validation edges in iteration " + i + ": " + validationEdges.size());
            }

            // Calculate current accuracy of algorithm
            double currentResult = calculateCurrentResult(trainedEdges.size(), validationEdges.size());
            iterationResults.put(i, currentResult);
            if (consoleLogger.isDebugEnabled()) {
                consoleLogger.debug("Current result in iteration " + i + ": " + currentResult);
            }
        }
    }

    /**
     * Determines number of edges to predict and current graph model.
     *
     * @param gc              Current graph controller
     * @param initialEdges    Set of edges of initial graph
     * @param validationEdges Set of edges from validation graph
     * @return Current graph model
     */
    private GraphModel determineCurrentGraphModel(GraphController gc, Set<Edge> initialEdges,
                                                  Set<Edge> validationEdges) {
        consoleLogger.debug("Determine current graph model");
        GraphModel currentGraphModel;

        // Determine which graph model to use for validation
        if (initialEdges.size() > validationEdges.size()) {
            diffEdgeCount = initialEdges.size() - validationEdges.size();
            currentGraphModel = gc.getGraphModel(validationWS);
            consoleLogger.debug("Initial graph is bigger than validation graph");
        } else {
            diffEdgeCount = validationEdges.size() - initialEdges.size();
            currentGraphModel = gc.getGraphModel(initialWS);
            consoleLogger.debug("Validation graph is bigger than initial graph");
        }

        return currentGraphModel;
    }

    /**
     * Calculates the metric at current situation.
     *
     * @param trainedEdgesSize    Number of edges of trained graph
     * @param validationEdgesSize Number of edges of validation graph
     * @return Metric result
     */
    private double calculateCurrentResult(int trainedEdgesSize, int validationEdgesSize) {
        consoleLogger.debug("Calculate current result");
        double currentResult;
        int addedEdges = validationEdgesSize - trainedEdgesSize;

        // Calculate metric
        if (trainedEdgesSize > validationEdgesSize) {
            consoleLogger.debug("More trained edges than validation edges");
            currentResult = calculate(addedEdges, validation, trained, statistic);
        } else if (trainedEdgesSize < validationEdgesSize) {
            consoleLogger.debug("Less trained edges than validation edges");
            currentResult = calculate(addedEdges, trained, validation, statistic);
        } else {
            consoleLogger.debug("Same number of trained edges and validation edges");
            currentResult = calculate(addedEdges, trained, validation, statistic);
        }

        return currentResult;
    }
}
