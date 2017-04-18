package com.wztech.androidusbcamera;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.dreamguard.api.CameraType;
import com.dreamguard.api.USBCamera;
import com.dreamguard.usb.camera.IFrameCallback;
import com.dreamguard.usb.detect.DeviceFilter;
import com.dreamguard.usb.detect.OnDeviceConnectListener;
import com.dreamguard.usb.detect.USBMonitor;
import com.dreamguard.usb.detect.USBStatus;
import com.dreamguard.usb.detect.UsbControlBlock;

import java.nio.ByteBuffer;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    USBCamera camera;
    USBMonitor usbMonitor;
    SurfaceTexture surfaceTexture = new SurfaceTexture(10);


    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {
            Log.d(TAG,"onFrame app");

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usbMonitor = new USBMonitor();
        usbMonitor.init(this);
        usbMonitor.register(mOnDeviceConnectListener);

        camera = new USBCamera();
        camera.init(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usbMonitor.unregister();
        usbMonitor.destroy();
        camera.destroy();
    }

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Toast.makeText(MainActivity.this, "onAttach", Toast.LENGTH_SHORT).show();
            usbMonitor.connectDevice(0);
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.v(TAG, "onConnect:");
            Toast.makeText(MainActivity.this, "onConnect", Toast.LENGTH_SHORT).show();
            camera.open(ctrlBlock);
            camera.setPreviewSize(640,480);
            camera.setPreviewTexture(surfaceTexture);
            camera.setFrameCallback(mIFrameCallback);
            camera.startPreview();
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            Toast.makeText(MainActivity.this, "onDisconnect", Toast.LENGTH_SHORT).show();
            Log.v(TAG, "onDisconnect:");
            camera.stopPreview();
            camera.close();
        }

        @Override
        public void onDetach(final UsbDevice device) {
            Toast.makeText(MainActivity.this, "onDetach", Toast.LENGTH_SHORT).show();
            Log.v(TAG, "onDetach:");

        }

        @Override
        public void onCancel() {
        }
    };

}
