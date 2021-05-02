package com.polyak.iconswitch;

import android.content.Context;

import androidx.annotation.DimenRes;

public class Dimensions {
    private final Context context;
    private final int minimumIconSize;
    private final int thumbMinimumPadding;
    private final int iconMinimumPadding;

    private int iconSize;
    private int iconVerticalPadding;
    private int iconHorizontalPadding;
    private int thumbHorizontalPadding;
    private int thumbVerticalPadding;

    public Dimensions(Context context) {
        this.context = context;
        this.minimumIconSize = getPxDimenFromResource(R.dimen.isw_icon_minimum_size);
        this.thumbMinimumPadding = getPxDimenFromResource(R.dimen.isw_thumb_minimum_padding);
        this.iconMinimumPadding = getPxDimenFromResource(R.dimen.isw_icon_minimum_padding);
        this.iconSize = getPxDimenFromResource(R.dimen.isw_default_icon_size);
        this.iconVerticalPadding = iconMinimumPadding;
        this.iconHorizontalPadding = iconMinimumPadding;
        this.thumbVerticalPadding = thumbMinimumPadding;
        this.thumbHorizontalPadding = thumbMinimumPadding;
    }

    private int getPxDimenFromResource(@DimenRes int resId) {
        return context.getResources().getDimensionPixelSize(resId);
    }

    public int getSwitchWidth() {
        if (thumbHorizontalPadding > iconHorizontalPadding) {
            return getIconWidth() * 2 + (thumbHorizontalPadding - iconHorizontalPadding) * 2;
        }
        return getIconWidth() * 2;
    }

    public int getSwitchHeight() {
        return Math.max(getThumbHeight(), getIconHeight());
    }

    public int getThumbWidth() {
        return iconSize + thumbHorizontalPadding * 2;
    }

    public int getThumbHeight() {
        return iconSize + thumbVerticalPadding * 2;
    }

    public int getThumbStartLeft() {
        return Math.max(0, iconHorizontalPadding - thumbHorizontalPadding);
    }

    public int getThumbEndLeft() {
        return getThumbStartLeft() + getIconWidth();
    }

    public int getThumbDragDistance() {
        return getThumbEndLeft() - getThumbStartLeft();
    }

    public int getThumbTop() {
        return Math.max(0, iconVerticalPadding - thumbVerticalPadding);
    }

    public int getThumbBottom() {
        return getThumbTop() + getThumbHeight();
    }

    public int getThumbLeft(float position) {
        return getThumbStartLeft() + (int) (position * getThumbDragDistance());
    }

    public int getThumbRight(float position) {
        return getThumbLeft(position) + getThumbWidth();
    }

    private int getIconWidth() {
        return iconSize + iconHorizontalPadding * 2;
    }

    private int getIconHeight() {
        return iconSize + iconVerticalPadding * 2;
    }

    public int getIconTop() {
        return Math.max(thumbVerticalPadding, iconVerticalPadding);
    }

    public int getIconBottom() {
        return getIconTop() + iconSize;
    }

    public int getLeftIconLeft() {
        return Math.max(thumbHorizontalPadding, iconHorizontalPadding);
    }

    public int getLeftIconRight() {
        return getLeftIconLeft() + iconSize;
    }

    public int getRightIconLeft() {
        return getLeftIconRight() + iconHorizontalPadding * 2;
    }

    public int getRightIconRight() {
        return getRightIconLeft() + iconSize;
    }

    public int getIconSize() {
        return iconSize;
    }

    public int getBackgroundWidth() {
        return getIconWidth() * 2;
    }

    public int getBackgroundHeight() {
        return getIconHeight();
    }

    public int getThumbHorizontalPadding() {
        return thumbHorizontalPadding;
    }

    public int getThumbVerticalPadding() {
        return thumbVerticalPadding;
    }

    public int getIconHorizontalPadding() {
        return iconHorizontalPadding;
    }

    public int getIconVerticalPadding() {
        return iconVerticalPadding;
    }

    public void setIconSize(int iconSize) {
        this.iconSize = Math.max(iconSize, minimumIconSize);
    }

    public void setThumbHorizontalPadding(int thumbHorizontalPadding) {
        this.thumbHorizontalPadding = Math.max(thumbHorizontalPadding, thumbMinimumPadding);
    }

    public void setThumbVerticalPadding(int thumbVerticalPadding) {
        this.thumbVerticalPadding = Math.max(thumbVerticalPadding, thumbMinimumPadding);
    }

    public void setIconHorizontalPadding(int iconHorizontalPadding) {
        this.iconHorizontalPadding = Math.max(iconHorizontalPadding, iconMinimumPadding);
    }

    public void setIconVerticalPadding(int iconVerticalPadding) {
        this.iconVerticalPadding = Math.max(iconVerticalPadding, iconMinimumPadding);
    }
}
