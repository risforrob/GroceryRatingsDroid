package app.subversive.groceryratings.UI;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import app.subversive.groceryratings.R;
import app.subversive.groceryratings.Utils;

/**
 * Created by rob on 12/25/14.
 */
public class Rater extends View {

    private int numStars = 5;
    private int paddingStars;
    private int radius;
    private int actualRadius;
    private int mRating = 3;
    private int strokeWidth;

    private Paint fgPaint, bgPaint;

    public Rater(Context context) {
        this(context, null);
    }

    public Rater(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Rater(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.rater, 0, 0);
        init(
                context,
                arr.getColor(R.styleable.rater_foregroundColor, getResources().getColor(R.color.raterFilled)),
                arr.getColor(R.styleable.rater_backgroundColor, getResources().getColor(R.color.raterEmpty)),
                arr.getInt(R.styleable.rater_numStars, numStars),
                arr.getDimensionPixelSize(R.styleable.rater_radius, Utils.dp2px(6)),
                arr.getDimensionPixelSize(R.styleable.rater_paddingStars, Utils.dp2px(2)),
                arr.getDimensionPixelSize(R.styleable.rater_ringWidth, Utils.dp2px(1))
            );
        arr.recycle();
    }

    private void init(Context context, int fg, int bg, int numStars, int radius, int paddingStars, int ringRadius) {
        this.paddingStars = paddingStars;
        this.radius = radius;
        this.numStars = numStars;
        this.strokeWidth = ringRadius;

        actualRadius = radius + ((strokeWidth  % 2 == 1) ? (strokeWidth-1/2) : (strokeWidth/2));

        fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        fgPaint.setStrokeWidth(strokeWidth);
        fgPaint.setColor(fg);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(strokeWidth);
        bgPaint.setColor(bg);
    }

    public void setRating(int rating) {
        mRating = rating;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = (actualRadius * 2) + getPaddingTop() + getPaddingBottom();
        int desiredWidth  = (numStars * actualRadius * 2) + (paddingStars * (numStars - 1)) + getPaddingLeft() + getPaddingRight();

        setMeasuredDimension(desiredWidth, desiredHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int cy = ((getBottom() - getPaddingBottom()) - (getTop()+getPaddingTop())) / 2;
        int cx = getPaddingLeft() + actualRadius;
        for (int i=0 ; i < numStars ; i++) {
            Paint paint = i < mRating ? fgPaint : bgPaint;
            canvas.drawCircle(cx, cy, radius, paint);
            cx += paddingStars + (actualRadius * 2);
        }
    }
}
