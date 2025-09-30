package com.sczg.apple.ui.fragment;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.sczg.apple.AppContants;
import com.sczg.apple.R;
import com.sczg.apple.base.BaseFragment;
import com.sczg.apple.camrea.CameraNormalManager;
import com.sczg.apple.camrea.CaptureListener;
import com.sczg.apple.presenter.TakePhotoPresenter;
import com.sczg.apple.presenter.viewImpl.ITakePhotoView;
import com.sczg.apple.utils.AnimUtil;
import com.sczg.apple.utils.CapturePostUtil;
import com.sczg.apple.utils.LogUtil;
import com.sczg.apple.utils.TaskServiceUtil;
import com.sczg.apple.utils.ToastUtils;

import java.io.File;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class TakePhotoFragment extends BaseFragment<TakePhotoPresenter, ITakePhotoView> implements ITakePhotoView {
    @BindView(R.id.takephoto_tv_capture)
    LinearLayout mTvCapture;
    @BindView(R.id.takephoto_tv_post)
    LinearLayout mTvPost;
    Unbinder unbinder;

    private CameraNormalManager cameraNormalManager;

    @BindView(R.id.control_tv_preview)
    LinearLayout mTvPreview;
    @BindView(R.id.reset_capture_task)
    LinearLayout mTvReset;

    @Override
    protected void fetchData() {

    }

    @Override
    protected void init(View view, @Nullable Bundle savedInstanceState) {
        cameraNormalManager = CameraNormalManager.getInstance();
        setCaptureListener();
    }

    private void setCaptureListener() {
        cameraNormalManager.setOnCaptureListener(new CaptureListener() {
            @Override
            public void onCaptureFinish(Bitmap bitmap, File file, String name) {
                LogUtil.i("截图成功");
                showCapture(bitmap);
            }
        });
    }

    public void showCapture(final Bitmap bitmap) {
        // 直接显示照片
        final DialogPlus dialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(R.layout.show_pic_imageview))
                .setCancelable(true)
                .setGravity(Gravity.CENTER)
                .setOverlayBackgroundResource(Color.TRANSPARENT)
                .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .create();
        getActivity().runOnUiThread(() -> {
            dialogPlus.show();
            ImageView imageView = dialogPlus.getHolderView().findViewById(R.id.show_pic_imageview);
            imageView.setOnClickListener(v -> dialogPlus.dismiss());
            imageView.setImageBitmap(bitmap);
        });
    }

    @Override
    protected int tellMeLayout() {
        return R.layout.fragment_take_photo;
    }

    @OnClick({R.id.takephoto_tv_post, R.id.control_tv_preview, R.id.takephoto_tv_capture, R.id.reset_capture_task})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.control_tv_preview:
                mTvPreview.startAnimation(AnimUtil.alphHalf2All());
                ARouter.getInstance()
                        .build(AppContants.ARouterUrl.PreviewActivity)
                        .navigation();
                break;
            case R.id.takephoto_tv_post:
                mTvPost.startAnimation(AnimUtil.alphHalf2All());
                new Thread(CapturePostUtil::findLocalPic).start();
                break;
            case R.id.takephoto_tv_capture: //执行直接拍照
                takePhoto();
                break;
            case R.id.reset_capture_task:
                mTvReset.startAnimation(AnimUtil.alphHalf2All());
                TaskServiceUtil.resetPhotoTasks();
                ToastUtils.showToast("拍照定时任务重启");
                break;
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

    @Override
    protected TakePhotoPresenter initPresenter() {
        return new TakePhotoPresenter();
    }

    @Override
    protected void onRetryListener() {

    }

    @Override
    protected View getStatusTargetView() {
        return null;
    }

    private void takePhoto() {
        mTvCapture.startAnimation(AnimUtil.alphHalf2All());
        new Thread(() -> {
            cameraNormalManager.unInitCamera();
            cameraNormalManager.initCamera();
            cameraNormalManager.connectCamera();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cameraNormalManager.captureExecute(AppContants.CaptureSource.CAPTURE_SOURCE_HAND);
            try {
                Thread.sleep(20 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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
