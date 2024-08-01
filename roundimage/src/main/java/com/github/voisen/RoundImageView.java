package com.github.voisen;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;


public class RoundImageView extends View {
    private final String TAG = "RoundImageView";
    private static final int COLOR_DRAWABLE_DIMENSION = 2;
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private Bitmap mBitmap;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix mShaderMatrix = new Matrix();
    private BitmapShader mBitmapShader;
    private final RectF mDrawRect = new RectF();
    private float mBorderWidth = 0;
    private int mBorderColor = Color.TRANSPARENT;
    private final Path mPath = new Path();

    private float mTopLeftRadius = 0f;
    private float mTopRightRadius = 0f;
    private float mBottomLeftRadius = 0f;
    private float mBottomRightRadius = 0f;
    private int mScaleType = 0;

    public static final int SCALE_TYPE_CENTER_CROP = 0;
    public static final int SCALE_TYPE_FIT_XY = 1;

    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        int srcId = attributes.getResourceId(R.styleable.RoundImageView_android_src, -1);
        if (srcId != -1){
            setImageResource(srcId);
        }
        float mRadius = attributes.getDimension(R.styleable.RoundImageView_android_radius, -1);
        mBorderColor = attributes.getColor(R.styleable.RoundImageView_border_color, mBorderColor);
        mBorderWidth = attributes.getDimension(R.styleable.RoundImageView_border_width, mBorderWidth);

        mTopLeftRadius = attributes.getDimension(R.styleable.RoundImageView_android_topLeftRadius, mRadius);
        mTopRightRadius = attributes.getDimension(R.styleable.RoundImageView_android_topRightRadius, mRadius);
        mBottomLeftRadius = attributes.getDimension(R.styleable.RoundImageView_android_bottomLeftRadius, mRadius);
        mBottomRightRadius = attributes.getDimension(R.styleable.RoundImageView_android_bottomRightRadius, mRadius);
        mScaleType = attributes.getInt(R.styleable.RoundImageView_scaleMode, mScaleType);
        attributes.recycle();
    }

    public void setScaleType(int scaleType) {
        this.mScaleType = scaleType;
        updateDrawArgs();
    }

    public void setImageBitmap(Bitmap bm) {
        if (mBitmap != null && !mBitmap.isRecycled()){
            mBitmap.recycle();
        }
        mBitmap = bm;
        updateDrawArgs();
    }

    public void setImageDrawable(@Nullable Drawable drawable) {
        Bitmap bitmap = getBitmapFromDrawable(drawable);
        setImageBitmap(bitmap);
    }

    public void setImageResource(@DrawableRes int resource){
        Drawable drawable = getResources().getDrawable(resource);
        setImageDrawable(drawable);
    }


    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        try {
            Bitmap bitmap;
            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLOR_DRAWABLE_DIMENSION, COLOR_DRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "getBitmapFromDrawable: ", e);
            return null;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateDrawArgs();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        updateDrawArgs();
    }


    public void setBorderWidth(float borderWidth){
        this.mBorderWidth = borderWidth;
        updateDrawArgs();
    }

    public void setBorderColor(@ColorInt int color){
        this.mBorderColor = color;
        updateDrawArgs();
    }

    public void setTopLeftRadius(float topLeftRadius) {
        this.mTopLeftRadius = topLeftRadius;
        updateDrawArgs();
    }

    public void setTopRightRadius(float topRightRadius) {
        this.mTopRightRadius = topRightRadius;
        updateDrawArgs();
    }

    public void setBottomRightRadius(float bottomRightRadius) {
        this.mBottomRightRadius = bottomRightRadius;
        updateDrawArgs();
    }

    public void setBottomLeftRadius(float bottomLeftRadius) {
        this.mBottomLeftRadius = bottomLeftRadius;
        updateDrawArgs();
    }

    public void setRadius(float radius) {
        this.mTopLeftRadius = radius;
        this.mTopRightRadius = radius;
        this.mBottomRightRadius = radius;
        this.mBottomLeftRadius = radius;
        updateDrawArgs();
    }

    private void updateDrawArgs(){
        if (mBitmap == null){
            postInvalidate();
            return;
        }
        int width = (int) (getWidth() - getPaddingLeft() - getPaddingRight() - mBorderWidth + 0.5f);
        int height = (int) (getHeight() - getPaddingTop() - getPaddingBottom() - mBorderWidth + 0.5f);
        int bitmapWidth = mBitmap.getWidth();
        int bitmapHeight = mBitmap.getHeight();

        mDrawRect.top = getPaddingTop() + mBorderWidth/2;
        mDrawRect.left = getPaddingLeft() + mBorderWidth/2;
        mDrawRect.right = mDrawRect.left + width;
        mDrawRect.bottom = mDrawRect.top + height;
        mShaderMatrix.reset();
        float sx = mDrawRect.width()/bitmapWidth;
        float sy = mDrawRect.height()/bitmapHeight;
        if (mScaleType==0){
            float scale = Math.max(sx, sy);
            float realW = bitmapWidth * scale;
            float realH = bitmapHeight * scale;
            mShaderMatrix.setScale(scale, scale);
            mShaderMatrix.postTranslate(mDrawRect.left + (width - realW)/2.0f, mDrawRect.top + (height - realH)/2.0f);
        }else{
            mShaderMatrix.setScale(sx, sy);
            mShaderMatrix.postTranslate(mDrawRect.left, mDrawRect.top);
        }
        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapShader.setLocalMatrix(mShaderMatrix);
        mPath.reset();
        mPath.addRoundRect(mDrawRect, new float[]{
                mTopLeftRadius, mTopLeftRadius,
                mTopRightRadius, mTopRightRadius,
                mBottomRightRadius, mBottomRightRadius,
                mBottomLeftRadius, mBottomLeftRadius
        }, Path.Direction.CW);
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mBitmap == null){
            return;
        }
        if (mBitmapShader == null){
            return;
        }
        mPaint.setStrokeWidth(0);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setShader(mBitmapShader);
        canvas.drawPath(mPath, mPaint);
        if (mBorderWidth > 0){
            mPaint.setShader(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mBorderWidth);
            mPaint.setColor(mBorderColor);
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY){
            if (mBitmap != null){
                widthSize = Math.max(mBitmap.getWidth(), 45);
            }else{
                widthSize = 45;
            }
            widthSize = Math.max(widthSize, getMinimumWidth());
        }
        if (heightMode != MeasureSpec.EXACTLY){
            if (mBitmap != null){
                heightSize = Math.max(mBitmap.getHeight(), 45);
            }else{
                heightSize = 45;
            }
            heightSize = Math.max(heightSize, getMinimumHeight());
        }
        setMeasuredDimension(widthSize, heightSize);
    }
}
