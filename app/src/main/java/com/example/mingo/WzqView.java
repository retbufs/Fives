package com.example.mingo;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WzqView extends View {
    private static final String TAG = "WzqView";
    private int mPanelWidth;
    private float mLineHeight;
    //
    private float MAX_LINE = 15;
    private static float MAX_LIMIT_LINE_NUM = 255;
    private Bitmap mBlackPiece;
    private Bitmap mWhitePiece;
    private float ratioPaceOfLineHeight = 3 * 1.0f / 4;
    private Paint mPaint = new Paint();
    private SoundPool mSoundPool = null;
    //白棋先手
    private boolean mIsWhitePiece = true;
    private List<Point> mWhitePoint = new ArrayList<>();
    private List<Point> mBlackPoint = new ArrayList<>();
    private int load;
    private int bg_id;
    private boolean mIsGameOver = false;
    private boolean mIsWhiteVictory = false;
    public static final int MAX_COUNT_IN_LINE = 5;
    private MediaPlayer mMediaPlayer;
    private OnGameOverListeners mOnGameOverListeners = null;
    private OnPlayChessListeners mOnPlayChessListeners = null;

    public WzqView(Context context) {
        super(context);
        init(context);
    }

    public WzqView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WzqView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 常见初始化操作
     */
    private void init(Context context) {
        setBackgroundColor(0xFFD4AC59);
        mPaint.setColor(0x88000000);
        mMediaPlayer = MediaPlayer.create(getContext(), R.raw.bg5);
        mSoundPool = new SoundPool(100, AudioManager.STREAM_SYSTEM, 5);
        try {
            bg_id = mSoundPool.load(getResources().getAssets().openFd("bg_music1.mp3"), 1);
            load = mSoundPool.load(getContext(), R.raw.ding, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dingPayMusic();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mMediaPlayer.start();
            }
        });
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mWhitePiece = BitmapFactory.decodeResource(getResources(), R.mipmap.hong);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.mipmap.hei);
        //设置默认白棋先行
        if (mOnPlayChessListeners != null) {
            mOnPlayChessListeners.onWhite();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //获取棋盘的最小值
        int width = Math.min(widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            width = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            width = widthSize;
        }
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPiece(canvas);
        //检查棋子状态
        checkVictory();
    }

    private void checkVictory() {
        boolean whiteVictory = checkPointVictory(mWhitePoint);
        boolean blackVictory = checkPointVictory(mBlackPoint);

        if (whiteVictory || blackVictory) {
            //游戏结束
            mIsGameOver = true;
            mIsWhiteVictory = whiteVictory;
        } else {
            return;
        }
        if (mOnGameOverListeners != null) {
            mOnGameOverListeners.onOver(mIsWhiteVictory);
        }
        //游戏结束
        String text = mIsWhiteVictory ? "白棋获胜游戏结束" : "黑棋获胜游戏结束";
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    private boolean checkPointVictory(List<Point> points) {
        boolean isVictory = false;
        for (int i = 0, n = points.size(); i < n; i++) {
            Point point = points.get(i);
            boolean isHorizontal = checkHorizontal(point.x, point.y, points);
            boolean isVertical = checkVertical(point.x, point.y, points);
            boolean isDiagonal = checkDiagonal(point.x, point.y, points);
            boolean isDis = checkRightDiagonal(point.x, point.y, points);
            if (isHorizontal || isVertical || isDiagonal || isDis) {
                Log.i(TAG, "checkPointVictory: "
                        + isDiagonal +
                        ":isVertical:" + isVertical +
                        "isDiagonal:" + isDiagonal +
                        "isDis:" + isDis +
                        "");
                isVictory = true;
                break;
            }
        }
        return isVictory;
    }

    /**
     * 检查x ，y 位置，是否有相邻的五个一致
     *
     * @param x
     * @param y
     * @param points
     * @return
     */
    private boolean checkHorizontal(int x, int y, List<Point> points) {
        int count = 1;
        //左
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x - i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        //右边
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        return false;
    }

    private boolean checkDiagonal(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1, n = MAX_COUNT_IN_LINE; i < n; i++) {
            if (points.contains(new Point(x - i, y + i))) {
                Log.i(TAG, "checkDiagonal:x= " + (x - i) + "y=" + (y + i));
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        //右边
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        return false;
    }

    private boolean checkRightDiagonal(int x, int y, List<Point> points) {
        int count = 1;
        for (int i = 1, n = MAX_COUNT_IN_LINE; i < n; i++) {
            if (points.contains(new Point(x - i, y - i))) {
                Log.i(TAG, "checkDiagonal:x= " + (x - i) + "y=" + (y + i));
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        //右边
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x + i, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        return false;
    }

    private boolean checkVertical(int x, int y, List<Point> points) {
        int count = 1;
        //下
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y - i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        //上
        for (int i = 1; i < MAX_COUNT_IN_LINE; i++) {
            if (points.contains(new Point(x, y + i))) {
                count++;
            } else {
                break;
            }
        }
        if (count == MAX_COUNT_IN_LINE) return true;
        return false;
    }

    private Point getValidPoint(int x, int y) {
        return new Point((int) (x / mLineHeight), (int) (y / mLineHeight));
    }

    //绘制棋子
    private void drawPiece(Canvas canvas) {
        //绘制白色棋子
        for (int i = 0, n = mWhitePoint.size(); i < n; i++) {
            Point whitePoint = mWhitePoint.get(i);
//            Log.i(TAG, "WhitePoint_drawPiece:x= " + whitePoint.x);
//            Log.i(TAG, "WhitePoint_drawPiece:y=" + whitePoint.y);
            canvas.drawBitmap(mWhitePiece,
                    (float) ((whitePoint.x + (1 - ratioPaceOfLineHeight) / 2) * mLineHeight),
                    (float) ((whitePoint.y + (1 - ratioPaceOfLineHeight) / 2) * mLineHeight),
                    null);
        }
        //绘制黑色棋子
        for (int i = 0, n = mBlackPoint.size(); i < n; i++) {
            Point blackPoint = mBlackPoint.get(i);
//            Log.i(TAG, "BlackPoint_drawPiece:x= " + blackPoint.x);
//            Log.i(TAG, "BlackPoint_drawPiece:y=" + blackPoint.y);
            // Log.i(TAG, "BlackPoint_drawPiece:mLineHeight=" + (mLineHeight + (blackPoint.x + ((1 - ratioPaceOfLineHeight) / 2) * mLineHeight)));
            canvas.drawBitmap(mBlackPiece,
                    (float) ((blackPoint.x + ((1 - ratioPaceOfLineHeight) / 2)) * mLineHeight),
                    (float) ((blackPoint.y + ((1 - ratioPaceOfLineHeight) / 2)) * mLineHeight),
                    null);
        }
    }

    //绘制棋局
    private void drawBoard(Canvas canvas) {
        int w = mPanelWidth;
        float lineHeight = mLineHeight;
        Log.i(TAG, "drawBoard: 格子高度" + lineHeight);
        for (int i = 0; i < MAX_LINE; i++) {
            int startX = (int) (lineHeight / 2);
            int endX = (int) (w - lineHeight / 2);
            int y = (int) ((0.5 + i) * lineHeight);
            canvas.drawLine(startX, y, endX, y, mPaint);
            canvas.drawLine(y, startX, y, endX, mPaint);
            if (i == 3 || i == 7 || i == 11) {
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawCircle(y, (i + 1) * mLineHeight - (startX), 15, mPaint);
                if (i != 7) {
                    canvas.drawCircle(12 * mLineHeight - startX, y, 15, mPaint);
                }
                if (i == 3) {
                    canvas.drawCircle((i + 1) * mLineHeight - startX, (12) * mLineHeight - startX, 15, mPaint);
                }
                mPaint.setStyle(Paint.Style.STROKE);
            }
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPanelWidth = w;
//        Log.i(TAG, "onSizeChanged: mPanelWidth=" + mPanelWidth);
        mLineHeight = mPanelWidth / 1.0f / MAX_LINE;
//        Log.i(TAG, "onSizeChanged:mLineHeight=" + mLineHeight);
        int pieceWidth = (int) (mLineHeight * ratioPaceOfLineHeight);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        //游戏结束不在监听消息结束时间
        if (mIsGameOver) return false;
        if (action == MotionEvent.ACTION_UP) {
            //取出点击位置坐标
            int x = (int) event.getX();
            int y = (int) event.getY();
            Point p = getValidPoint(x, y);
            Log.i(TAG, "onTouchEvent:接收到点击事件====>:x=" + p.x + "\ty=" + p.y);
            //防止放置同一位置
            if (mBlackPoint.contains(p) ||
                    mWhitePoint.contains(p)) {
                return false;
            }
            if (mIsWhitePiece) {
                mWhitePoint.add(p);
            } else {
                mBlackPoint.add(p);
            }
            mIsWhitePiece = !mIsWhitePiece;
            mSoundPool.play(load, 1, 1, 0, 0, 1);
            if (mIsWhitePiece) {
                if (mOnPlayChessListeners != null) {
                    mOnPlayChessListeners.onWhite();
                }
            } else {
                if (mOnPlayChessListeners != null) {
                    mOnPlayChessListeners.onBlack();
                }
            }
            invalidate();
        }
        return true;
    }

    public void dingPayMusic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Log.i(TAG, "init: 播放背景音效");
                    // mSoundPool.play(bg_id, 1, 1, 0, -1, 1);
                    mMediaPlayer.start();
                } catch (Exception e) {
                    Log.i(TAG, "dingPayMusic: 异常");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setOnGameOverListeners(OnGameOverListeners onGameOverListeners) {
        this.mOnGameOverListeners = onGameOverListeners;
    }

    public void setOnPlayChessListeners(OnPlayChessListeners onPlayChessListeners) {
        this.mOnPlayChessListeners = onPlayChessListeners;
    }

    //游戏结束监听接口
    public interface OnGameOverListeners {
        //判断是否是白棋获胜
        public void onOver(boolean isWhiteVictory);
    }

    //当前轮到谁下子
    public interface OnPlayChessListeners {
        public void onWhite();

        public void onBlack();
    }

    public interface OnPlayListeners {
        //游戏开始
        public void start();

        //游戏暂停
        public void pause();

        //游戏重新开始
        public void restart();
    }

    private OnPlayListeners mPlayListeners;

    public void setOnPlayListeners(OnPlayListeners playListeners) {
        this.mPlayListeners = playListeners;
    }

    //游戏重新开始
    public void restart() {
        mIsWhitePiece = true;
        mIsGameOver = false;
        mIsWhiteVictory = false;
        mWhitePoint.clear();
        mBlackPoint.clear();
        invalidate();
    }
}
