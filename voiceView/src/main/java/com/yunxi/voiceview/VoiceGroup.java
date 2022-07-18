package com.yunxi.voiceview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

/**
 * @author : longyue
 * @data : 2022/7/18
 * @email : changyl@yunxi.tv
 */
public class VoiceGroup extends RelativeLayout {
    private VoiceDbView voiceDbView;
    private VoiceDbText dbView;
    private TimeView timeRuleView;
    private AudioSurfaceView audioSurfaceView;

    public VoiceGroup(Context context) {
        this(context,null);
    }

    public VoiceGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(getContext()).inflate(R.layout.audio_group, this);
        initView(context);
    }

    private void initView(Context context) {
        audioSurfaceView=findViewById(R.id.bsv_singleChannel);
        voiceDbView = findViewById(R.id.voiceDb_view);
        dbView = findViewById(R.id.voiceDb_text);
        timeRuleView = findViewById(R.id.time_rule_view);
    }


}
