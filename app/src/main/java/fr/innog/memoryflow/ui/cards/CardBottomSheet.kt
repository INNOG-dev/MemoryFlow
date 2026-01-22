package fr.innog.memoryflow.ui.cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import fr.innog.memoryflow.databinding.BottomsheetCardBinding

class CardBottomSheet(private val action : CardBottomSheetAction) : BottomSheetDialogFragment() {

    interface CardBottomSheetAction {
        fun onEdit()

        fun onDuplicate()

        fun onDelete()
    }

    private var _binding: BottomsheetCardBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomsheetCardBinding.inflate(inflater, container, false)

        val binding = _binding!!

        binding.editBtn.setOnClickListener { view ->
            action.onEdit()
            dismiss()
        }

        binding.dupplicateBtn.setOnClickListener { view ->
            action.onDuplicate()
            dismiss()
        }

        binding.deleteBtn.setOnClickListener { view ->
            action.onDelete()
            dismiss()
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}