package com.darkndev.everkeep.recyclerview.selection

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem

class NoteActionMode(
    private val menuId: Int,
    private val onActionItemClicked: (Int) -> Boolean,
    private val onDestroyActionMode: () -> Unit
) :
    ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(menuId, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        val value = onActionItemClicked(item.itemId)
        mode.finish()
        return value
    }

    override fun onDestroyActionMode(mode: ActionMode) = onDestroyActionMode()
}