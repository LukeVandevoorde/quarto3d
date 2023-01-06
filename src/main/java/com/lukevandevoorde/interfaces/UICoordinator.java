package com.lukevandevoorde.interfaces;

public interface UICoordinator {

    public void setMinPriority(int priority);

    public void setMaxPriority(int priority);

    public void add(MouseInteractable element);

    public void remove(MouseInteractable element);

    public int getMouseX();

    public int getMouseY();

    public int getPrevMouseX();

    public int getPrevMouseY();

}
