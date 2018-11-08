package nl.liacs.takesstatisticsui;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

import nl.liacs.takesstatistics.*;

@ServiceProvider(service = StatisticsUI.class)
public class DiameterMetricUI implements StatisticsUI {
    private DiameterMetricPanel panel;
    private DiameterMetric metric;

    @Override
    public JPanel getSettingsPanel() {
        panel = new DiameterMetricPanel();
        return (JPanel) panel;
    }

    @Override
    public void setup(Statistics statistics) {
        this.metric = (DiameterMetric) statistics;
        if (panel != null) {
            panel.setEccentricitiesFlag(metric.getEccentricitiesFlag());
            panel.setPeripheryFlag(metric.getPeripheryFlag());
            panel.setCenterFlag(metric.getCenterFlag());
        }
    }

    @Override
    public void unsetup() {
        if (panel != null && metric != null) {
            metric.setEccentricitiesFlag(panel.getEccentricitiesFlag());
            metric.setPeripheryFlag(panel.getPeripheryFlag());
            metric.setCenterFlag(panel.getCenterFlag());
        }
        panel = null;
        metric = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return DiameterMetric.class;
    }

    @Override
    public String getValue() {
        return (metric != null) ? (metric.getDiameter() + " / " + metric.getRadius()) : "";
    }

    @Override
    public String getDisplayName() {
        return "BoundingDiameter";
    }

    @Override
    public String getShortDescription() {
        return "Compute diameter and radius, using the BoundingDiameter algorithm";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 900;
    }
    
}