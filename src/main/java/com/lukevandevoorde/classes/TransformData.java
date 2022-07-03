package com.lukevandevoorde.classes;

import processing.core.PGraphics;
import processing.core.PVector;

public class TransformData {

    public static final TransformData ZERO_TRANSFORM = new TransformData();

    private PVector position;
    private PVector rotation;

    public TransformData() {
        this(0,0,0,0,0,0);
    }

    public TransformData(PVector position, PVector rotation) {
        this.position = new PVector(position.x, position.y, position.z);
        this.rotation = new PVector(rotation.x, rotation.y, rotation.z);
    }

    public TransformData(float tx, float ty, float tz, float rx, float ry, float rz) {
        this.position = new PVector(tx, ty, tz);
        this.rotation = new PVector(rx, ry, rz);
    }

    public static TransformData lerp(TransformData start, TransformData end, float amt) {
        return new TransformData(PVector.lerp(start.position, end.position, amt), PVector.lerp(start.rotation, end.rotation, amt));
    }

    // First, translates pg to position then does the rotation that is equivalent to
    // rotating the vector (0, 0, 1) to the point on the unit sphere that makes an angle of:
    //  - rotation.x radians with the XY (horizontal) plane
    //  - rotation.y radians with the ZY (vertical, forward-back) plane
    // And finally rotates the plane normal to this vector by rotation.z
    public void transform(PGraphics pg) {
        pg.translate(position.x, position.y, position.z);
        pg.rotateY(rotation.y);
        pg.rotateX(-rotation.x);
        pg.rotateZ(-rotation.z);
    }

    public PVector getPosition() {
        return new PVector(position.x, position.y, position.z);
    }

    public PVector getRotation() {
        return new PVector(rotation.x, rotation.y, rotation.z);
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }

    public float getZ() {
        return position.z;
    }

    public void setX(float x) {
        this.position.x = x;
    }

    public void setY(float y) {
        this.position.y = y;
    }

    public void setZ(float z) {
        this.position.z = z;
    }

    public float getRotX() {
        return rotation.x;
    }

    public float getRotY() {
        return rotation.y;
    }

    public float getRotZ() {
        return rotation.z;
    }

    public void setRotX(float x) {
        this.rotation.x = x;
    }

    public void setRotY(float y) {
        this.rotation.y = y;
    }

    public void setRotZ(float z) {
        this.rotation.z = z;
    }
}
