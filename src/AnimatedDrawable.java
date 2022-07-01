import processing.core.PVector;

public class AnimatedDrawable extends Drawable {
    private Drawable drawable;
    private TransformData transformTo;
    private PVector dimensionTo;
    private int millisStart, millisDuration;
    private boolean animating;
    
    public AnimatedDrawable(Drawable drawable) {
        super(drawable.viewport, drawable.transform, drawable.dimensions);
        drawable.setTransform(TransformData.ZERO_TRANSFORM);
        this.drawable = drawable;
        this.animating = false;
        this.transformTo = transform;
        this.dimensionTo = dimensions;
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        drawable.setDimensions(newDimensions);
        // this.dimensions = newDimensions;
    }

    @Override
    public void setTransform(TransformData newTransform) {
        this.transform = newTransform;
        // drawable.setTransform(newTransform);
    }

    @Override
    public void draw() {
        if (animating && Main.TIME_KEEPER.millis() > millisStart + millisDuration) {
            animating = false;
            this.transform = this.transformTo;
            this.dimensions = this.dimensionTo;
            this.transformTo = null;
            this.dimensionTo = null;
        }
        drawable.setTransform(this.currentTransform());
        drawable.setDimensions(this.currentDimension());
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

    public void reverse() {
        System.out.println("Reversed");
        assert transformTo != null;
        assert dimensionTo != null;

        TransformData tempTfm = this.transformTo;
        PVector tempDim = this.dimensionTo;

        this.transformTo = transform;
        this.dimensionTo = dimensions;
        this.transform = tempTfm;
        this.dimensions = tempDim;

        int timeElapsedSinceAnimationStart = Main.TIME_KEEPER.millis() - millisStart;
        
        if (animating || timeElapsedSinceAnimationStart < millisDuration) {
            millisStart = millisStart + 2*timeElapsedSinceAnimationStart - millisDuration;
        } else {
            millisStart = Main.TIME_KEEPER.millis();
        }
        // millisDuration = Math.min(millisDuration, Main.TIME_KEEPER.millis() - millisStart);
        animating = true;
    }

    public void animate(TransformData t, PVector d, int millisDuration) {
        // if (animating) {
        //     this.transform = this.currentTransform();
        //     this.dimensions = this.currentDimension();
        // } else {
        //     this.transform = this.transformTo;
        //     this.dimensions = this.dimensionTo;
        // }
        this.transform = this.currentTransform();
        this.dimensions = this.currentDimension();
        this.transformTo = t;
        this.dimensionTo = d;
        millisStart = Main.TIME_KEEPER.millis();
        this.millisDuration = millisDuration;
        animating = true;
    }

    public TransformData currentTransform() {
        if (!animating) {
            return this.transform;
        }
        return TransformData.lerp(this.transform, this.transformTo, (float)(Main.TIME_KEEPER.millis() - millisStart) / millisDuration);
    }

    public PVector currentDimension() {
        if (!animating) {
            return this.dimensions;
        }
        return PVector.lerp(this.dimensions, this.dimensionTo, (float)(Main.TIME_KEEPER.millis() - millisStart) / millisDuration);
    }
}
