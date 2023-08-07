package com.app.materialwallpaper.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.materialwallpaper.R;
import com.app.materialwallpaper.databases.prefs.SharedPref;
import com.app.materialwallpaper.models.Menu;
import com.app.materialwallpaper.utils.Constant;

import java.util.List;

public class AdapterMenu extends RecyclerView.Adapter<AdapterMenu.ViewHolder> {

    private List<Menu> items;
    Context context;
    private OnItemClickListener mOnItemClickListener;
    private int clickedItemPosition = -1;
    SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Menu obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterMenu(Context context, List<Menu> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView menuTitle;
        public ImageView menuIcon;
        public LinearLayout lytItem;
        public LinearLayout lytParent;

        public ViewHolder(View v) {
            super(v);
            menuTitle = v.findViewById(R.id.menu_title);
            menuIcon = v.findViewById(R.id.menu_icon);
            lytItem = v.findViewById(R.id.lyt_item);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sort, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"RecyclerView", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Menu obj = items.get(position);

        holder.menuTitle.setText(obj.menu_title);

        holder.lytParent.setOnClickListener(view -> {
            new Handler().postDelayed(()-> {
                Constant.LAST_SELECTED_ITEM_POSITION = position;
                clickedItemPosition = Constant.LAST_SELECTED_ITEM_POSITION;
                notifyDataSetChanged();

                switch (obj.menu_order) {
                    case "recent":
                        Constant.ORDER = Constant.ORDER_RECENT;
                        break;
                    case "featured":
                        Constant.ORDER = Constant.ORDER_FEATURED;
                        break;
                    case "popular":
                        Constant.ORDER = Constant.ORDER_POPULAR;
                        break;
                    case "random":
                        Constant.ORDER = Constant.ORDER_RANDOM;
                        break;
                    case "live":
                        Constant.ORDER = Constant.ORDER_LIVE;
                        break;
                    default:
                        Constant.ORDER = Constant.ORDER_DEFAULT;
                        break;
                }

                switch (obj.menu_filter) {
                    case "wallpaper":
                        Constant.FILTER = Constant.FILTER_WALLPAPER;
                        break;
                    case "live":
                        Constant.FILTER = Constant.FILTER_LIVE;
                        break;
                    case "both":
                        Constant.FILTER = Constant.FILTER_ALL;
                        break;
                    default:
                        Constant.FILTER = Constant.FILTER_DEFAULT;
                        break;
                }
            }, 200);

        });

        if (clickedItemPosition == position) {
            holder.menuTitle.setTextColor(ContextCompat.getColor(context, R.color.color_light_primary));
            holder.menuIcon.setImageResource(R.drawable.ic_radio_button_on);
            holder.menuIcon.setColorFilter(context.getResources().getColor(R.color.color_light_primary), PorterDuff.Mode.SRC_IN);
        } else {
            holder.menuIcon.setImageResource(R.drawable.ic_radio_button_off);
            if (sharedPref.getIsDarkTheme()) {
                holder.menuTitle.setTextColor(ContextCompat.getColor(context, R.color.color_dark_text_default));
                holder.menuIcon.setColorFilter(context.getResources().getColor(R.color.color_dark_text_default), PorterDuff.Mode.SRC_IN);
            } else {
                holder.menuTitle.setTextColor(ContextCompat.getColor(context, R.color.color_light_text_default));
                holder.menuIcon.setColorFilter(context.getResources().getColor(R.color.color_light_text_default), PorterDuff.Mode.SRC_IN);
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void setListData(List<Menu> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resetListData() {
        this.items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}