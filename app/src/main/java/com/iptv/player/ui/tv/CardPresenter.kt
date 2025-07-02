package com.iptv.player.ui.tv

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.iptv.player.R

class CardPresenter : Presenter() {
    
    private var selectedBackgroundColor = -1
    private var defaultBackgroundColor = -1
    private var defaultCardImage: Drawable? = null
    
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        selectedBackgroundColor = ContextCompat.getColor(parent.context, R.color.purple_500)
        defaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.gray_600)
        defaultCardImage = ContextCompat.getDrawable(parent.context, R.drawable.ic_tv_placeholder)
        
        val cardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                updateCardBackgroundColor(this, selected)
                super.setSelected(selected)
            }
        }
        
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        updateCardBackgroundColor(cardView, false)
        
        return ViewHolder(cardView)
    }
    
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val cardView = viewHolder.view as ImageCardView
        
        when (item) {
            is String -> {
                cardView.titleText = item
                cardView.contentText = ""
                cardView.setMainImageDimensions(300, 200)
                cardView.mainImage = defaultCardImage
            }
            // Add more item types as needed
        }
    }
    
    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
    
    private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
        val color = if (selected) selectedBackgroundColor else defaultBackgroundColor
        view.setBackgroundColor(color)
        view.findViewById<ViewGroup>(androidx.leanback.R.id.info_field)?.setBackgroundColor(color)
    }
}