package app.subversive.groceryratings.UI;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

import com.google.zxing.ResultPoint;

import java.util.LinkedList;

import app.subversive.groceryratings.R;
import app.subversive.groceryratings.Utils;
import app.subversive.groceryratings.camera.CameraUtil;

/**
 * Created by rob on 10/1/14.
 */
public class FocusableSurfaceView extends SurfaceView {
    private int radius;
    private boolean drawFocus;
    final Paint focusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    final RectF focusRect = new RectF();
    private float desiredAspectRatio = 1;
    private final int prvRectDivider = 15; // make preview rect 1/15th of sensor size.
    Path focusPath;
    float lastx, lasty;

    LinkedList<ResultPoint> pointsQueue = new LinkedList<>();

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
        focusPaint.setStrokeJoin(Paint.Join.ROUND);
        focusPaint.setStrokeCap(Paint.Cap.ROUND);
        focusPaint.setStrokeWidth(Utils.dp2px(4));
        focusPath = new Path();

        pointPaint.setColor(Color.WHITE);
        pointPaint.setStyle(Paint.Style.FILL);
    }

    public float getRadius() { return radius; }

    public void setFocus(float x, float y) {
        drawFocus = true;
        focusRect.set(x - radius, y - radius, x + radius, y + radius);
        CameraUtil.clampRectF(focusRect, 0, 0, getWidth(), getHeight());
        focusPath.offset(-lastx, -lasty);
        focusPath.offset(focusRect.centerX(), focusRect.centerY());
        lastx = focusRect.centerX();
        lasty = focusRect.centerY();
        invalidate();
    }

    public void unsetFocus() {
        drawFocus = false;
        focusPath.offset(-lastx, -lasty);
        lastx = 0;
        lasty = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawFocus) {
//            focusDrawable.draw(canvas);
            canvas.drawPath(focusPath, focusPaint);
        }

        for (ResultPoint r : pointsQueue) {
            canvas.drawCircle(r.getY(), r.getX(), 8, pointPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        radius = Math.max(w,h) / prvRectDivider;
        rebuildPath();
    }

    private void rebuildPath() {
        float thdr = ((float) radius) / 3;
        focusPath.reset();
        focusPath.moveTo(0,0);

        focusPath.moveTo(-thdr, -thdr*2);
        focusPath.rLineTo(thdr,-thdr);
        focusPath.rLineTo(thdr, thdr);

        focusPath.rMoveTo(thdr, thdr);
        focusPath.rLineTo(thdr, thdr);
        focusPath.rLineTo(-thdr, thdr);

        focusPath.rMoveTo(-thdr, thdr);
        focusPath.rLineTo(-thdr, thdr);
        focusPath.rLineTo(-thdr, -thdr);

        focusPath.rMoveTo(-thdr, -thdr);
        focusPath.rLineTo(-thdr, -thdr);
        focusPath.rLineTo(thdr, -thdr);
    }

    public void setDesiredAspectRatio(float a) {
        desiredAspectRatio = a;
        forceLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        float measuredAspectRatio = (float) width / (float) height;
        Log.d("Focusable|OrigRatio", String.format("%d x %d = %.2f (%.2f)", width, height, measuredAspectRatio, desiredAspectRatio));
        if (measuredAspectRatio == desiredAspectRatio) { return; }
        else if (measuredAspectRatio < desiredAspectRatio) {
            // grow width
            width = Math.round(desiredAspectRatio * height);
        } else {
            //grow height
            height = Math.round((1/desiredAspectRatio) * width);
        }
        float newAspectRatio = (float) width / (float) height;
        Log.d("Focusable|resultRatio", String.format("%d x %d = %.2f (%.2f)", width, height, newAspectRatio, desiredAspectRatio));
        setMeasuredDimension(width, height);
    }

    public void PointUpdate(ResultPoint rPoint) {
        Log.v("result point", String.format("%.2f %.2f", rPoint.getX(), rPoint.getY()));
        if (!pointsQueue.isEmpty() && pointsQueue.peekFirst().equals(rPoint)) {
            return;
        }
        if (pointsQueue.size() > 5) {
            pointsQueue.removeLast();
        }
        pointsQueue.add(rPoint);
        postInvalidate();
//        invalidate();
    }
}
