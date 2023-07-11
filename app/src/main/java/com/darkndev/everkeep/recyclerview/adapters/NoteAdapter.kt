package com.darkndev.everkeep.recyclerview.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.darkndev.everkeep.databinding.LayoutNoteCardBinding
import com.darkndev.everkeep.models.Note

class NoteAdapter(private val onItemClick: (Note) -> Unit) :
    ListAdapter<Note, NoteAdapter.NoteViewHolder>(NOTE_DIFF_UTIL) {

    private var tracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    companion object {
        private val NOTE_DIFF_UTIL = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldNote: Note, newNote: Note): Boolean {
                return oldNote.id == newNote.id
            }

            override fun areContentsTheSame(oldNote: Note, newNote: Note): Boolean {
                return oldNote.title == newNote.title &&
                        oldNote.content == newNote.content &&
                        oldNote.imageArray.contentEquals(newNote.imageArray) &&
                        oldNote.label == newNote.label &&
                        oldNote.priority == newNote.priority &&
                        oldNote.modified == newNote.modified &&
                        oldNote.pinned == newNote.pinned &&
                        oldNote.archived == newNote.archived
            }
        }
    }

    fun setSelection(tracker: SelectionTracker<Long>) {
        this.tracker = tracker
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = LayoutNoteCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }


    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
        tracker?.let {
            holder.binding.card.isChecked =
                tracker!!.isSelected(getItemId(position))
        }
    }

    override fun getItemId(position: Int) = getItem(position).id

    inner class NoteViewHolder(val binding: LayoutNoteCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val value = if (tracker == null) true else !tracker!!.hasSelection()
                if (bindingAdapterPosition != RecyclerView.NO_POSITION && value) {
                    onItemClick(getItem(bindingAdapterPosition))
                }
            }
        }

        fun bind(note: Note) {
            binding.apply {
                titleCard.text = note.title
                contentCard.text = note.content
                scribbleCard.isVisible = note.imageArray.isNotEmpty()
                if (note.imageArray.isNotEmpty()) {
                    Glide.with(itemView.context).load(note.imageArray)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(scribbleCard)
                }
            }
        }
    }
}