package com.lukevandevoorde;

public interface MouseCoordinator {

    public void add(Draggable draggable);

    public void remove(Draggable draggable);

    public int getMouseX();

    public int getMouseY();
}
