package com.dreamguard.api;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.dreamguard.usb.camera.CameraHandler;
import com.dreamguard.usb.camera.IFrameCallback;
import com.dreamguard.usb.detect.DeviceFilter;
import com.dreamguard.usb.detect.IUSBMonitor;
import com.dreamguard.usb.detect.OnDeviceConnectListener;
import com.dreamguard.usb.detect.USBMonitor;
import com.dreamguard.usb.detect.USBStatus;
import com.dreamguard.usb.detect.UsbControlBlock;

import java.util.List;

/**
 * Created by hailin on 2016/12/10.
 */

public class USBCamera {

    private final static String TAG = "USBCamera";

    private CameraHandler mHandler;

    private SurfaceTexture mSurfaceTexture;

    private final Object mSync = new Object();

    public USBCamera() {
    }

    public void init(Context context) {
        Log.v(TAG, "init.");
        mHandler = CameraHandler.createHandler(context);
    }

    public void destroy() {
        Log.v(TAG, "destroy.");
        mHandler = null;
    }

    public void setPreviewSize(int width, int height) {
        mHandler.setPreviewSize(width,height);
    }

    public void setFrameCallback(IFrameCallback callback){
        mHandler.setFrameCallback(callback);
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture){
        mSurfaceTexture = surfaceTexture;
    }

    public void startPreview(){
        mHandler.startPreview(new Surface(mSurfaceTexture));
    }

    public void stopPreview() {
        mHandler.stopPreview();
    }

    public boolean open(UsbControlBlock ctrlBlock) {
        mHandler.openCamera(ctrlBlock);
        return true;
    }

    public void close() {
        mHandler.closeCamera();
    }

    public boolean isCameraOpened() {
        return mHandler.isCameraOpened();
    }

    public void captureStill() {
        mHandler.captureStill();
    }

    public void startRecording(){
        mHandler.startRecording();
    }

    public void stopRecording(){
        mHandler.stopRecording();
    }

    public boolean isRecording(){
        return mHandler.isRecording();
    }

}
