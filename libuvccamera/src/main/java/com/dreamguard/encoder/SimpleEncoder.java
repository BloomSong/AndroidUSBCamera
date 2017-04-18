package com.dreamguard.encoder;

import java.io.IOException;


/**
 * Created by hailin.dai on 1/9/17.
 * email:hailin.dai@wz-tech.com
 */

public class SimpleEncoder {
    private MediaMuxerWrapper mMuxer;

    private MediaVideoEncoder videoEncoder;

    private MediaAudioEncoder audioEncoder;

    private int recordWidth = 640;

    private int recordHeight = 480;

    private boolean mIsRecording = false;


    public static boolean isRtmp = false;

    /**
     * callback methods from encoder
     */
    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            mIsRecording = true;
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            mIsRecording = false;
        }
    };

    public void startRecording(){
        if (mMuxer == null) {
            try {
                mMuxer = new MediaMuxerWrapper(".mp4");    // if you record audio only, ".m4a" is also OK.
                if (true) {
                    // for video capturing
                    videoEncoder = new MediaVideoEncoder(mMuxer, mMediaEncoderListener, recordWidth,recordHeight);
                }
                mMuxer.prepare();
                mMuxer.startRecording();
            } catch (final IOException e) {
            }
        }


    }

    public void stopRecording(){
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
        }

    }

    public boolean isRecording(){
        return mIsRecording;
    }

    public void encoderVideoFrame(byte[] buf){
        if(videoEncoder != null){
            videoEncoder.encodeFrame(buf);
        }
    }

    public void setRecordWidth(int width){
        recordWidth = width;
    }

    public void setRecordHeight(int height){
        recordHeight = height;
    }
}
