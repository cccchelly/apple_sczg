package com.sczg.apple.ui.fragment;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.sczg.apple.AppContants;
import com.sczg.apple.R;
import com.sczg.apple.base.BaseFragment;
import com.sczg.apple.camrea.CameraNormalManager;
import com.sczg.apple.camrea.CameraInitListener;
import com.sczg.apple.camrea.CaptureListener;
import com.sczg.apple.presenter.TakePhotoPresenter;
import com.sczg.apple.presenter.viewImpl.ITakePhotoView;
import com.sczg.apple.utils.AnimUtil;
import com.sczg.apple.utils.BitmapCompressUtil;
import com.sczg.apple.utils.CapturePostUtil;
import com.sczg.apple.utils.LogUtil;
import com.sczg.apple.utils.ShareUtil;
import com.sczg.apple.utils.TaskServiceUtil;
import com.sczg.apple.utils.ToastUtils;
import com.orhanobut.logger.Logger;

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
    private DialogPlus progressDialog;
    private ProgressBar progressBar;
    private TextView progressText;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

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
        setCameraInitListener();
    }

    private void setCaptureListener() {
        cameraNormalManager.setOnCaptureListener(new CaptureListener() {
            @Override
            public void onCaptureFinish(Bitmap bitmap, File file, String name) {
                LogUtil.i("截图成功，收到回调");
                // 添加Toast提示确认回调执行
                mainHandler.post(() -> {
                    ToastUtils.showToast("截图完成！");
                });
                
                if (bitmap != null) {
                    LogUtil.i("bitmap不为空，尺寸: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                } else {
                    LogUtil.e("bitmap为空！");
                }
                hideProgressDialog();
                if (bitmap != null) {
                    showCapture(bitmap);
                } else {
                    LogUtil.e("无法显示图片：bitmap为空");
                }
            }

            @Override
            public void onCaptureStart(long totalDelayMs) {
                LogUtil.i("开始抓图，总延迟时间: " + totalDelayMs + "ms");
                showProgressDialog(totalDelayMs);
            }

            @Override
            public void onCaptureProgress(long remainingMs, long totalDelayMs) {
                updateProgress(remainingMs, totalDelayMs);
            }
            
            @Override
            public void onCaptureProcessing() {
                LogUtil.i("倒计时完毕，正在处理截图");
                mainHandler.post(() -> {
                    ToastUtils.showToast("正在保存图片，请稍候...");
                });
                // 更新进度对话框文本
                updateProgressText("正在保存图片...");
            }
        });
    }

    /**
     * 设置相机初始化监听器
     * 提供详细的相机初始化过程反馈
     */
    private void setCameraInitListener() {
        cameraNormalManager.setOnCameraInitListener(new CameraInitListener() {
            @Override
            public void onInitStart() {
                LogUtil.i("相机初始化开始");
                mainHandler.post(() -> {
                    ToastUtils.showToast("正在初始化相机...");
                });
            }

            @Override
            public void onDeviceEnumerated(int deviceCount) {
                LogUtil.i("设备枚举完成，发现设备数量: " + deviceCount);
                mainHandler.post(() -> {
                    if (deviceCount > 0) {
                        ToastUtils.showToast("发现 " + deviceCount + " 个相机设备");
                    } else {
                        ToastUtils.showToast("未发现相机设备");
                    }
                });
            }

            @Override
            public void onDeviceConnecting() {
                LogUtil.i("开始连接相机设备");
                mainHandler.post(() -> {
                    ToastUtils.showToast("正在连接相机设备...");
                });
            }

            @Override
            public void onDeviceConnected() {
                LogUtil.i("相机设备连接成功");
                mainHandler.post(() -> {
                    ToastUtils.showToast("相机设备连接成功");
                });
            }

            @Override
            public void onDeviceConnectFailed(String error) {
                LogUtil.e("相机设备连接失败: " + error);
                mainHandler.post(() -> {
                    ToastUtils.showToast("相机连接失败: " + error);
                });
            }

            @Override
            public void onStartGrabbing() {
                LogUtil.i("开始获取图像流");
                mainHandler.post(() -> {
                    ToastUtils.showToast("正在启动图像流...");
                });
            }

            @Override
            public void onFirstFrameReceived() {
                LogUtil.i("获取到第一帧图像");
                mainHandler.post(() -> {
                    ToastUtils.showToast("获取到第一帧图像");
                });
            }

            @Override
            public void onInitCompleted() {
                LogUtil.i("相机初始化完成，设备就绪");
                mainHandler.post(() -> {
                    ToastUtils.showToast("相机初始化完成，设备就绪！");
                });
            }
        });
    }

    /**
     * 显示进度对话框
     * @param totalDelayMs 总延迟时间
     */
    private void showProgressDialog(long totalDelayMs) {
        mainHandler.post(() -> {
            if (progressDialog == null) {
                progressDialog = DialogPlus.newDialog(getContext())
                        .setContentHolder(new ViewHolder(R.layout.dialog_capture_progress))
                        .setCancelable(false)
                        .setGravity(Gravity.CENTER)
                        .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                        .setContentWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                        .create();
            }
            
            if (!progressDialog.isShowing()) {
                progressDialog.show();
                progressBar = progressDialog.getHolderView().findViewById(R.id.progress_bar);
                progressText = progressDialog.getHolderView().findViewById(R.id.tv_progress_text);
                
                // 初始化进度
                progressBar.setMax(100);
                progressBar.setProgress(0);
                progressText.setText("准备中...剩余" + (totalDelayMs / 1000) + "秒");
            }
        });
    }

    /**
     * 隐藏进度对话框
     */
    private void hideProgressDialog() {
        mainHandler.post(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }

    /**
     * 更新进度
     * @param remainingMs 剩余时间
     * @param totalDelayMs 总延迟时间
     */
    private void updateProgress(long remainingMs, long totalDelayMs) {
        mainHandler.post(() -> {
            if (progressBar != null && progressText != null) {
                int progress = (int) ((totalDelayMs - remainingMs) * 100 / totalDelayMs);
                progressBar.setProgress(progress);
                progressText.setText("抓图中...剩余" + (remainingMs / 1000) + "秒");
            }
        });
    }
    
    /**
     * 更新进度对话框文本
     * @param text 要显示的文本
     */
    private void updateProgressText(String text) {
        mainHandler.post(() -> {
            if (progressText != null) {
                progressText.setText(text);
                // 将进度条设置为100%，表示倒计时完成，正在处理
                if (progressBar != null) {
                    progressBar.setProgress(100);
                }
            }
        });
    }

    public void showCapture(final Bitmap bitmap) {
        LogUtil.i("showCapture被调用，bitmap是否为空: " + (bitmap == null));
        if (bitmap == null) {
            LogUtil.e("showCapture: bitmap为空，无法显示");
            return;
        }
        
        // 确保在主线程中执行UI操作
        mainHandler.post(() -> {
            try {
                // 检查Fragment是否已附加到Activity
                if (getContext() == null || getActivity() == null || !isAdded()) {
                    LogUtil.e("Fragment未附加到Activity，无法显示对话框");
                    return;
                }
                
                LogUtil.i("开始创建图片显示对话框，Context: " + getContext());
                
                // 检查并压缩图片以避免GPU纹理限制
                Bitmap displayBitmap = bitmap;
                if (BitmapCompressUtil.isExceedTextureLimit(bitmap)) {
                    LogUtil.i("图片尺寸超出GPU限制，开始压缩处理");
                    displayBitmap = BitmapCompressUtil.compressBitmapForDisplay(bitmap);
                    if (displayBitmap == null) {
                        LogUtil.e("图片压缩失败，使用原始图片");
                        displayBitmap = bitmap;
                    } else {
                        LogUtil.i("图片压缩成功，新尺寸: " + displayBitmap.getWidth() + "x" + displayBitmap.getHeight());
                    }
                } else {
                    LogUtil.i("图片尺寸在GPU限制内，无需压缩");
                }
                
                // 创建ImageView并设置压缩后的图片
                ImageView imageView = new ImageView(getContext());
                imageView.setImageBitmap(displayBitmap);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setAdjustViewBounds(true);
                
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                builder.setTitle("截图结果")
                       .setView(imageView)
                       .setPositiveButton("确定", (dialog, which) -> {
                           LogUtil.i("用户点击确定，关闭对话框");
                           dialog.dismiss();
                       })
                       .setCancelable(true);
                
                android.app.AlertDialog dialog = builder.create();
                LogUtil.i("显示图片对话框");
                dialog.show();
                LogUtil.i("图片设置完成");
                
            } catch (Exception e) {
                LogUtil.e("显示图片时发生异常: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    protected int tellMeLayout() {
        return R.layout.fragment_take_photo;
    }

    @OnClick({R.id.takephoto_tv_post, R.id.control_tv_preview, R.id.takephoto_tv_capture, R.id.reset_capture_task})
    public void onViewClicked(View view) {
        int id = view.getId();
        if (id == R.id.control_tv_preview) {
            mTvPreview.startAnimation(AnimUtil.alphHalf2All());
            ARouter.getInstance()
                    .build(AppContants.ARouterUrl.PreviewActivity)
                    .navigation();
        } else if (id == R.id.takephoto_tv_post) {
            mTvPost.startAnimation(AnimUtil.alphHalf2All());
            new Thread(CapturePostUtil::findLocalPic).start();
        } else if (id == R.id.takephoto_tv_capture) { //执行直接拍照
            takePhoto();
        } else if (id == R.id.reset_capture_task) {
            mTvReset.startAnimation(AnimUtil.alphHalf2All());
            TaskServiceUtil.resetPhotoTasks();
            ToastUtils.showToast("拍照定时任务重启");
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

    /**
     * 设置抓图延迟时间
     * @param delayMs 延迟时间（毫秒）
     */
    public void setCaptureDelayMs(long delayMs) {
        if (cameraNormalManager != null) {
            cameraNormalManager.setCaptureDelayMs(delayMs);
        }
    }

    /**
     * 手动拍照方法
     * 现在通过CameraInitListener提供详细的初始化过程反馈
     */
    private void takePhoto() {
        mTvCapture.startAnimation(AnimUtil.alphHalf2All());
        
        new Thread(() -> {
            try {
                // 从设置中读取抓图延迟时间并应用到手动抓图
                long captureDelayMs = ShareUtil.getCaptureDelay();
                cameraNormalManager.setCaptureDelayMs(captureDelayMs);
                
                // 重新初始化相机（会触发详细的初始化回调）
                cameraNormalManager.unInitCamera();
                cameraNormalManager.initCamera();
                cameraNormalManager.connectCamera();
                
                // 等待相机稳定
                Thread.sleep(2000);
                
                // 开始拍照流程
                cameraNormalManager.captureExecute(AppContants.CaptureSource.CAPTURE_SOURCE_HAND);
                
                Thread.sleep(20 * 1000);
                
            } catch (InterruptedException e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    ToastUtils.showToast("拍照过程被中断");
                });
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    ToastUtils.showToast("拍照过程发生错误：" + e.getMessage());
                });
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
