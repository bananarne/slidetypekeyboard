package com.latinsud.android.slidetypekeyboard

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

class EmojiSwipeBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

    private val emojiList = listOf(
        "😃", "😄", "😁", "😬", "😅", "😂", "😊", "🥲", "😍", "😘", "🤨", "🥳", "🥺", "😢", 
        "😭", "😤", "😠", "🤯", "🥵", "🥶", "😱", "🤔", "🫠", "🤫", "😑", "🙄", "😧", "😯", 
        "😵‍💫", "🥴", "🤢", "🤤", "🤝", "👍", "👎", "🙌", "🫶", "✌️", "👋", "💪", "🫦", 
        "👀", "🤷‍♂️", "🌚", "🔥"
    )

    private val linearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(16, 8, 16, 8)
    }

    var onEmojiClickListener: ((String) -> Unit)? = null

    init {
        setupView()
        populateEmojis()
    }

    private fun setupView() {
        setHorizontalScrollBarEnabled(false)
        addView(linearLayout)
        
        // Set background color
        setBackgroundColor(Color.rgb(40, 40, 45))
        
        // Set fixed height
        layoutParams?.height = 120
    }

    private fun populateEmojis() {
        emojiList.forEach { emoji ->
            val emojiView = TextView(context).apply {
                text = emoji
                textSize = 24f
                setPadding(12, 8, 12, 8)
                setBackgroundColor(Color.TRANSPARENT)
                
                setOnClickListener {
                    onEmojiClickListener?.invoke(emoji)
                }
            }
            linearLayout.addView(emojiView)
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Allow horizontal scrolling
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Intercept horizontal scroll gestures
        return super.onInterceptTouchEvent(ev)
    }
}