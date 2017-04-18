package com.dreamguard.api;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import com.dreamguard.renderer.RenderHandler;
import com.dreamguard.widget.UVCCameraTextureView;

import java.util.HashMap;

/**
 * Created by hailin.dai on 12/28/16.
 * email:hailin.dai@wz-tech.com
 */

public class CameraView  extends TextureView    // API >= 14
        implements TextureView.SurfaceTextureListener, CameraViewInterface  {
    private static final boolean DEBUG = true;	// TODO set false on release
    private static final String TAG = "UVCCameraTextureView";

    private double mRequestedAspect = -1.0;
    private boolean mHasSurface;
    private RenderHandler mRenderHandler;

    public CameraView(final Context context) {
        this(context, null, 0);
    }

    public CameraView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setSurfaceTextureListener(this);
    }

    @Override
    public void setAspectRatio(final double aspectRatio) {
        if (aspectRatio < 0) {
            throw new IllegalArgumentException();
        }
        if (mRequestedAspect != aspectRatio) {
            mRequestedAspect = aspectRatio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mRequestedAspect > 0) {
            int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
            int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

            final int horizPadding = getPaddingLeft() + getPaddingRight();
            final int vertPadding = getPaddingTop() + getPaddingBottom();
            initialWidth -= horizPadding;
            initialHeight -= vertPadding;

            final double viewAspectRatio = (double)initialWidth / initialHeight;
            final double aspectDiff = mRequestedAspect / viewAspectRatio - 1;

            if (Math.abs(aspectDiff) > 0.01) {
                if (aspectDiff > 0) {
                    // width priority decision
                    initialHeight = (int) (initialWidth / mRequestedAspect);
                } else {
                    // height priority decison
                    initialWidth = (int) (initialHeight * mRequestedAspect);
                }
                initialWidth += horizPadding;
                initialHeight += vertPadding;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
        if (DEBUG) Log.v(TAG, "onSurfaceTextureAvailable:" + surface);
        mRenderHandler = RenderHandler.createHandler(surface);
        mHasSurface = true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
        if (DEBUG) Log.v(TAG, "onSurfaceTextureSizeChanged:" + surface);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
        if (DEBUG) Log.v(TAG, "onSurfaceTextureDestroyed:" + surface);
        if (mRenderHandler != null) {
            mRenderHandler.release();
            mRenderHandler = null;
        }
        mHasSurface = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(final SurfaceTexture surface) {

    }

    @Override
    public boolean hasSurface() {
        return mHasSurface;
    }


    @Override
    public SurfaceTexture getSurfaceTexture() {
        if (DEBUG) Log.v(TAG, "getSurfaceTexture:" + mRenderHandler);
        return mRenderHandler != null ? mRenderHandler.getPreviewTexture() : super.getSurfaceTexture();
    }

    @Override
    public void setRendererParam(HashMap<String,String> param) {
        if(mRenderHandler != null){
            mRenderHandler.updateRendererParam(param);
        }
    }

    @Override
    public void captureStill() {

    }

    @Override
    public void startRecording() {

    }

    @Override
    public void stopRecording() {

    }
}
