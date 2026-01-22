package fr.innog.memoryflow.ui.fragments.startfragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import fr.innog.memoryflow.R
import fr.innog.memoryflow.databinding.FragmentStartBinding
import fr.innog.memoryflow.ui.fragments.deckfragment.DeckListFragment
import fr.innog.memoryflow.ui.utils.AndroidUtils

class StartFragment : Fragment() {

    private var _binding : FragmentStartBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SliderAdapter


    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        val next = (binding.viewPager.currentItem + 1) % adapter.itemCount
        binding.viewPager.setCurrentItem(next, true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentStartBinding.inflate(inflater, container, false)

        val items = listOf(
            SliderItem(R.drawable.illustration_habits, "Construis des habitudes solides."),
            SliderItem(R.drawable.illustration_focus, "Reste concentré et motivé."),
            SliderItem(R.drawable.illustration_growth, "Observe ton évolution chaque jour.")
        )

        val binding = this.binding

        adapter = SliderAdapter(items)

        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 3

        binding.indicator.setViewPager(binding.viewPager)

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    sliderHandler.removeCallbacks(sliderRunnable)
                    sliderHandler.postDelayed(sliderRunnable, 3000)
                }
            }
        )

        binding.startButton.setOnClickListener()
        {
            AndroidUtils.displayFragment(R.id.fragmentContainerView, DeckListFragment.newInstance(), parentFragmentManager)
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = StartFragment()
    }
}

