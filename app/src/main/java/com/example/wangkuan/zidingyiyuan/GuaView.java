package com.example.wangkuan.zidingyiyuan;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by wangkuan on 2016/11/5.
 */
public class GuaView extends View {
    private String mInfo = "谢谢惠顾";  //默认值
    private boolean mIsAward = false;  //默认值
    private float mTextSize = 100;      //默认值
    private int mTextColor = Color.BLACK;//默认值
    private Path mPath;
    private Paint mOuterPaint;
    private Paint mInnerPaint;
    private Bitmap mCoverBitmap; //遮盖图
    private Rect mTextBound;
    private AtomicBoolean mComplete; //保证原子性操作
    private int mWidth, mHeight;
    private Bitmap mBitmap; //遮盖的图层
    private Canvas mCanvas; //绘制遮盖图层
    private float mLastX;
    private float mLastY;
    private onGuaGuaKaCompletedListener mOnGuaGuaKaCompletedListener;

    public interface onGuaGuaKaCompletedListener {
        void complete(String message);
    }

    public void setOnGuaGuaKaCompletedListener(onGuaGuaKaCompletedListener mOnGuaGuaKaCompletedListener) {
        this.mOnGuaGuaKaCompletedListener = mOnGuaGuaKaCompletedListener;
    }

    public GuaView(Context context) {
        this(context, null);
    }

    public GuaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获得属性类
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.guaguaka);
        //得到属性的个数
        int indexCount = typedArray.getIndexCount();
        //以个数为条件循环
        for (int i = 0; i < indexCount; i++) {
            //得到下标
            int attrIndex = typedArray.getIndex(i);
            switch (attrIndex) {
                case R.styleable.guaguaka_text:
                    mInfo = typedArray.getString(attrIndex);
                    break;
                case R.styleable.guaguaka_isAward:
                    mIsAward = typedArray.getBoolean(attrIndex, false);
                    break;
                case R.styleable.guaguaka_textSize:
                    mTextSize = typedArray.getDimension(attrIndex, 100);
                    break;
                case R.styleable.guaguaka_textColor:
                    mTextColor = typedArray.getColor(attrIndex, Color.BLACK);
                    break;
            }
        }
        //回收
        typedArray.recycle();
        //准备画笔
        init();
    }

    private void init() {
        //路径类
        mPath = new Path();
        //准备两个画笔
        mOuterPaint = new Paint();
        mInnerPaint = new Paint();
        //初始化覆盖图片图片
        mCoverBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.fg_guaguaka);
        //设置刮刮卡大小
        mTextBound = new Rect();
        //保持原子性操作
        mComplete = new AtomicBoolean(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //得到测量过的宽高
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        //private Bitmap mBitmap; //遮盖的图层
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);//设置成画布,也就是遮盖的图层

        mCanvas.drawColor(Color.parseColor("#abc777"));//图片中有空白像素的地方，先在位图上绘制一个底色
        mCanvas.drawRoundRect(new RectF(0, 0, mWidth, mHeight), 30, 30, mOuterPaint); //画个圆角矩形
//        mCanvas.drawBitmap(mCoverBitmap, 0, 0, null);
        mCanvas.drawBitmap(mCoverBitmap, null, new Rect(0, 0, mWidth, mHeight), null); //再贴上遮盖的图片
        setOuterPaint();
        setInnerPaint();

    }

    //设置内部画笔
    private void setInnerPaint() {
        mInnerPaint.setColor(mTextColor);
        mInnerPaint.setTextSize(mTextSize);
        mInnerPaint.setStyle(Paint.Style.STROKE);
        mInnerPaint.setStrokeCap(Paint.Cap.ROUND);
        mInnerPaint.setStrokeJoin(Paint.Join.ROUND);
        mInnerPaint.setAntiAlias(true);
        mInnerPaint.setDither(true); //防抖
        mInnerPaint.setStrokeWidth(5);
        mInnerPaint.setTextAlign(Paint.Align.CENTER);
        mInnerPaint.getTextBounds(mInfo, 0, mInfo.length(), mTextBound); //计算绘制文本所占的区域 put in rect
    }

    //设置外部画笔
    private void setOuterPaint() {
        mOuterPaint.setColor(Color.GREEN);
        mOuterPaint.setStyle(Paint.Style.STROKE);
        mOuterPaint.setStrokeCap(Paint.Cap.ROUND);
        mOuterPaint.setStrokeJoin(Paint.Join.ROUND);
        mOuterPaint.setAntiAlias(true);
        mOuterPaint.setDither(true); //防抖
        mOuterPaint.setStrokeWidth(20);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mComplete.get()) return true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                mPath.moveTo(mLastX, mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                float deltaX = Math.abs(x - mLastX);
                float deltaY = Math.abs(y - mLastY);
                if (deltaX > 5 || deltaY > 5) {
                    mPath.lineTo(x, y);
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (!mComplete.get()) {
                    new Thread(mRunnable).start();
                }
                break;
        }
        if (!mComplete.get()) {
            invalidate();
        }
        return true;


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //背景的底色
        canvas.drawColor(Color.parseColor("#bbbbbb")); //背景底色  灰色

//        canvas.drawText(mInfo, mWidth / 2, mHeight / 4 * 3, mInnerPaint); //绘制文本
        //设置内部位子的排列方式
        mInnerPaint.setTextAlign(Paint.Align.LEFT);
        //绘制字体
        canvas.drawText(mInfo, mWidth / 2 - mTextBound.width() / 2, mHeight / 2 + mTextBound.height() / 2, mInnerPaint); //绘制文本
        //非原子性操作
        if (!mComplete.get()) {
            canvas.drawBitmap(mBitmap, 0, 0, null); //绘制mBitmap   这是一个可变的bitmap，通过mCanvas绘制，首先绘制了mCoverBitmap
            drawPath();
        } else {
            if (mOnGuaGuaKaCompletedListener != null) {
                if (mIsAward) {
                    mOnGuaGuaKaCompletedListener.complete("恭喜您中奖" + mInfo + "元!!");
                } else {
                    mOnGuaGuaKaCompletedListener.complete(mInfo);
                }
            }
        }
    }

    private void drawPath() {
        //使用该mode：dst和src相交后， 只保留dst，且除去相交的部份
        mOuterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mOuterPaint);
    }
    //线程
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();
            float wipeArea = 0; //已擦除的像素区
            float totalArea = w * h; //总共的像素区
            Bitmap bitmap = mBitmap;
            int[] mPixels = new int[w * h];
            /*
pixels      接收位图颜色值的数组
offset      写入到pixels[]中的第一个像素索引值
stride      pixels[]中的行间距个数值(必须大于等于位图宽度)。可以为负数
x          　从位图中读取的第一个像素的x坐标值。
y           从位图中读取的第一个像素的y坐标值
width    　　从每一行中读取的像素宽度
height 　　　读取的行数
             */
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
//                    int index = i + j * w;
                    int index = j + i * h;
                    if (mPixels[index] == 0) {
                        wipeArea++; //绘制的mBitmap本身就有空白的地方， 所以这个数据本身就有一部分
                    }
                }
            }
            if (wipeArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea); //擦除的百分比
//                System.out.println(percent + "%");
                if (percent > 55) {
                    mComplete.set(true);
                    postInvalidate();
                }
            }
        }
    };
}
