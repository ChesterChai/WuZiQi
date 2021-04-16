package com.example.wuziqi.core;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.example.wuziqi.MainActivity;
import com.example.wuziqi.R;
import com.example.wuziqi.view.CheckerBoard;
import com.example.wuziqi.view.dialog.WarningDialog;

import java.io.Serializable;

import ego.gomoku.core.Config;
import ego.gomoku.entity.Point;
import ego.gomoku.enumeration.Color;
import ego.gomoku.enumeration.Level;
import ego.gomoku.helper.WinChecker;
import ego.gomoku.player.GomokuPlayer;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String GAME_START_PARAM = "GAME_START_PARAM";
    CheckerBoard checkerBoard;
    Color[][] boardPieceMap;
    //在map中的棋子颜色不重要，只要playerColor和antPlayerColor相对就好
    Color PLAYER_COLOR =  Color.BLACK;
    Color ANT_PLAYER_COLOR = Color.WHITE;
    Color NULL_COLOR = Color.NULL;
    Point DRAW_WIN = new Point(-3, -3); //平手
    boolean GAME_STARTED = false;

    boolean PLAYER_IS_FIRST = false; //玩家先走
    Level AI_PLAYER_LEVEL = Level.EASY;
    CheckerBoard.BoardSizeMode BOARD_SIZE_MODE = CheckerBoard.BoardSizeMode.MAX;

    //https://blog.csdn.net/cysion1989/article/details/84501399
    CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        init();
    }

    private void init(){
        GameStartParam gameStartParam=(GameStartParam)getIntent().getSerializableExtra(GAME_START_PARAM);
        PLAYER_IS_FIRST = gameStartParam.isPlayerIsBlack();
        AI_PLAYER_LEVEL = gameStartParam.getAiLevel();
        BOARD_SIZE_MODE = gameStartParam.getSizeMode();

        //设置AI引擎配置
        Config.size = BOARD_SIZE_MODE.getV();

        checkerBoard = new CheckerBoard(this, BOARD_SIZE_MODE, PLAYER_IS_FIRST);
        ((ViewGroup)findViewById(R.id.checkbox_container)).addView(checkerBoard);

        reset();

        checkerBoard.setOnPlayerPieceDropListener((x ,y) ->{
            boardPieceMap[x][y] = PLAYER_COLOR;
            checkWiner();
            mDisposable.add(
                    Observable.create(new ObservableOnSubscribe<Point>() {
                        @Override
                        public void subscribe(@NonNull ObservableEmitter<Point> emitter) throws Exception {
                            GomokuPlayer gomokuPlayer = new GomokuPlayer(boardPieceMap, AI_PLAYER_LEVEL);
                            Point pos;
                            try{
                                pos = gomokuPlayer.play(ANT_PLAYER_COLOR).getPoint();
                            }catch (Exception ignored){
                                pos = DRAW_WIN;
                            }
                            emitter.onNext(pos);
                            emitter.onComplete();
                        }
                    }).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Point>() {
                        @Override
                        public void accept(Point point) throws Exception {
                            if(point.equals(DRAW_WIN)){
                                draw();
                            }else{
                                boardPieceMap[point.getX()][point.getY()] = ANT_PLAYER_COLOR;
                                checkerBoard.drawPiece(point.getX(), point.getY());
                                checkWiner();
                            }
                        }
                    })
            );
        });
        checkerBoard.setOnPlayerPieceRedoListener((x, y, noPiece) -> {
            boardPieceMap[x][y] = NULL_COLOR;
            if(noPiece && !PLAYER_IS_FIRST){
                randomBegin();
            }
        });
        checkerBoard.setOnAntPlayerPieceRedoListener((x, y, noPiece) -> {
            boardPieceMap[x][y] = NULL_COLOR;
            if(noPiece && !PLAYER_IS_FIRST){
                randomBegin();
            }
        });

        findViewById(R.id.redo).setOnClickListener(v -> {
            redo(true);
        });
    }

    /**
     * 开始，AI先行，在任意位置放一个棋子
     */
    private void randomBegin(){
        GomokuPlayer gomokuPlayer = new GomokuPlayer(boardPieceMap, AI_PLAYER_LEVEL);
        Point result = gomokuPlayer.randomBegin(ANT_PLAYER_COLOR).getPoint();
        boardPieceMap[result.getX()][result.getY()] = ANT_PLAYER_COLOR;
        checkerBoard.drawPiece(result.getX(), result.getY());
        GAME_STARTED = true;
    }

    private void checkWiner(){
        Color winer;
        if ((winer = WinChecker.win(boardPieceMap)) != null) {
            if(winer == PLAYER_COLOR){
                WarningDialog dialog = new WarningDialog(this);
                dialog.build("结束", "你获胜了！", "确认", v -> {
                    reset();
                    GAME_STARTED = false;
                }).show();
            }else {
                WarningDialog dialog = new WarningDialog(this);
                dialog.build("结束", "AI获胜了！", "悔棋", "确认").setOnButtonClickListener(
                    new WarningDialog.OnButtonClickListener() {
                        @Override
                        public void onLeftButtonClick() {
                            redo(true);
                        }

                        @Override
                        public void onRightButtonClick() {
                            reset();
                            GAME_STARTED = false;
                        }
                    }
                ).show();
            }
        }
    }

    private void draw(){
        WarningDialog dialog = new WarningDialog(this);
        dialog.build("结束", "平局！", "确认", v -> {
            reset();
            GAME_STARTED = false;
        }).show();
    }

    /**
     * 悔棋
     * @param isPlayer 是否是玩家悔棋
     */
    private void redo(boolean isPlayer){
        if(isPlayer){
            //不接收AI计算的结果了
            mDisposable.clear();
            checkerBoard.redo(true);
        }else {
            checkerBoard.redo(false);
        }
    }

    private void reset(){
        boardPieceMap = new Color[BOARD_SIZE_MODE.getV()][BOARD_SIZE_MODE.getV()];
        for (int i = 0; i < BOARD_SIZE_MODE.getV(); i++) {
            for (int j = 0; j < BOARD_SIZE_MODE.getV(); j++) {
                boardPieceMap[i][j] = NULL_COLOR;
            }
        }

        if(!PLAYER_IS_FIRST){
            checkerBoard.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
                @Override
                public void onDraw() {
                    if(!GAME_STARTED){
                        randomBegin();
                    }
                }
            });
        }

        checkerBoard.reset();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //如果activity退出，就将subscribe dispose；
        mDisposable.dispose();
        System.gc();
    }

    public static class GameStartParam implements Serializable {
        int sizeMode; //1,2,3,else ==> MIN，MID，MAX，MID
        boolean playerIsBlack = true; //true,false ==> true,false;
        int aiLevel;//1,2,3,4,else ==> EASY, NORMAL, HIGH, VERY_HIGH, EASY

        public GameStartParam(int sizeMode, boolean playerIsBlack, int aiLevel) {
            this.sizeMode = sizeMode;
            this.playerIsBlack = playerIsBlack;
            this.aiLevel = aiLevel;
        }

        public CheckerBoard.BoardSizeMode getSizeMode() {
            CheckerBoard.BoardSizeMode mode;
            switch (sizeMode){
                case 1:
                    mode = CheckerBoard.BoardSizeMode.MIN;
                    break;
                case 2:
                    mode = CheckerBoard.BoardSizeMode.MID;
                    break;
                case 3:
                    mode = CheckerBoard.BoardSizeMode.MAX;
                    break;
                default:
                    mode = CheckerBoard.BoardSizeMode.MID;
                    break;
            }
            return mode;
        }

        public boolean isPlayerIsBlack() {
            return playerIsBlack;
        }

        public Level getAiLevel() {
            Level level;
            switch (aiLevel){
                case 1:
                    level = Level.EASY;
                    break;
                case 2:
                    level = Level.NORMAL;
                    break;
                case 3:
                    level = Level.HIGH;
                    break;
                case 4:
                    level = Level.VERY_HIGH;
                    break;
                default:
                    level = Level.EASY;
                    break;
            }
            return level;
        }
    }
}