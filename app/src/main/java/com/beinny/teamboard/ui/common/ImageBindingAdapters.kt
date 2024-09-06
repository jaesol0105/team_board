package com.beinny.teamboard.ui.common

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.beinny.teamboard.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("app:imageUri")
fun loadImage(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide
            .with(view)
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(view)
    }
}