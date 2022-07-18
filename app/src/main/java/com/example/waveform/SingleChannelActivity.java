package com.example.waveform;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.yunxi.audio.YunxiAudioWrapper;
import com.yunxi.voiceview.VoiceDbText;
import com.yunxi.voiceview.AudioSurfaceView;
import com.yunxi.voiceview.Constant;
import com.example.waveform.utils.MicManager;
import com.example.waveform.utils.NormalDialog;
import com.example.waveform.utils.TitleView;
import com.yunxi.voiceview.TimeView;
import com.yunxi.voiceview.VoiceDbView;

public class SingleChannelActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = SingleChannelActivity.class.getSimpleName();

    private ImageView iv_singleChannel;
    private MicManager manager=null;
    private TitleView title_view;
    private NormalDialog rePlayDialog;
    private Button btn_singleChannel;
    private VoiceDbView voiceDbView;
    private VoiceDbText dbView;
    private TimeView timeRuleView;
    private AudioSurfaceView bsv_singleChannel;


    private Handler handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MicManager.MODE_RECORD_FILE:
                    currentTime=currentTime+1.4f;
                    timeRuleView.setCurrentTime(currentTime);

//                    bsv_singleChannel.addAudioData((byte[])msg.obj, msg.arg1, Constant.SINGLE_CHANNEL_SAMPLEER_RATE, Constant.SINGLE_CHANNLE_BIT_WIDTH, false);
                    bsv_singleChannel.addAudioData((byte[])msg.obj,16000, Constant.SINGLE_CHANNEL_SAMPLEER_RATE, Constant.SINGLE_CHANNLE_BIT_WIDTH, false);


                    // TODO: 2022/7/17
                    float recordVolume = YunxiAudioWrapper.getRecordVolume();
                    voiceDbView.setVoice(recordVolume);
                    break;
//                case MicManager.MODE_VOLUME:
////                    Random random = new Random();
////                    int voiceCount = random.nextInt(100);
//                    float volume = (float) msg.obj;
//                    double showVolume = 60 +volume;
//                    if(showVolume<0){
//                        showVolume=0;
//                    }
//                    Log.d(TAG,"showVolume:"+showVolume);
//                    voiceDbView.setVoice((int) showVolume);
//                    break;
                default:break;
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

    private float currentTime = 0;
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

//        timeRuleView.computeScroll();

    }

    private void initView(){
        btn_singleChannel=(Button)findViewById(R.id.btn_singleChannel);
        btn_singleChannel.setOnClickListener(this);
        title_view=(TitleView) findViewById(R.id.title_view);
        title_view.setTitleText("单声道波形绘制");
        title_view.setBackOnClickListener(this);
        bsv_singleChannel=(AudioSurfaceView)findViewById(R.id.bsv_singleChannel);
        iv_singleChannel=(ImageView) findViewById(R.id.iv_singleChannel);
        voiceDbView = findViewById(R.id.voiceDb_view);
        dbView = findViewById(R.id.voiceDb_text);
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
