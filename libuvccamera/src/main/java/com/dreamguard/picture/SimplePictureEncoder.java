package com.dreamguard.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.os.Environment;
import android.util.Log;

import com.dreamguard.api.R;
import com.dreamguard.util.ImageProc;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created by hailin.dai on 1/9/17.
 * email:hailin.dai@wz-tech.com
 */

public class SimplePictureEncoder {

    private static final String TAG = "SimplePictureEncoder";

    private Context mContext;

    private String photoPath = Environment.getExternalStorageDirectory().getPath() + "/K3DX/Picture/";

    public static String photoActualPath;

    private SoundPool mSoundPool;

    private int mSoundId;

    public SimplePictureEncoder(Context context) {
        this.mContext = context;
        loadSutterSound();
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }


//    static struct SwsContext *swsContext;
//
//    swsContext = sws_getContext(c->width, c->height,PIX_FMT_NV21,
//    c->width, c->height,
//    PIX_FMT_YUV420P,
//    SWS_FAST_BILINEAR, NULL, NULL, NULL);
//
//    avpicture_fill((AVPicture*)picture, data, PIX_FMT_NV21, c->width, c->height);
//
//    avpicture_fill((AVPicture*)outpic, outbuffer, PIX_FMT_YUV420P, c->width, c->height);
//
//    sws_scale(swsContext, picture->data, picture->linesize, 0, c->height, outpic->data, outpic->linesize);
//
//    sws_freeContext(swsContext);
    public void takePhoto(ByteBuffer frame, int width, int height){

        mSoundPool.play(mSoundId, 0.2f, 0.2f, 0, 0, 1.0f);

        File outputFile = null;
        BufferedOutputStream os = null;
        int rgb[] = new int[width*height];
        try {
            photoActualPath = photoPath + System.currentTimeMillis() + ".jpg";
            outputFile = new File(photoActualPath);
            os = new BufferedOutputStream(new FileOutputStream(outputFile));
            byte buf[] = new byte[width*height*3/2];
            frame.get(buf);

            ImageProc.decodeYUV420SP(rgb,buf,width,height);
            Bitmap bitmap = Bitmap.createBitmap(rgb,width, height, Bitmap.Config.ARGB_8888);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

            updateMedia(photoPath + System.currentTimeMillis() + ".jpg");
        } catch (Exception e){
            Log.e(TAG,"onFrame Capture still Error Exception");
        }
    }

//    public void takePhoto2(ByteBuffer frame, int width, int height){
//
//        swscale mScale = new swscale();
//        swscale.SwsContext mContext = mScale.sws_getContext(2560,720, AV_PIX_FMT_NV21,1280,720,AV_PIX_FMT_NV21,SWS_X, null,null,(double[])null);
//
//        mSoundPool.play(mSoundId, 0.2f, 0.2f, 0, 0, 1.0f);
//
//        File outputFile = null;
//        BufferedOutputStream os = null;
//        int rgb[] = new int[width*height];
//        try {
//            outputFile = new File(photoPath + System.currentTimeMillis() + ".jpg");
//            os = new BufferedOutputStream(new FileOutputStream(outputFile));
//            byte buf1[] = new byte[width*height];
//            byte buf2[] = new byte[width*height/2];
//            frame.get(buf1,0,width*height);
//            frame.get(buf2,0,width*height/2);
//
//            int srcStride[] = {2560,1280,0,0};
//            int dstStride[] = {1280,640,0,0};
//
//            PointerPointer<BytePointer> pPointer1 = new PointerPointer<BytePointer>(2);
//            BytePointer b1[] = new BytePointer[2];
//            b1[0] = new BytePointer(buf1);
//            b1[1] = new BytePointer(buf2);
//            pPointer1.put(b1);
//
////            BytePointer t = pPointer1.get(BytePointer.class);
////            t.capacity(width*height);
////            t.limit(width*height);
//
//
//            byte buf3[] = new byte[width*height/2];
//            byte buf4[] = new byte[width*height/4];
//
//            PointerPointer<BytePointer> pPointer2 = new PointerPointer<BytePointer>(width*height*3/4);
//            BytePointer b2[] = new BytePointer[2];
//            b2[0] = new BytePointer(buf3);
//            b2[1] = new BytePointer(buf4);
//            pPointer2.put(b2);
//
//            IntPointer iPointer1 = new IntPointer(1);
//            iPointer1.put(srcStride);
//
//            IntPointer iPointer2 = new IntPointer(1);
//            iPointer2.put(srcStride);
//
//            sws_scale(mContext,pPointer1,iPointer1,0,720,pPointer2,iPointer2);
//
//            BytePointer t1 = pPointer2.get(BytePointer.class);
//
//            t1.capacity(width*height/2);
//            t1.limit(width*height/2);
//
//            BytePointer t2 = pPointer2.get(BytePointer.class,1);
//
//            t2.capacity(width*height/4);
//            t2.limit(width*height/4);
//
//            byte buf5[] = new byte[width*height*3/4];
//
//            t1.get(buf5);
//            t2.get(buf5,width*height/2,width*height/4);
////            buffer1.get(buf5);
//
//            ImageProc.decodeYUV420SP(rgb,buf5,width/2,height);
//            Bitmap bitmap = Bitmap.createBitmap(rgb,width/2, height, Bitmap.Config.ARGB_8888);
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
//
//            os.flush();
//            os.close();
//            sws_freeContext(mContext);
//            updateMedia(photoPath + System.currentTimeMillis() + ".jpg");
//        } catch (Exception e){
//            Log.e(TAG,"onFrame Capture still Error Exception");
//        }
//    }

    private void updateMedia(final String path) {
        final Context parent = mContext;
        if (parent != null && parent.getApplicationContext() != null) {
            try {
                MediaScannerConnection.scanFile(parent.getApplicationContext(), new String[]{ path }, null, null);
            } catch (final Exception e) {
                Log.e(TAG, "handleUpdateMedia:", e);
            }
        } else {
            Log.w(TAG, "MainActivity already destroyed");
            // give up to add this movice to MediaStore now.
            // Seeing this movie on Gallery app etc. will take a lot of time.
        }
    }

    @SuppressWarnings("deprecation")
    private void loadSutterSound() {
        // get system stream type using refrection
        int streamType;
        try {
            final Class<?> audioSystemClass = Class.forName("android.media.AudioSystem");
            final Field sseField = audioSystemClass.getDeclaredField("STREAM_SYSTEM_ENFORCED");
            streamType = sseField.getInt(null);
        } catch (final Exception e) {
            streamType = AudioManager.STREAM_SYSTEM;	// set appropriate according to your app policy
        }
        if (mSoundPool != null) {
            try {
                mSoundPool.release();
            } catch (final Exception e) {
            }
            mSoundPool = null;
        }
        // load sutter sound from resource
        mSoundPool = new SoundPool(2, streamType, 0);
        mSoundId = mSoundPool.load(mContext, R.raw.camera_click, 1);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mSoundPool.release();
        mSoundPool = null;
    }
}
