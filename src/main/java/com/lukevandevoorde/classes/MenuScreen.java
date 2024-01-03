package com.lukevandevoorde.classes;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class MenuScreen extends Screen {


    private Viewport viewport;

    public MenuScreen(PApplet app) {
        this.viewport = new Viewport(app.createGraphics(app.width, app.height, PApplet.P2D), new PVector(0, 0), 0);
    }

    @Override
    public PGraphics display() {
        return viewport.getGraphics();
    }
}
