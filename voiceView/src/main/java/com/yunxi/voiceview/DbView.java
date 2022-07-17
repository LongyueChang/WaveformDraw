package com.yunxi.voiceview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.Calendar;

/**
 * @author : longyue
 * @data : 2022/7/15
 * @email : changyl@yunxi.tv
 */
public class DbView extends View {
    private final String TAG = DbView.class.getSimpleName();


    private static final boolean LOG_ENABLE = true;
    public static final int MAX_TIME_VALUE = 24 * 3600;

    private int bgColor;
    /**
     * 刻度颜色
     */
    private int gradationColor;
    /**
     * 时间块的高度
     */
    private float partHeight;
    /**
     * 时间块的颜色
     */
    private int partColor;
    /**
     * 刻度宽度
     */
    private float gradationWidth;
    /**
     * 秒、分、时刻度的长度
     */
    private float dbLen;
    private float hourLen;
    /**
     * 刻度数值颜色、大小、与时刻度的距离
     */
    private int gradationTextColor;
    private float gradationTextSize;
    private float gradationTextGap;

    /**
     * 当前时间，单位：s
     */
    private @IntRange(from = 0, to = MAX_TIME_VALUE)
    int currentTime;
    /**
     * 指针颜色
     */
    private int indicatorColor;
    /**
     * 指针上三角形的边长
     */
    private float indicatorTriangleSideLen;
    /**
     * 指针的宽度
     */
    private float indicatorWidth;

    /**
     * 最小单位对应的单位秒数值，一共四级: 10s、1min、5min、15min
     * 与 {@link #mPerTextCounts} 和 {@link #mPerCountScaleThresholds} 对应的索引值
     * <p>
     * 可以组合优化成数组
     */
    private static int[] mUnitSeconds = {
            1, 10, 10, 10,
            60, 60,
            5 * 60, 5 * 60,
            15 * 60, 15 * 60, 15 * 60, 15 * 60, 15 * 60, 15 * 60
    };

    /**
     * 数值显示间隔。一共13级，第一级最大值，不包括
     */
    @SuppressWarnings("all")
    private static int[] mPerTextCounts = {
            60, 60, 2 * 60, 4 * 60, // 10s/unit: 最大值, 1min, 2min, 4min
            5 * 60, 10 * 60, // 1min/unit: 5min, 10min
            20 * 60, 30 * 60, // 5min/unit: 20min, 30min
            3600, 2 * 3600, 3 * 3600, 4 * 3600, 5 * 3600, 6 * 3600 // 15min/unit
    };

    /**
     * 与 {@link #mPerTextCounts} 对应的阈值，在此阈值与前一个阈值之间，则使用此阈值对应的间隔数值
     * 如：1.5f 代表 4*60 对应的阈值，如果 mScale >= 1.5f && mScale < 1.8f，则使用 4*60
     * <p>
     * 这些数值，都是估算出来的
     */
    @SuppressWarnings("all")
    private float[] mPerCountScaleThresholds = {
            6f, 3.6f, 1.8f, 1.5f, // 10s/unit: 最大值, 1min, 2min, 4min
            0.8f, 0.4f,   // 1min/unit: 5min, 10min
            0.25f, 0.125f, // 5min/unit: 20min, 30min
            0.07f, 0.04f, 0.03f, 0.025f, 0.02f, 0.015f // 15min/unit: 1h, 2h, 3h, 4h, 5h, 6h
    };
    /**
     * 默认mScale为1
     */
    private float mScale = 1;
    /**
     * 1s对应的间隔，比较好估算
     */
    private final float mOneSecondGap = dp2px(12) / 60f;
    /**
     * 当前最小单位秒数值对应的间隔
     */
    private final float mUnitGap = mOneSecondGap * 60;
    /**
     * 默认索引值
     */
    private int mPerTextCountIndex = 1;
    /**
     * 一格代表的秒数。默认1min
     */
    private int mUnitSecond = mUnitSeconds[mPerTextCountIndex];

    /**
     * 数值文字宽度的一半：时间格式为“00:00”，所以长度固定
     */
    private final float mTextHalfWidth;

    private final int SCROLL_SLOP;
    private final int MIN_VELOCITY;
    private final int MAX_VELOCITY;

    /**
     * 当前时间与 00:00 的距离值
     */
    private float mCurrentDistance;


    private Paint mPaint;
    private TextPaint mTextPaint;
    private Path mTrianglePath;
    private Scroller mScroller;


    /**
     * 缩放手势检测器
     */
    private ScaleGestureDetector mScaleGestureDetector;

    private int mWidth, mHeight;
    private int mHalfWidth;

    /**
     * 刻度按照屏幕宽等额分配分数
     */
    private int dbDivision=61;


    private OnTimeChangedListener mListener;

    public interface OnTimeChangedListener {
        void onTimeChanged(int newTimeValue);
    }


    public DbView(Context context) {
        this(context, null);
    }

    public DbView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DbView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);

        init(context);
        initScaleGestureDetector(context);

        mTextHalfWidth = mTextPaint.measureText("-60") * .5f;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        SCROLL_SLOP = viewConfiguration.getScaledTouchSlop();
        MIN_VELOCITY = viewConfiguration.getScaledMinimumFlingVelocity();
        MAX_VELOCITY = viewConfiguration.getScaledMaximumFlingVelocity();

        calculateValues();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimeRuleView);
        bgColor = ta.getColor(R.styleable.TimeRuleView_yunxi_bgColor, Color.parseColor("#1F1F1F"));
        gradationColor = ta.getColor(R.styleable.TimeRuleView_yunxi_gradationColor, Color.parseColor("#888888"));
        partHeight = ta.getDimension(R.styleable.TimeRuleView_trv_partHeight, dp2px(20));
        partColor = ta.getColor(R.styleable.TimeRuleView_trv_partColor, Color.parseColor("#F58D24"));
        gradationWidth = ta.getDimension(R.styleable.TimeRuleView_trv_gradationWidth, 1);
        dbLen = ta.getDimension(R.styleable.TimeRuleView_trv_minuteLen, dp2px(6));
        hourLen = ta.getDimension(R.styleable.TimeRuleView_trv_hourLen, dp2px(4));
        gradationTextColor = ta.getColor(R.styleable.TimeRuleView_trv_gradationTextColor, Color.parseColor("#FFFFFF"));
        gradationTextSize = ta.getDimension(R.styleable.TimeRuleView_trv_gradationTextSize, sp2px(8));
        gradationTextGap = ta.getDimension(R.styleable.TimeRuleView_trv_gradationTextGap, dp2px(2));
        currentTime = ta.getInt(R.styleable.TimeRuleView_trv_currentTime, 0);
        indicatorTriangleSideLen = ta.getDimension(R.styleable.TimeRuleView_trv_indicatorTriangleSideLen, dp2px(15));
        indicatorWidth = ta.getDimension(R.styleable.TimeRuleView_yunxi_indicatorLineWidth, dp2px(1));
        indicatorColor = ta.getColor(R.styleable.TimeRuleView_yunxi_indicatorLineColor, Color.RED);
        ta.recycle();
    }

    private void calculateValues() {
        mCurrentDistance = Float.parseFloat(currentTime+"")/Float.parseFloat(mUnitSecond+"") * mUnitGap;
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(gradationTextSize);
        mTextPaint.setColor(gradationTextColor);

        mTrianglePath = new Path();

        mScroller = new Scroller(context);
    }

    private void initScaleGestureDetector(Context context) {
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {

            /**
             * 缩放被触发(会调用0次或者多次)，
             * 如果返回 true 则表示当前缩放事件已经被处理，检测器会重新积累缩放因子
             * 返回 false 则会继续积累缩放因子。
             */
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                final float scaleFactor = detector.getScaleFactor();
                logD("onScale...focusX=%f, focusY=%f, scaleFactor=%f",
                        detector.getFocusX(), detector.getFocusY(), scaleFactor);

                final float maxScale = mPerCountScaleThresholds[0];
                final float minScale = mPerCountScaleThresholds[mPerCountScaleThresholds.length - 1];
                if (scaleFactor > 1 && mScale >= maxScale) {
                    // 已经放大到最大值
                    return true;
                } else if (scaleFactor < 1 && mScale <= minScale) {
                    // 已经缩小到最小值
                    return true;
                }

                mScale *= scaleFactor;
                mScale = Math.max(minScale, Math.min(maxScale, mScale));
                mPerTextCountIndex = findScaleIndex(mScale);

                mUnitSecond = mUnitSeconds[mPerTextCountIndex];
//                mUnitGap = mScale * mOneSecondGap * mUnitSecond;
                logD("onScale: mScale=%f, mPerTextCountIndex=%d, mUnitSecond=%d, mUnitGap=%f",
                        mScale, mPerTextCountIndex, mUnitSecond, mUnitGap);

                mCurrentDistance = (float) currentTime / mUnitSecond * mUnitGap;
                invalidate();
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                logD("onScaleBegin...");

                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

                logD("onScaleEnd...");
            }
        });

        // 调整最小跨度值。默认值27mm(>=sw600dp的32mm)，太大了，效果不好
        Class clazz = ScaleGestureDetector.class;
        int newMinSpan = ViewConfiguration.get(context).getScaledTouchSlop();
        try {
            @SuppressLint("SoonBlockedPrivateApi") Field mMinSpanField = clazz.getDeclaredField("mMinSpan");
            mMinSpanField.setAccessible(true);
            mMinSpanField.set(mScaleGestureDetector, newMinSpan);
            mMinSpanField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 二分法查找缩放值对应的索引值
     */
    private int findScaleIndex(float scale) {
        final int size = mPerCountScaleThresholds.length;
        int min = 0;
        int max = size - 1;
        int mid = (min + max) >> 1;
        while (!(scale >= mPerCountScaleThresholds[mid] && scale < mPerCountScaleThresholds[mid - 1])) {
            if (scale >= mPerCountScaleThresholds[mid - 1]) {
                // 因为值往小区，index往大取，所以不能为mid -1
                max = mid;
            } else {
                min = mid + 1;
            }
            mid = (min + max) >> 1;
            if (min >= max) {
                break;
            }
            if (mid == 0) {
                break;
            }
        }
        return mid;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            mHeight = dp2px(60);
        }

        mHalfWidth = 0;

        setMeasuredDimension(mWidth, mHeight);
    }

    private void computeTime() {
        // 不用转float，肯定能整除
        float maxDistance = MAX_TIME_VALUE / mUnitSecond * mUnitGap;
        // 限定范围
        mCurrentDistance = Math.min(maxDistance, Math.max(0, mCurrentDistance));
        currentTime = (int) (mCurrentDistance / mUnitGap * mUnitSecond);
        if (mListener != null) {
            mListener.onTimeChanged(currentTime);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 背景
        canvas.drawColor(bgColor);

        // 刻度
        drawRule(canvas);



        // 当前时间指针
//        drawTimeIndicator(canvas);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mCurrentDistance = mScroller.getCurrX();
            computeTime();
        }
    }

    /**
     * 绘制刻度
     */
    private void drawRule(Canvas canvas) {
        // 移动画布坐标系
        canvas.save();
        mPaint.setColor(gradationColor);
        mPaint.setStrokeWidth(gradationWidth);

        // 刻度
        int start = 0;

        float offset = mWidth/dbDivision+dp2px(5);
        Log.d(TAG,"offset:"+offset+";mWidth:"+mWidth);



        while (start <= 60) {
            // 时间数值
            if (start % 3 == 0) {
                String text = formatDbText(start);
                Log.d(TAG,"text:"+text);
                canvas.drawText(text, offset - mTextHalfWidth, hourLen + gradationTextGap + gradationTextSize + 10 , mTextPaint);
            }

            Log.d(TAG,"draw line count:"+start+";offset:"+offset);
            if(start%3==0){
                canvas.drawLine(offset, 0, offset, dbLen, mPaint);
            } else {
                //细分db刻度
                canvas.drawLine(offset, 0, offset, hourLen, mPaint);
            }

            start += 1;
            offset +=  mWidth/dbDivision;
        }

        canvas.restore();
    }

    /**
     * 绘制当前时间指针
     */
    private void drawTimeIndicator(Canvas canvas) {
        // 指针
        mPaint.setColor(indicatorColor);
        mPaint.setStrokeWidth(indicatorWidth);
        canvas.drawLine(mHalfWidth, 0, mHalfWidth, mHeight * 2 / 3, mPaint);

        // 正三角形
        if (mTrianglePath.isEmpty()) {
            //
            final float halfSideLen = indicatorTriangleSideLen * .5f;
            mTrianglePath.moveTo(mHalfWidth - halfSideLen, 0);
            mTrianglePath.rLineTo(indicatorTriangleSideLen, 0);
            mTrianglePath.rLineTo(-halfSideLen, (float) (Math.sin(Math.toRadians(60)) * halfSideLen));
            mTrianglePath.close();
        }
        mPaint.setStrokeWidth(1);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mTrianglePath, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
    }


    private static String formatDbText(int startIdx){
        StringBuilder sb = new StringBuilder();
        if(startIdx==0){
            sb.append("dB");
            return sb.toString();
        } else if(startIdx==60){
            //空格占位 -
            sb.append("  ").append(0);
            return sb.toString();
        }
        sb.append("-").append(60-startIdx);
        return sb.toString();
    }


    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    @SuppressWarnings("all")
    private void logD(String format, Object... args) {
        if (LOG_ENABLE) {
            Log.d("MoneySelectRuleView", String.format( format, args));
        }
    }
}
