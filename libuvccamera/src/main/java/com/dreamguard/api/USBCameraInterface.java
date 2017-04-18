package com.dreamguard.api;

import android.content.Context;
import android.graphics.SurfaceTexture;

/**
 * Created by hailin.dai on 12/28/16.
 * email:hailin.dai@wz-tech.com
 */

public interface USBCameraInterface {

    public void init(Context context,CameraType cameraType);

    public void destroy();

    public boolean open(int id);

    public void close();

    public boolean isCameraOpened();

    public void setPreviewSize(int width, int height);

    public void setPreviewTexture(SurfaceTexture surfaceTexture);

    public void startPreview();

    public void stopPreview();

    public void captureStill();

    public void startRecording();

    public void stopRecording();

    public boolean isRecording();

    public void registerUSBMonitor(USBMonitorCallback callback);

    public void unRegisterUSBMonitor(USBMonitorCallback callback);
}
