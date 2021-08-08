package com.rosi.tictactoe.utils

import android.content.res.Resources
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

fun TextView.setShadowColor(resources: Resources, colorResId: Int) {
    this.setShadowLayer(
        this.shadowRadius,
        this.shadowDx,
        this.shadowDy,
        ResourcesCompat.getColor(resources, colorResId, null)
    )
}

fun TextView.setTextColor(resources: Resources, colorResId: Int) {
    this.setTextColor(ResourcesCompat.getColor(resources, colorResId, null))
}