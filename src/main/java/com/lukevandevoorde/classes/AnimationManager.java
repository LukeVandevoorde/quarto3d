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

    private final TransformData lerpTransformData;
    private final PVector lerpPVector;

    public AnimationManager(TransformData initTransform, PVector initDimensions) {
        transformSequence = new LinkedList<TransformData>();
        dimensionSequence = new LinkedList<PVector>();
        durations = new LinkedList<Integer>();
        lerpTransformData = new TransformData();
        lerpPVector = new PVector();
        baseTransform = new TransformData(initTransform);
        baseDimensions = new PVector(initDimensions.x, initDimensions.y, initDimensions.z);
        animating = false;
    }

    // Adds an animation step, and starts animating if necessary
    public void enqueueAnimation(TransformData transform, PVector dimensions, int millisDuration) {
        if (!animating) {
            animating = true;
            millisStart = Main.TIME_KEEPER.millis();
        }

        transformSequence.add(transform);
        dimensionSequence.add(dimensions);
        durations.add(millisDuration);
    }

    // Skips through all the animations and sets transform and dimensions to those found in the last step
    public void flush() {
        if (!animating) return;

        baseTransform = transformSequence.getLast();
        transformSequence.clear();
        baseDimensions = dimensionSequence.getLast();
        dimensionSequence.clear();

        animating = false;
    }

    // Skips through all the animations and sets transform and dimensions to those passed in. If none are passed in, then the last transform/dim are used
    public void flushAndSetBase(TransformData transform, PVector dimensions) {
        baseTransform = (transform == null) ? transformSequence.getLast() : transform;
        baseDimensions = (dimensions == null) ? dimensionSequence.getLast() : dimensions;
        transformSequence.clear();
        dimensionSequence.clear();
        durations.clear();
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
            float frac = (Main.TIME_KEEPER.millis() - millisStart) / this.durations.getFirst();
            lerpPVector.set(frac*(this.dimensionSequence.getFirst().x - this.baseDimensions.x) + baseDimensions.x,
                            frac*(this.dimensionSequence.getFirst().y - this.baseDimensions.y) + baseDimensions.y,
                            frac*(this.dimensionSequence.getFirst().z - this.baseDimensions.z) + baseDimensions.z);
            return lerpPVector;
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