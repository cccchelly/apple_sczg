package com.sczg.apple.ui.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.sczg.apple.AppContants;
import com.sczg.apple.R;
import com.sczg.apple.utils.BitmapCompressUtil;
import com.sczg.apple.utils.CapturePostUtil;
import com.sczg.apple.utils.FileUtils;
import com.sczg.apple.utils.LogUtil;
import com.sczg.apple.utils.ToastUtils;
import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Route(path = AppContants.ARouterUrl.ShowCaptureActivity)
public class ShowCaptureActivity extends AppCompatActivity {

    @Autowired(name = AppContants.SHOW_PIC_URL_KEY)
    String name;
    @BindView(R.id.show_capture_image)
    ImageView mImage;
    @BindView(R.id.show_capture_btn_post)
    Button mBtnPost;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_capture);
        ButterKnife.bind(this);
        init();
    }


    private void init() {
        ARouter.getInstance().inject(this);
        file = FileUtils.getFileFromSdcard(name);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        
        // 检查并压缩图片以避免GPU纹理限制
        if (bitmap != null) {
            LogUtil.i("ShowCaptureActivity: 原始图片尺寸 " + bitmap.getWidth() + "x" + bitmap.getHeight());
            
            if (BitmapCompressUtil.isExceedTextureLimit(bitmap)) {
                LogUtil.i("ShowCaptureActivity: 图片尺寸超出GPU限制，开始压缩处理");
                Bitmap compressedBitmap = BitmapCompressUtil.compressBitmapForDisplay(bitmap);
                if (compressedBitmap != null) {
                    LogUtil.i("ShowCaptureActivity: 图片压缩成功，新尺寸: " + compressedBitmap.getWidth() + "x" + compressedBitmap.getHeight());
                    mImage.setImageBitmap(compressedBitmap);
                } else {
                    LogUtil.e("ShowCaptureActivity: 图片压缩失败，使用原始图片");
                    mImage.setImageBitmap(bitmap);
                }
            } else {
                LogUtil.i("ShowCaptureActivity: 图片尺寸在GPU限制内，无需压缩");
                mImage.setImageBitmap(bitmap);
            }
        } else {
            LogUtil.e("ShowCaptureActivity: 无法加载图片文件");
        }
    }

    @OnClick(R.id.show_capture_btn_post)
    public void onViewClicked() {
        ToastUtils.showToast("开始上传");
        CapturePostUtil.normalPost(file,name);
    }
}
