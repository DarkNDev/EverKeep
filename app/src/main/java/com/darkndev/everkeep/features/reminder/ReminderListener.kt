package com.darkndev.everkeep.features.reminder

import com.darkndev.everkeep.models.Note

interface ReminderListener {
    fun schedule(note: Note)
    fun cancel(note: Note)
}