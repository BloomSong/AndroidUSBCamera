package com.dreamguard.usb.detect;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.util.Log;
import android.util.SparseArray;

import java.lang.ref.WeakReference;

/**
 * Created by hailin.dai on 12/29/16.
 * email:hailin.dai@wz-tech.com
 */

public class UsbControlBlock {
    private static final boolean DEBUG = false;	// TODO set false on production
    private static final String TAG = "UsbControlBlock";

    private final WeakReference<USBMonitor> mWeakMonitor;
    private final WeakReference<UsbDevice> mWeakDevice;
    protected UsbDeviceConnection mConnection;
    private final SparseArray<UsbInterface> mInterfaces = new SparseArray<UsbInterface>();

    /**
     * this class needs permission to access USB device before constructing
     * @param monitor
     * @param device
     */
    public UsbControlBlock(final USBMonitor monitor, final UsbDevice device) {
        if (DEBUG) Log.i(TAG, "UsbControlBlock:constructor");
        mWeakMonitor = new WeakReference<USBMonitor>(monitor);
        mWeakDevice = new WeakReference<UsbDevice>(device);
        mConnection = monitor.mUsbManager.openDevice(device);
        final String name = device.getDeviceName();
        if (mConnection != null) {
            if (DEBUG) {
                final int desc = mConnection.getFileDescriptor();
                final byte[] rawDesc = mConnection.getRawDescriptors();
                Log.i(TAG, "UsbControlBlock:name=" + name + ", desc=" + desc + ", rawDesc=" + rawDesc);
            }
        } else {
            Log.e(TAG, "could not connect to device " + name);
        }
    }

    public UsbDevice getDevice() {
        return mWeakDevice.get();
    }

    public String getDeviceName() {
        final UsbDevice device = mWeakDevice.get();
        return device != null ? device.getDeviceName() : "";
    }

    public UsbDeviceConnection getUsbDeviceConnection() {
        return mConnection;
    }

    public synchronized int getFileDescriptor() {
        return mConnection != null ? mConnection.getFileDescriptor() : -1;
    }

    public byte[] getRawDescriptors() {
        return mConnection != null ? mConnection.getRawDescriptors() : null;
    }

    public int getVenderId() {
        final UsbDevice device = mWeakDevice.get();
        return device != null ? device.getVendorId() : 0;
    }

    public int getProductId() {
        final UsbDevice device = mWeakDevice.get();
        return device != null ? device.getProductId() : 0;
    }

    public synchronized String getSerial() {
        return mConnection != null ? mConnection.getSerial() : null;
    }

    /**
     * open specific interface
     * @param interfaceIndex
     * @return
     */
    public synchronized UsbInterface open(final int interfaceIndex) {
        if (DEBUG) Log.i(TAG, "UsbControlBlock#open:" + interfaceIndex);
        final UsbDevice device = mWeakDevice.get();
        UsbInterface intf = null;
        intf = mInterfaces.get(interfaceIndex);
        if (intf == null) {
            intf = device.getInterface(interfaceIndex);
            if (intf != null) {
                synchronized (mInterfaces) {
                    mInterfaces.append(interfaceIndex, intf);
                }
            }
        }
        return intf;
    }

    /**
     * close specified interface. USB device itself still keep open.
     * @param interfaceIndex
     */
    public void close(final int interfaceIndex) {
        UsbInterface intf = null;
        synchronized (mInterfaces) {
            intf = mInterfaces.get(interfaceIndex);
            if (intf != null) {
                mInterfaces.delete(interfaceIndex);
                mConnection.releaseInterface(intf);
            }
        }
    }

    /**
     * close specified interface. USB device itself still keep open.
     * @param
     */
    public synchronized void close() {
        if (DEBUG) Log.i(TAG, "UsbControlBlock#close:");

        if (mConnection != null) {
            final int n = mInterfaces.size();
            int key;
            UsbInterface intf;
            for (int i = 0; i < n; i++) {
                key = mInterfaces.keyAt(i);
                intf = mInterfaces.get(key);
                mConnection.releaseInterface(intf);
            }
            mConnection.close();
            mConnection = null;
            final USBMonitor monitor = mWeakMonitor.get();
            if (monitor != null) {
                if (monitor.mOnDeviceConnectListener != null) {
                    final UsbDevice device = mWeakDevice.get();
                    monitor.mOnDeviceConnectListener.onDisconnect(device, this);
                }
                monitor.mCtrlBlocks.remove(getDevice());
            }
        }
    }
}
