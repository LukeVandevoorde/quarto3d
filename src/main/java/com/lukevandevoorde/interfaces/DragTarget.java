package com.lukevandevoorde.interfaces;

public interface DragTarget<T> extends MouseInteractable {

    public boolean willAccept(Draggable<T> draggable);

    public boolean accept(Draggable<T> draggable);

}
