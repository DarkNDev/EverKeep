package com.darkndev.everkeep.ui.label

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.darkndev.everkeep.R
import com.darkndev.everkeep.databinding.FragmentLabelBinding
import com.darkndev.everkeep.recyclerview.adapters.LabelAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LabelFragment : Fragment(R.layout.fragment_label) {

    private var _binding: FragmentLabelBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LabelViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLabelBinding.bind(view)

        val chipAdapter = LabelAdapter()

        binding.apply {

            viewModelBinding = viewModel
            lifecycleOwner = viewLifecycleOwner

            val callback = object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    viewModel.deleteLabel(chipAdapter.getLabel(viewHolder.bindingAdapterPosition))
                }

            }

            ItemTouchHelper(callback).attachToRecyclerView(recyclerView)

            recyclerView.apply {
                layoutManager = FlexboxLayoutManager(context).apply {
                    flexDirection = FlexDirection.ROW
                }
                adapter = chipAdapter
            }

            labelEditTextLayout.setEndIconOnClickListener {
                viewModel.updateLabel(labelEditText.text.toString().trim())
            }

            labelEditText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    viewModel.updateLabel(labelEditText.text.toString().trim())
                }
                true
            }

            labelEditText.setOnFocusChangeListener { v, hasFocus ->
                if (v is TextInputEditText && !hasFocus) {
                    val imm =
                        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }

            viewModel.labels.observe(viewLifecycleOwner) { list ->
                chipAdapter.submitList(list.sortedBy { it.label })
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.labelEvent.collectLatest { event ->
                when (event) {
                    is LabelViewModel.LabelEvent.ShowActionMessage -> {
                        binding.apply {
                            labelEditText.setText("")
                            labelEditText.clearFocus()
                        }
                        Snackbar.make(view, event.message, Snackbar.LENGTH_SHORT)
                            .setAction("UNDO") {
                                viewModel.undoDeleteLabel(event.label)
                            }.show()
                    }

                    is LabelViewModel.LabelEvent.ShowMessage -> {
                        binding.apply {
                            labelEditText.setText("")
                            labelEditText.clearFocus()
                        }
                        Snackbar.make(view, event.message, Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}