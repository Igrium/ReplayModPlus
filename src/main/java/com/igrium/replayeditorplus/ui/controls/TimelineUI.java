package com.igrium.replayeditorplus.ui.controls;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Region;

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

    private final DoubleProperty prefScaleProperty = new SimpleDoubleProperty(1);

    public void setPrefScale(double scale) {
        prefScaleProperty.set(scale);
    }

    private ObservableList<TimelineNode<?>> nodes = FXCollections.observableArrayList();
    private BiMap<TimelineNode<?>, Node> baseNodes = HashBiMap.create();

    /**
     * Get all the nodes that are a part of this timeline.
     * @return A modifiable list of all the nodes.
     */
    public ObservableList<TimelineNode<?>> getNodes() {
        return nodes;
    }

    public TimelineUI() {
        nodes.addListener(new ListChangeListener<TimelineNode<?>>() {

            public void onChanged(Change<? extends TimelineNode<?>> c) {
                c.getAddedSubList().forEach(TimelineUI.this::onAddNode);
                c.getRemoved().forEach(TimelineUI.this::onRemoveNode);
            }
            
        });
    }

    private void onAddNode(TimelineNode<?> node) {
        getChildren().add(node.getNode());
        baseNodes.put(node, node.getNode());
    }

    private void onRemoveNode(TimelineNode<?> node) {
        getChildren().remove(node.getNode());
        baseNodes.remove(node);
    }
}
