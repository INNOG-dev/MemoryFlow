package fr.innog.memoryflow.ui.fragments.deckfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.innog.memoryflow.R
import fr.innog.memoryflow.core.navigation.NavKeys
import fr.innog.memoryflow.databinding.FragmentDeckContentBinding
import fr.innog.memoryflow.ui.cards.CardAdapter
import fr.innog.memoryflow.ui.cards.CardBottomSheet
import fr.innog.memoryflow.ui.fragments.deckfragment.DeckAddCardFragment
import fr.innog.memoryflow.ui.fragments.cardfragment.StudyCardFragment
import fr.innog.memoryflow.ui.utils.AndroidUtils
import kotlinx.coroutines.launch
import kotlin.getValue
import fr.innog.memoryflow.ui.cards.DeckContentViewModel

@AndroidEntryPoint
class DeckContentFragment : Fragment() {

    var _binding : FragmentDeckContentBinding? = null

    val binding get() = _binding!!

    private lateinit var adapter: CardAdapter

    private val viewModel: DeckContentViewModel by viewModels()

    private var deckId: Long = -1
    private var deckName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        deckName = requireArguments().getString(NavKeys.ARG_DECK_NAME).toString()
        deckId = requireArguments().getLong(NavKeys.ARG_DECK_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeckContentBinding.inflate(inflater, container, false)

        val binding = this.binding

        binding.addCardFab.setOnClickListener {
            AndroidUtils.displayFragment(R.id.fragmentContainerView, DeckAddCardFragment.newInstance(
                DeckAddCardFragment.UIState.ADD, deckId), parentFragmentManager)
        }

        adapter = CardAdapter({ card ->
            CardBottomSheet(object : CardBottomSheet.CardBottomSheetAction {
                override fun onEdit() {
                    AndroidUtils.displayFragment(R.id.fragmentContainerView, DeckAddCardFragment.newInstance(
                        DeckAddCardFragment.UIState.EDIT, deckId, card.id), parentFragmentManager)
                }

                override fun onDuplicate() {
                    viewModel.duplicateCard(card)
                }

                override fun onDelete() {
                    viewModel.deleteCard(card)
                }

            }).show(parentFragmentManager, "CardBottomSheet")
        })

        binding.cardsRecyclerView.adapter = adapter
        binding.cardsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.cards.collect { list ->
                    val sortedList = list.sortedByDescending {
                        it.cardReviewData.nextReviewDate <= System.currentTimeMillis()
                    }
                    binding.deckInfo.text = "${list.size} cartes"
                    adapter.submitList(sortedList)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.cardsToStudy.collect { it ->
                    binding.studyButton.isEnabled = !it.isEmpty()
                }
            }
        }

        binding.deckTitle.text = deckName



        binding.studyButton.setOnClickListener { view ->
            AndroidUtils.displayFragment(R.id.fragmentContainerView, StudyCardFragment.newInstance(deckId), parentFragmentManager)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance(deckName:String, deckId:Long) : DeckContentFragment {
            return DeckContentFragment().apply {
                arguments = Bundle().apply {
                    putString(NavKeys.ARG_DECK_NAME, deckName)
                    putLong(NavKeys.ARG_DECK_ID, deckId)
                }
            }
        }
    }
}