package com.lukevandevoorde;

public interface DragTarget<T> extends MouseInteractable {

    public boolean accept(Draggable<T> draggable);
}
