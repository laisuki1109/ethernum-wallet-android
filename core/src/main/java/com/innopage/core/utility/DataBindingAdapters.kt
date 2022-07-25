package com.innopage.core.utility

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

@BindingAdapter("imgUrl")
fun loadImageFromUrl(view: ImageView, url: String?) {
    Glide.with(view)
        .load(url.orEmpty())
        .transition(DrawableTransitionOptions.withCrossFade())
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .into(view)
}


