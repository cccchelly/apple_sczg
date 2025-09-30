package com.sczg.apple.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sczg.apple.R;
import com.sczg.apple.http.loading.NetLoadingHelper;
import com.sczg.apple.utils.ActivityBrightnessManager;
import com.sczg.apple.utils.ToastUtils;
import com.sczg.apple.widget.VaryViewHelper;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.Disposable;

public abstract class BaseActivity<T extends BasePresenter<V>,V extends BaseMvpView> extends AppCompatActivity implements IBaseView {

    private   VaryViewHelper        mVaryViewHelper;
    protected Activity              mActivity;
    protected T                     mPresenter;
    private   Unbinder              mUnbinder;
    private NetLoadingHelper mNetLoadingHelper;


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //全屏模式
      /*  if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }*/
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;
        setContentView(tellMeLayout());

        mPresenter = initPresenter();
        if (mPresenter != null) {
            mPresenter.attach((V) this);
        }
        mUnbinder = ButterKnife.bind(this);

        //bundle
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            getBundleExtras(bundle);
        }

        mNetLoadingHelper = new NetLoadingHelper(this);
        if (getStatusTargetView() != null) {
            mVaryViewHelper = new VaryViewHelper.Builder()
                    .setDataView(getStatusTargetView())//如果根部局无效，套一层父布局即可
                    //                    .setLoadingView(LayoutInflater.from(mContext).inflate(R.layout.layout_loadingview, null))
                    .setEmptyView(LayoutInflater.from(mActivity).inflate(R.layout.layout_emptyview, null))
                    .setErrorView(LayoutInflater.from(mActivity).inflate(R.layout.layout_errorview, null))
                    .setRefreshListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onRetryListener();
                        }
                    })
                    .build();
        }

        init(savedInstanceState);
    }

    /**
     * 初始化方法
     */
    protected abstract void init(@Nullable Bundle savedInstanceState);

    /**
     * 传递bundle数据
     * @param bundle
     */
    protected abstract void getBundleExtras(Bundle bundle);

    /**
     * 布局
     * @return
     */
    protected abstract int tellMeLayout();

    protected abstract T initPresenter();

    protected T getPresenter() {
        if (mPresenter == null) {
            throw new RuntimeException("presenter cannot be initialized!");
        }
        return mPresenter;
    }


    /**
     * 点击错误页面重新加载数据
     */
    protected  abstract void onRetryListener();

    /**
     *
     * @return
     */
    protected  abstract View getStatusTargetView();

    @Override
    public void addDisposable(Disposable disposable) {
        mPresenter.addDisposable(disposable);
    }

    public void startActivityWithAnim(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.activity_anim_in,R.anim.activity_anim_stay);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_anim_stay,R.anim.activity_anim_out);
    }


    @Override
    public void showLoadingView(String showText){
        mNetLoadingHelper.showLoadingView(showText);
    }


    @Override
    public void dissmissLoadingView(){
        mNetLoadingHelper.dissLoadingView();
    }

    @Override
    public void showErrorView(){
        dissmissLoadingView();
        if (mVaryViewHelper != null) {
            mVaryViewHelper.showErrorView();
        }
    }

    @Override
    public void showEmptyView(){
        dissmissLoadingView();
        if (mVaryViewHelper != null) {
            mVaryViewHelper.showEmptyView();
        }
    }

    @Override
    public void showDataView(){
        dissmissLoadingView();
        if (mVaryViewHelper != null) {
            mVaryViewHelper.showDataView();
        }
    }

    @Override
    public void showException(String msg) {
        ToastUtils.showToast(msg);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {

        if (mVaryViewHelper != null){
            mVaryViewHelper.releaseVaryView();
            mVaryViewHelper = null;
        }

        if (mNetLoadingHelper != null) {
            mNetLoadingHelper.releaseView();
            mNetLoadingHelper = null;
        }

        if (mPresenter != null) {
            mPresenter.unDisposable();
            mPresenter.detachView();
            mPresenter = null;
        }
        mUnbinder.unbind();
        mActivity = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) { //屏幕被点击刷新最后点击时间并点亮屏幕
        ActivityBrightnessManager.setScreenBrightness(255);
        return super.dispatchTouchEvent(ev);
    }

}
