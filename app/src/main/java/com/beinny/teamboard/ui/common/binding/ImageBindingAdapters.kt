package com.beinny.teamboard.ui.common.binding
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.beinny.teamboard.R
import com.bumptech.glide.Glide

@BindingAdapter("app:imageUri")
fun loadImage(view: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrBlank()) {
        Glide
            .with(view)
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(view)
    }
}

@BindingAdapter(value = ["imageUri", "fallbackUrl"], requireAll = false)
fun loadImage(view: ImageView, imageUri: String?, fallbackUrl: String?) {
    val imageToLoad = if (!imageUri.isNullOrBlank()) imageUri else fallbackUrl

    if (!imageToLoad.isNullOrBlank()) {
        Glide
            .with(view)
            .load(imageToLoad)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(view)
    }
}