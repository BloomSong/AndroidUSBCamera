package com.dreamguard.api;

import android.hardware.usb.UsbDevice;

import com.dreamguard.usb.detect.USBMonitor;
import com.dreamguard.usb.detect.UsbControlBlock;

/**
 * Created by hailin.dai on 12/28/16.
 * email:hailin.dai@wz-tech.com
 */

public abstract class USBMonitorCallback {
    /**
     * called when device attached
     * @param device
     */
    public abstract void onAttach(UsbDevice device);
    /**
     * called when device detach(after onDisconnect)
     * @param device
     */
    public abstract void onDetach(UsbDevice device);
    /**
     * called after device opened
     * @param device
     * @param ctrlBlock
     */
    public abstract void onConnect(UsbDevice device, UsbControlBlock ctrlBlock, boolean createNew);
    /**
     * called when USB device removed or its power off (this callback is called after device closing)
     * @param device
     * @param ctrlBlock
     */
    public abstract void onDisconnect(UsbDevice device, UsbControlBlock ctrlBlock);


}
