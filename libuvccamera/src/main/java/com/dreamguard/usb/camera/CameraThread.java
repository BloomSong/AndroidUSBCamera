package com.dreamguard.usb.camera;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import com.dreamguard.encoder.SimpleEncoder;
import com.dreamguard.picture.SimplePictureEncoder;
import com.dreamguard.usb.detect.UsbControlBlock;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import static com.dreamguard.usb.camera.CameraHandler.is3D;

/**
 * Created by hailin.dai on 1/10/17.
 * email:hailin.dai@wz-tech.com
 */

public class CameraThread extends Thread {
    private static final boolean DEBUG = true;
    private static final String TAG_THREAD = "CameraThread";
    private static final String TAG = "CameraThread";
    protected final Object mSync = new Object();
    private final WeakReference<Context> mWeakParent;
    private boolean isCaptureStill = false;

    private CameraHandler mHandler;
    /**
     * for accessing UVC camera
     */
    private UVCCamera mUVCCamera;
    /**
     * muxer for audio/video recording
     */
    private SimpleEncoder mEncoder;

    private SimplePictureEncoder mPictureEncoder;

    private int mPreviewWidth;
    private int mPreviewHeight;

    public void setmPreviewWidth(int mPreviewWidth) {
        this.mPreviewWidth = mPreviewWidth;
    }

    public void setmPreviewHeight(int mPreviewHeight) {
        this.mPreviewHeight = mPreviewHeight;
    }

    public CameraThread(final Context parent) {
        super("CameraThread");
        mWeakParent = new WeakReference<Context>(parent);

        mEncoder = new SimpleEncoder();

        mPictureEncoder = new SimplePictureEncoder(mWeakParent.get());

    }

    @Override
    protected void finalize() throws Throwable {
        Log.i(TAG, "CameraThread#finalize");
        super.finalize();
    }

    public CameraHandler getHandler() {
        if (DEBUG) Log.v(TAG_THREAD, "getHandler:");
        synchronized (mSync) {
            if (mHandler == null)
                try {
                    mSync.wait();
                } catch (final InterruptedException e) {
                }
        }
        return mHandler;
    }

    public boolean isCameraOpened() {
        return mUVCCamera != null;
    }

    public boolean isRecording() {
        return mEncoder.isRecording();
    }

    public void handleOpen(UsbControlBlock ctrlBlock) {
        if (DEBUG) Log.v(TAG_THREAD, "handleOpen:");
//            handleClose();
        mUVCCamera = new UVCCamera();
        mUVCCamera.open(ctrlBlock);
        if (DEBUG) Log.i(TAG, "supportedSize:" + mUVCCamera.getSupportedSize());
    }

    public void handleClose() {
        if (DEBUG) Log.v(TAG_THREAD, "handleClose:");
        handleStopRecording();
        if (mUVCCamera != null) {
            mUVCCamera.stopPreview();
            mUVCCamera.destroy();
            mUVCCamera = null;
        }
    }

    public void handleStartPreview(final Surface surface) {
        if (DEBUG) Log.v(TAG_THREAD, "handleStartPreview:");
        if (mUVCCamera == null) return;
        try {
            mUVCCamera.setPreviewSize(mPreviewWidth, mPreviewHeight, 1);

        } catch (final IllegalArgumentException e) {
            try {
                // fallback to YUV mode
                mUVCCamera.setPreviewSize(mPreviewWidth, mPreviewHeight, UVCCamera.DEFAULT_PREVIEW_MODE);
            } catch (final IllegalArgumentException e1) {
                handleClose();
            }
        }
        if (mUVCCamera != null) {
            mUVCCamera.setPreviewDisplay(surface);
            mUVCCamera.startPreview();
        }
    }

    public void handleSetFrameCallback(IFrameCallback callback){
        if(mUVCCamera != null){
            mUVCCamera.setFrameCallback(callback, UVCCamera.PIXEL_FORMAT_NV21);
        }
    }

    public void handleStopPreview() {
        if (DEBUG) Log.v(TAG_THREAD, "handleStopPreview:");
        if (mUVCCamera != null) {
            mUVCCamera.stopPreview();
        }
        synchronized (mSync) {
            mSync.notifyAll();
        }
    }

    public void handleCaptureStill() {
        isCaptureStill = true;
        if (DEBUG) Log.v(TAG_THREAD, "handleCaptureStill:");
    }

    public void handleStartRecording() {
        if (DEBUG) Log.v(TAG_THREAD, "handleStartRecording:");
        if(mEncoder != null){
            mEncoder.setRecordWidth(mPreviewWidth);
            mEncoder.setRecordHeight(mPreviewHeight);
            mEncoder.startRecording();
        }
    }

    public void handleStopRecording() {
        if (DEBUG) Log.v(TAG_THREAD, "handleStopRecording:");
        if(mEncoder != null) {
            mEncoder.stopRecording();
        }
    }


    public void handleUpdateMedia(final String path) {
        if (DEBUG) Log.v(TAG_THREAD, "handleUpdateMedia:path=" + path);
        final Context parent = mWeakParent.get();
        if (parent != null && parent.getApplicationContext() != null) {
            try {
                if (DEBUG) Log.i(TAG, "MediaScannerConnection#scanFile");
                MediaScannerConnection.scanFile(parent.getApplicationContext(), new String[]{ path }, null, null);
            } catch (final Exception e) {
                Log.e(TAG, "handleUpdateMedia:", e);
            }
        } else {
            Log.w(TAG, "MainActivity already destroyed");
            // give up to add this movice to MediaStore now.
            // Seeing this movie on Gallery app etc. will take a lot of time.
            handleRelease();
        }
    }

    public void handleRelease() {
        if (DEBUG) Log.v(TAG_THREAD, "handleRelease:");
        handleClose();
        if (!mEncoder.isRecording())
            Looper.myLooper().quit();
    }

    private void captureStill(ByteBuffer frame){
        Log.d(TAG,"onFrame Capture still");
        mPictureEncoder.takePhoto(frame,mPreviewWidth,mPreviewHeight);

    }


    @Override
    public void run() {
        Looper.prepare();
        synchronized (mSync) {
            mHandler = new CameraHandler(this);
            mSync.notifyAll();
        }
        Looper.loop();
        synchronized (mSync) {
            mHandler = null;
            mSync.notifyAll();
        }
    }
}