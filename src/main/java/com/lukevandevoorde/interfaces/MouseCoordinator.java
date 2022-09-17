package com.lukevandevoorde.interfaces;

public interface MouseCoordinator {

    public void add(Draggable<?> draggable);

    public void add(Hoverable hoverable);

    public void add(Clickable clickable);

    public void remove(Draggable<?> draggable);

    public void remove(Hoverable hoverable);

    public void remove(Clickable clickable);

    public int getMouseX();

    public int getMouseY();

}
