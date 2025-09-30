package com.sczg.apple.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.facebook.drawee.view.SimpleDraweeView;
import com.sczg.apple.AppContants;
import com.sczg.apple.R;
import com.sczg.apple.base.BaseActivity;
import com.sczg.apple.camrea.CameraNormalManager;
import com.sczg.apple.presenter.PreviewPresenter;
import com.sczg.apple.presenter.viewImpl.IPreviewView;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

@Route(path = AppContants.ARouterUrl.PreviewActivity)
public class PreviewActivity extends BaseActivity<PreviewPresenter, IPreviewView> implements IPreviewView {
    @BindView(R.id.preview_view_group)
    RelativeLayout glViewGroup;
    private CameraNormalManager cameraNormalManager;

    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        cameraNormalManager = CameraNormalManager.getInstance();
        cameraNormalManager.initCamera();
        cameraNormalManager.connectCamera();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cameraNormalManager.preview(bitmap -> new Handler(Looper.getMainLooper()).post(() -> {
            if (glViewGroup != null) {
                glViewGroup.removeAllViews();

                SimpleDraweeView imageView = new SimpleDraweeView(PreviewActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(params);
                imageView.setImageBitmap(bitmap);
                glViewGroup.addView(imageView);
            }
        }));
    }

    @Override
    protected void getBundleExtras(Bundle bundle) {

    }

    @Override
    protected int tellMeLayout() {
        return R.layout.activity_preview;
    }

    @Override
    protected PreviewPresenter initPresenter() {
        return new PreviewPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }

    @Override
    public void onDestroy() {
        cameraNormalManager.unInitCamera();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }
}
