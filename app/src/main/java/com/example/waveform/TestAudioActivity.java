package com.example.waveform;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.yunxi.voiceview.VoiceGroup;

/**
 * @author : longyue
 * @data : 2022/7/18
 * @email : changyl@yunxi.tv
 */
public class TestAudioActivity extends AppCompatActivity {
    private final String TAG = TestAudioActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        VoiceGroup voiceGroup = new VoiceGroup(this);
        setContentView(R.layout.test_audio_activity);
    }
}
