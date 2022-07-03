package com.lukevandevoorde.interfaces;

public interface DragTarget<T> extends MouseInteractable {

    public boolean accept(Draggable<T> draggable);

}
