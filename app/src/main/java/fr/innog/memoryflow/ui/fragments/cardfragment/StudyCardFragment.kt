package fr.innog.memoryflow.ui.fragments.cardfragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import fr.innog.memoryflow.core.navigation.NavKeys
import fr.innog.memoryflow.data.local.entity.Card
import fr.innog.memoryflow.data.local.model.QuizState
import fr.innog.memoryflow.data.mapper.getQuizAnswers
import fr.innog.memoryflow.data.mapper.hasQuiz
import fr.innog.memoryflow.databinding.FragmentStudyCardBinding
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.getValue
import androidx.core.view.isVisible
import fr.innog.memoryflow.R
import fr.innog.memoryflow.data.local.model.CardColors
import fr.innog.memoryflow.data.local.model.CardDifficulty
import fr.innog.memoryflow.data.mapper.getCorrectAnswers
import fr.innog.memoryflow.ui.cards.CardStudyViewModel
import fr.innog.memoryflow.ui.utils.AndroidUtils

@AndroidEntryPoint
class StudyCardFragment : Fragment() {

    var _binding : FragmentStudyCardBinding? = null

    val binding get() = _binding!!

    private val viewModel: CardStudyViewModel by viewModels()

    private var deckId : Long = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        deckId = requireArguments().getLong(NavKeys.ARG_DECK_ID)

        viewModel.startStudySession(deckId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        _binding = FragmentStudyCardBinding.inflate(inflater, container, false)

        val binding = this.binding

        binding.btnClose.setOnClickListener { view ->
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.validateBtn.setOnClickListener { view ->
            binding.cardWebView.evaluateJavascript(
                "setContent(${JSONObject.quote(viewModel.uiState.value.currentDisplayedCard!!.answer)})",
                null
            )
            binding.layoutValidate.visibility = View.GONE
            binding.layoutTimer.visibility = View.VISIBLE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if(state.isFinished)
                    {
                        AndroidUtils.displayFragment(R.id.fragmentContainerView, StudyFinishFragment.newInstance(state.cardsToStudy.size), parentFragmentManager,false)
                    }
                    if(state.currentDisplayedCard != null)
                    {
                        binding.tvProgress.text = "${state.currentCardIndex+1}/${state.cardsToStudy.size}"

                        displayCard(state.currentDisplayedCard)

                        setupCardInterval()
                    }
                }
            }
        }

        binding.btnAgain.setOnClickListener { view ->
            viewModel.onApplyIntervalTime(CardDifficulty.AGAIN)
        }
        binding.btnHard.setOnClickListener { view ->
            viewModel.onApplyIntervalTime(CardDifficulty.HARD)
        }
        binding.btnGood.setOnClickListener { view ->
            viewModel.onApplyIntervalTime(CardDifficulty.GOOD)
        }
        binding.btnEasy.setOnClickListener { view ->
            viewModel.onApplyIntervalTime(CardDifficulty.EASY)
        }

        setupQuizAction()


        return _binding?.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun formatInterval(seconds: Long) : String
    {
        if(seconds <= 0) return "0s"

        val mins = seconds / 60
        val days = seconds / (60*60*24)
        val months = days / 30

        return when
        {
            mins < 60 -> "${mins}m"
            days < 30 -> "${days}j"
            else -> "${months}mois"
        }
    }

    fun setupCardInterval()
    {
        binding.btnAgain.text = "Encore\n${formatInterval(viewModel.getIntervalPreview(
            CardDifficulty.AGAIN))}"
        binding.btnHard.text = "Difficile\n${formatInterval(viewModel.getIntervalPreview(
            CardDifficulty.HARD))}"
        binding.btnGood.text = "Bien\n${formatInterval(viewModel.getIntervalPreview(CardDifficulty.GOOD))}"
        binding.btnEasy.text = "Facile\n${formatInterval(viewModel.getIntervalPreview(CardDifficulty.EASY))}"
    }

    fun setupQuizAction()
    {
        getQuizButtons().forEach { action ->
            action.setOnClickListener { view ->
                viewModel.validateAnswer(action.text.toString())
            }
        }
    }

    fun updateCardSize()
    {
        val scroll = binding.nestedScrollView
        val card = binding.cardContent
        val quiz = binding.quizContainer
        val timerLayout = binding.layoutTimer

        scroll.doOnLayout {
                val viewportHeight = scroll.height - scroll.paddingTop - scroll.paddingBottom

                val cardMargins = card.marginTop + card.marginBottom

                val quizTotalHeight = if (quiz.isVisible) {
                    val quizMargins = quiz.marginTop + quiz.marginBottom


                    quiz.height + quizMargins + if (timerLayout.isVisible) timerLayout.height - timerLayout.paddingTop - timerLayout.paddingBottom else 0
                } else {
                    binding.layoutValidate.height
                }

                val targetMinHeight =
                    (viewportHeight - quizTotalHeight - cardMargins).coerceAtLeast(0)

                card.minimumHeight = targetMinHeight
        }
    }

    fun resetCardDisplay()
    {
        val binding = this.binding

        binding.layoutTimer.visibility = View.GONE
        binding.quizContainer.visibility = View.GONE
    }

    fun displayCard(card : Card)
    {
        val binding = this.binding

        resetCardDisplay()

        binding.cardContent.setCardBackgroundColor(CardColors.colors[card.cardColorIndex].color)

        with(binding.cardWebView) {
            settings.javaScriptEnabled = true
            binding.cardWebView.setBackgroundColor(Color.TRANSPARENT)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    evaluateJavascript(
                        "setContent(${JSONObject.quote(card.question)})",
                        null
                    )
                }
            }

            loadUrl("file:///android_asset/display.html")
        }

        if(card.hasQuiz())
        {
            viewModel.startQuiz()

            binding.quizContainer.visibility = View.VISIBLE

            binding.layoutValidate.visibility = View.GONE

            val buttons = getQuizButtons()
            val answers = card.getQuizAnswers()
            if(card.getCorrectAnswers().size == 1)
            {
                binding.quizText.text = "Choisissez la bonne réponse"
            }
            else
            {
                binding.quizText.text = "Choisissez les bonnes réponses"
            }

            observeQuizEvents()

            buttons.forEachIndexed { index, button ->
                if(!answers[index].value.isEmpty())
                {
                    button.text = answers[index].value
                }
                else
                {
                    button.visibility = View.GONE
                }
            }
        }
        else
        {
            binding.layoutValidate.visibility = View.VISIBLE
        }

        updateCardSize()
    }


    fun observeQuizEvents()
    {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.quizState.collect { result ->
                    if(result != QuizState.Idle)
                    {
                        onQuizFinished(result)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedAnswers.collect { answers ->
                    getQuizButtons().forEach { button ->
                        if(button.text == viewModel.lastSelectedAnswer)
                        {
                            setQuizButtonState(button, QuizState.Correct)
                        }
                    }
                }
            }
        }
    }

    fun onQuizFinished(state: QuizState)
    {
        if(state == QuizState.Wrong)
        {
            setQuizButtonState(getQuizButton(viewModel.lastSelectedAnswer!!), state)
        }

        binding.cardWebView.evaluateJavascript(
            "setContent(${JSONObject.quote(viewModel.uiState.value.currentDisplayedCard?.answer)})",
            null
        )

        binding.layoutTimer.visibility = View.VISIBLE

        binding.layoutTimer.doOnLayout {
            updateCardSize()
        }
    }

    fun getQuizButton(value : String) : MaterialButton
    {
        return getQuizButtons().find { btn -> btn.text == value }!!
    }

    fun setQuizButtonState(btn : MaterialButton, state : QuizState)
    {
        btn.setBackgroundColor(if(state == QuizState.Correct) Color.GREEN else Color.RED)
    }

    fun getQuizButtons() : List<MaterialButton>
    {
        return listOf<MaterialButton>(binding.quizA, binding.quizB, binding.quizC, binding.quizD)
    }

    companion object {
        @JvmStatic
        fun newInstance(deckId: Long) : StudyCardFragment
        {
            return StudyCardFragment().apply {
                arguments = Bundle().apply {
                    putLong(NavKeys.ARG_DECK_ID, deckId)
                }
            }
        }
    }

}