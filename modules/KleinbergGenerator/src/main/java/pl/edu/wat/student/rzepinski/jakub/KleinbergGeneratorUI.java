package pl.edu.wat.student.rzepinski.jakub;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.*;

import static pl.edu.wat.student.rzepinski.jakub.KleinbergGenerator.DEFAULT_CLUSTERING_COEFFICIENT;
import static pl.edu.wat.student.rzepinski.jakub.KleinbergGenerator.DEFAULT_GRID_SIZE;

@ServiceProvider(service = KleinbergGeneratorUI.class)
public class KleinbergGeneratorUI implements GeneratorUI {

    private static final String GRID_SIZE_LABEL = "Grid size: ";
    private static final String CLUSTERING_COEFFICIENT_LABEL = "Clustering coefficient: ";
    private static final String TORUS_MODE_LABEL = "Torus mode: ";

    private final JPanel panel;
    private JSpinner gridSizeSpinner;
    private JSpinner clusteringCoefficientSpinner;
    private JCheckBox torusModeCheckbox;
    private KleinbergGenerator generator;

    public KleinbergGeneratorUI() {
        panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2));
        int padding = 15;
        panel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

        panel.add(new JLabel(GRID_SIZE_LABEL, SwingConstants.RIGHT));
        gridSizeSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_GRID_SIZE, 2, null, 1));
        panel.add(gridSizeSpinner);

        panel.add(new JLabel(CLUSTERING_COEFFICIENT_LABEL, SwingConstants.RIGHT));
        clusteringCoefficientSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_CLUSTERING_COEFFICIENT, 0, null, 1));
        panel.add(clusteringCoefficientSpinner);

        panel.add(new JLabel(TORUS_MODE_LABEL, SwingConstants.RIGHT));
        torusModeCheckbox = new JCheckBox();
        torusModeCheckbox.setSelected(KleinbergGenerator.DEFAULT_TORUS_MODE);
        panel.add(torusModeCheckbox);
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void setup(Generator generator) {
        if (!(generator instanceof KleinbergGenerator)) {
            throw new IllegalArgumentException("Wrong generator's type: " + generator.getClass() + ", only KleinbergGenerator accepted");
        }
        this.generator = (KleinbergGenerator) generator;

        gridSizeSpinner.setValue(this.generator.getGridSize());
        clusteringCoefficientSpinner.setValue(this.generator.getClusteringCoefficient());
        torusModeCheckbox.setSelected(this.generator.isTorusMode());
    }

    @Override
    public void unsetup() {
        this.generator.setGridSize((Integer) gridSizeSpinner.getValue());
        this.generator.setClusteringCoefficient((Integer) clusteringCoefficientSpinner.getValue());
        this.generator.setTorusMode(torusModeCheckbox.isSelected());
    }
}