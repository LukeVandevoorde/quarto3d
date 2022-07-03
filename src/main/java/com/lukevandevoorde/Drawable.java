package com.lukevandevoorde;

import processing.core.PVector;

public abstract class Drawable {

    protected Viewport viewport;
    protected TransformData transform;
    protected PVector dimensions;

    public Drawable(Viewport viewport, TransformData transform, PVector dimensions) {
        this.viewport = viewport;
        this.transform = transform;
        this.dimensions = dimensions;
    }

    public abstract void draw();

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public void setTransform(TransformData transformData) {
        this.transform = transformData;
    }

    public abstract void setDimensions(PVector newDimensions);

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
