package com.darkndev.everkeep.ui.note_reminder

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.darkndev.everkeep.databinding.FragmentNoteReminderBinding
import com.darkndev.everkeep.ui.note_edit.NoteEditFragment.Companion.NOTE_MESSAGE
import com.darkndev.everkeep.ui.note_edit.NoteEditFragment.Companion.NOTE_REQUEST
import com.darkndev.everkeep.utils.MORNING
import com.darkndev.everkeep.utils.SELECT_DATE
import com.darkndev.everkeep.utils.SELECT_TIME
import com.darkndev.everkeep.utils.TOMORROW
import com.darkndev.everkeep.utils.getDatePickerDate
import com.darkndev.everkeep.utils.getDuration
import com.darkndev.everkeep.utils.getTimePickerTime
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@AndroidEntryPoint
class NoteReminderFragment : DialogFragment() {

    private var _binding: FragmentNoteReminderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteReminderViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentNoteReminderBinding.inflate(layoutInflater, null, false)

        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setView(binding.root)
            .setTitle("Add Reminder")
            .setNegativeButton("Cancel") { _, _ ->
                val text = viewModel.cancelReminder()
                if (text != null) setFragmentResult(NOTE_REQUEST, bundleOf(NOTE_MESSAGE to text))
            }
            .setNeutralButton("Delete") { _, _ ->
                val text = viewModel.deleteReminder()
                if (text != null) setFragmentResult(NOTE_REQUEST, bundleOf(NOTE_MESSAGE to text))
            }
            .setPositiveButton("Schedule") { _, _ ->
                val text = viewModel.scheduleReminder()
                setFragmentResult(NOTE_REQUEST, bundleOf(NOTE_MESSAGE to text))
            }

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
                    .build()
            )
            .setSelection(
                viewModel.note.reminderTime?.toEpochSecond()?.times(1000)
                    ?: MaterialDatePicker.todayInUtcMilliseconds()
            )
            .build()

        val timePicker = MaterialTimePicker.Builder()
            .setTitleText("Select Time")
            .setInputMode(INPUT_MODE_CLOCK)
            .setHour(viewModel.note.reminderTime?.hour ?: 8)
            .setMinute(viewModel.note.reminderTime?.minute ?: 0)
            .build()

        binding.apply {
            reminderDateText.setSimpleItems(viewModel.dateListMap.keys.toTypedArray())
            reminderDateText.setText(
                getDatePickerDate(viewModel.note.reminderTime?.toLocalDate()) ?: TOMORROW, false
            )

            reminderTimeText.setSimpleItems(viewModel.timeListMap.keys.toTypedArray())
            reminderTimeText.setText(
                getTimePickerTime(viewModel.note.reminderTime?.toLocalTime()) ?: MORNING, false
            )

            reminderRepeatText.setSimpleItems(viewModel.repeatListMap.keys.toTypedArray())
            reminderRepeatText.setText(getDuration(viewModel.note.reminderRepeat), false)

            reminderDateText.doAfterTextChanged {
                if (it.toString() == SELECT_DATE) {
                    datePicker.show(childFragmentManager, "Date Picker")
                } else if (viewModel.dateListMap.containsKey(it.toString())) {
                    viewModel.date.value = viewModel.dateListMap[it.toString()]
                }
            }

            reminderTimeText.doAfterTextChanged {
                if (it.toString() == SELECT_TIME) {
                    timePicker.show(childFragmentManager, "Time Picker")
                } else if (viewModel.timeListMap.containsKey(it.toString())) {
                    viewModel.time.value = viewModel.timeListMap[it.toString()]
                }
            }

            reminderRepeatText.doAfterTextChanged {
                viewModel.repeat.value = viewModel.repeatListMap[it.toString()]!!
            }

            datePicker.addOnPositiveButtonClickListener { dateLong ->
                val localDate =
                    Instant.ofEpochMilli(dateLong).atZone(ZoneId.systemDefault()).toLocalDate()
                reminderDateText.setText(getDatePickerDate(localDate), false)
                viewModel.date.value = localDate
            }


            timePicker.addOnPositiveButtonClickListener {
                val localTime = LocalTime.of(timePicker.hour, timePicker.minute)
                reminderTimeText.setText(getTimePickerTime(localTime), false)
                viewModel.time.value = localTime
            }
        }

        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}