package com.yunxi.voiceview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;


/**
 * @author:wsh
 * @date:2022/6/13
 */
public class VoiceDbView extends View {

    private int bgColor;
    private int startColor;
    private int centerColor;
    private int endColor;
    private Paint paint;

    private int[] colors;
    private float[] positions;
    private LinearGradient linearGradient;

    private int progress = 0;
    private float progressPercent = 0f;

    public VoiceDbView(Context context) {
        this(context, null);
    }

    public VoiceDbView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceDbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NewVoiceView, defStyleAttr, 0);
        bgColor = typedArray.getColor(R.styleable.NewVoiceView_bgColor, Color.parseColor("#3D000000"));
        startColor = typedArray.getColor(R.styleable.NewVoiceView_startColor, Color.parseColor("#03da00"));
        centerColor = typedArray.getColor(R.styleable.NewVoiceView_centerColor, Color.parseColor("#e1a300"));
        endColor = typedArray.getColor(R.styleable.NewVoiceView_endColor, Color.parseColor("#da0000"));
        typedArray.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setVoice(int voiceCount) {
        progress = voiceCount;
        progressPercent = progress * 1.0f / 100;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        if (progress == 0) {
            //全灰
            colors = new int[]{bgColor,bgColor};
            positions = new float[]{0f,1f};
        } else if (progress <= 60) {
            //全绿
            colors = new int[]{startColor, bgColor};
            positions = new float[]{0f, progressPercent};
        } else if (progress <= 90) {
            //60%绿 progressPercent-0.6橙
            colors = new int[]{startColor, centerColor, bgColor};
            positions = new float[]{0f, 0.6f, progressPercent};
        } else if (progress < 100) {
            //60%绿 30%橙 progressPercent-0.9红
            colors = new int[]{startColor, centerColor, endColor, bgColor};
            positions = new float[]{0, 0.6f, 0.9f, progressPercent};
        } else {
            //60%绿 30%橙  10%红
            colors = new int[]{startColor, centerColor, endColor};
            positions = new float[]{0f, 0.6f, 0.9f};
        }

        linearGradient = new LinearGradient(0, 0, width, 0, colors, positions, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        canvas.drawRect(0, 0, width, height, paint);
    }
}
