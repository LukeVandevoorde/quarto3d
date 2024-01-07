package com.lukevandevoorde.classes;

import com.lukevandevoorde.Main;
import com.lukevandevoorde.interfaces.Clickable;

import processing.core.PVector;

public class Button extends Drawable implements Clickable {

    private String text;
    private int fontsize;
    private Runnable onClick;

    public Button(Viewport viewport, TransformData transform, PVector dimensions, String text, int fontsize, Runnable onClick) {
        super(viewport, transform, dimensions);
        this.text = text;
        this.fontsize = fontsize;
        this.onClick = onClick;
    }

    @Override
    public boolean mouseOver() {
        float mx = viewport.effectiveX(Main.UI_COORDINATOR.getMouseX());
        float my = viewport.effectiveY(Main.UI_COORDINATOR.getMouseY());
        float x = transform.getX();
        float y = transform.getY();
        return mx >= x && my >= y && mx < x + dimensions.x && my < y + dimensions.y;
    }

    @Override
    public void onClick() {
        this.onClick.run();
    }

    @Override
    public void onDoubleClick() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onDoubleClick'");
    }

    @Override
    public void draw() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'draw'");
    }

    @Override
    public void setDimensions(PVector newDimensions) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDimensions'");
    }
    
}
