package com.sczg.apple.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sczg.apple.App;
import com.sczg.apple.R;
import com.sczg.apple.http.loading.NetLoadingHelper;
import com.sczg.apple.utils.ToastUtils;
import com.sczg.apple.widget.VaryViewHelper;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.Disposable;

public abstract class BaseFragment <T extends BasePresenter<V>,V extends BaseMvpView> extends Fragment implements IBaseView{

    private   VaryViewHelper        mVaryViewHelper;
    protected Context               mContext;
    protected T                     mPresenter;
    private   Unbinder              mUnbinder;
    private NetLoadingHelper mNetLoadingHelper;
    protected boolean isViewInitiated;
    protected boolean isVisibleToUser;
    protected boolean isDataInitiated;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(tellMeLayout(), container,false);
        mUnbinder = ButterKnife.bind(this,view);

        mPresenter = initPresenter();
        if (mPresenter != null) {
            mPresenter.attach((V) this);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mNetLoadingHelper = new NetLoadingHelper(mContext);
        if (getStatusTargetView() != null) {
            mVaryViewHelper = new VaryViewHelper.Builder()
                    .setDataView(getStatusTargetView())//如果根部局无效，套一层父布局即可
                    //                    .setLoadingView(LayoutInflater.from(mContext).inflate(R.layout.layout_loadingview, null))
                    .setEmptyView(LayoutInflater.from(mContext).inflate(R.layout.layout_emptyview, null))
                    .setErrorView(LayoutInflater.from(mContext).inflate(R.layout.layout_errorview, null))
                    .setRefreshListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onRetryListener();
                        }
                    })
                    .build();
        }

        init(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isViewInitiated = true;
        prepareFetchData();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        prepareFetchData();
    }

    protected abstract void fetchData();

    protected boolean prepareFetchData() {
        return prepareFetchData(false);
    }

    /**
     * 预留强制刷新数据
     * @param forceUpdate true 强制刷新
     * @return
     */
    private boolean prepareFetchData(boolean forceUpdate) {
        if (isVisibleToUser && isViewInitiated && (!isDataInitiated || forceUpdate)) {
            fetchData();
            isDataInitiated = true;
            return true;
        }
        return false;
    }

    /**
     * 初始化方法
     */
    protected abstract void init(View view,@Nullable Bundle savedInstanceState);

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
    public void onDestroyView() {

        if (mVaryViewHelper != null){
            mVaryViewHelper.releaseVaryView();
            mVaryViewHelper = null;
        }

        if (mNetLoadingHelper != null) {
            mNetLoadingHelper.releaseView();
            mNetLoadingHelper = null;
        }

        mUnbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {

        if (mPresenter != null) {
            mPresenter.unDisposable();
            mPresenter.detachView();
            mPresenter = null;
        }
        mContext = null;
        super.onDestroy();
    }
}
