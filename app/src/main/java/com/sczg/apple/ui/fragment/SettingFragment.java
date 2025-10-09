package com.sczg.apple.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    @BindView(R.id.tv_capture_delay_show)
    TextView tvCaptureDelayShow;
    @BindView(R.id.btn_set_capture_delay)
    LinearLayout btnSetCaptureDelay;
    private TimePickerView timePickerView;
    private long time = 0;

    @Override
    protected void fetchData() {
    }

    @Override
    protected void init(View view, @Nullable Bundle savedInstanceState) {
        initTimeDialog();
        setTimeShow();
        setCaptureDelayShow();
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
            return;
        }
        tvLocationShow.setText("设备位置：" + lat + "," + lng);
    }

    /**
     * 设置抓图延迟时间显示
     */
    private void setCaptureDelayShow() {
        int delaySeconds = ShareUtil.getCaptureDelaySeconds();
        tvCaptureDelayShow.setText("抓图延迟：" + delaySeconds + "秒");
    }

    /**
     * 显示抓图延迟时间设置对话框
     */
    private void showCaptureDelayDialog() {
        if (getActivity() == null) {
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("设置抓图延迟时间");
        
        // 创建输入框
        final EditText input = new EditText(getActivity());
        input.setHint("请输入延迟时间（秒）");
        input.setText(String.valueOf(ShareUtil.getCaptureDelaySeconds()));
        input.setSelection(input.getText().length()); // 光标移到末尾
        builder.setView(input);
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            String delayStr = input.getText().toString().trim();
            if (delayStr.isEmpty()) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "请输入延迟时间", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            
            try {
                 int delaySeconds = Integer.parseInt(delayStr);
                 if (delaySeconds < 1 || delaySeconds > 300) {
                     if (getActivity() != null) {
                         Toast.makeText(getActivity(), "延迟时间范围：1-300秒", Toast.LENGTH_SHORT).show();
                     }
                     return;
                 }
                
                // 保存设置
                ShareUtil.saveCaptureDelaySeconds(delaySeconds);
                setCaptureDelayShow();
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "设置成功", Toast.LENGTH_SHORT).show();
                }
                
            } catch (NumberFormatException e) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void initTimeDialog() {
        if (getActivity() == null) {
            return;
        }
        
        timePickerView = new TimePickerBuilder(getActivity(), (date, v) -> {
            time = TimeUtils.date2Millis(date);
            TaskTimeUtil.saveTaskTime(time);
            setTimeShow();
            if (timePickerView != null) {
                timePickerView.dismiss();
            }
        }).setType(new boolean[]{false, false, false, true, true, false})// 显示时分
                .build();
    }

    @OnClick({R.id.set_capture_task_btn_choose_time, R.id.capture_task_btn_refresh, R.id.btn_refresh_location, R.id.btn_set_capture_delay})
    public void onViewClicked(View view) {
        int id = view.getId();
        if (id == R.id.set_capture_task_btn_choose_time) {
            if (tvChooseTime != null) {
                tvChooseTime.startAnimation(AnimUtil.alphHalf2All());
            }
            if (timePickerView != null) {
                timePickerView.show();
            }
        } else if (id == R.id.capture_task_btn_refresh) {
            if (tvRefresh != null) {
                tvRefresh.startAnimation(AnimUtil.alphHalf2All());
            }
            setTimeShow();
        } else if (id == R.id.btn_refresh_location) {
            if (tvRefreshLocation != null) {
                tvRefreshLocation.startAnimation(AnimUtil.alphHalf2All());
            }
            setLocationShow();
        } else if (id == R.id.btn_set_capture_delay) {
            if (btnSetCaptureDelay != null) {
                btnSetCaptureDelay.startAnimation(AnimUtil.alphHalf2All());
            }
            showCaptureDelayDialog();
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
        if (enter && getActivity() != null) {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.activity_anim_in);
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
    }

}
