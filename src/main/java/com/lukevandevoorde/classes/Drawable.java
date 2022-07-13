package com.lukevandevoorde.classes;

import processing.core.PVector;

public abstract class Drawable {

    protected Viewport viewport;
    protected final TransformData transform;
    protected final PVector dimensions;

    public Drawable(Viewport viewport, TransformData transform, PVector dimensions) {
        this.viewport = viewport;
        this.transform = new TransformData(transform);
        this.dimensions = new PVector(dimensions.x, dimensions.y, dimensions.z);
    }

    public abstract void draw();

    public abstract void setDimensions(PVector newDimensions);

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }
    
    public void setTransform(TransformData transformData) {
        transform.setX(transformData.getX());
        transform.setY(transformData.getY());
        transform.setZ(transformData.getZ());
        transform.setRotX(transformData.getRotX());
        transform.setRotY(transformData.getRotY());
        transform.setRotZ(transformData.getRotZ());
    }

    public float getWidth() {
        return dimensions.x;
    }

    public float getDepth() {
        return dimensions.y;
    }

    public float getHeight() {
        return dimensions.z;
    }
}
