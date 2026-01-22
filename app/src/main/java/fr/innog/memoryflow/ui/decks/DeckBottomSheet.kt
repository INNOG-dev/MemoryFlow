package fr.innog.memoryflow.ui.decks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import androidx.core.graphics.toColorInt
import fr.innog.memoryflow.databinding.BottomsheetDeckBinding

class DeckBottomSheet(private val title : String, private val inputContent: String = "", private val confirmationCallback: (String) -> Unit) : BottomSheetDialogFragment() {


    private var _binding: BottomsheetDeckBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =  BottomsheetDeckBinding.inflate(inflater, container, false)

        val binding = _binding!!

        binding.deckBottomsheetTitle.text = title

        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.deckInput.setText(inputContent)

        binding.confirmBtn.setOnClickListener {
            confirmationCallback(binding.deckInput.text.toString())
        }

        return _binding!!.root
    }

    suspend fun showError(message: String)
    {
        binding.statusMsg.visibility = View.VISIBLE
        binding.statusMsg.text = message
        binding.deckNameLayout.boxStrokeColor = "#FF0000".toColorInt()
        delay(5000)
        binding.statusMsg.visibility = View.GONE
        binding.deckNameLayout.boxStrokeColor = "#AAAAAAFF".toColorInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}