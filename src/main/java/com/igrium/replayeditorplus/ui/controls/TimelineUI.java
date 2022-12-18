package com.igrium.replayeditorplus.ui.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

/**
 * Renders a timeline preview with a playhead
 */
public class TimelineUI extends Region {

    /**
     * A single node within the timeline.
     */
    public static class TimelineNode<T extends Node> {
        private final T node;

        /**
         * Create a timeline node in order to add a JavaFX node to a timeline.
         * @param node The base node.
         * @throws IllegalArgumentException If the base node already has a parent.
         */
        public TimelineNode(T node) throws IllegalArgumentException {
            if (node.getParent() != null) {
                throw new IllegalArgumentException("This node already has a parent.");
            }
            this.node = node;
        }

        /**
         * Get the base node.
         * @return The base node.
         */
        public final T getNode() {
            return node;
        }

        private DoubleProperty timeProperty = new SimpleDoubleProperty();

        /**
         * Get the time of this node.
         * @return This node's place in the timeline.
         */
        public double getTime() {
            return timeProperty.get();
        }
        
        /**
         * Set the time of this node.
         * @param time This node's place in the timeline.
         */
        public void setTime(double time) {
            timeProperty.set(time);
        }

        /**
         * This node's place in the timeline.
         * @return Time property.
         */
        public DoubleProperty timeProperty() {
            return timeProperty;
        }

        private ReadOnlyObjectWrapper<TimelineUI> parentProperty = new ReadOnlyObjectWrapper<>();
        
        /**
         * Get the timeline that this node belongs to.
         * @return The parent timeline.
         */
        public TimelineUI getParent() {
            return parentProperty.get();
        }

        final void setParent(TimelineUI parent) {
            parentProperty.set(parent);
        }

        /**
         * The parent timeline.
         */
        public ReadOnlyObjectProperty<TimelineUI> parentProperty() {
            return parentProperty.getReadOnlyProperty();
        }
    }

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

    private final DoubleProperty timeProperty = new SimpleDoubleProperty(0);

    public double getTime() {
        return timeProperty.get();
    }

    public void setTime(double time) {
        timeProperty.set(time);
    }

    public DoubleProperty timeProperty() {
        return timeProperty;
    }

    private final DoubleProperty prefScaleProperty = new SimpleDoubleProperty(128);

    public void setPrefScale(double scale) {
        prefScaleProperty.set(scale);
    }

    public double getPrefScale() {
        return prefScaleProperty.get();
    }

    public DoubleProperty prefScaleProperty() {
        return prefScaleProperty;
    }

    private final ObjectProperty<Paint> playheadColor = new SimpleObjectProperty<>(Color.ORANGE);

    public Paint getPlayheadColor() {
        return playheadColor.get();
    }

    public void setPlayheadColor(Paint color) {
        playheadColor.set(color);
    }

    public ObjectProperty<Paint> playheadColorProperty() {
        return playheadColor;
    }

    private ObservableList<TimelineNode<?>> nodes = FXCollections.observableArrayList();
    private BiMap<TimelineNode<?>, Node> baseNodes = HashBiMap.create();
    // private Playhead playhead;
    private Node playhead;

    /**
     * Get all the nodes that are a part of this timeline.
     * @return A modifiable list of all the nodes.
     */
    public ObservableList<TimelineNode<?>> getNodes() {
        return nodes;
    }

    private InvalidationListener invalidationListener = val -> requestLayout();

    public TimelineUI() {
        nodes.addListener(new ListChangeListener<TimelineNode<?>>() {

            public void onChanged(Change<? extends TimelineNode<?>> c) {
                c.getAddedSubList().forEach(TimelineUI.this::onAddNode);
                c.getRemoved().forEach(TimelineUI.this::onRemoveNode);
                playhead.toFront();

                requestLayout();
            }
            
        });
        playhead = createPlayhead();
        getChildren().add(playhead);

        startProperty.addListener(invalidationListener);
        endProperty.addListener(invalidationListener);
        timeProperty.addListener(invalidationListener);
        prefScaleProperty.addListener(invalidationListener);
    }

    private Node createPlayhead() {        
        Polygon polygon = new Polygon(
            -5, 0,
            5, 0,
            5, 5,
            0, 10,
            -5, 5
        );
        polygon.fillProperty().bind(playheadColorProperty());

        Line line = new Line(0, 0, 0, 128);
        line.endYProperty().bind(heightProperty());
        line.strokeProperty().bind(playheadColorProperty());
        Group group = new Group(polygon, line);
        
        DropShadow dropShadow = new DropShadow();
        dropShadow.setOffsetX(1);
        dropShadow.setOffsetY(1);
        dropShadow.setRadius(1);
        group.setEffect(dropShadow);
        
        return group;
    }

    private void onAddNode(TimelineNode<?> node) {
        getChildren().add(node.getNode());
        baseNodes.put(node, node.getNode());
        node.timeProperty().addListener(invalidationListener);
    }

    private void onRemoveNode(TimelineNode<?> node) {
        getChildren().remove(node.getNode());
        baseNodes.remove(node);
        node.timeProperty().removeListener(invalidationListener);
    }

    private double timeToPos(double time) {
        double start = getStart();
        double end = getEnd();
        Bounds bounds = getBoundsInLocal();

        return bounds.getMinX() + (time - start) * bounds.getWidth() / (end - start);
    }

    @Override
    protected double computePrefHeight(double width) {
        return 128;
    }

    @Override
    protected double computePrefWidth(double height) {
        return (getEnd() - getStart()) * getPrefScale();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        // Set playhead
        double playheadPos = timeToPos(getTime());
        playhead.autosize();
        playhead.relocate(playheadPos, 0);
        
        List<Bounds> occupiedRanges = new ArrayList<>();
        for (TimelineNode<?> timelineNode : nodes) {
            Node node = timelineNode.getNode();
            node.autosize();
            
            Bounds localBounds = node.getBoundsInLocal();
            double xPos = timeToPos(timelineNode.getTime());
            double yPos = localBounds.getMinY(); // Start below the top of the frame
            Bounds bounds = transformBounds(localBounds, xPos, yPos);

            // The complexity of this isn't great, but the data set isn't large so I don't care.
            Bounds overlapping;
            while ((overlapping = getIf(occupiedRanges, bounds::intersects)) != null) {
                yPos = overlapping.getMaxY() + localBounds.getMinY();
                bounds = transformBounds(localBounds, xPos, yPos);
            }
            
            occupiedRanges.add(bounds);
            node.relocate(xPos, yPos);
        }
    }

    private Bounds transformBounds(Bounds bounds, double x, double y) {
        return new BoundingBox(bounds.getMinX() + x, bounds.getMinY() + y, bounds.getWidth(), bounds.getHeight());
    }
    
    private <T> T getIf(Iterable<T> collection, Predicate<T> predicate) {
        for (T item : collection) {
            if (predicate.test(item)) return item;
        }
        return null;
    }
}
