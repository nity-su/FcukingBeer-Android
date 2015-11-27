package xyz.pongsakorn.fcukingbeer;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter {
    private List<String> filename;
    private Context context;

    public HistoryAdapter(Context context, List<String> filename) {
        this.filename = filename;
        this.context= context;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ImageViewHolder h = (ImageViewHolder) holder;

        final String label = filename.get(position);

        Glide.with(context)
                .load(new File(label))
                .into(h.imView);


        h.imView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_image);
                ZoomableImageView im = (ZoomableImageView) dialog.findViewById(R.id.image);

                Glide.with(context)
                        .load(new File(label))
                        .into(im);

                dialog.setCancelable(true);
                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filename.size();
    }

    private class ImageViewHolder extends RecyclerView.ViewHolder  {

        public ImageView imView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imView = (ImageView) itemView.findViewById(R.id.image);
        }
    }

    public void update(List<String> filename) {
        this.filename = filename;
        notifyDataSetChanged();
    }
}