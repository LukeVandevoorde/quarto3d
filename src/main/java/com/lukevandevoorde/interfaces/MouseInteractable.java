package com.lukevandevoorde.interfaces;

public interface MouseInteractable extends Comparable<MouseInteractable> {
    
    // returns true if the mouse is on top of the object
    public boolean mouseOver();

    default int priority() { return 0; }

    @Override
    default int compareTo(MouseInteractable o) {
        return this.priority() - o.priority();
    }

}
