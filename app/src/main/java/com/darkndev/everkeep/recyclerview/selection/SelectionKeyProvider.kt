package com.darkndev.everkeep.recyclerview.selection

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView

class SelectionKeyProvider(private val recyclerView: RecyclerView) :
    ItemKeyProvider<Long>(SCOPE_MAPPED) {

    private lateinit var holder: RecyclerView.ViewHolder

    override fun getKey(position: Int): Long {
        return holder.itemId
    }

    override fun getPosition(key: Long): Int {
        holder = recyclerView.findViewHolderForItemId(key)
        return holder.bindingAdapterPosition
    }
}