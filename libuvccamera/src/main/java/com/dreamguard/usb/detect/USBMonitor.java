package com.dreamguard.usb.detect;
/*
 * UVCCamera
 * library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: USBMonitor.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
 * Files in the jni/libjpeg, jni/libusb, jin/libuvc, jni/rapidjson folder may have a different license, see the respective files.
*/

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

public final class USBMonitor implements IUSBMonitor{

	private static final boolean DEBUG = false;	// TODO set false on production
	private static final String TAG = "USBMonitor";

	private static final String ACTION_USB_PERMISSION_BASE = "com.dreamguard.USB_PERMISSION.";
	private final String ACTION_USB_PERMISSION = ACTION_USB_PERMISSION_BASE + hashCode();

	protected final ConcurrentHashMap<UsbDevice, UsbControlBlock> mCtrlBlocks = new ConcurrentHashMap<UsbDevice, UsbControlBlock>();

	private WeakReference<Context> mWeakContext;
	protected UsbManager mUsbManager;
	protected OnDeviceConnectListener mOnDeviceConnectListener;
	private PendingIntent mPermissionIntent = null;
	private DeviceFilter mDeviceFilter;

	private final Handler mHandler = new Handler();

	public USBMonitor() {
		if (DEBUG) Log.v(TAG, "USBMonitor:Constructor");
	}

	public void init(Context context){
		mWeakContext = new WeakReference<Context>(context);
		mUsbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
	}

	public void destroy() {
		if (DEBUG) Log.i(TAG, "destroy:");
		unregister();
		final Set<UsbDevice> keys = mCtrlBlocks.keySet();
		if (keys != null) {
			UsbControlBlock ctrlBlock;
			try {
				for (final UsbDevice key: keys) {
					ctrlBlock = mCtrlBlocks.remove(key);
					ctrlBlock.close();
				}
			} catch (final Exception e) {
				Log.e(TAG, "destroy:", e);
			}
			mCtrlBlocks.clear();
		}
	}

	public void connectDevice(int id){
		final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(mWeakContext.get(), com.dreamguard.api.R.xml.device_filter);
		List<UsbDevice> deviceList = getDeviceList(filter.get(0));
		UsbDevice currentDevice = null;
		if (deviceList.size() > 0) {
			currentDevice = deviceList.get(0);
		}
		if (currentDevice != null) {
			Log.v(TAG, "open :" + currentDevice.toString());
			requestPermission(currentDevice);
		} else {
			Log.v(TAG, "open null:");
		}
	}

	/**
	 * register BroadcastReceiver to monitor USB events
	 */
	public synchronized void register(OnDeviceConnectListener listener) {

		mOnDeviceConnectListener = listener;

		if (mPermissionIntent == null) {
			if (DEBUG) Log.i(TAG, "registerter:");
			final Context context = mWeakContext.get();
			if (context != null) {
				mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
				final IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
				filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
				filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
				context.registerReceiver(mUsbReceiver, filter);
			}
		}

	}

	/**
	 * unregister BroadcastReceiver
	 */
	public synchronized void unregister() {

		mOnDeviceConnectListener = null;

		if (mPermissionIntent != null) {
			if (DEBUG) Log.i(TAG, "unregister:");
			final Context context = mWeakContext.get();
			if (context != null) {
				context.unregisterReceiver(mUsbReceiver);
			}
			mPermissionIntent = null;
		}
	}


	public synchronized boolean isRegistered() {
		return mPermissionIntent != null;
	}

	/**
	 * set device filter
	 * @param filter
	 */
	public void setDeviceFilter(final DeviceFilter filter) {
		mDeviceFilter = filter;
	}

	/**
	 * return the number of connected USB devices that matched device filter
	 * @return
	 */
	public int getDeviceCount() {
		return getDeviceList().size();
	}

	/**
	 * return device list, return empty list if no device matched
	 * @return
	 */
	public List<UsbDevice> getDeviceList() {
		return getDeviceList(mDeviceFilter);
	}

	/**
	 * return device list, return empty list if no device matched
	 * @param filter
	 * @return
	 */
	public List<UsbDevice> getDeviceList(final DeviceFilter filter) {
		final HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
		final List<UsbDevice> result = new ArrayList<UsbDevice>();
		if (deviceList != null) {
			final Iterator<UsbDevice> iterator = deviceList.values().iterator();
			UsbDevice device;
			while (iterator.hasNext()) {
			    device = iterator.next();
			    if ((filter == null) || (filter.matches(device))) {
					result.add(device);
				}
			}
		}
		return result;
	}
	/**
	 * get USB device list
	 * @return
	 */
	public Iterator<UsbDevice> getDevices() {
		Iterator<UsbDevice> iterator = null;
		final HashMap<String, UsbDevice> list = mUsbManager.getDeviceList();
		if (list != null)
			iterator = list.values().iterator();
		return iterator;
	}

	/**
	 * output device list to LogCat
	 */
	public final void dumpDevices() {
		final HashMap<String, UsbDevice> list = mUsbManager.getDeviceList();
		if (list != null) {
			final Set<String> keys = list.keySet();
			if (keys != null && keys.size() > 0) {
				final StringBuilder sb = new StringBuilder();
				for (final String key: keys) {
					final UsbDevice device = list.get(key);
					final int num_interface = device != null ? device.getInterfaceCount() : 0;
					sb.setLength(0);
					for (int i = 0; i < num_interface; i++) {
						sb.append(String.format("interface%d:%s", i, device.getInterface(i).toString()));
					}
					Log.i(TAG, "key=" + key + ":" + device + ":" + sb.toString());
				}
			} else {
				Log.i(TAG, "no device");
			}
		} else {
			Log.i(TAG, "no device");
		}
	}

	/**
	 * return whether the specific Usb device has permission
	 * @param device
	 * @return
	 */
	public boolean hasPermission(final UsbDevice device) {
		return mUsbManager.hasPermission(device);
	}

	/**
	 * request permission to access to USB device
	 * @param device
	 */
	public synchronized void requestPermission(final UsbDevice device) {
		if (DEBUG) Log.v(TAG, "requestPermission:device=" + device);
		if (mPermissionIntent != null) {
			if (device != null) {
				if (mUsbManager.hasPermission(device)) {
					processConnect(device);
				} else {
					mUsbManager.requestPermission(device, mPermissionIntent);
				}
			} else {
				processCancel(device);
			}
		} else {
			processCancel(device);
		}
	}

	/**
	 * BroadcastReceiver for USB permission
	 */
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (USBMonitor.this) {
					final UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							processConnect(device);
						}
					} else {
						processCancel(device);
					}
				}
			} else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				final UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				processAttach(device);
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				final UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				if (device != null) {
					UsbControlBlock ctrlBlock = null;
					ctrlBlock = mCtrlBlocks.remove(device);
					if (ctrlBlock != null) {
						ctrlBlock.close();
					}
					processDettach(device);
				}
			}
		}
	};


	private final void processConnect(final UsbDevice device) {
		if (DEBUG) Log.v(TAG, "processConnect:");
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				UsbControlBlock ctrlBlock;
				final boolean createNew;
				ctrlBlock = mCtrlBlocks.get(device);
				if (ctrlBlock == null) {
					ctrlBlock = new UsbControlBlock(USBMonitor.this, device);
					mCtrlBlocks.put(device, ctrlBlock);
					createNew = true;
				} else {
					createNew = false;
				}
				if (mOnDeviceConnectListener != null) {
					final UsbControlBlock ctrlB = ctrlBlock;
					mOnDeviceConnectListener.onConnect(device, ctrlB, createNew);
				}
			}
		});
	}

	private final void processCancel(final UsbDevice device) {
		if (DEBUG) Log.v(TAG, "processCancel:");
		if (mOnDeviceConnectListener != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mOnDeviceConnectListener.onCancel();
				}
			});
		}
	}

	private final void processAttach(final UsbDevice device) {
		if (DEBUG) Log.v(TAG, "processAttach:");
		if (mOnDeviceConnectListener != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mOnDeviceConnectListener.onAttach(device);
				}
			});
		}
	}

	private final void processDettach(final UsbDevice device) {
		if (DEBUG) Log.v(TAG, "processDettach:");
		if (mOnDeviceConnectListener != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mOnDeviceConnectListener.onDetach(device);
				}
			});
		}
	}

}
