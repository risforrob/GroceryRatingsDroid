package app.subversive.groceryratings.UI;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.SurfaceView;

import app.subversive.groceryratings.camera.CameraUtil;

/**
 * Created by rob on 10/1/14.
 */
public class FocusableSurfaceView extends SurfaceView {
    private int radius;
    private boolean drawFocus;
    final Paint focusPaint = new Paint();
    final RectF focusRect = new RectF();

    public FocusableSurfaceView(Context context) {
        super(context);
        init();
    }

    public FocusableSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FocusableSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        focusPaint.setColor(Color.WHITE);
        focusPaint.setStyle(Paint.Style.STROKE);
        focusPaint.setStrokeWidth(4);


    }

    public float getRadius() { return radius; }

    public void setFocus(float x, float y) {
        drawFocus = true;
        focusRect.set(x - radius, y - radius, x + radius, y + radius);
        CameraUtil.clampRectF(focusRect, 0, 0, getWidth(), getHeight());
        invalidate();
    }

    public void unsetFocus() {
        drawFocus = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawFocus && focusRect != null) {
            canvas.drawRect(focusRect, focusPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        radius = Math.max(w,h) / 12;
    }
}
