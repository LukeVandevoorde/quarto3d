package com.lukevandevoorde.classes;

import processing.core.PVector;

public class AnimatedDrawable extends Drawable {
    private Drawable drawable;
    private AnimationManager animationManager;

    public AnimatedDrawable(Drawable drawable) {
        super(drawable.viewport, new TransformData(), drawable.dimensions);
        animationManager = new AnimationManager(drawable.transform, drawable.dimensions);
        this.drawable = drawable;
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        animationManager.flushAndSetBase(null, newDimensions);
    }

    @Override
    public void setTransform(TransformData newTransform) {
        animationManager.flushAndSetBase(newTransform, null);
    }

    @Override
    public void draw() {
        drawable.setTransform(animationManager.currentTransform());
        drawable.setDimensions(animationManager.currentDimensions());
        drawable.draw();
    }

    @Override
    public float getHeight() {
        return drawable.getHeight();
    }

    @Override
    public float getWidth() {
        return drawable.getWidth();
    }

    @Override
    public float getDepth() {
        return drawable.getDepth();
    }

    public void animate(TransformData t, PVector d, int millisDuration) {
        animationManager.enqueueAnimation(t, d, millisDuration);
    }

    public void skipAnimation() {
        animationManager.flush();
    }
}
