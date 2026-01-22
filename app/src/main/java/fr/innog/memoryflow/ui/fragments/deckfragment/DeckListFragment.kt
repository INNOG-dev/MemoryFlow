package fr.innog.memoryflow.ui.fragments.deckfragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fr.innog.memoryflow.R
import fr.innog.memoryflow.data.local.entity.Deck
import fr.innog.memoryflow.data.local.relation.DeckWithCards
import fr.innog.memoryflow.databinding.FragmentListBinding
import fr.innog.memoryflow.ui.decks.DeckAdapter
import fr.innog.memoryflow.ui.decks.DeckBottomSheet
import fr.innog.memoryflow.ui.decks.DeckListViewModel
import fr.innog.memoryflow.ui.utils.AndroidUtils
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeckListFragment : Fragment() {

    var _list_binding : FragmentListBinding? = null

    val listBinding get() = _list_binding!!

    private val viewModel: DeckListViewModel by viewModels()

    private lateinit var adapter: DeckAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _list_binding = FragmentListBinding.inflate(inflater, container, false)

        val binding = _list_binding!!

        adapter = DeckAdapter(object : DeckAdapter.DeckListener {
            override fun onEdit(deck: Deck) {
                lateinit var bottomSheet: DeckBottomSheet

                bottomSheet = DeckBottomSheet("Modification du deck" , deck.name) { input ->
                    viewModel.renameDeck(input, deck)
                    bottomSheet.dismiss()
                }

                bottomSheet.show(parentFragmentManager, "EditDeck")
            }

            override fun onDelete(deck: Deck) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Supprimer le deck")
                    .setMessage("Cette action est définitive.")
                    .setPositiveButton("Supprimer") { _,_ ->
                        viewModel.deleteDeck(deck)
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            }

            override fun onClicked(deck: Deck) {
                AndroidUtils.displayFragment(R.id.fragmentContainerView, DeckContentFragment.newInstance(deck.name,deck.uid), parentFragmentManager)
            }

            override fun getProgress(deckWithCards: DeckWithCards): Int {
                return 100 - ((getCardCountToStudy(deckWithCards).toFloat() / deckWithCards.cards.size) * 100).toInt()
            }

            override fun getCardCountToStudy(deckWithCards: DeckWithCards): Int {
                return viewModel.getCardCountToStudyInDeck(deckWithCards)
            }
        })

        binding.deckList.adapter = adapter
        binding.deckList.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
            {
                viewModel.filteredDecks.collect { list ->
                    binding.deckToStudy.text = "${viewModel.getDeckToStudyCount()} decks à réviser aujourd'hui"
                    adapter.submitList(list)
                }
            }
        }

        binding.fabAddDeck.setOnClickListener {

            lateinit var bottomSheet: DeckBottomSheet

            bottomSheet = DeckBottomSheet("Ajout de paquet") { deckName ->

                    lifecycleScope.launch {
                        val success = viewModel.addDeck(deckName)
                        if(!success)
                        {
                            bottomSheet.showError("Le deck existe déjà")
                        }
                        else
                        {
                            bottomSheet.dismiss()
                        }
                    }

            }

            bottomSheet.show(parentFragmentManager, "AddDeck")
        }

        binding.searchInputEditText.addTextChangedListener { text ->
            viewModel.search(text.toString())
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _list_binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = DeckListFragment()
    }
}