package com.dreamguard.usb.camera;

/**
 * Created by hailin.dai on 12/2/16.
 * email:hailin.dai@wz-tech.com
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SizeF;
import android.view.Surface;

import com.dreamguard.api.R;
import com.dreamguard.encoder.MediaAudioEncoder;
import com.dreamguard.encoder.MediaEncoder;
import com.dreamguard.encoder.MediaMuxerWrapper;
import com.dreamguard.encoder.MediaVideoEncoder;
import com.dreamguard.encoder.SimpleEncoder;
import com.dreamguard.picture.SimplePictureEncoder;
import com.dreamguard.usb.detect.USBMonitor;
import com.dreamguard.usb.detect.UsbControlBlock;
import com.dreamguard.util.ImageProc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * Handler class to execute camera releated methods sequentially on private thread
 */
public class CameraHandler extends Handler {

    private static final boolean DEBUG = true;
    private static final String TAG = "CameraHandler";

    /**
     * preview resolution(width)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    public static int PREVIEW_WIDTH = 640;
    /**
     * preview resolution(height)
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     */
    public static int PREVIEW_HEIGHT = 480;
    /**
     * preview mode
     * if your camera does not support specific resolution and mode,
     * {@link UVCCamera#setPreviewSize(int, int, int)} throw exception
     * 0:YUYV, other:MJPEG
     */

    public static int RECORD_WIDTH = 1280;

    public static int RECORD_HEIGHT = 720;

    public static int CAPTURE_WIDTH = 640;

    public static int CAPTURE_HEIGHT = 480;

    public static boolean is3D = false;

    private static final int PREVIEW_MODE = 1;


    private static final int MSG_OPEN = 0;
    private static final int MSG_CLOSE = 1;
    private static final int MSG_PREVIEW_START = 2;
    private static final int MSG_PREVIEW_STOP = 3;
    private static final int MSG_CAPTURE_STILL = 4;
    private static final int MSG_CAPTURE_START = 5;
    private static final int MSG_CAPTURE_STOP = 6;
    private static final int MSG_MEDIA_UPDATE = 7;
    private static final int MSG_PREVIEW_SIZE = 8;
    private static final int MSG_SET_FRAME_CALLBACK = 9;
    private static final int MSG_RELEASE = 10;

    private final WeakReference<CameraThread> mWeakThread;

    public static final CameraHandler createHandler(final Context parent) {
        final CameraThread thread = new CameraThread(parent);
        thread.start();
        return thread.getHandler();
    }

    public CameraHandler(final CameraThread thread) {
        mWeakThread = new WeakReference<CameraThread>(thread);
    }

    public boolean isCameraOpened() {
        final CameraThread thread = mWeakThread.get();
        return thread != null ? thread.isCameraOpened() : false;
    }

    public boolean isRecording() {
        final CameraThread thread = mWeakThread.get();
        return thread != null ? thread.isRecording() :false;
    }

    public void openCamera(final UsbControlBlock ctrlBlock) {
        sendMessage(obtainMessage(MSG_OPEN, ctrlBlock));
    }

    public void closeCamera() {
        stopPreview();
        sendEmptyMessage(MSG_CLOSE);
    }

    public void startPreview(final Surface sureface) {
        if (sureface != null)
            sendMessage(obtainMessage(MSG_PREVIEW_START, sureface));
    }

    public void stopPreview() {
        stopRecording();
        final CameraThread thread = mWeakThread.get();
        if (thread == null) return;
        synchronized (thread.mSync) {
            sendEmptyMessage(MSG_PREVIEW_STOP);
            // wait for actually preview stopped to avoid releasing Surface/SurfaceTexture
            // while preview is still running.
            // therefore this method will take a time to execute
            try {
                thread.mSync.wait();
            } catch (final InterruptedException e) {
            }
        }
    }

    public void captureStill() {
        sendEmptyMessage(MSG_CAPTURE_STILL);
    }

    public void startRecording() {
        sendEmptyMessage(MSG_CAPTURE_START);
    }

    public void stopRecording() {
        sendEmptyMessage(MSG_CAPTURE_STOP);
    }

/*		public void release() {
			sendEmptyMessage(MSG_RELEASE);
		} */

    public void setPreviewSize(int width,int height){
        Point previewSize = new Point(width,height);
        sendMessage(obtainMessage(MSG_PREVIEW_SIZE, previewSize));
    }

    public void setFrameCallback(IFrameCallback callback){
        sendMessage(obtainMessage(MSG_SET_FRAME_CALLBACK,callback));
    }

    @Override
    public void handleMessage(final Message msg) {
        final CameraThread thread = mWeakThread.get();
        if (thread == null) return;
        switch (msg.what) {
            case MSG_OPEN:
                thread.handleOpen((UsbControlBlock)msg.obj);
                break;
            case MSG_CLOSE:
                thread.handleClose();
                break;
            case MSG_PREVIEW_START:
                thread.handleStartPreview((Surface)msg.obj);
                break;
            case MSG_PREVIEW_STOP:
                thread.handleStopPreview();
                break;
            case MSG_CAPTURE_STILL:
                thread.handleCaptureStill();
                break;
            case MSG_CAPTURE_START:
                thread.handleStartRecording();
                break;
            case MSG_CAPTURE_STOP:
                thread.handleStopRecording();
                break;
            case MSG_MEDIA_UPDATE:
                thread.handleUpdateMedia((String)msg.obj);
                break;
            case MSG_PREVIEW_SIZE:
                Point size = (Point)msg.obj;
                thread.setmPreviewWidth(size.x);
                thread.setmPreviewHeight(size.y);
                break;
            case MSG_SET_FRAME_CALLBACK:
                thread.handleSetFrameCallback((IFrameCallback) msg.obj);
                break;
            case MSG_RELEASE:
                thread.handleRelease();
                break;
            default:
                throw new RuntimeException("unsupported message:what=" + msg.what);
        }
    }

}