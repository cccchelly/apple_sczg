package com.sczg.apple.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.sczg.apple.App;
import com.sczg.apple.AppContants;
import com.sczg.apple.R;
import com.sczg.apple.base.BaseActivity;
import com.sczg.apple.presenter.MainPresenter;
import com.sczg.apple.presenter.viewImpl.IMainView;
import com.sczg.apple.ui.fragment.AboutUsFragment;
import com.sczg.apple.ui.fragment.SettingFragment;
import com.sczg.apple.ui.fragment.TakePhotoFragment;
import com.sczg.apple.utils.ActivityBrightnessManager;
import com.sczg.apple.utils.TaskServiceUtil;
import com.sczg.apple.view.AppTabView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.OnClick;

@Route(path = AppContants.ARouterUrl.MAIN_ACTIVITY)
public class MainActivity extends BaseActivity<MainPresenter, IMainView> implements IMainView {
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1001;

    @BindView(R.id.left_tab_view)
    AppTabView mAppTabView;
    @BindView(R.id.fl_container)
    FrameLayout mFlContainer;
    @BindView(R.id.main_tv_title)
    TextView mTvTitle;
    @BindView(R.id.main_rl_topbar)
    RelativeLayout mRlTopbar;
    private TakePhotoFragment mTakePhotoFragment;
    private SettingFragment mSettingFragment;
    private AboutUsFragment mAboutUsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        App.setMainActivity(this);
        checkLocationPermission();
        initFragment(savedInstanceState);
        TaskServiceUtil.resetTasks();
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION);
        }
    }


    private void initListener() {
        mAppTabView.setOnSelectedChangeListener((view, position) -> {
            FragmentTransaction transaction = MainActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            switch (position) {
                case 0:
                    transaction.show(mTakePhotoFragment)
                            .hide(mSettingFragment)
                            .hide(mAboutUsFragment)
                            .commitAllowingStateLoss();
                    break;
                case 1:
                    transaction.hide(mTakePhotoFragment)
                            .show(mSettingFragment)
                            .hide(mAboutUsFragment)
                            .commitAllowingStateLoss();
                    break;
                case 2:
                    transaction.hide(mTakePhotoFragment)
                            .hide(mSettingFragment)
                            .show(mAboutUsFragment)
                            .commitAllowingStateLoss();
                    break;
                default:
            }
        });
    }

    private void initFragment(Bundle savedInstanceState) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        int currentTabPosition = 0;
        if (savedInstanceState != null) {
            mTakePhotoFragment = (TakePhotoFragment) getSupportFragmentManager().findFragmentByTag("TakePhotoFragment");
            mSettingFragment = (SettingFragment) getSupportFragmentManager().findFragmentByTag("SettingFragment");
            mAboutUsFragment = (AboutUsFragment) getSupportFragmentManager().findFragmentByTag("AboutUsFragment");
            currentTabPosition = savedInstanceState.getInt(AppContants.HOME_TAB_INDEX);
        } else {
            mTakePhotoFragment = new TakePhotoFragment();
            mSettingFragment = new SettingFragment();
            mAboutUsFragment = new AboutUsFragment();
            transaction.add(R.id.fl_container, mTakePhotoFragment, "TakePhotoFragment");
            transaction.add(R.id.fl_container, mSettingFragment, "SettingFragment");
            transaction.add(R.id.fl_container, mAboutUsFragment, "AboutUsFragment");
        }
        transaction.commit();

        initListener();
        mAppTabView.setSelectPosition(currentTabPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAppTabView != null) {
            outState.putInt(AppContants.HOME_TAB_INDEX, mAppTabView.getSelectPosition());
        }
    }

    @Override
    protected void getBundleExtras(Bundle bundle) {

    }

    @Override
    protected int tellMeLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected MainPresenter initPresenter() {
        return new MainPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }


    @Override
    public Activity getActivity() {
        return this;
    }

    @OnClick({R.id.main_tv_title})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.main_tv_title:
                ActivityBrightnessManager.setScreenBrightness(0);
                break;
        }
    }
}
