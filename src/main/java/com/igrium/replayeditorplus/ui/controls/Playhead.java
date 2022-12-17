package com.igrium.replayeditorplus.ui.controls;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;

class Playhead extends Region {
    private final Canvas canvas = new Canvas();
    private ObjectProperty<Paint> paintProperty = new SimpleObjectProperty<>();
    private DoubleProperty handleSizeProperty = new SimpleDoubleProperty(5);

    private InvalidationListener invalidationListener = val -> redraw();
    
    public Paint getPaint() {
        return paintProperty.get();
    }

    public void setPaint(Paint paint) {
        paintProperty.set(paint);
    }

    public ObjectProperty<Paint> paintProperty() {
        return paintProperty;
    }

    public double getHandleSize() {
        return handleSizeProperty.get();
    }

    public void setHandleSize(double handleSize) {
        handleSizeProperty.set(handleSize);
    }

    public Playhead() {
        getChildren().add(canvas);
        paintProperty.addListener(invalidationListener);
        handleSizeProperty.addListener(invalidationListener);
    }

    @Override
    protected void layoutChildren() {
        layoutInArea(canvas, 0, 0, getWidth(), getHeight(), 0, HPos.LEFT, VPos.TOP);
        redraw();
    }
    
    @Override
    protected double computeMinWidth(double height) {
        return getHandleSize() * 2;
    }

    private void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Affine transform = new Affine(Affine.translate(getWidth() / 2, 0));
        // gc.setTransform(transform);

        double handleSize = getHandleSize();

        double[] xPoints = new double[] {
            handleSize,
            -handleSize,
            -handleSize,
            0,
            handleSize
        };

        double[] yPoints = new double[] {
            0,
            0,
            -handleSize,
            -handleSize * 2,
            -handleSize
        };
        
        gc.setFill(getPaint());
        gc.fillPolygon(xPoints, yPoints, 5);

        gc.setStroke(getPaint());
        gc.strokeLine(0, 0, 0, getHeight());
    }

    
}