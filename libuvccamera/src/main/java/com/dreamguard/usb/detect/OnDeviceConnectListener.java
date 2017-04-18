package com.dreamguard.usb.detect;

import android.hardware.usb.UsbDevice;

/**
 * Created by hailin.dai on 12/29/16.
 * email:hailin.dai@wz-tech.com
 */

public interface OnDeviceConnectListener {
    /**
     * called when device attached
     * @param device
     */
    public void onAttach(UsbDevice device);
    /**
     * called when device dettach(after onDisconnect)
     * @param device
     */
    public void onDetach(UsbDevice device);
    /**
     * called after device opend
     * @param device
     * @param ctrlBlock
     * @param createNew
     */
    public void onConnect(UsbDevice device, UsbControlBlock ctrlBlock, boolean createNew);
    /**
     * called when USB device removed or its power off (this callback is called after device closing)
     * @param device
     * @param ctrlBlock
     */
    public void onDisconnect(UsbDevice device, UsbControlBlock ctrlBlock);
    /**
     * called when canceled or could not get permission from user
     */
    public void onCancel();
}