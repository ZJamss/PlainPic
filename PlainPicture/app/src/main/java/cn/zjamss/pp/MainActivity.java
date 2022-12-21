package cn.zjamss.pp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Outline;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.zjamss.pp.databinding.ActivityMainBinding;
import cn.zjamss.pp.net.PictureService;
import cn.zjamss.pp.service.UpdateService;
import jp.wasabeef.glide.transformations.CropTransformation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 1;
    private static final int CHOOSE_PHOTO = 2;
    private static String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


    private ImageCapture imageCapture;
    private ActivityMainBinding binding;

    private File outputDir;
    private File cache;
    ExecutorService cameraExecutor;
    private boolean isBack = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //设置全屏
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        init();

        //初始化启动相机
        initCamera();

        //初始化各类事件
        initEvents();


    }

    private void init() {
        this.outputDir = this.getCacheDir();
        this.cameraExecutor = Executors.newSingleThreadExecutor();

        //启动服务
        if (!isServiceRunning("UpdateService")) {
            startService(new Intent(MainActivity.this, UpdateService.class));
        }

        //生成并获取queue_name
        SharedPreferences pp = getSharedPreferences("pp", Context.MODE_PRIVATE);
        if (pp.getString("queue_name", null) == null) {
            SharedPreferences sp = getSharedPreferences("pp", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("queue_name", UUID.randomUUID().toString());
            editor.apply();
        }
    }

    private void initEvents() {
        //拍照事件
        binding.cameraCaptureButton.setOnClickListener(v -> {
            if (imageCapture == null) return;
            File photo = new File(outputDir, "cache_photo_" + new Date().getTime() + ".jpg");
            ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photo).build();
            imageCapture.takePicture(options, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Uri uri = Uri.fromFile(photo);
                    if (uri != null) {
                        cache = photo;
                        runOnUiThread(() -> showPhoto(uri));
                    }
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    exception.printStackTrace();
                }
            });
        });

        //切换摄像头
        binding.bring.setOnClickListener(v -> {
            CameraSelector cameraSelector;
            if (isBack) {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
            } else {
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
            }
            isBack = !isBack;
            startCamera(cameraSelector);
        });

        //选择相册
        binding.album.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, CHOOSE_PHOTO);
        });
    }

    private boolean isServiceRunning(final String className) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    showPhoto(uri);
                    String path = uri.getPath();
                    if (path.startsWith("/raw"))
                        path = path.substring(4);
                    cache = new File(path);
                }
                break;
        }
    }

    public void showPhoto(Uri uri) {
        binding.showPhoto.setVisibility(View.VISIBLE);
        Glide.with(MainActivity.this).load(uri)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(50)))
                .apply(RequestOptions.bitmapTransform(new CropTransformation(350, 350)))
                .into(binding.cachePhoto);

        //发送
        binding.send.setOnClickListener(v -> {
            if (cache == null) {
                return;
            }
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            RequestBody param = RequestBody.create(MediaType.parse("image/**"), cache);
            builder.addFormDataPart("file", cache.getName(), param);
            MultipartBody.Part part = builder.build().part(0);

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("提示");
            progressDialog.setMessage("发送中");
            progressDialog.setCancelable(false);
            progressDialog.show();
            PictureService.service.uploadPic(part).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        binding.showPhoto.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        binding.showPhoto.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        //关闭
        binding.close.setOnClickListener(v -> binding.showPhoto.setVisibility(View.INVISIBLE));
    }


    private void initCamera() {
        if (allPermissionsGranted()) {
            startCamera(CameraSelector.DEFAULT_BACK_CAMERA);
            //设置相机圆角
            Outline outline = new Outline();
            outline.setRoundRect(new Rect(0, 0, binding.viewFinder.getWidth(), binding.viewFinder.getHeight()), 15 * getResources().getDisplayMetrics().density);
            ViewOutlineProvider outlineProvider = binding.viewFinder.getOutlineProvider();
            outlineProvider.getOutline(binding.viewFinder, outline);
            binding.viewFinder.setOutlineProvider(outlineProvider);
            binding.viewFinder.setClipToOutline(true);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera(CameraSelector cameraSelector) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                        .build();
                preview.setSurfaceProvider(binding.viewFinder.createSurfaceProvider());
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    //获取相机权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSIONS:
                if (allPermissionsGranted()) {
                    startCamera(CameraSelector.DEFAULT_BACK_CAMERA);
                } else {
                    Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
}