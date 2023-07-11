package com.darkndev.everkeep.recyclerview.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.darkndev.everkeep.databinding.LayoutLabelItemBinding
import com.darkndev.everkeep.models.Label

class LabelAdapter : ListAdapter<Label, LabelAdapter.LabelViewHolder>(LABEL_DIFF_UTIL) {

    companion object {
        private val LABEL_DIFF_UTIL = object : DiffUtil.ItemCallback<Label>() {
            override fun areItemsTheSame(oldLabel: Label, newLabel: Label) =
                oldLabel.id == newLabel.id

            override fun areContentsTheSame(oldLabel: Label, newLabel: Label) = oldLabel == newLabel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LabelViewHolder(
        LayoutLabelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: LabelViewHolder, position: Int) {
        holder.binding.labelChip.text = getItem(position).label
    }

    fun getLabel(position: Int): Label = getItem(position)

    class LabelViewHolder(val binding: LayoutLabelItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}