package com.example.nav_contacts.presentation.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.nav_contacts.R
import com.example.nav_contacts.presentation.handler.IconWithNameHandler

class IconWithNameLayout:RelativeLayout,IconWithNameHandler{
    var imageView: ImageView
    var textView: TextView
    constructor(context: Context):super(context)
    constructor(context: Context, attributeSet:AttributeSet):super(context,attributeSet)
    constructor(context: Context,attributeSet:AttributeSet, defaultStyle:Int):super(context, attributeSet, defaultStyle)
    init {
        LayoutInflater.from(context).inflate(R.layout.icon_with_name_view, this,true)
        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
    }

    override fun setImageAndText(resourceId: Int, text: String) {
        imageView.setBackgroundResource(resourceId)
        textView.text = text
    }

}