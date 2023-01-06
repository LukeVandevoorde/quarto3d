package com.lukevandevoorde.classes;

import java.util.LinkedList;
import com.lukevandevoorde.Main;
import processing.core.PVector;

public class AnimationManager {
    
    private TransformData baseTransform;
    private PVector baseDimensions;
    private final LinkedList<TransformData> transformSequence;
    private final LinkedList<PVector> dimensionSequence;
    private final LinkedList<Integer> durations;  
    private int millisStart;
    private boolean animating;
    private boolean buffered;

    private final TransformData lerpTransformData;
    private final PVector lerpDimensions;

    public AnimationManager(TransformData initTransform, PVector initDimensions) {
        transformSequence = new LinkedList<TransformData>();
        dimensionSequence = new LinkedList<PVector>();
        durations = new LinkedList<Integer>();
        lerpTransformData = new TransformData();
        lerpDimensions = new PVector();
        baseTransform = new TransformData(initTransform);
        baseDimensions = new PVector(initDimensions.x, initDimensions.y, initDimensions.z);
        animating = false;
        buffered = true;
    }

    public static int calcAnimationTime(int pixelsPerSecond, int radiansPerSecond, TransformData t1, TransformData t2) {
        float dist = t1.getPosition().dist(t2.getPosition());
        float maxAng = Math.max(Math.abs(t2.getRotX()-t1.getRotX()), Math.max(Math.abs(t2.getRotY()-t1.getRotY()), Math.abs(t2.getRotZ()-t1.getRotZ())));

        return (int)(1000*Math.max(dist/pixelsPerSecond, maxAng/radiansPerSecond));
    }

    public boolean animating() {
        return this.animating;
    }

    // Adds an animation step, and starts animating if necessary
    public void enqueueAnimation(TransformData transform, PVector dimensions, int millisDuration) {
        if (!animating) {
            animating = true;
            buffered = false;
            millisStart = Main.TIME_KEEPER.millis();
        }

        transformSequence.add(transform == null ? (transformSequence.size() > 0 ? transformSequence.getLast() : baseTransform) : new TransformData(transform));
        dimensionSequence.add(dimensions == null ? (dimensionSequence.size() > 0 ? dimensionSequence.getLast() : baseDimensions) : new PVector(dimensions.x, dimensions.y, dimensions.z));
        durations.add(millisDuration);
    }

    public void set(Drawable drawable) {
        if (animating) {
            drawable.setTransform(this.currentTransform());
            drawable.setDimensions(this.currentDimensions());
        } else if (!buffered) {
            drawable.setTransform(this.currentTransform());
            drawable.setDimensions(this.currentDimensions());
            buffered = true;
        }
    }

    // Skips through all the animations and sets transform and dimensions to those found in the last step
    public void flush() {
        if (!animating) return;
        animating = false;
        baseTransform = transformSequence.getLast();
        transformSequence.clear();
        baseDimensions = dimensionSequence.getLast();
        dimensionSequence.clear();

        durations.clear();
    }

    public void flushSetTransform(TransformData transform) {
        flush();
        baseTransform = new TransformData(transform);
        buffered = false;
    }

    public void flushSetDimensions(PVector dimensions) {
        flush();
        baseDimensions = new PVector(dimensions.x, dimensions.y, dimensions.z);
        buffered = false;
    }

    // If animating, returns the linearly interpolated transform based on the time through the current remaining animation steps.
    // Otherwise, returns baseTransform
    public TransformData currentTransform() {
        if (!animating) {
            return baseTransform;
        }
        
        checkPop();

        if (!animating) {
            return baseTransform;
        } else {
            float frac = (float)(Main.TIME_KEEPER.millis() - millisStart) / this.durations.getFirst();
            TransformData.doLerp(lerpTransformData, this.baseTransform, this.transformSequence.getFirst(), frac);
            return lerpTransformData;
        }
    }

    // If animating, returns the linearly interpolated dimensions vector based on the time through the current remaining animation steps.
    // Otherwise, returns baseTransform
    public PVector currentDimensions() {
        if (!animating) { 
            return baseDimensions;
        }
        
        checkPop();

        if (!animating) {
            return baseDimensions;
        } else {
            float frac = (float)(Main.TIME_KEEPER.millis() - millisStart) / this.durations.getFirst();
            lerpDimensions.set(frac*(this.dimensionSequence.getFirst().x - this.baseDimensions.x) + baseDimensions.x,
                            frac*(this.dimensionSequence.getFirst().y - this.baseDimensions.y) + baseDimensions.y,
                            frac*(this.dimensionSequence.getFirst().z - this.baseDimensions.z) + baseDimensions.z);
            return lerpDimensions;
        }
    }

    private void checkPop() {
        while (animating && Main.TIME_KEEPER.millis() > millisStart + durations.getFirst()) {
            millisStart += durations.removeFirst();
            baseTransform = transformSequence.removeFirst();
            baseDimensions = dimensionSequence.removeFirst();
            animating = durations.size() > 0;
        }
    }
}