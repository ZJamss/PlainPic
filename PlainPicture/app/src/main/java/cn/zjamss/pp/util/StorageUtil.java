package cn.zjamss.pp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.zjamss.pp.Constant;

/**
 * @Program: PlainPicture
 * @Description:
 * @Author: ZJamss
 * @Create: 2022-12-25 22:24
 **/
public class StorageUtil {

    public static void savePic(Context context, Bitmap bitmap) {
        SharedPreferences sp = context.getSharedPreferences(Constant.PICTURES_FILE, Context.MODE_PRIVATE);
        String name = UUID.randomUUID().toString();
        Gson gson = new Gson();
        ArrayList list;
        if (sp.getString(Constant.PICTURES_KEY, null) != null) {
            list = gson.fromJson(sp.getString(Constant.PICTURES_KEY, null), ArrayList.class);
        } else {
            list = new ArrayList<String>();
        }
        list.add(name);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, context.openFileOutput(name, Context.MODE_PRIVATE));
            SharedPreferences.Editor edit = sp.edit();
            edit.putString(Constant.PICTURES_KEY, gson.toJson(list));
            edit.apply();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getPic(Context context, String name) {
        try {
            return BitmapFactory.decodeStream(context.openFileInput(name));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
