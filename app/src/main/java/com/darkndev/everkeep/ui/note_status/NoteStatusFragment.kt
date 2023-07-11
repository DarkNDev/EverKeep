package com.darkndev.everkeep.ui.note_status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.darkndev.everkeep.R
import com.darkndev.everkeep.databinding.FragmentNoteStatusBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NoteStatusFragment : BottomSheetDialogFragment(R.layout.fragment_note_status) {

    private var _binding: FragmentNoteStatusBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteStatusViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteStatusBinding.inflate(inflater, container, false)

        binding.apply {

            val job = viewLifecycleOwner.lifecycleScope.launch {
                val labelList = viewModel.labelList.first()
                labelList.forEach {
                    val chip = layoutInflater.inflate(
                        R.layout.layout_label_chip,
                        chipGroup,
                        false
                    ) as Chip
                    chip.text = it.label
                    chip.id = it.id.toInt()
                    chipGroup.addView(chip)
                    if (viewModel.label.value == it.label) {
                        chip.isChecked = true
                    }
                }
                prioritySlider.value = viewModel.priority.value?.toFloat()!!
            }

            viewLifecycleOwner.lifecycleScope.launch {
                job.join()

                prioritySlider.addOnChangeListener { _, value, _ ->
                    viewModel.afterPriorityChanged(value.toInt())
                }

                chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                    val chip = group.findViewById<Chip>(checkedIds.first())
                    viewModel.afterLabelChanged(chip.text.toString())
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}