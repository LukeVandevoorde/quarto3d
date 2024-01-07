package com.lukevandevoorde;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;

import com.lukevandevoorde.classes.GameScreen;
import com.lukevandevoorde.classes.Screen;
import com.lukevandevoorde.interfaces.Clickable;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.interfaces.UICoordinator;
import com.lukevandevoorde.interfaces.MouseInteractable;
import com.lukevandevoorde.interfaces.TimeKeeper;

public class Main extends PApplet implements UICoordinator, TimeKeeper {

    private class ComparableJob implements Comparable<ComparableJob> {
        Runnable job;
        int executionTime;

        public ComparableJob(int executionTime, Runnable job) {
            this.job = job;
            this.executionTime = executionTime;
        }

        public int compareTo(ComparableJob o) {
            return this.executionTime - o.executionTime;
        }
    }

    private static final int DOUBLE_CLICK_TIME = 225;

    public static TimeKeeper TIME_KEEPER;
    public static UICoordinator UI_COORDINATOR;

    private PriorityQueue<ComparableJob> jobs;

    private Stack<Screen> screens;
    private Stack<ArrayList<Clickable>> clickables;
    private Clickable selectedClickable;
    private Stack<ArrayList<Draggable<?>>> draggables;
    private Draggable<?> selectedDraggable;
    private Stack<Integer> minPriority, maxPriority;
    private boolean dragging, clickedAndWaitingForDoubleClick, mouseMovedSinceClicked;

    

    public static void main(String[] args) {
        PApplet.main("com.lukevandevoorde.Main");
    }

    public Main() {
        TIME_KEEPER = this;
        UI_COORDINATOR = this;

        minPriority = new Stack<Integer>();
        minPriority.push(0);
        maxPriority = new Stack<Integer>();
        maxPriority.push(1);
        dragging = false;
        clickedAndWaitingForDoubleClick = false;
        mouseMovedSinceClicked = false;
        jobs = new PriorityQueue<>();
        draggables = new Stack<ArrayList<Draggable<?>>>();
        draggables.push(new ArrayList<Draggable<?>>());
        clickables = new Stack<ArrayList<Clickable>>();
        clickables.push(new ArrayList<Clickable>());

        screens = new Stack<Screen>();
    }

    public void settings() {
        fullScreen(P3D);
        // size(1920, 1080, P3D);
        smooth(4);
    }

    public void setup() {
        screens.push(new GameScreen(this));
    }

    public void draw() {
        while (!jobs.isEmpty() && jobs.peek().executionTime <= this.millis()) {
            jobs.poll().job.run();
        }

        background(255);
        screens.peek().display(this);
    }

    public void scheduleJob(int millisUntilExecution, Runnable job) {
        this.jobs.add(new ComparableJob(this.millis() + millisUntilExecution, job));
    }

    @Override
    public void setMinPriority(int priority) {
        this.minPriority.pop();
        this.minPriority.push(priority);
    }

    @Override
    public void setMaxPriority(int priority) {
        this.maxPriority.pop();
        this.maxPriority.push(priority);
    }

    @Override
    public void add(MouseInteractable element) {
        if (element instanceof Draggable) {
            Draggable<?> d = (Draggable<?>) element;
            if (!draggables.peek().contains(d)) {
                draggables.peek().add(d);
                draggables.peek().sort(null);
            }
        }
        if (element instanceof Clickable) {
            Clickable c = (Clickable) element;
            if (!clickables.peek().contains(c)) {
                clickables.peek().add(c);
                clickables.peek().sort(null);
            }
        }
    }

    @Override
    public void remove(MouseInteractable element) {
        if (element instanceof Draggable) draggables.peek().remove((Draggable<?>) element);
        if (element instanceof Clickable) clickables.peek().remove((Clickable) element);
    }

    @Override
    public int getMouseX() {
        return this.mouseX;
    }

    @Override
    public int getMouseY() {
        return this.mouseY;
    }

    @Override
    public int getPrevMouseX() {
        return this.pmouseX;
    }

    @Override
    public int getPrevMouseY() {
        return this.pmouseY;
    }

    // @Override
    // public void mouseWheel(MouseEvent e) {
    //     super.mouseWheel(e);
    //     quartoBoard.adjustDistance(e.getCount());
    // }

    @Override
    public void mousePressed() {
        super.mousePressed();
        mouseMovedSinceClicked = false;

        for (int i = clickables.peek().size() - 1; i >= 0; i--) {
            Clickable c = clickables.peek().get(i);
            if (c.priority() > maxPriority.peek()) continue;
            if (c.priority() >= minPriority.peek() && c.mouseOver()) {
                selectedClickable = c;
                break;
            }
        }

        for (int i = draggables.peek().size() - 1; i >= 0; i--) {
            Draggable<?> d = draggables.peek().get(i);
            if (d.priority() > maxPriority.peek()) continue;
            if (d.priority() >= minPriority.peek() && d.mouseOver()) {
                selectedDraggable = d;
                break;
            }
        }
    }

    @Override
    public void mouseReleased() {
        super.mouseReleased();
        if (dragging && selectedDraggable != null) {
            selectedDraggable.endDrag();
        } else if (selectedClickable != null) {
            if (clickedAndWaitingForDoubleClick && !mouseMovedSinceClicked) {
                clickedAndWaitingForDoubleClick = false;
                selectedClickable.onDoubleClick();
            } else {
                clickedAndWaitingForDoubleClick = true;

                Runnable registerClick = new Runnable() {
                    public void run() {
                        if (clickedAndWaitingForDoubleClick) {
                            selectedClickable.onClick();
                            selectedClickable = null;
                            clickedAndWaitingForDoubleClick = false;
                        }
                    }
                };

                this.scheduleJob(DOUBLE_CLICK_TIME, registerClick);
            }
        }
        dragging = false;
        selectedDraggable = null;
    }

    @Override
    public void mouseMoved() {
        super.mouseMoved();
        mouseMovedSinceClicked = true;
    }

    @Override
    public void mouseDragged() {
        super.mouseDragged();
        if (!dragging) {
            dragging = true;
            if (selectedDraggable != null) selectedDraggable.startDrag();
        }

        if (selectedDraggable != null) {
            selectedDraggable.update();
        }
    }
}
