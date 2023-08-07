package com.app.materialwallpaper.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.app.materialwallpaper.R;
import com.google.android.material.button.MaterialButton;

import org.w3c.dom.Text;

public class HorizontalPagingIndicator extends LinearLayout {

    private MaterialButton mPrevButton;
    private ViewGroup[] mPageButtons;
    private MaterialButton mNextButton;
    private PageChangeListener mPageChangeListener;

    private int mCurrentPage = 0;

    private int mButtonTextColor;
    private int mButtonBackgroundColor;
    private int mSelectedButtonBackgroundColor;
    private int mNextPreBackgroundColor;
    private int totalPages;

    private GradientDrawable mButtonBackground;
    private GradientDrawable mSelectedButtonBackground;
    private GradientDrawable mNextPreButtonBackground;

    private OnClickListener btnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPageChangeListener != null) {
                mPageChangeListener.onPageChanged(mCurrentPage + 1);
            }
        }
    };

    public HorizontalPagingIndicator(Context context) {
        this(context, null);
    }

    public HorizontalPagingIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setButtonBackgroundColor(R.color.color_dark_toolbar);
        int paddingLR = (int) getResources().getDimension(R.dimen.padding_bottom_pager_lr);
        int paddingTB = (int) getResources().getDimension(R.dimen.padding_bottom_pager_tb);
        setPadding(paddingLR, paddingTB, paddingLR, paddingTB);
        // Set default button colors
        mButtonTextColor = Color.WHITE;
        mButtonBackgroundColor = ContextCompat.getColor(context, R.color.color_dark_accent);
        mSelectedButtonBackgroundColor = ContextCompat.getColor(context, R.color.color_light_primary);
        mNextPreBackgroundColor = ContextCompat.getColor(context, R.color.color_dark_accent);
        mSelectedButtonBackground = getSelectedButtonBackgroundDrawable();
        mButtonBackground = getButtonBackgroundDrawable();
        mNextPreButtonBackground = getNextPreButtonBackgroundDrawable();
    }


    private ViewGroup createButton(String text) {
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1F;
        layoutParams.gravity = Gravity.CENTER;
        FrameLayout lyt = new FrameLayout(getContext());
        lyt.setLayoutParams(layoutParams);
        lyt.setForegroundGravity(Gravity.CENTER);

        TextView button = new TextView(getContext());
        button.setText(text);
        button.setGravity(Gravity.CENTER);
        button.setTextSize(spToPx(5));
        button.setTextColor(mButtonTextColor);
        button.setBackground(getButtonBackgroundDrawable());
        int btnSize = (int) dpToPx(44);
        button.setWidth(btnSize);
        button.setHeight(btnSize);
        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(btnSize, btnSize);
        btnParams.gravity = Gravity.CENTER;
        lyt.addView(button, btnParams);
        return lyt;
    }

    private MaterialButton createNextPrevButton(String text, int icon, int iconGravity) {

        MaterialButton button = new MaterialButton(getContext());
        button.setText(text);
        button.setIcon(ContextCompat.getDrawable(getContext(), icon));
        button.setIconGravity(iconGravity);

        button.setInsetBottom(0);
        button.setInsetTop(0);
        button.setTextSize(spToPx(5));
       // button.setIconSize((int) dpToPx(32));
        button.setTextColor(mButtonTextColor);

        button.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_dark_accent));
        button.setIconTint(ColorStateList.valueOf(Color.WHITE));

        int padding = (int) dpToPx(5);
        int paddingLR1 = (int) dpToPx(20);
        int paddingLR2 = (int) dpToPx(10);
        if(iconGravity == Gravity.LEFT) {
            button.setPadding(paddingLR1, padding, paddingLR2, padding);

        } else {
            button.setPadding(paddingLR2, padding, paddingLR1, padding);

        }
        button.setCompoundDrawablePadding(-10);
        button.setCornerRadius((int) dpToPx(30));
//        button.setBackground(mNextPreButtonBackground);
        return button;
    }

    private float spToPx(float sp) {
        return sp * getContext().getResources().getDisplayMetrics().scaledDensity;
    }

    private float pxToDp(final float px) {
        return px / getContext().getResources().getDisplayMetrics().density;
    }

    private float dpToPx(final float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }

    private GradientDrawable getButtonBackgroundDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setCornerRadius(8);
        drawable.setColor(mButtonBackgroundColor);
        return drawable;
    }


    private GradientDrawable getNextPreButtonBackgroundDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(160);
        drawable.setColor(mNextPreBackgroundColor);
        return drawable;
    }

    private GradientDrawable getSelectedButtonBackgroundDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setCornerRadius(8);
        drawable.setColor(mSelectedButtonBackgroundColor);
        return drawable;
    }

    private void updatePageButtons() {

        removeAllViews();

        // Show Previous button if not on the first page

            mPrevButton = createNextPrevButton("Prev", R.drawable.chevron_left_24, Gravity.RIGHT);

            mPrevButton.setOnClickListener(v -> {
                mCurrentPage--;
                updatePageButtons();
                btnClickListener.onClick(v);
            });
            addView(mPrevButton, new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (mCurrentPage > 0) {
            mPrevButton.setEnabled(true);
            mPrevButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
            mPrevButton.setTextColor(Color.WHITE);
        } else {
            mPrevButton.setEnabled(false);
            mPrevButton.setIconTint(ColorStateList.valueOf(Color.GRAY));
            mPrevButton.setTextColor(Color.GRAY);
        }

        // Show up to three page buttons
        int startPage;
        int endPage;

        if (mCurrentPage == 0) {
            startPage = mCurrentPage;
            endPage = Math.min(getPageCount() - 1, startPage + 2);
        } else if (mCurrentPage == getPageCount() - 1) {
            startPage = Math.max(0, mCurrentPage - 2);
            endPage = mCurrentPage;
        } else {
            startPage = Math.max(0, mCurrentPage - 1);
            endPage = Math.min(getPageCount() - 1, startPage + 2);
        }

        mPageButtons = new ViewGroup[endPage - startPage + 1];
        for (int i = 0; i < mPageButtons.length; i++) {
            final int pageIndex = startPage + i;
            mPageButtons[i] = createButton(String.valueOf(pageIndex + 1));
            mPageButtons[i].setOnClickListener(v -> {
                mCurrentPage = pageIndex;
                updatePageButtons();
                btnClickListener.onClick(v);
            });
            addView(mPageButtons[i]);
        }

        // Show Next button if not on the last page

            mNextButton = createNextPrevButton("Next", R.drawable.chevron_right_24, Gravity.LEFT);
            mNextButton.setOnClickListener(v -> {
                mCurrentPage++;
                updatePageButtons();
                btnClickListener.onClick(v);
            });
            addView(mNextButton);

        if (mCurrentPage < getPageCount() - 1) {
            mNextButton.setEnabled(true);
            mNextButton.setIconTint(ColorStateList.valueOf(Color.WHITE));
            mNextButton.setTextColor(Color.WHITE);
        } else {
            mNextButton.setEnabled(false);
            mNextButton.setIconTint(ColorStateList.valueOf(Color.GRAY));
            mNextButton.setTextColor(Color.GRAY);
        }

        // Update button styles based on current page
        for (int i = 0; i < mPageButtons.length; i++) {
            TextView child = ((TextView) mPageButtons[i].getChildAt(0));
            if (i == mCurrentPage - startPage) {
                child.setTextColor(mButtonTextColor);
                child.setBackground(mSelectedButtonBackground);

            } else {
                child.setTextColor(mButtonTextColor);
                child.setBackground(mButtonBackground);
            }
        }
    }

    public void setPageCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Page count must be non-negative");
        }
        totalPages = count;
        //mCurrentPage = 0;
        updatePageButtons();
    }

    public int getPageCount() {
        return totalPages;//getChildCount() - (mPrevButton != null ? 1 : 0) - (mNextButton != null ? 1 : 0);
    }

    public void setButtonTextColor(int color) {
        mButtonTextColor = color;
        updatePageButtons();
    }

    public void setButtonBackgroundColor(int color) {
        mButtonBackgroundColor = color;
        updatePageButtons();
    }

    public void setSelectedButtonBackgroundColor(int color) {
        mSelectedButtonBackgroundColor = color;
        updatePageButtons();
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void setCurrentPage(int page) {
        if (page < 0 || page >= getPageCount()) {
            throw new IllegalArgumentException("Invalid page index");
        }
        mCurrentPage = page;
        updatePageButtons();
    }

    public PageChangeListener getPageChangeListener() {
        return mPageChangeListener;
    }

    public void setPageChangeListener(PageChangeListener mPageChangeListener) {
        this.mPageChangeListener = mPageChangeListener;
    }

    public interface PageChangeListener {
        void onPageChanged(int pageNumber);
    }
}