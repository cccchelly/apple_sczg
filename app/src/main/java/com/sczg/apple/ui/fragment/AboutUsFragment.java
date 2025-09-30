package com.sczg.apple.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sczg.apple.R;
import com.sczg.apple.base.BaseFragment;
import com.sczg.apple.presenter.AboutPresenter;
import com.sczg.apple.presenter.viewImpl.IAboutView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AboutUsFragment extends BaseFragment<AboutPresenter, IAboutView> implements IAboutView {
    Unbinder unbinder;

    @Override
    protected void fetchData() {

    }

    @Override
    protected void init(View view, @Nullable Bundle savedInstanceState) {
        //mImei.setText("设备号："+AppMsgUtil.getIMEI(getActivity()));
    }

    @Override
    protected int tellMeLayout() {
        return R.layout.fragment_about;
    }

    @Override
    protected AboutPresenter initPresenter() {
        return null;
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            return AnimationUtils.loadAnimation(getActivity(), R.anim.activity_anim_in);
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
