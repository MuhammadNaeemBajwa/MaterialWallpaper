package com.app.materialwallpaper.view;

import static com.app.materialwallpaper.view.MWPopupWindow.HorizontalPosition.CENTER;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.materialwallpaper.R;
import com.skydoves.expandablelayout.ExpandableLayout;


public class CustomFilterDropDown extends FrameLayout {
    private OnItemChangedListener onItemChangedListener;

    public CustomFilterDropDown(@NonNull Context context) {
        super(context);
        init();
    }

    public CustomFilterDropDown(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomFilterDropDown(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CustomFilterDropDown(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.custom_dropdown, this);


        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.premium_free_dropdown, null);
        MWPopupWindow mwPopupWindow = new MWPopupWindow(popupView);
        ExpandableLayout expandableLayout = findViewById(R.id.expandable);
        mwPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mwPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);

        expandableLayout.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> mwPopupWindow.setWidth(right - left));
        expandableLayout.bringToFront();
        expandableLayout.setOnClickListener(v -> {
            if (expandableLayout.isExpanded()) {
                mwPopupWindow.dismiss();
                expandableLayout.collapse();
            } else {
                mwPopupWindow.showOnAnchor(v, MWPopupWindow.VerticalPosition.ALIGN_TOP, CENTER, 0, -5, false);
                expandableLayout.expand();
            }
        });

        View free = popupView.findViewById(R.id.itemFree);
        View premium = popupView.findViewById(R.id.itemPremium);
        View buy = popupView.findViewById(R.id.itemBuy);
        TextView txtFilter = expandableLayout.findViewById(R.id.txtFilter);
        View all = popupView.findViewById(R.id.itemAll);
        all.setOnClickListener((v) -> {
            expandableLayout.collapse();
            mwPopupWindow.dismiss();
            String text = ((TextView) v).getText().toString();
            txtFilter.setText(text);
            if (onItemChangedListener != null) {
                onItemChangedListener.onItemChanged(text);
            }
        });
        free.setOnClickListener((v) -> {
            expandableLayout.collapse();
            mwPopupWindow.dismiss();
            String text = ((TextView) v).getText().toString();
            txtFilter.setText(text);
            if (onItemChangedListener != null) {
                onItemChangedListener.onItemChanged(text);
            }
        });
        premium.setOnClickListener((v) -> {
            expandableLayout.collapse();
            mwPopupWindow.dismiss();
            String text = ((TextView) v).getText().toString();
            txtFilter.setText(text);
            if (onItemChangedListener != null) {
                onItemChangedListener.onItemChanged(text);
            }
        });
        buy.setOnClickListener((v) -> {
            expandableLayout.collapse();
            mwPopupWindow.dismiss();

            if (onItemChangedListener != null) {
                onItemChangedListener.onBuySelected();
            }

        });


    }

    public void setOnItemChangedListener(OnItemChangedListener onItemChangedListener) {
        this.onItemChangedListener = onItemChangedListener;
    }

    public interface OnItemChangedListener {
        void onItemChanged(String text);

        void onBuySelected();
    }
}


