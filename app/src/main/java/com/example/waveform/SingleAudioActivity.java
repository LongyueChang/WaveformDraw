package com.example.waveform;

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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.yunxi.voiceview.WaveSurfaceView;
import com.yunxi.voiceview.draw.WaveCanvas;

/**
 * @author : longyue
 * @data : 2022/7/15
 * @email : changyl@yunxi.tv
 */
public class SingleAudioActivity extends AppCompatActivity {
    private final String TAG = SingleAudioActivity.class.getSimpleName();
    private WaveSurfaceView audioWaveView;
    private ImageView iv_singleChannel;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_audio_layout);

        initView();
        initAudio();
    }

    private void initView() {
        audioWaveView = findViewById(R.id.audio_view);
        iv_singleChannel=findViewById(R.id.iv_audio_singleChannel);
        if(audioWaveView != null) {
            audioWaveView.setLine_off(42);
            //解决surfaceView黑色闪动效果
            audioWaveView.setZOrderOnTop(true);
            audioWaveView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
    }


    private WaveCanvas waveCanvas;
    private AudioRecord audioRecord;
    private static final int FREQUENCY = 16000;// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static final int CHANNELCONGIFIGURATION = AudioFormat.CHANNEL_IN_MONO;// 设置单声道声道
    private static final int AUDIOENCODING = AudioFormat.ENCODING_PCM_16BIT;// 音频数据格式：每个样本16位
    public final static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;// 音频获取源
    private int recBufSize;// 录音最小buffer大小
    private final String mFileName="audio";
    public static final String DATA_DIRECTORY = Environment
            .getExternalStorageDirectory() + "/record";
    /**
     * 初始化录音  申请录音权限
     */
    public void initAudio(){
        recBufSize = AudioRecord.getMinBufferSize(FREQUENCY,
                CHANNELCONGIFIGURATION, AUDIOENCODING);// 录音组件
        audioRecord = new AudioRecord(AUDIO_SOURCE,// 指定音频来源，这里为麦克风
                FREQUENCY, // 16000HZ采样频率
                CHANNELCONGIFIGURATION,// 录制通道
                AUDIO_SOURCE,// 录制编码格式
                recBufSize);// 录制缓冲区大小 //先修改
    }
    /**
     * 开始录音
     */
    private void startAudio(){
        waveCanvas = new WaveCanvas();
        waveCanvas.baseLine = audioWaveView.getHeight() / 2;
        waveCanvas.Start(audioRecord, recBufSize, audioWaveView, mFileName, DATA_DIRECTORY, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return true;
            }
        });
    }

    private void stopAudio(){
        waveCanvas.Stop();
        waveCanvas = null;
    }

    private boolean isPlay=false;
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_audio_singleChannel:
                double[] ddd={-1,-2,-3,-4,-20,-35,-100,-200,-230};
                for (int i = 0; i < ddd.length; i++) {
//                    double abs = Math.abs(ddd[i]);
                    double v = 30 +ddd[i] ;
                    Log.d(TAG,"ddd:"+ddd[i]+" == "+v);
                }

//                if(!isPlay){
//                    isPlay=true;
//                    iv_singleChannel.setImageDrawable(getResources().getDrawable(R.drawable.stop_play));
//                    startAudio();
//                } else {
//                    isPlay=false;
//                    iv_singleChannel.setImageDrawable(getResources().getDrawable(R.drawable.start_play));
//                    stopAudio();
//                }
                break;
            default:break;
        }
    }
}
