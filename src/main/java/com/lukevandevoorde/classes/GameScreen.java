package com.lukevandevoorde.classes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.lukevandevoorde.interfaces.DragTarget;
import com.lukevandevoorde.interfaces.Draggable;
import com.lukevandevoorde.quartolayer.QuartoPiece;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class GameScreen extends Screen {

    private Viewport boardViewport;
    private BoardDrawable quartoBoard;
    private PieceBank leftPieceBank, rightPieceBank;
    private PieceOfferingHolder p1PieceOfferingHolder, p2PieceOfferingHolder;
    private TransformData userView, selectView;
    private PVector hintPos;

    private GameFlowManager gameManager;

    public GameScreen(PApplet app) {
        boardViewport = new Viewport(app.createGraphics(app.width, app.height, PApplet.P3D), new PVector(0, 0), 0.27f*PApplet.PI);
        userView = new TransformData(new PVector(boardViewport.width()/2, 3*boardViewport.height()/5, -boardViewport.height()/2), new PVector(-PApplet.THIRD_PI, 0, 0));
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

        Player p2 = new UIPlayer(quartoBoard, p2PieceOfferingHolder, p1PieceOfferingHolder);
        // Player p2 = new ComputerPlayer();
        
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
        g.textAlign(PApplet.LEFT);
        float size = g.height/30;
        g.textSize(size);
        hintPos = new PVector(g.width*widthFrac, size);
    }

    public PGraphics display() {
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
        return boardView;
        // drawer.image(boardView, boardViewport.getPosition().x, boardViewport.getPosition().y);
    }
}
