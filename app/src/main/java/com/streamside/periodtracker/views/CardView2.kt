package com.streamside.periodtracker.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.streamside.periodtracker.R

class CardView2 @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : CardView(context,attrs) {
    private var cv2Image: ImageView
    private var cv2Text: TextView
    private var cardText: String
    private var cardTextSize: Int
    private var cardImage: Drawable?

    init {
        val view = View.inflate(context, R.layout.cardview2, this)
        cv2Image = view.findViewById(R.id.cv2Image)
        cv2Text = view.findViewById(R.id.cv2Text)
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CardView2, 0, 0)

        try {
            cardText = a.getString(R.styleable.CardView2_cardText).toString()
            cv2Text.text = (if (cardText != "null") cardText else "").toString()

            cardTextSize = a.getDimensionPixelSize(R.styleable.CardView2_cardTextSize, 0)
            if (cardTextSize > 0) cv2Text.setTextSize(TypedValue.COMPLEX_UNIT_PX, cardTextSize.toFloat())

            cardImage = a.getDrawable(R.styleable.CardView2_cardImage)
            if (cardImage != null) cv2Image.setImageDrawable(cardImage)
            else cv2Image.setImageResource(R.drawable.default_library_image)
        } finally {
            a.recycle()
        }
    }

    fun getCardText() = cardText
    fun setCardText(text: String) {
        cardText = text
        cv2Text.text = cardText
    }

    fun getCardTextSize() = cardTextSize
    fun setCardTextSize(size: Int) {
        cardTextSize = size
        cv2Text.text = cardText
        if (cardTextSize > 0) cv2Text.setTextSize(TypedValue.COMPLEX_UNIT_PX, cardTextSize.toFloat())
    }

    fun getCardImage() = cardImage
    fun setCardImage(drawable: Drawable?) {
        cardImage = drawable
        cv2Image.setImageDrawable(cardImage)
    }
}