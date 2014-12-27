package app.subversive.groceryratings.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import app.subversive.groceryratings.Utils;

/**
 * Created by rob on 12/25/14.
 */
public class Rater extends View {

    private int numStars = 5;
    private int paddingStars;
    private int radius;
    private int mRating = 3;
    private int strokeWidth = 4;

    private Paint fgPaint, bgPaint;

    public Rater(Context context) {
        this(context, null);
    }

    public Rater(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Rater(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.rater, 0, 0);
//        Log.v("rater", String.format("%d", attrs.getAttributeCount()));
//        init(context, arr.getColor(R.styleable.rater_forgroundColor, Color.LTGRAY), arr.getColor(R.styleable.rater_backgroundColor, Color.DKGRAY));
//        arr.recycle();
        init(context, 0, 0);
    }

    private void init(Context context, int fg, int bg) {
        paddingStars = Utils.dp2px(2);
        radius = Utils.dp2px(6);

        fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        fgPaint.setStrokeWidth(strokeWidth);
        fgPaint.setColor(Color.rgb(36,164,36));
//        fgPaint.setColor(Color.LTGRAY);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(strokeWidth);
        bgPaint.setColor(Color.GRAY);
//        bgPaint.setColor(Color.LTGRAY);
//        bgPaint.setColor(Color.rgb(32,128,32));

    }

    public void setRating(int rating) {
        mRating = rating;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = (radius * 2) + (strokeWidth) + getPaddingTop() + getPaddingBottom();
        int desiredWidth  = (numStars * (radius + strokeWidth) * 2) + (paddingStars * (numStars - 1)) + getPaddingLeft() + getPaddingRight();

        setMeasuredDimension(desiredWidth, desiredHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int cy = ((getBottom() - getPaddingBottom()) - (getTop()+getPaddingTop())) / 2;
        int cx = getPaddingLeft() + radius + (strokeWidth/2);
        for (int i=0 ; i < numStars ; i++) {
            Paint paint = i < mRating ? fgPaint : bgPaint;
            canvas.drawCircle(cx, cy, radius, paint);
            cx += paddingStars + (radius * 2) + strokeWidth;
        }
    }
}
