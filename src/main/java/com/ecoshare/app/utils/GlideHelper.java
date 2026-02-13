package com.ecoshare.app.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.ecoshare.app.R;

public class GlideHelper {

    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_notification)
            .error(R.drawable.ic_notification)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView);
    }

    public static void loadCircularImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_notification)
            .error(R.drawable.ic_notification)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView);
    }

    public static void loadImageWithCenterCrop(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;

        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_notification)
            .error(R.drawable.ic_notification)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView);
    }

    public static void loadImageWithCallback(Context context, String imageUrl, ImageView imageView, 
                                            final ImageLoadCallback callback) {
        if (context == null || imageView == null) return;

        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.ic_notification)
            .error(R.drawable.ic_notification)
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, 
                                          Target<Drawable> target, boolean isFirstResource) {
                    if (callback != null) {
                        callback.onLoadFailed();
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, 
                                             Target<Drawable> target, DataSource dataSource, 
                                             boolean isFirstResource) {
                    if (callback != null) {
                        callback.onLoadSuccess();
                    }
                    return false;
                }
            })
            .into(imageView);
    }

    public static void clearCache(Context context) {
        if (context == null) return;
        Glide.get(context).clearMemory();
        new Thread(() -> Glide.get(context).clearDiskCache()).start();
    }

    public interface ImageLoadCallback {
        void onLoadSuccess();
        void onLoadFailed();
    }
}
