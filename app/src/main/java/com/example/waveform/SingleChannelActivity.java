package com.example.waveform;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.yunxi.voiceview.AudioWaveView;
import com.yunxi.voiceview.TimeRuleView;
import com.yunxi.voiceview.BaseAudioSurfaceView;
import com.yunxi.voiceview.Constant;
import com.example.waveform.utils.MicManager;
import com.example.waveform.utils.NormalDialog;
import com.example.waveform.utils.TitleView;
import com.yunxi.voiceview.TimeView;
import com.yunxi.voiceview.VoiceDbView;
import com.yunxi.voiceview.WaveSurfaceView;
import com.yunxi.voiceview.draw.WaveCanvas;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class SingleChannelActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = SingleChannelActivity.class.getSimpleName();
    private BaseAudioSurfaceView bsv_singleChannel;
    private ImageView iv_singleChannel;
    private MicManager manager=null;
    private TitleView title_view;
    private NormalDialog rePlayDialog;
    private Button btn_singleChannel;
    private VoiceDbView voiceDbView;
    private TimeView timeRuleView;

    private ArrayList<Short> audioData;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MicManager.MODE_RECORD_FILE:
                    Random random = new Random();
                    int voiceCount = random.nextInt(100);
                    voiceDbView.setVoice(voiceCount);


//                    byte[] audioByte = (byte[]) msg.obj;
//                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
//                    short audioShort = bytesToShort(audioByte);
//                    if (audioData.size() > maxSize) {
//                        audioData.remove(0);
//                    }
//                    audioData.add(audioShort);
//                    audioWaveView.upRecData(audioData);
//                    Log.d(TAG,"audioData size:"+audioData);

                    currentTime=currentTime+1;
                    timeRuleView.setCurrentTime(currentTime);

                    bsv_singleChannel.addAudioData((byte[])msg.obj, msg.arg1, Constant.SINGLE_CHANNEL_SAMPLEER_RATE, Constant.SINGLE_CHANNLE_BIT_WIDTH, false);
                    break;
            }
        }
    };
    private int maxSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.single_channel_layout);
        initView();
        manager=new MicManager(Constant.SINGLE_FILE);
        manager.setHandler(handler);
        manager.setAudioParameters(Constant.SINGLE_CHANNEL_SAMPLEER_RATE,Constant.SINGLE_CAHNNEL_FORMAT ,Constant.SINGLE_CHANNEL_CONFIG );

        initData();
    }

    private int currentTime = 0;
    private void initData() {

//        List<TimeRuleView.TimePart> timeParts = new ArrayList<>();
//        TimeRuleView.TimePart timePart = new TimeRuleView.TimePart();
//        timePart.startTime = 0;
//        timePart.endTime = 10;
//        timeParts.add(timePart);
//
//
//        TimeRuleView.TimePart timePart2 = new TimeRuleView.TimePart();
//        timePart2.startTime = 20;
//        timePart2.endTime = 100;
//        timeParts.add(timePart2);
//        timeRuleView.setTimePartList(timeParts);

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Log.d(TAG,"update Time ");
////                currentTime=currentTime+60;
//                currentTime=currentTime+1;
//                timeRuleView.setCurrentTime(currentTime);
//
//            }
//        },500,16);

        timeRuleView.computeScroll();
        timeRuleView.setOnTimeChangedListener(new TimeView.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(int newTimeValue) {
                Log.d(TAG,"onTimeChanged:"+newTimeValue);
            }
        });

    }

    private void initView(){
        btn_singleChannel=(Button)findViewById(R.id.btn_singleChannel);
        btn_singleChannel.setOnClickListener(this);
        title_view=(TitleView) findViewById(R.id.title_view);
        title_view.setTitleText("单声道波形绘制");
        title_view.setBackOnClickListener(this);
        bsv_singleChannel=(BaseAudioSurfaceView)findViewById(R.id.bsv_singleChannel);
        iv_singleChannel=(ImageView) findViewById(R.id.iv_singleChannel);
        voiceDbView = findViewById(R.id.voiceDb_view);
        timeRuleView = findViewById(R.id.time_rule_view);
        iv_singleChannel.setOnClickListener(this);



    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bsv_singleChannel:
                if (rePlayDialog==null){
                    rePlayDialog=new NormalDialog();
                }
                rePlayDialog.show(getSupportFragmentManager(), "", "确认真的要重新录制吗？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_NEGATIVE:
                                rePlayDialog.dismiss();
                                break;
                            case DialogInterface.BUTTON_POSITIVE:
                                manager.destroy();

                                bsv_singleChannel.reDrawThread();
                                btn_singleChannel.setVisibility(View.GONE);
                                iv_singleChannel.setImageDrawable(getResources().getDrawable(R.drawable.stop_play));
                                manager.startRecord(MicManager.MODE_RECORD_FILE);
                                break;
                        }
                    }
                });
                break;
            case R.id.iv_back:
                manager.destroy();

                bsv_singleChannel.stopDrawThread();
                this.finish();
                break;
            case R.id.iv_singleChannel:
                if (manager.getOperationStatus()==MicManager.MODE_RECORD_FILE){

                    manager.stopRecord();
                    btn_singleChannel.setVisibility(View.VISIBLE);
                    iv_singleChannel.setImageDrawable(getResources().getDrawable(R.drawable.start_play));
                }else{
                    btn_singleChannel.setVisibility(View.GONE);
                    manager.startRecord(MicManager.MODE_RECORD_FILE);
                    iv_singleChannel.setImageDrawable(getResources().getDrawable(R.drawable.stop_play));
                }
                break;
        }
    }







    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.destroy();
        bsv_singleChannel.stopDrawThread();
    }
}
