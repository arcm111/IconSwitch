package com.polyak.iconswitch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.customview.widget.ViewDragHelper;

/**
 * Created by polyak01 on 31.03.2017.
 * https://github.com/polyak01
 */
public class IconSwitch extends ViewGroup {
    private static final String EXTRA_SUPER = "extra_super";
    private static final String EXTRA_CHECKED = "extra_is_checked";
    private static final int DEF_STYLE_RES = R.style.IconSwitchStyle_Switch;
    private static final int UNITS_VELOCITY = 1000;
    private final double TOUCH_SLOP_SQUARE;
    private final int FLING_MIN_VELOCITY;

    private ImageView leftIcon;
    private ImageView rightIcon;
    private ThumbView thumb;
    private IconSwitchBg background;

    private final ViewDragHelper thumbDragHelper;
    private VelocityTracker velocityTracker;

    private float thumbPosition;

    private int inactiveIconTint;
    private int activeIconTint;
    private int thumbColor;
    private int thumbColor2;
    private int backgroundColor;

    private final Dimensions dimens;
    private final PointF downPoint;
    private boolean isClick;
    private int translationX, translationY;

    private Checked currentChecked;
    private CheckedChangeListener listener;

    public IconSwitch(Context context) {
        super(context);
        init(null, 0, DEF_STYLE_RES);
    }

    public IconSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, DEF_STYLE_RES);
    }

    public IconSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, DEF_STYLE_RES);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public IconSwitch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, (defStyleRes == 0) ? DEF_STYLE_RES : defStyleRes);
    }

    {
        ViewConfiguration viewConf = ViewConfiguration.get(getContext());
        FLING_MIN_VELOCITY = viewConf.getScaledMinimumFlingVelocity();
        TOUCH_SLOP_SQUARE = Math.pow(viewConf.getScaledTouchSlop(), 2);
        thumbDragHelper = ViewDragHelper.create(this, new ThumbDragCallback());
        downPoint = new PointF();
        dimens = new Dimensions(getContext());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putParcelable(EXTRA_SUPER, super.onSaveInstanceState());
        state.putInt(EXTRA_CHECKED, currentChecked.ordinal());
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcel) {
        Bundle state = (Bundle) parcel;
        super.onRestoreInstanceState(state.getParcelable(EXTRA_SUPER));
        currentChecked = Checked.values()[state.getInt(EXTRA_CHECKED, 0)];
        thumbPosition = currentChecked == Checked.LEFT ? 0f : 1f;
        ensureCorrectColors();
    }

    private void init(AttributeSet attr, int defStyleAttr, int defStyleRes) {
        addView(thumb = new ThumbView(getContext()));
        addView(leftIcon = new ImageView(getContext()));
        addView(rightIcon = new ImageView(getContext()));

        setBackground(background = new IconSwitchBg());

        loadDefaultAttributes();
        if (attr != null) {
            loadAttribute(attr, defStyleAttr, defStyleRes);
        }

        thumbPosition = (currentChecked == Checked.LEFT) ? 0f : 1f;
        ensureCorrectColors();
    }

    private void loadDefaultAttributes() {
        currentChecked = Checked.LEFT;
        inactiveIconTint = getResources().getColor(R.color.isw_defaultIconActiveTintColour);
        activeIconTint = getResources().getColor(R.color.isw_defaultIconActiveTintColour);
        background.setColor(getResources().getColor(R.color.isw_defaultBg));
        thumbColor = getResources().getColor(R.color.isw_defaultThumbColour);
        leftIcon.setImageResource(R.drawable.ic_add_photo_alternate_white_24dp);
        rightIcon.setImageResource(R.drawable.ic_add_photo_alternate_white_24dp);
    }

    private void loadAttribute(AttributeSet attr, int defStyleAttr, int defStyleRes) {
        TypedArray ta = getContext().obtainStyledAttributes(attr, R.styleable.IconSwitch, defStyleAttr, defStyleRes);
        inactiveIconTint = ta.getColor(R.styleable.IconSwitch_isw_icon_inactive_tint, inactiveIconTint);
        activeIconTint = ta.getColor(R.styleable.IconSwitch_isw_icon_active_tint, activeIconTint);
        backgroundColor = ta.getColor(R.styleable.IconSwitch_isw_background_color, backgroundColor);
        thumbColor = ta.getColor(R.styleable.IconSwitch_isw_thumb_color, thumbColor);
        thumbColor2 = ta.getColor(R.styleable.IconSwitch_isw_thumb_color_alternative, 0);
        leftIcon.setImageDrawable(getDrawableFromResource(ta, R.styleable.IconSwitch_isw_icon_left));
        rightIcon.setImageDrawable(getDrawableFromResource(ta, R.styleable.IconSwitch_isw_icon_right));
        currentChecked = Checked.values()[ta.getInt(R.styleable.IconSwitch_isw_default_selection, 0)];
        setEnabled(ta.getBoolean(R.styleable.IconSwitch_isw_enabled, true));
        dimens.setIconSize(ta.getDimensionPixelSize(R.styleable.IconSwitch_isw_icon_size, dimens.getIconSize()));
        dimens.setThumbHorizontalPadding(ta.getDimensionPixelSize(R.styleable.IconSwitch_isw_thumb_padding_horizontal, dimens.getThumbHorizontalPadding()));
        dimens.setThumbVerticalPadding(ta.getDimensionPixelSize(R.styleable.IconSwitch_isw_thumb_padding_vertical, dimens.getThumbVerticalPadding()));
        dimens.setIconHorizontalPadding(ta.getDimensionPixelSize(R.styleable.IconSwitch_isw_icon_padding_horizontal, dimens.getIconHorizontalPadding()));
        dimens.setIconVerticalPadding(ta.getDimensionPixelSize(R.styleable.IconSwitch_isw_icon_padding_vertical, dimens.getIconVerticalPadding()));
        ta.recycle();
    }

    private Drawable getDrawableFromResource(TypedArray ta, @StyleableRes int resAttr) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ta.getDrawable(resAttr);
        }
        return AppCompatResources.getDrawable(getContext(), ta.getResourceId(resAttr, -1));
    }

    private void ensureCorrectColors() {
        if (isEnabled()) {
            leftIcon.setColorFilter(isLeftChecked() ? activeIconTint : inactiveIconTint);
            rightIcon.setColorFilter(isLeftChecked() ? inactiveIconTint : activeIconTint);
            thumb.setColor((!isLeftChecked() && thumbColor2 != 0) ? thumbColor2 : thumbColor);
            background.setColor(backgroundColor);
        } else {
            leftIcon.setColorFilter(desaturate(isLeftChecked() ? activeIconTint : inactiveIconTint));
            rightIcon.setColorFilter(desaturate(isLeftChecked() ? inactiveIconTint : activeIconTint));
            thumb.setColor(desaturate(thumbColor));
            background.setColor(desaturate(backgroundColor));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getSize(widthMeasureSpec, dimens.getSwitchWidth());
        int height = getSize(heightMeasureSpec, dimens.getSwitchHeight());

        int thumbWidthSpec = MeasureSpec.makeMeasureSpec(dimens.getThumbWidth(), MeasureSpec.EXACTLY);
        int thumbHeightSpec = MeasureSpec.makeMeasureSpec(dimens.getThumbHeight(), MeasureSpec.EXACTLY);
        thumb.measure(thumbWidthSpec, thumbHeightSpec);

        int iconSpec = MeasureSpec.makeMeasureSpec(dimens.getIconSize(), MeasureSpec.EXACTLY);
        leftIcon.measure(iconSpec, iconSpec);
        rightIcon.measure(iconSpec, iconSpec);

        background.init(width, height, dimens.getBackgroundWidth(), dimens.getBackgroundHeight());

        translationX = (width / 2) - (dimens.getSwitchWidth() / 2);
        translationY = (height / 2) - (dimens.getSwitchHeight() / 2);

        setMeasuredDimension(width, height);
    }

    private int getSize(int measureSpec, int fallbackSize) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST:
                return Math.min(size, fallbackSize);
            case MeasureSpec.EXACTLY:
                return size;
            case MeasureSpec.UNSPECIFIED:
                return fallbackSize;
        }
        return size;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        leftIcon.layout(dimens.getLeftIconLeft(), dimens.getIconTop(), dimens.getLeftIconRight(), dimens.getIconBottom());
        rightIcon.layout(dimens.getRightIconLeft(), dimens.getIconTop(), dimens.getRightIconRight(), dimens.getIconBottom());
        thumb.layout(dimens.getThumbLeft(thumbPosition), dimens.getThumbTop(), dimens.getThumbRight(thumbPosition), dimens.getThumbBottom());
//        background.setBounds(dimens.getBackgroundBounds());
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        canvas.save();
        canvas.translate(translationX, translationY);
        boolean result = super.drawChild(canvas, child, drawingTime);
        canvas.restore();
        return result;
    }

    @Override
    public void computeScroll() {
        if (thumbDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!isEnabled()) return true;

        MotionEvent event = MotionEvent.obtain(e);
        event.setLocation(e.getX() - translationX, e.getY() - translationY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                break;
            case MotionEvent.ACTION_UP:
                onUp(event);
                clearTouchInfo();
                performClick();
                break;
            case MotionEvent.ACTION_CANCEL:
                clearTouchInfo();
                break;
        }
        thumbDragHelper.processTouchEvent(event);
        event.recycle();
        return true;
    }

    private void onDown(MotionEvent e) {
        velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(e);
        downPoint.set(e.getX(), e.getY());
        isClick = true;
        thumbDragHelper.captureChildView(thumb, e.getPointerId(0));
    }

    private void onMove(MotionEvent e) {
        velocityTracker.addMovement(e);
        double distance = Math.hypot(e.getX() - downPoint.x, e.getY() - downPoint.y);
        if (isClick) {
            isClick = distance < TOUCH_SLOP_SQUARE;
        }
    }

    private void onUp(MotionEvent e) {
        velocityTracker.addMovement(e);
        velocityTracker.computeCurrentVelocity(UNITS_VELOCITY);
        if (isClick) {
            isClick = Math.abs(velocityTracker.getXVelocity()) < FLING_MIN_VELOCITY;
        }
        if (isClick) {
            toggleSwitch();
            notifyCheckedChanged();
        }
    }

    private void clearTouchInfo() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void notifyCheckedChanged() {
        if (listener != null) {
            listener.onCheckChanged(currentChecked);
        }
    }

    private void applyPositionalTransform() {
        float clampedPosition = Math.max(0f, Math.min(thumbPosition, 1f)); //Ignore overshooting
        int leftColor = Evaluator.ofArgb(clampedPosition, activeIconTint, inactiveIconTint);
        leftIcon.setColorFilter(leftColor);
        int rightColor = Evaluator.ofArgb(clampedPosition, inactiveIconTint, activeIconTint);
        rightIcon.setColorFilter(rightColor);
        if (thumbColor2 != 0) {
            int thumbTransitionalColor = Evaluator.ofArgb(clampedPosition, thumbColor, thumbColor2);
            thumb.setColor(thumbTransitionalColor);
        }
        float closenessToCenter = 1f - Math.abs(clampedPosition - 0.5f) / 0.5f;
        float iconScale = 1f - (closenessToCenter * 0.3f);
        leftIcon.setScaleX(iconScale);
        leftIcon.setScaleY(iconScale);
        rightIcon.setScaleX(iconScale);
        rightIcon.setScaleY(iconScale);
    }

    private int getLeftAfterFling(float direction) {
        return direction > 0 ? dimens.getThumbEndLeft() : dimens.getThumbStartLeft();
    }

    private class ThumbDragCallback extends ViewDragHelper.Callback {
        private int dragState;

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            if (child != thumb) {
                thumbDragHelper.captureChildView(thumb, pointerId);
                return false;
            }
            return true;
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            if (isClick) {
                return;
            }
            boolean isFling = Math.abs(xvel) >= FLING_MIN_VELOCITY;
            int newLeft = isFling ? getLeftAfterFling(xvel) : getLeftToSettle();
            Checked newChecked = (newLeft == dimens.getThumbStartLeft()) ? Checked.LEFT : Checked.RIGHT;
            if (newChecked != currentChecked) {
                currentChecked = newChecked;
                notifyCheckedChanged();
            }
            thumbDragHelper.settleCapturedViewAt(newLeft, dimens.getThumbTop());
            invalidate();
        }

        private int getLeftToSettle() {
            return thumbPosition > 0.5f ? dimens.getThumbEndLeft() : dimens.getThumbStartLeft();
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            thumbPosition = ((float) (left - dimens.getThumbStartLeft())) / dimens.getThumbDragDistance();
            applyPositionalTransform();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if (dragState == ViewDragHelper.STATE_DRAGGING) {
                return Math.max(dimens.getThumbStartLeft(), Math.min(left, dimens.getThumbEndLeft()));
            }
            return left;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            return dimens.getThumbTop();
        }

        @Override
        public void onViewDragStateChanged(int state) {
            dragState = state;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
            return child == thumb ? dimens.getThumbDragDistance() : 0;
        }
    }

    public void getThumbCenter(Point point) {
        final int thumbLeft = (int) (dimens.getThumbStartLeft() + dimens.getThumbDragDistance() * thumbPosition);
        final int thumbCenterX = thumbLeft + translationX;
        final int thumbCenterY = dimens.getThumbHeight() + translationY;
        point.set(thumbCenterX, thumbCenterY);
    }

    private int dpToPx(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }

    /**
     * Convert an RGB color to Grayscale.
     * desaturation formula used from {@link "https://stackoverflow.com/a/596241/2038544"}.
     * @param rgb the color to be desaturated.
     * @return the desaturated color.
     */
    private @ColorInt int desaturate(@ColorInt int rgb) {
        //Luma conversion formula Photometric/digital ITU BT.709
        int v = (int) (0.2126 * Color.green(rgb) + 0.7152 * Color.red(rgb) + 0.0722 * Color.blue(rgb));

        // level brightness and decrease contrast
        // factor ranges between -1 to 1 depending on v value
        float factor = (v - 123.0f) / 123.0f;
        // adjust brightness by reducing value by up to -50/50
        int a = (int) (v - factor * 50);

        // create new desaturated rgb color (same value r, g, b means grayscale color)
        int r = Color.rgb(a, a, a);
        Log.i("MODULE", rgb + ":" + v + ":" + a + ":" + r);
        return r;
    }

    private boolean isLeftChecked() {
        return currentChecked == Checked.LEFT;
    }

    private void toggleSwitch() {
        currentChecked = currentChecked.toggle();
        int newLeft = (currentChecked == Checked.LEFT) ? dimens.getThumbStartLeft() : dimens.getThumbEndLeft();
        if (thumbDragHelper.smoothSlideViewTo(thumb, newLeft, dimens.getThumbTop())) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Enable or disable the switch.
     * Makes use of the native {@code ViewGroup.setEnabled()} method.
     * @param enabled should enable value
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ensureCorrectColors();
        invalidate();
    }

    public void setChecked(Checked newChecked) {
        if (currentChecked != newChecked) {
            toggleSwitch();
            notifyCheckedChanged();
        }
    }

    public void toggle() {
        toggleSwitch();
        notifyCheckedChanged();
    }

    public Checked getChecked() {
        return currentChecked;
    }

    public void setThumbColor(@ColorInt int thumbColor) {
        this.thumbColor = thumbColor;
        ensureCorrectColors();
    }

    public void setThumbAlternativeColor(@ColorInt int thumbColor2) {
        this.thumbColor2 = thumbColor2;
        ensureCorrectColors();
    }

    public void setInactiveIconTint(@ColorInt int inactiveIconTint) {
        this.inactiveIconTint = inactiveIconTint;
        ensureCorrectColors();
    }

    public void setActiveIconTint(@ColorInt int activeIconTint) {
        this.activeIconTint = activeIconTint;
        ensureCorrectColors();
    }

    public void setBackgroundColor(@ColorInt int color) {
        background.setColor(color);
    }

    public ImageView getLeftIcon() {
        return leftIcon;
    }

    public ImageView getRightIcon() {
        return rightIcon;
    }

    public void setIconSize(int dp) {
        dimens.setIconSize(dpToPx(dp));
        requestLayout();
    }

    public void setIconHorizontalPadding(int padding) {
        dimens.setIconHorizontalPadding(padding);
        requestLayout();
    }

    public void setIconVerticalPadding(int padding) {
        dimens.setIconVerticalPadding(padding);
        requestLayout();
    }

    public void setThumbHorizontalPadding(int padding) {
        dimens.setThumbHorizontalPadding(padding);
        requestLayout();
    }

    public void setThumbVerticalPadding(int padding) {
        dimens.setThumbVerticalPadding(padding);
        requestLayout();
    }

    public void setCheckedChangeListener(CheckedChangeListener listener) {
        this.listener = listener;
    }

    public interface CheckedChangeListener {
        void onCheckChanged(Checked current);
    }

    public enum Checked {
        LEFT {
            @Override
            public Checked toggle() {
                return RIGHT;
            }
        },
        RIGHT {
            @Override
            public Checked toggle() {
                return LEFT;
            }
        };

        public abstract Checked toggle();
    }
}
