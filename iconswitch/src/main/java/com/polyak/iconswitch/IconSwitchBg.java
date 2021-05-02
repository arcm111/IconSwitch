package com.polyak.iconswitch;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

/**
 * Created by polyak01 on 31.03.2017.
 * https://github.com/polyak01
 */
class IconSwitchBg extends Drawable {
    private final RectF bounds;
    private final Paint paint;

    private float radius;

    public IconSwitchBg() {
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.bounds = new RectF();
    }

    public void init(int width, int height, int bgWidth, int bgHeight) {
        final int cx = width / 2;
        final int cy = height / 2;
        final int w = bgWidth / 2;
        final int h = bgHeight / 2;

        bounds.set(cx - w, cy - h, cx + w, cy + h);
        radius = bounds.height() * 0.5f;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRoundRect(bounds, radius, radius, paint);
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }
}
