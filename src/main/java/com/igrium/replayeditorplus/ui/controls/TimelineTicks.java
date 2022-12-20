package com.igrium.replayeditorplus.ui.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;

class TimelineTicks extends Region {
    private final DoubleProperty startProperty = new SimpleDoubleProperty(0);
    
    public double getStart() {
        return startProperty.get();
    }

    public void setStart(double start) {
        startProperty.set(start);
    }

    public DoubleProperty startProperty() {
        return startProperty;
    }

    private final DoubleProperty endProperty = new SimpleDoubleProperty(10);

    public double getEnd() {
        return endProperty.get();
    }

    public void setEnd(double end) {
        endProperty.set(end);
    }

    public DoubleProperty endProperty() {
        return endProperty;
    }

    private final DoubleProperty tickDistanceProperty = new SimpleDoubleProperty(8);

    public double getTickDistance() {
        return tickDistanceProperty.get();
    }

    public void setTickDistance(double tickDistance) {
        tickDistanceProperty.set(tickDistance);
    }

    public DoubleProperty tickDistanceProperty() {
        return tickDistanceProperty;
    }

    private final ObjectProperty<Paint> tickColorProperty = new SimpleObjectProperty<>(Color.GRAY);

    public Paint getTickColor() {
        return tickColorProperty.get();
    }

    public void setTickColor(Paint color) {
        tickColorProperty.set(color);
    }

    public ObjectProperty<Paint> tickColorProperty() {
        return tickColorProperty;
    }

    public TimelineTicks() {
        startProperty.addListener((obs, oldVal, newVal) -> requestLayout());
        endProperty.addListener((obs, oldVal, newVal) -> requestLayout());
        tickDistanceProperty.addListener((obs, oldVal, newVal) -> requestLayout());
    }

    @Override
    protected void layoutChildren() {
        int numSeconds = (int) Math.ceil(getEnd() - getStart());

        final double secondWidth = timeToPos(1);
        final double maxHeight = getHeight() / 2;
        double lineStart = getHeight();

        getChildren().clear();

        int level = 1;
        double subdivision = 1;
        while (secondWidth / (subdivision = Math.pow(2, level - 1)) >= tickDistanceProperty.get()) {
            double lineEnd = lineStart - maxHeight / level;
            for (int i = 0; i <= numSeconds * subdivision; i++) {
                if (level != 1 && i % 2 == 0) continue; // We already drew this line.
                double lineX = i * secondWidth / subdivision;
                Line line = new Line(lineX, lineStart, lineX, lineEnd);
                line.setStrokeWidth(1);
                line.strokeProperty().bind(tickColorProperty);
                getChildren().add(line);
            }
            level++;
        }
    }

    private double timeToPos(double time) {
        double start = getStart();
        double end = getEnd();
        Bounds bounds = getBoundsInLocal();

        return bounds.getMinX() + (time - start) * bounds.getWidth() / (end - start);
    }
}
