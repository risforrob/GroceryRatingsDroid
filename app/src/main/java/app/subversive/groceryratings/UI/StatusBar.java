package app.subversive.groceryratings.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

/**
 * Created by rob on 8/26/14.
 */
public class StatusBar extends View {
    private int textSize = 20;
    private Paint mTextPaint, mBackgroundPaint;
    private float preferredHeight;
    private String mText;
    private float dpPadding = 8;
    private float pxPadding;
    private float textbottom;
    boolean showSpinner;

    public StatusBar(Context context) {
        super(context);
        init(context);
    }

    public StatusBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StatusBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        DisplayMetrics dMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dMetrics);
        mTextPaint.setTextSize(dMetrics.scaledDensity * textSize);
        pxPadding = dpPadding * dMetrics.density;

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(Color.BLACK);
        mBackgroundPaint.setAlpha(192);

        Paint.FontMetrics fMetrics = mTextPaint.getFontMetrics();
        preferredHeight = fMetrics.descent - fMetrics.ascent + (pxPadding*2);
        textbottom = fMetrics.descent+pxPadding;
    }

    public void setText(String text, boolean showSpinner) {
        mText = text;
        this.showSpinner = showSpinner;

        if (showSpinner) {
            Rect r = new Rect();
            mTextPaint.getTextBounds(text, 0, text.length(), r);
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int h = resolveSizeAndState((int) preferredHeight, heightMeasureSpec, 0);
        setMeasuredDimension(widthMeasureSpec, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
        if (mText != null) {
            canvas.drawText(mText, canvas.getWidth()/2, canvas.getHeight()-textbottom, mTextPaint);
        }
    }
}
