package com.dreamguard.usb.detect;

import android.content.Context;
import android.hardware.usb.UsbDevice;

import java.util.List;

/**
 * Created by hailin.dai on 12/29/16.
 * email:hailin.dai@wz-tech.com
 */

public interface IUSBMonitor {

    public void init(Context context);

    public void destroy();

    /**
     * start monitor usb events
     * @param listener
     */
    public void register(OnDeviceConnectListener listener);

    /**
     *  stop monitor usb events
     */
    public void unregister();

    /**
     * get usb device list by filter
     * @param filter
     * @return
     */
    public List<UsbDevice> getDeviceList(final DeviceFilter filter);

    /**
     *  request permission to access an usb device
     * @param device
     */
    public void requestPermission(UsbDevice device);

}
