package com.lukevandevoorde;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import com.lukevandevoorde.classes.BoardDrawable;
import com.lukevandevoorde.classes.ComputerPlayer;
import com.lukevandevoorde.classes.GameFlowManager;
import com.lukevandevoorde.classes.PieceBank;
import com.lukevandevoorde.classes.PieceOfferingHolder;
import com.lukevandevoorde.classes.Player;
import com.lukevandevoorde.classes.TransformData;
import com.lukevandevoorde.classes.UIPlayer;
import com.lukevandevoorde.classes.Viewport;
import com.lukevandevoorde.interfaces.Clickable;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.UICoordinator;
import com.lukevandevoorde.quartolayer.QuartoPiece;
import com.lukevandevoorde.interfaces.MouseInteractable;
import com.lukevandevoorde.interfaces.TimeKeeper;

public class Main extends PApplet implements UICoordinator, TimeKeeper {

    private class ComparableJob implements Comparable<ComparableJob> {
        TimeKeeper.Job job;
        int executionTime;

        public ComparableJob(int executionTime, TimeKeeper.Job job) {
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

    private Viewport boardViewport;
    private BoardDrawable quartoBoard;
    private PieceBank leftPieceBank, rightPieceBank;
    private PieceOfferingHolder p1PieceOfferingHolder, p2PieceOfferingHolder;
    private TransformData userView, selectView;
    private PVector hintPos;

    private ArrayList<Clickable> clickables;
    private Clickable selectedClickable;
    private ArrayList<Draggable<?>> draggables;
    private Draggable<?> selectedDraggable;
    private int minPriority, maxPriority;
    private boolean dragging, clickedAndWaitingForDoubleClick, mouseMovedSinceClicked;

    private GameFlowManager gameManager;

    public static void main(String[] args) {
        PApplet.main("com.lukevandevoorde.Main");
    }

    public Main() {
        TIME_KEEPER = this;
        UI_COORDINATOR = this;
        minPriority = 0;
        maxPriority = 1;
        dragging = false;
        clickedAndWaitingForDoubleClick = false;
        mouseMovedSinceClicked = false;
        jobs = new PriorityQueue<>();
        draggables = new ArrayList<Draggable<?>>();
        clickables = new ArrayList<Clickable>();
    }

    public void settings() {
        fullScreen(P2D);
        smooth(4);
    }

    public void setup() {
        boardViewport = new Viewport(createGraphics(width, height, P3D), new PVector(0, 0), 0.27f*PI);
        userView = new TransformData(new PVector(boardViewport.width()/2, 3*boardViewport.height()/5, -boardViewport.height()/2), new PVector(-THIRD_PI, 0, 0));
        selectView = new TransformData(new PVector(boardViewport.width()/2, boardViewport.height()/2, -boardViewport.height()/2), new PVector(0, 0, 0));

        gameManager = new GameFlowManager();

        float holderHeightFrac = 0.3f;
        float widthFrac = 0.15f;

        quartoBoard = new BoardDrawable(boardViewport, userView, selectView, BoardDrawable.recommendedDimensions((1-widthFrac)*boardViewport.width(), boardViewport.height()), gameManager.getQuartoBoardState());
        gameManager.registerBoardDrawable(quartoBoard);

        // Accept taken care of by GameFlowManager
        Draggable.CallBack placingCallback = new Draggable.CallBack() {
            public void onStartDrag() {
                quartoBoard.enterSelectView(UIPlayer.SELECT_SPEED);
            }

            public void onReject() {
                quartoBoard.enterUserView(UIPlayer.USER_VIEW_SPEED);
            }
        };

        p1PieceOfferingHolder = new PieceOfferingHolder(boardViewport,
                                                        new TransformData(new PVector(0, (1-holderHeightFrac)*boardViewport.height()), new PVector()),
                                                        new PVector(widthFrac*boardViewport.width(), holderHeightFrac*boardViewport.height()),
                                                        quartoBoard, placingCallback, Player.P1_COLOR, Player.P2_COLOR, true, "P1");
        p2PieceOfferingHolder = new PieceOfferingHolder(boardViewport,
                                                        new TransformData(new PVector((1-widthFrac)*boardViewport.width(), (1-holderHeightFrac)*boardViewport.height()), new PVector()),
                                                        new PVector(widthFrac*boardViewport.width(), holderHeightFrac*boardViewport.height()),
                                                        quartoBoard, placingCallback, Player.P2_COLOR, Player.P1_COLOR, false, "P2");
        
        List<DragTarget<QuartoPiece>> holders = Arrays.asList(new PieceOfferingHolder[]{p1PieceOfferingHolder, p2PieceOfferingHolder});

        // Pieces without holes
        leftPieceBank = new PieceBank(boardViewport,
                                        new TransformData(new PVector(), new PVector()), 
                                        new PVector(widthFrac*boardViewport.width(), (1-holderHeightFrac)*boardViewport.height()),
                                        gameManager.getQuartoBoardState().getRemainingPieces().stream().filter(p -> p.getFilled()).collect(Collectors.toSet()),
                                        holders);

        // Pieces with holes
        rightPieceBank = new PieceBank(boardViewport,
                                        new TransformData(new PVector((1-widthFrac)*boardViewport.width(), 0, 0), new PVector()),
                                        new PVector(widthFrac*boardViewport.width(), (1-holderHeightFrac)*boardViewport.height()),
                                        gameManager.getQuartoBoardState().getRemainingPieces().stream().filter(p -> !p.getFilled()).collect(Collectors.toSet()),
                                        holders);

        Player p1 = new UIPlayer(quartoBoard, p1PieceOfferingHolder, p2PieceOfferingHolder);
        // Player p1 = new ComputerPlayer();

        // Player p2 = new UIPlayer(quartoBoard, p2PieceOfferingHolder, p1PieceOfferingHolder);
        Player p2 = new ComputerPlayer();
        
        gameManager.registerPlayer(p1, GameFlowManager.P1);
        gameManager.registerPlayer(p2, GameFlowManager.P2);
        gameManager.registerPieceOfferingHolder(p1PieceOfferingHolder, GameFlowManager.P1);
        gameManager.registerPieceOfferingHolder(p2PieceOfferingHolder, GameFlowManager.P2);
        HashSet<PieceBank> banks = new HashSet<>();
        banks.add(leftPieceBank);
        banks.add(rightPieceBank);
        gameManager.registerPieceBanks(banks);
        gameManager.startGame();

        PGraphics g = boardViewport.getGraphics();
        g.textAlign(LEFT);
        float size = g.height/30;
        g.textSize(size);
        hintPos = new PVector(g.width*widthFrac, size);
    }

    public void draw() {
        while (!jobs.isEmpty() && jobs.peek().executionTime <= this.millis()) {
            jobs.poll().job.execute();
        }

        background(255);
        
        PGraphics boardView = boardViewport.getGraphics();
        boardView.beginDraw();
        boardView.background(255);
        boardView.fill(0);
        boardView.lights();
        boardView.smooth(4);

        boardView.text(gameManager.getUIHint(), hintPos.x, hintPos.y);

        quartoBoard.draw();
        p1PieceOfferingHolder.draw();
        p2PieceOfferingHolder.draw();
        leftPieceBank.draw();
        rightPieceBank.draw();
        
        boardView.endDraw();
        image(boardView, boardViewport.getPosition().x, boardViewport.getPosition().y);
    }

    public void scheduleJob(int millisUntilExecution, TimeKeeper.Job job) {
        this.jobs.add(new ComparableJob(this.millis() + millisUntilExecution, job));
    }

    @Override
    public void setMinPriority(int priority) {
        this.minPriority = priority;
    }

    @Override
    public void setMaxPriority(int priority) {
        this.maxPriority = priority;
    }

    @Override
    public void add(MouseInteractable element) {
        if (element instanceof Draggable) {
            Draggable<?> d = (Draggable<?>) element;
            if (!draggables.contains(d)) {
                draggables.add(d);
                draggables.sort(null);
            }
        }
        if (element instanceof Clickable) {
            Clickable c = (Clickable) element;
            if (!clickables.contains(c)) {
                clickables.add(c);
                clickables.sort(null);
            }
        }
    }

    @Override
    public void remove(MouseInteractable element) {
        if (element instanceof Draggable) draggables.remove((Draggable<?>) element);
        if (element instanceof Clickable) clickables.remove((Clickable) element);
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

    @Override
    public void mouseWheel(MouseEvent e) {
        super.mouseWheel(e);
        quartoBoard.adjustDistance(e.getCount());
    }

    @Override
    public void mousePressed() {
        super.mousePressed();
        mouseMovedSinceClicked = false;

        for (int i = clickables.size() - 1; i >= 0; i--) {
            Clickable c = clickables.get(i);
            if (c.priority() > maxPriority) continue;
            if (c.priority() >= minPriority && c.mouseOver()) {
                selectedClickable = c;
                break;
            }
        }

        for (int i = draggables.size() - 1; i >= 0; i--) {
            Draggable<?> d = draggables.get(i);
            if (d.priority() > maxPriority) continue;
            if (d.priority() >= minPriority && d.mouseOver()) {
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

                TimeKeeper.Job registerClick = new Job() {
                    public void execute() {
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
