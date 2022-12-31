package com.lukevandevoorde;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import com.lukevandevoorde.classes.AnimatedDrawable;
import com.lukevandevoorde.classes.BoardDrawable;
import com.lukevandevoorde.classes.GameFlowManager;
import com.lukevandevoorde.classes.PieceBank;
import com.lukevandevoorde.classes.PieceDraggable;
import com.lukevandevoorde.classes.PieceOfferingHolder;
import com.lukevandevoorde.classes.Player;
import com.lukevandevoorde.classes.TransformData;
import com.lukevandevoorde.classes.UIPlayer;
import com.lukevandevoorde.classes.Viewport;
import com.lukevandevoorde.interfaces.Clickable;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Hoverable;
import com.lukevandevoorde.interfaces.MouseCoordinator;
import com.lukevandevoorde.interfaces.TimeKeeper;

public class Main extends PApplet implements MouseCoordinator, TimeKeeper {

    public static TimeKeeper TIME_KEEPER;
    public static MouseCoordinator MOUSE_COORDINATOR;

    private Viewport boardViewport;
    private AnimatedDrawable quartoBoard;
    private PieceBank leftPieceBank, rightPieceBank;
    private PieceOfferingHolder p1PieceOfferingHolder, p2PieceOfferingHolder;
    private TransformData userView, selectView;

    private HashSet<Hoverable> hoverables;
    private HashSet<Clickable> clickables;
    private Clickable selectedClickable;
    private HashSet<Draggable<?>> draggables;
    private Draggable<?> selectedDraggable;
    private boolean dragging;

    private GameFlowManager gameManager;

    public static void main(String[] args) {
        PApplet.main("com.lukevandevoorde.Main");
    }

    public Main() {
        TIME_KEEPER = this;
        MOUSE_COORDINATOR = this;
        dragging = false;
        draggables = new HashSet<Draggable<?>>();
        hoverables = new HashSet<Hoverable>();
        clickables = new HashSet<Clickable>();
    }

    public void settings() {
        fullScreen(P2D);
        smooth(4);
    }

    public void setup() {
        boardViewport = new Viewport(createGraphics(width, height, P3D), new PVector(0, 0), QUARTER_PI);
        userView = new TransformData(new PVector(boardViewport.width()/2, 3*boardViewport.height()/5, -boardViewport.height()/2), new PVector(-THIRD_PI, 0, 0));
        selectView = new TransformData(new PVector(boardViewport.width()/2, boardViewport.height()/2, -boardViewport.height()/2), new PVector(0, 0, 0));
        // selectView = new TransformData(new PVector(boardViewport.width()/2, boardViewport.height()/2, -boardViewport.height()/6), new PVector(0, 0, 0));
        // selectView = new TransformData(new PVector(boardViewport.width()/2, boardViewport.height()/2, 0), new PVector(0, 0, 0));

        gameManager = new GameFlowManager();

        BoardDrawable qb = new BoardDrawable(boardViewport, selectView, BoardDrawable.recommendedDimensions(boardViewport.width(), boardViewport.height()), gameManager.getQuartoBoardState());
        quartoBoard = new AnimatedDrawable(qb);
        quartoBoard.animate(userView, BoardDrawable.recommendedDimensions(boardViewport.width()/2, boardViewport.height()), 1500);

        Draggable.CallBack placingCallback = new Draggable.CallBack() {
            public void onStartDrag() {
                selectView.setRotZ(((int)((userView.getRotZ()) / HALF_PI + 0.5f)) * HALF_PI);
                quartoBoard.animate(quartoBoard.getCurrentTransform(), quartoBoard.getCurrentDimensions(), 0);
                quartoBoard.skipAnimation();
                quartoBoard.animate(selectView, BoardDrawable.recommendedDimensions(boardViewport.width()/2, boardViewport.height()), 350);
            }

            public void onReject() {
                quartoBoard.animate(quartoBoard.getCurrentTransform(), quartoBoard.getCurrentDimensions(), 0);
                quartoBoard.skipAnimation();
                quartoBoard.animate(userView, BoardDrawable.recommendedDimensions(boardViewport.width()/2, boardViewport.height()), 350);
            }

            public void onAccept() {
                quartoBoard.animate(quartoBoard.getCurrentTransform(), quartoBoard.getCurrentDimensions(), 0);
                quartoBoard.skipAnimation();
                quartoBoard.hold(225);
                quartoBoard.animate(userView, BoardDrawable.recommendedDimensions(boardViewport.width()/2, boardViewport.height()), 350);
            }
        };

        p1PieceOfferingHolder = new PieceOfferingHolder(boardViewport,
                                                        new TransformData(new PVector(0, 0.8f*boardViewport.height()), new PVector()),
                                                        new PVector(boardViewport.width()/4, 0.2f*boardViewport.height()),
                                                        qb,
                                                        placingCallback);
        p2PieceOfferingHolder = new PieceOfferingHolder(boardViewport,
                                                        new TransformData(new PVector(3*boardViewport.width()/4, 0.8f*boardViewport.height()), new PVector()),
                                                        new PVector(boardViewport.width()/4, 0.2f*boardViewport.height()),
                                                        qb,
                                                        placingCallback);
        
        Player p1 = new UIPlayer(qb, p1PieceOfferingHolder, p2PieceOfferingHolder);
        Player p2 = new UIPlayer(qb, p2PieceOfferingHolder, p1PieceOfferingHolder);
        gameManager.registerPlayer(p1, GameFlowManager.P1);
        gameManager.registerPlayer(p2, GameFlowManager.P2);
        
        DragTarget<PieceDraggable>[] holders = new PieceOfferingHolder[]{p1PieceOfferingHolder, p2PieceOfferingHolder};

        // Pieces without holes
        leftPieceBank = new PieceBank(boardViewport,
                                        new TransformData(new PVector(0, 0, selectView.getZ() + BoardDrawable.recommendedDimensions(boardViewport.width(), boardViewport.height()).z), new PVector()), 
                                        new PVector(boardViewport.width()/4, 0.8f*boardViewport.height()),
                                        gameManager.getQuartoBoardState().getRemainingPieces().stream().filter(p -> p.getFilled()).collect(Collectors.toSet()),
                                        Arrays.asList(holders)
                                        );

        // Pieces with holes
        rightPieceBank = new PieceBank(boardViewport,
                                        new TransformData(new PVector(3*boardViewport.width()/4, 0, 0), new PVector()),
                                        new PVector(boardViewport.width()/4, 0.8f*boardViewport.height()),
                                        gameManager.getQuartoBoardState().getRemainingPieces().stream().filter(p -> !p.getFilled()).collect(Collectors.toSet()),
                                        Arrays.asList(holders));
        
        gameManager.startGame();
    }

    public void draw() {        
        background(255);
        
        PGraphics boardView = boardViewport.getGraphics();
        boardView.beginDraw();
        boardView.background(255);
        boardView.fill(0);
        boardView.lights();
        boardView.smooth(4);

        quartoBoard.draw();
        p1PieceOfferingHolder.draw();
        p2PieceOfferingHolder.draw();
        leftPieceBank.draw();
        rightPieceBank.draw();
        
        boardView.endDraw();
        image(boardView, boardViewport.getPosition().x, boardViewport.getPosition().y);
    }

    @Override
    public void add(Draggable<?> draggable) {
        draggables.add(draggable);
    }

    @Override
    public void add(Hoverable hoverable) {
        hoverables.add(hoverable);
    }

    @Override
    public void add(Clickable clickable) {
        clickables.add(clickable);
    }

    @Override
    public void remove(Draggable<?> draggable) {
        draggables.remove(draggable);
    }

    @Override
    public void remove(Hoverable hoverable) {
        hoverables.remove(hoverable);
    }

    @Override
    public void remove(Clickable clickable) {
        clickables.remove(clickable);
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
    public void mouseWheel(MouseEvent e) {
        super.mouseWheel(e);
        int num = e.getCount();
        userView.setZ(max(min(userView.getZ() - num*height/20, 0), -2*height));
        quartoBoard.animate(null, null, 0);
    }

    @Override
    public void mousePressed() {
        dragging = false;

        for (Clickable c: clickables) {
            if (c.mouseHover(mouseX, mouseY)) {
                selectedClickable = c;
                break;
            }
        }

        for (Draggable<?> d: draggables) {
            if (d.mouseHover(mouseX, mouseY)) {
                selectedDraggable = d;
                dragging = true;
                d.startDrag();
                break;
            }
        }
    }

    @Override
    public void mouseReleased() {
        if (dragging && selectedDraggable != null) { // Drag complete
            dragging = false;
            selectedDraggable.endDrag();
        } else if (selectedClickable != null) {    // Click complete
            selectedClickable.onClick();
        }
    }

    @Override
    public void mouseDragged() {
        super.mouseDragged();

        if (dragging) {
            selectedDraggable.update();
        } else {
            float centerX = boardViewport.getGraphics().screenX(userView.getX(), userView.getY(), userView.getZ()) + boardViewport.getPosition().x;
            float centerY = boardViewport.getGraphics().screenY(userView.getX(), userView.getY(), userView.getZ()) + boardViewport.getPosition().y;

            PVector arm = new PVector(pmouseX - centerX, pmouseY - centerY);
            PVector drag = new PVector(mouseX - pmouseX, mouseY - pmouseY);

            userView.setRotX(min(0, max(userView.getRotX() + 0.01f*(this.mouseY - this.pmouseY), -HALF_PI)));
            userView.setRotZ(((userView.getRotZ() + (0.01f) * drag.cross(arm).z / arm.mag()) % TWO_PI + TWO_PI) % TWO_PI);
            quartoBoard.animate(null, null, 0);
        }
    }
}
