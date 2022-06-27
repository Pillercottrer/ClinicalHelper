package com.ehbmed.clinicalhelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CustomOnDrawView extends View {

    static float cropx1 = 0.36f, cropx2 = 0.73f, cropy1 = 0.2f, cropy2 = 0.5f;

    public CustomOnDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomOnDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public void onDraw(Canvas canvas)
    {
        Paint paint = new Paint(Color.BLACK);
        //canvas.drawRect(width*0.5f, height*0.2f, width*0.75f, height*0.5f, paint);

        canvas.drawLine(width*0.36f, height*0.2f, width*0.73f, height*0.2f, paint);
        canvas.drawLine(width*0.73f, height*0.2f, width*0.73f, height*0.5f, paint);
        canvas.drawLine(width*0.73f, height*0.5f, width*0.36f, height*0.5f, paint);
        canvas.drawLine(width*0.36f, height*0.5f, width*0.36f, height*0.2f, paint);
    }

    private int width, height;
    @Override
    protected void onSizeChanged(int wNew, int hNew, int wOld, int hOld)
    {
        super.onSizeChanged(wNew, hNew, wOld, hOld);

        width = wNew;
        height = hNew;
    }

}
