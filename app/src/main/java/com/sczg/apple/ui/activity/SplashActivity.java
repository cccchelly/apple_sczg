package com.sczg.apple.ui.activity;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sczg.apple.AppContants;
import com.sczg.apple.R;
import com.sczg.apple.base.BaseActivity;
import com.sczg.apple.presenter.SplashPresenter;
import com.sczg.apple.presenter.viewImpl.ISplashView;
import com.sczg.apple.runtimepermission.PermissionsManager;
import com.sczg.apple.runtimepermission.PermissionsResultAction;
import com.sczg.apple.utils.AppMsgUtil;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.logger.Logger;

import butterknife.BindView;

@Route(path = AppContants.ARouterUrl.SPLASH_ACTIVITY)
public class SplashActivity extends BaseActivity<SplashPresenter, ISplashView> implements ISplashView {
    private static final String TAG = SplashActivity.class.getName();
    @BindView(R.id.tv_time)
    TextView mTvTime;

    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        requestPermissions();
    }

    @Override
    protected void getBundleExtras(Bundle bundle) {

    }

    @Override
    protected void onResume() {
        Log.i(TAG, "====imei==" + AppMsgUtil.getIMEI(this));
        enterMain();
        super.onResume();
    }

    @Override
    protected int tellMeLayout() {
        return R.layout.activity_splash;
    }

    @Override
    protected SplashPresenter initPresenter() {
        return new SplashPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }

    @Override
    public void enterMain() {
        Logger.d("准备跳转到主界面: %s", AppContants.ARouterUrl.MAIN_ACTIVITY);
        try {
            ARouter.getInstance().build(AppContants.ARouterUrl.MAIN_ACTIVITY)
                    .navigation();
            Logger.d("主界面跳转成功");
        } catch (Exception e) {
            Logger.e(e, "主界面跳转失败");
        }
    }


    @TargetApi(23)
    private void requestPermissions() {
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
                //Toast.makeText(MainActivity.this, "All permissions have been granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(String permission) {
                //VLog.d("permission: "+permission);
                //                Toast.makeText(MainActivity.this, "Permission " + permission + " has been denied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }

}
