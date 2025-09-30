package com.sczg.apple.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.sczg.apple.R;
import com.sczg.apple.base.BaseFragment;
import com.sczg.apple.presenter.SettingPresenter;
import com.sczg.apple.presenter.viewImpl.ISettingView;
import com.sczg.apple.utils.AnimUtil;
import com.sczg.apple.utils.ShareUtil;
import com.sczg.apple.utils.TaskTimeUtil;
import com.sczg.apple.utils.TimeUtils;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.OnClick;

public class SettingFragment extends BaseFragment<SettingPresenter, ISettingView> implements ISettingView {
    @BindView(R.id.capture_task_tv_show_time)
    TextView tvTimeShow;
    @BindView(R.id.tv_location_show)
    TextView tvLocationShow;
    @BindView(R.id.set_capture_task_btn_choose_time)
    LinearLayout tvChooseTime;
    @BindView(R.id.capture_task_btn_refresh)
    LinearLayout tvRefresh;
    @BindView(R.id.btn_refresh_location)
    LinearLayout tvRefreshLocation;
    private TimePickerView timePickerView;
    private long time = 0;

    @Override
    protected void fetchData() {
    }

    @Override
    protected void init(View view, @Nullable Bundle savedInstanceState) {
        initTimeDialog();
        setTimeShow();
    }

    private void setTimeShow() {
        String time = ShareUtil.getTaskTime();
        tvTimeShow.setText("任务时间：" + time);
    }

    private void setLocationShow() {
        float lat = ShareUtil.getLocationLat();
        float lng = ShareUtil.getLocationLng();
        if (lat == 0 || lng == 0) {
            tvLocationShow.setText("");
        }
        tvLocationShow.setText("设备位置：" + lat + "," + lng);
    }

    private void initTimeDialog() {
        timePickerView = new TimePickerBuilder(getActivity(), (date, v) -> {
            time = TimeUtils.date2Millis(date);
            TaskTimeUtil.saveTaskTime(time);
            setTimeShow();
            timePickerView.dismiss();
        }).setType(new boolean[]{false, false, false, true, true, false})// 显示时分
                .build();
    }

    @OnClick({R.id.set_capture_task_btn_choose_time, R.id.capture_task_btn_refresh, R.id.btn_refresh_location})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.set_capture_task_btn_choose_time:
                tvChooseTime.startAnimation(AnimUtil.alphHalf2All());
                timePickerView.show();
                break;
            case R.id.capture_task_btn_refresh:
                tvRefresh.startAnimation(AnimUtil.alphHalf2All());
                setTimeShow();
                break;
            case R.id.btn_refresh_location:
                tvRefreshLocation.startAnimation(AnimUtil.alphHalf2All());
                setLocationShow();
                break;
        }
    }

    @Override
    protected int tellMeLayout() {
        return R.layout.fragment_setting;
    }

    @Override
    protected SettingPresenter initPresenter() {
        return new SettingPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.activity_anim_in);
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
    }

}
