package fr.innog.memoryflow.ui.fragments.cardfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import fr.innog.memoryflow.core.navigation.NavKeys
import fr.innog.memoryflow.R
import fr.innog.memoryflow.databinding.FragmentStudyFinishedBinding
import fr.innog.memoryflow.ui.fragments.deckfragment.DeckListFragment
import fr.innog.memoryflow.ui.utils.AndroidUtils

@AndroidEntryPoint
class StudyFinishFragment : Fragment() {

    var _binding : FragmentStudyFinishedBinding? = null

    val binding get() = _binding!!

    var cardCount : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cardCount = requireArguments().getInt(NavKeys.ARG_CARD_STUTIED_COUNT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        _binding = FragmentStudyFinishedBinding.inflate(inflater, container, false)

        val binding = this.binding

        binding.finishButton.setOnClickListener { view ->
            AndroidUtils.displayFragment(R.id.fragmentContainerView, DeckListFragment.newInstance(), parentFragmentManager, false)
        }

        binding.CardStudiedTxt.text = "${if(cardCount < 2) "$cardCount carte a été étudiée" else "$cardCount cartes ont étaient étudiées"}"

        binding.finishCard.apply {
            scaleX = 0.8f
            scaleY = 0.8f
            alpha = 0f

            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(OvershootInterpolator())
                .start()
        }

        binding.finishIcon.animate()
            .rotationBy(360f)
            .setDuration(900)
            .setInterpolator(DecelerateInterpolator())
            .start()


        return _binding?.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        @JvmStatic
        fun newInstance(cardCount: Int) : StudyFinishFragment
        {
            return StudyFinishFragment().apply {
                arguments = Bundle().apply {
                    putInt(NavKeys.ARG_CARD_STUTIED_COUNT, cardCount)
                }
            }
        }
    }

}