package cn.zjamss.pp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.util.ArrayList;

import cn.zjamss.pp.Constant;
import cn.zjamss.pp.R;
import cn.zjamss.pp.adapter.PictureAdapter;
import cn.zjamss.pp.databinding.ActivityMainBinding;
import cn.zjamss.pp.databinding.ActivityRecordBinding;
import cn.zjamss.pp.util.StorageUtil;

public class RecordActivity extends AppCompatActivity {

    private ActivityRecordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
        SharedPreferences sp = getSharedPreferences(Constant.PICTURES_FILE, MODE_PRIVATE);
        String json = sp.getString(Constant.PICTURES_KEY, null);

        if (json != null) {
            ArrayList list = new Gson().fromJson(json, ArrayList.class);
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager
                    (2, StaggeredGridLayoutManager.VERTICAL);
            binding.list.setAdapter(new PictureAdapter(list, this));
            binding.list.setLayoutManager(layoutManager);
        }


    }
}