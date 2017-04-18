# AndroidUSBCamera

	一款AndroidUsb摄像头数据获取库。无需root权限。


# 使用方法

Step 1. Add it in your root build.gradle at the end of repositories:
	allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}

Step 2. Add the dependency

	dependencies {
	        compile 'com.github.hailindai:AndroidUSBCamera:v1.0.0'
	}


	例子代码：

    private static final String TAG = "MainActivity";

    //摄像头控制类
    USBCamera camera;
    //USB插拔监听类
    USBMonitor usbMonitor;
    SurfaceTexture surfaceTexture = new SurfaceTexture(10);

    //类似 Andoid系统相机onPreviewFrame
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

        //初始化USB插拔监听类
        usbMonitor = new USBMonitor();
        usbMonitor.init(this);
        usbMonitor.register(mOnDeviceConnectListener);

        //初始化摄像头控制类
        camera = new USBCamera();
        camera.init(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁USB插拔监听类
        usbMonitor.unregister();
        usbMonitor.destroy();
        //销毁摄像头控制类
        camera.destroy();
    }

    //USB插拔监听回调接口
    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            Toast.makeText(MainActivity.this, "onAttach", Toast.LENGTH_SHORT).show();
            //请求连接摄像头
            usbMonitor.connectDevice(0);
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.v(TAG, "onConnect:");
            //连接摄像头成功，开始打开摄像头。
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
            //关闭摄像头
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
