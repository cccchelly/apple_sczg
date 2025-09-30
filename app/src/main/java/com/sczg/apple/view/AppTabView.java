package com.sczg.apple.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sczg.apple.R;

import java.util.ArrayList;

public class AppTabView extends FrameLayout {

    private int selectPosition = -1;
    private OnSelectedChangeListener mListener;

    public AppTabView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public AppTabView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AppTabView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    ArrayList<View> views = new ArrayList<>();

    public void initView(Context context) {
        View view = View.inflate(context, R.layout.left_tab_view, this);
        LinearLayout ll_device_controller = view.findViewById(R.id.ll_device_controller);
        LinearLayout ll_task_setting = view.findViewById(R.id.ll_task_setting);
        // LinearLayout ll_about_us = view.findViewById(R.id.ll_about_us);

        views.add(ll_device_controller);
        views.add(ll_task_setting);
        // views.add(ll_about_us);

        for (int i = 0; i < views.size(); i++) {
            View view1 = views.get(i);
            final int finalI = i;
            view1.setOnClickListener(v -> setSelectPosition(finalI));
        }

    }

    public void setSelectPosition(int position) {
        if (position == selectPosition) return;
        selectPosition = position;

        for (View view : views) {
            view.setBackgroundColor(Color.TRANSPARENT);
        }
        views.get(position).setBackgroundColor(getResources().getColor(R.color.black_50));

        if (mListener != null) {
            mListener.onSelectedPosition(views.get(position), selectPosition);
        }
    }

    public interface OnSelectedChangeListener {
        void onSelectedPosition(View view, int position);
    }

    public void setOnSelectedChangeListener(OnSelectedChangeListener listener) {
        mListener = listener;
    }

    public int getSelectPosition() {
        return selectPosition;
    }
}
