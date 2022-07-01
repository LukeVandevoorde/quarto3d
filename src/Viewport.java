import processing.core.PGraphics;
import processing.core.PApplet;
import processing.core.PVector;

public class Viewport {
    private PGraphics graphics;
    private PVector position;
    private float cameraZ;
    
    public Viewport(PGraphics graphics, PVector position, float fov) {
        this.graphics = graphics;
        this.position = position;
        setFov(fov);
    }

    // Should not be called while already drawing to this viewport's graphics
    public void setFov(float fov) {
        graphics.beginDraw();
        this.cameraZ = (graphics.height/2) / PApplet.tan(fov/2);
        graphics.perspective(fov, ((float) graphics.width) / graphics.height, cameraZ/10, cameraZ * 10);
        graphics.camera(graphics.width/2, graphics.height/2, cameraZ, graphics.width/2, graphics.height/2, 0, 0, 1, 0);
        graphics.endDraw();
    }

    public float getScale(float z) {
        // at z=0, scale = 1 (object exactly at screen)
        // z < 0, scale < 1 (object farther than screen)
        // z > 0, scale > 1 (object closer than screen)
        return cameraZ / (cameraZ - z);
    }

    // translates 2d global xy to local xy to help with mouse input
    public PVector getLocalPoint(PVector globalPoint) {
        return new PVector(globalPoint.x - position.x, globalPoint.y - position.y);
    }

    public PGraphics getGraphics() {
        return this.graphics;
    }

    public PVector getPosition() {
        return this.position;
    }

    public int width() {
        return this.graphics.width;
    }

    public int height() {
        return this.graphics.height;
    }

    public boolean contains(PVector screenPos) {
        return screenPos.x >= position.x && screenPos.y >= position.y && screenPos.x < position.x + graphics.width && screenPos.y < position.y + graphics.height;
    }

    public float effectiveX(float x) {
        return x - position.x;
    }

    public float effectiveY(float y) {
        return y - position.y;
    }
}
