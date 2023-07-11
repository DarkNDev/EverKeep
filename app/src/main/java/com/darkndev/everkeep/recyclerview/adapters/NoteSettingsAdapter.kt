package com.darkndev.everkeep.recyclerview.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.darkndev.everkeep.databinding.LayoutNoteSettingBinding
import com.darkndev.everkeep.models.NoteSetting

class NoteSettingsAdapter(
    private val items: List<NoteSetting>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<NoteSettingsAdapter.NoteSettingsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteSettingsViewHolder {
        val binding =
            LayoutNoteSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteSettingsViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: NoteSettingsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class NoteSettingsViewHolder(val binding: LayoutNoteSettingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(items[bindingAdapterPosition].drawableId)
                }
            }
        }

        fun bind(item: NoteSetting) {
            binding.apply {
                settingText.text = item.settingText
                iconView.setImageResource(item.drawableId)
            }
        }
    }
}