package com.example.wuziqi.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SizeF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.wuziqi.R;
import com.example.wuziqi.utils.MyStack;

public class CheckerBoard extends View {

    private static final String TAG = CheckerBoard.class.getSimpleName();

    public enum BoardSizeMode{
        MIN(13),MID(15),MAX(19);

        private int size;

        BoardSizeMode(int path) {
            this.size = path;
        }

        public int getV() {
            return size;
        }
    }

    //棋盘为正方形
    int size;
    int boardSize; //size - 2*padding
    int boardGridSize; // size - 2*padding - 2*innerPadding
    int boardGridSizeStartX; // padding + innerPadding
    int boardGridSizeStartY; // boardGridSizeStartX
    int padding; //棋盘边界到View的距离
    int innerPadding; //棋盘网格到棋盘边界的距离
    BoardSizeMode BOARD_SIZE_MODE; //min 13*13; mid 15*15; max 19*19
    float gridSize; //一个小方格的尺寸
    MyStack<SizeF> playerPiecePositions = new MyStack<>();//玩家一的棋子坐标
    MyStack<SizeF> antPlayerPiecePositions = new MyStack<>();//玩家二的棋子坐标
    SizeF clickPos; //点击的位置;
    boolean PLAYER_IS_PLAYER_ONE; //玩家是玩家一；玩家一先行，执黑子
    boolean PLAYER_JUST_DROP; //刚刚落下棋子的是玩家；
    OnPlayerPieceDropListener pieceDropListener;
    OnPlayerPieceRedoListener playerPieceRedoListener;
    OnAntPlayerPieceRedoListener antPlayerPieceRedoListener;

    Context context;

    Canvas canvas;

    Paint boardBoundPaint = new Paint();
    Paint boardGridLinePaint = new Paint();
    Paint boardPointPaint = new Paint();
    Paint boardPointMarkPaint = new Paint();
    Paint playerOnePiecePaint = new Paint();
    Paint playerTwoPiecePaint = new Paint();
    Paint pieceShadowPaint = new Paint();
    Paint pieceDropIconPaint = new Paint();

    Rect boardBoundRect; //棋盘边界坐标点

    float[] boardGridLineCoordinates;//棋盘网格坐标点
    float[] boardPointCoordinates;

    public CheckerBoard(Context context, BoardSizeMode sizeMode, boolean playerIsPlayerOne) {
        super(context);
        init(sizeMode, playerIsPlayerOne);
    }

    public CheckerBoard(Context context, @Nullable AttributeSet attrs, BoardSizeMode sizeMode, boolean playerIsPlayerOne) {
        super(context, attrs);
        init(sizeMode, playerIsPlayerOne);
    }

    public CheckerBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr, BoardSizeMode sizeMode, boolean playerIsPlayerOne) {
        super(context, attrs, defStyleAttr);
        init(sizeMode, playerIsPlayerOne);
    }

    private void init(BoardSizeMode sizeMode, boolean playerIsPlayerOne){
        context = getContext();

        BOARD_SIZE_MODE = sizeMode;
        PLAYER_IS_PLAYER_ONE = playerIsPlayerOne;

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setPadding(20, 20,20,20);

        innerPadding = 20;
        boardBoundRect = new Rect();
        boardGridLineCoordinates = new float[BOARD_SIZE_MODE.getV()* BOARD_SIZE_MODE.getV()];

        boardBoundPaint.setColor(context.getColor(R.color.boardBoundColor));
        boardBoundPaint.setStyle(Paint.Style.STROKE);
        boardBoundPaint.setStrokeWidth(10);

        boardGridLinePaint.setColor(context.getColor(R.color.boardLineColor));
        boardGridLinePaint.setStyle(Paint.Style.FILL);
        boardGridLinePaint.setStrokeWidth(2);

        boardPointPaint.setColor(context.getColor(R.color.boardPointColor));
        boardPointPaint.setStyle(Paint.Style.FILL);
        boardPointPaint.setStrokeWidth(1);
        boardPointPaint.setAntiAlias(true);

        boardPointMarkPaint.setColor(context.getColor(R.color.boardPointMarkColor));
        boardPointMarkPaint.setStyle(Paint.Style.FILL);
        boardPointMarkPaint.setStrokeWidth(10);
        boardPointMarkPaint.setAntiAlias(true);
        boardPointMarkPaint.setTextScaleX(gridSize);

        playerOnePiecePaint.setColor(context.getColor(R.color.playerOnePointColor));
        playerOnePiecePaint.setStyle(Paint.Style.FILL);
        playerOnePiecePaint.setStrokeWidth(1);
        playerOnePiecePaint.setAntiAlias(true);

        playerTwoPiecePaint.setColor(context.getColor(R.color.playerTwoPointColor));
        playerTwoPiecePaint.setStyle(Paint.Style.FILL);
        playerTwoPiecePaint.setStrokeWidth(1);
        playerTwoPiecePaint.setAntiAlias(true);

        pieceShadowPaint.setColor(context.getColor(R.color.pieceShadow));
        pieceShadowPaint.setStyle(Paint.Style.STROKE);
        pieceShadowPaint.setStrokeWidth(gridSize*0.4f*0.1f);
        pieceShadowPaint.setAntiAlias(true);

        pieceDropIconPaint.setColor(context.getColor(R.color.pieceDropIcon));
        pieceDropIconPaint.setStyle(Paint.Style.FILL);
        pieceShadowPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.canvas = canvas;
        canvas.drawColor(context.getColor(R.color.boardColor));
        canvas.drawRect(boardBoundRect, boardBoundPaint);
        canvas.drawLines(boardGridLineCoordinates, boardGridLinePaint);

        for (int i = 0; i < boardPointCoordinates.length/2; i++) {
            canvas.drawCircle(boardPointCoordinates[i*2], boardPointCoordinates[i*2+1], 5, boardPointPaint);
        }

        for(SizeF p: playerPiecePositions){
            canvas.drawCircle(p.getWidth(), p.getHeight(), gridSize*0.4f, playerOnePiecePaint);
            canvas.drawCircle(p.getWidth(), p.getHeight(), gridSize*0.4f, pieceShadowPaint);
            if(clickPos != null){
                canvas.drawCircle(clickPos.getWidth(), clickPos.getHeight(), gridSize*0.15f, pieceDropIconPaint);
            }
        }
        for(SizeF p: antPlayerPiecePositions){
            canvas.drawCircle(p.getWidth(), p.getHeight(), gridSize*0.4f, playerTwoPiecePaint);
            canvas.drawCircle(p.getWidth(), p.getHeight(), gridSize*0.4f, pieceShadowPaint);
            if(clickPos != null){
                canvas.drawCircle(clickPos.getWidth(), clickPos.getHeight(), gridSize*0.15f, pieceDropIconPaint);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        size = Math.min(h, w);
        padding = Math.max(Math.max(getPaddingLeft(), getPaddingRight()), Math.max(getPaddingTop(), getPaddingBottom()));
        boardSize = size - 2*padding;
        boardGridSize = size - 2*padding - 2*innerPadding;
        gridSize = (float) (boardSize-2*innerPadding)/(BOARD_SIZE_MODE.getV()-1);

        boardBoundRect.set(padding, padding, padding+boardSize, padding+boardSize);
        boardGridSizeStartX = padding+innerPadding;
        boardGridSizeStartY = boardGridSizeStartX;
        int startX = padding+innerPadding;
        int startY = startX;
        //棋盘网格竖线
        for (int i = 0; i < BOARD_SIZE_MODE.getV(); i++) {
            boardGridLineCoordinates[i*4] = startX + gridSize*i;
            boardGridLineCoordinates[i*4 + 1] = startY;
            boardGridLineCoordinates[i*4 + 2] = startX + gridSize*i;
            boardGridLineCoordinates[i*4 + 3] = startY + boardGridSize;
        }

        //棋盘网格横线
        for (int i = BOARD_SIZE_MODE.getV(); i < 2* BOARD_SIZE_MODE.getV(); i++) {
            boardGridLineCoordinates[i*4] = startX;
            boardGridLineCoordinates[i*4 + 1] = startY + gridSize*(i- BOARD_SIZE_MODE.getV());
            boardGridLineCoordinates[i*4 + 2] = startX + boardGridSize;
            boardGridLineCoordinates[i*4 + 3] = startY + gridSize*(i- BOARD_SIZE_MODE.getV());
        }


        //棋盘上的星位点坐标
        switch (BOARD_SIZE_MODE){
            case MIN: //13*13
                boardPointCoordinates = new float[8];
                //0，0
                boardPointCoordinates[0] = startX + 3*gridSize;
                boardPointCoordinates[1] = startY + 3*gridSize;
                //1，0
                boardPointCoordinates[2] = startX + boardGridSize - 3*gridSize;
                boardPointCoordinates[3] = startY + 3*gridSize;
                //0，1
                boardPointCoordinates[4] = startX + 3*gridSize;
                boardPointCoordinates[5] = startY + boardGridSize - 3*gridSize;
                //1，1
                boardPointCoordinates[6] = startX + boardGridSize - 3*gridSize;
                boardPointCoordinates[7] = startY + boardGridSize - 3*gridSize;
                break;
            case MID: //15*15
                boardPointCoordinates = new float[10];
                //0，0
                boardPointCoordinates[0] = startX + 3*gridSize;
                boardPointCoordinates[1] = startY + 3*gridSize;
                //2，0
                boardPointCoordinates[2] = startX + boardGridSize - 3*gridSize;
                boardPointCoordinates[3] = startY + 3*gridSize;
                //0，2
                boardPointCoordinates[4] = startX + 3*gridSize;
                boardPointCoordinates[5] = startY + boardGridSize - 3*gridSize;
                //2，2
                boardPointCoordinates[6] = startX + boardGridSize - 3*gridSize;
                boardPointCoordinates[7] = startY + boardGridSize - 3*gridSize;
                //1，1
                boardPointCoordinates[8] = startX + 7*gridSize;
                boardPointCoordinates[9] = startY + 7*gridSize;
                break;
            case MAX: //19*19
                boardPointCoordinates = new float[18];
                //0，0
                boardPointCoordinates[0] = startX + 3*gridSize;
                boardPointCoordinates[1] = startY + 3*gridSize;
                //0，1
                boardPointCoordinates[2] = startX + 9*gridSize;
                boardPointCoordinates[3] = startY + 3*gridSize;
                //0，2
                boardPointCoordinates[4] = startX + 15*gridSize;
                boardPointCoordinates[5] = startY + 3*gridSize;
                //1，0
                boardPointCoordinates[6] = startX + 3*gridSize;
                boardPointCoordinates[7] = startY + 9*gridSize;
                //1，1
                boardPointCoordinates[8] = startX + 9*gridSize;
                boardPointCoordinates[9] = startY + 9*gridSize;
                //1，2
                boardPointCoordinates[10] = startX + 15*gridSize;
                boardPointCoordinates[11] = startY + 9*gridSize;
                //2，0
                boardPointCoordinates[12] = startX + 3*gridSize;
                boardPointCoordinates[13] = startY + 15*gridSize;
                //2，1
                boardPointCoordinates[14] = startX + 9*gridSize;
                boardPointCoordinates[15] = startY + 15*gridSize;
                //2，2
                boardPointCoordinates[16] = startX + 15*gridSize;
                boardPointCoordinates[17] = startY + 15*gridSize;
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果刚刚玩家已落子，则需要等到drawPiece(int x, int y)被调用，
        //也就是对手玩家落子，才响应
        if(PLAYER_JUST_DROP) return true;

        if(event.getAction() == MotionEvent.ACTION_DOWN){
            float x = event.getX();
            float y = event.getY();
            if(x < boardSize+padding && x > padding && y < boardSize+padding && y > padding){
                drawPiece(x, y);
            }
        }
        return true;
    }

    //玩家落子调用
    private void drawPiece(float x, float y){
        int clickX = 0;
        int clickY = 0;

        if(x < boardGridSizeStartX){
            clickX = 0;
        }else if(x > boardGridSizeStartX+boardGridSize){
            clickX = BOARD_SIZE_MODE.getV()-1;
        }else {
            clickX = Math.round((x - boardGridSizeStartX)/gridSize);
        }

        if(y < boardGridSizeStartY){
            clickY = 0;
        }else if(y > boardGridSizeStartY+boardGridSize){
            clickY = BOARD_SIZE_MODE.getV()-1;
        }else {
            clickY = Math.round((y - boardGridSizeStartY)/gridSize);
        }

        clickPos = new SizeF(boardGridSizeStartX+gridSize*clickX, boardGridSizeStartY+gridSize*clickY);
        if(!playerPiecePositions.contains(clickPos) && !antPlayerPiecePositions.contains(clickPos)){
            if(PLAYER_IS_PLAYER_ONE){
                playerPiecePositions.push(clickPos);
            }else {
                antPlayerPiecePositions.push(clickPos);
            }
            if(pieceDropListener != null) pieceDropListener.listener(clickX, clickY);
            PLAYER_JUST_DROP = true;
            invalidate();
        }
    }

    //对手玩家落子调用，提供给外部调用，棋子颜色为与玩家的相反
    public void drawPiece(int x, int y){
        if(x < 0 || y < 0) return;
        if(x > BOARD_SIZE_MODE.getV() || y > BOARD_SIZE_MODE.getV()) return;

        clickPos = new SizeF(boardGridSizeStartX+gridSize*x, boardGridSizeStartY+gridSize*y);
        if(!playerPiecePositions.contains(clickPos) && !antPlayerPiecePositions.contains(clickPos)){
            if(PLAYER_IS_PLAYER_ONE){
                //外部就是玩家二
                antPlayerPiecePositions.push(clickPos);
            }else {
                playerPiecePositions.push(clickPos);
            }
            PLAYER_JUST_DROP = false;
            invalidate();
        }
    }

    /**
     * 悔棋
     * @param isPlayer 是否是玩家悔棋
     */
    public void redo(boolean isPlayer){
        if(isPlayer){
            if(PLAYER_JUST_DROP){
                if(PLAYER_IS_PLAYER_ONE){
                    popCallRedoListener(playerPiecePositions.safePop(), true);
                    clickPos = antPlayerPiecePositions.safePeek();
                }else{
                    popCallRedoListener(antPlayerPiecePositions.safePop(), true);
                    clickPos = playerPiecePositions.safePeek();
                }
            }else {
                if(PLAYER_IS_PLAYER_ONE){
                    popCallRedoListener(playerPiecePositions.safePop(), true);
                    popCallRedoListener(antPlayerPiecePositions.safePop(), false);
                    clickPos = antPlayerPiecePositions.safePeek();
                }else{
                    popCallRedoListener(playerPiecePositions.safePop(), false);
                    popCallRedoListener(antPlayerPiecePositions.safePop(), true);
                    clickPos = playerPiecePositions.safePeek();
                }
            }
            PLAYER_JUST_DROP = false;
        }else {
            if(PLAYER_JUST_DROP){
                if(PLAYER_IS_PLAYER_ONE){
                    popCallRedoListener(playerPiecePositions.safePop(), true);
                    popCallRedoListener(antPlayerPiecePositions.safePop(), false);
                    clickPos = playerPiecePositions.safePeek();
                }else{
                    popCallRedoListener(playerPiecePositions.safePop(), false);
                    popCallRedoListener(antPlayerPiecePositions.safePop(), true);
                    clickPos = antPlayerPiecePositions.safePeek();
                }
            }else {
                if(PLAYER_IS_PLAYER_ONE){
                    popCallRedoListener(antPlayerPiecePositions.safePop(), false);
                    clickPos = playerPiecePositions.safePeek();
                }else{
                    popCallRedoListener(playerPiecePositions.safePop(), false);
                    clickPos = antPlayerPiecePositions.safePeek();
                }
            }
            PLAYER_JUST_DROP = true;
        }
        invalidate();
    }

    //重置
    public void reset(){
        playerPiecePositions.removeAllElements();
        antPlayerPiecePositions.removeAllElements();
        clickPos = null;
        PLAYER_JUST_DROP = false;
        invalidate();
    }

    /**
     * 方便调用Redo类的listener (OnPlayerPieceRedoListener, OnAntPlayerPieceRedoListener)
     * @param pos stack pop出的值
     * @param isPlayer 是否是玩家stack pop出的
     */
    private void popCallRedoListener(SizeF pos, boolean isPlayer){
        if(pos == null) return;
        if(isPlayer){
            if(playerPieceRedoListener != null) playerPieceRedoListener.listener(
                    Math.round((pos.getWidth()-padding-innerPadding)/gridSize)
                    , Math.round((pos.getHeight()-padding-innerPadding)/gridSize)
                    , playerPiecePositions.isEmpty() && antPlayerPiecePositions.isEmpty()
            );
        }else{
            if(antPlayerPieceRedoListener != null) antPlayerPieceRedoListener.listener(
                    Math.round((pos.getWidth()-padding-innerPadding)/gridSize)
                    , Math.round((pos.getHeight()-padding-innerPadding)/gridSize)
                    , playerPiecePositions.isEmpty() && antPlayerPiecePositions.isEmpty()
            );
        }
    }

    /**
     ******************************* listener start ************************************
     */

    //listener应该尽快返回
    public void setOnPlayerPieceDropListener(OnPlayerPieceDropListener listener){
        this.pieceDropListener = listener;
    }

    public void setOnPlayerPieceRedoListener(OnPlayerPieceRedoListener listener){
        this.playerPieceRedoListener = listener;
    }

    public void setOnAntPlayerPieceRedoListener(OnAntPlayerPieceRedoListener listener){
        this.antPlayerPieceRedoListener = listener;
    }

    //玩家棋子落下
    public interface OnPlayerPieceDropListener{
        abstract void listener(int x, int y);
    }

    //玩家棋子提起（悔棋）
    public interface OnPlayerPieceRedoListener{
        abstract void listener(int x, int y, boolean noPieceInBoard);
    }

    //对手玩家棋子提起（悔棋）
    public interface OnAntPlayerPieceRedoListener{
        abstract void listener(int x, int y, boolean noPieceInBoard);
    }

    /**
     ******************************* listener end ************************************
     */
}
