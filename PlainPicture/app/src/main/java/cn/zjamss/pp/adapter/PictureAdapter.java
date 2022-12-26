package cn.zjamss.pp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.zjamss.pp.R;
import cn.zjamss.pp.util.StorageUtil;

/**
 * @Program: PlainPicture
 * @Description:
 * @Author: ZJamss
 * @Create: 2022-12-25 23:13
 **/
public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {

    private final List<String> mList;
    private final Context context;

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.img);
        }
    }

    public PictureAdapter(List<String> list, Context context) {
        mList = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //加载页面
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pic_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = mList.get(position);
        Bitmap bitmap = StorageUtil.getPic(context, name);
        holder.imageView.setImageBitmap(bitmap);
        holder.imageView.setOnClickListener(v -> {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog);
            ImageView img = dialog.findViewById(R.id.d_img);
            img.setImageBitmap(bitmap);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


}
