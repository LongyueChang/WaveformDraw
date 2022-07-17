package com.yunxi.voiceview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @author : longyue
 * @data : 2022/7/17
 * @email : changyl@yunxi.tv
 */
public class AudioDbText extends View {
    private final String TAG = AudioDbText.class.getSimpleName();
    private static final boolean LOG_ENABLE = true;

    private int bgColor;
    /**
     * 刻度颜色
     */
    private int gradationColor;

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
     * 数值文字宽度的一半：时间格式为“00:00”，所以长度固定
     */
    private final float mTextHalfWidth;

    private Paint mPaint;
    private TextPaint mTextPaint;
    private int mWidth, mHeight;

    /**
     * 刻度按照屏幕宽等额分配分数
     */
    private final int dbDivision=7;


    public AudioDbText(Context context) {
        this(context, null);
    }

    public AudioDbText(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioDbText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);

        init(context);

        mTextHalfWidth = mTextPaint.measureText("-12") * .5f;
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TimeRuleView);
        bgColor = ta.getColor(R.styleable.TimeRuleView_yunxi_bgColor, Color.parseColor("#1F1F1F"));
        gradationColor = ta.getColor(R.styleable.TimeRuleView_yunxi_gradationColor, Color.parseColor("#888888"));
        gradationWidth = ta.getDimension(R.styleable.TimeRuleView_trv_gradationWidth, 1);
        dbLen = ta.getDimension(R.styleable.TimeRuleView_trv_minuteLen, dp2px(6));
        hourLen = ta.getDimension(R.styleable.TimeRuleView_trv_hourLen, dp2px(4));
        gradationTextColor = ta.getColor(R.styleable.TimeRuleView_trv_gradationTextColor, Color.parseColor("#FFFFFF"));
        gradationTextSize = ta.getDimension(R.styleable.TimeRuleView_trv_gradationTextSize, sp2px(8));
        gradationTextGap = ta.getDimension(R.styleable.TimeRuleView_trv_gradationTextGap, dp2px(2));
        ta.recycle();
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(gradationTextSize);
        mTextPaint.setColor(gradationTextColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            mHeight = dp2px(60);
        }

        setMeasuredDimension(mWidth, mHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // 背景
        canvas.drawColor(bgColor);

        // 刻度
        drawRule(canvas);
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

        float offset = mHeight/dbDivision/2;
        Log.d(TAG,"offset:"+offset+";mWidth:"+mWidth);

        while (start <= 6) {
            String text = formatDbText(start);
            Log.d(TAG,"text:"+text);
            canvas.drawText(text, hourLen+5,  offset+(gradationTextSize/2), mTextPaint);

            //细分db刻度
            canvas.drawLine(0, offset, hourLen, offset, mPaint);

            start += 1;
            offset +=  mHeight/dbDivision;
        }

        canvas.restore();
    }



   private static final String[] DB_TEXT ={"-3","-6","-12","-∞","-12","-6","-3"};
    private static String formatDbText(int startIdx){
        return DB_TEXT[startIdx];
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
