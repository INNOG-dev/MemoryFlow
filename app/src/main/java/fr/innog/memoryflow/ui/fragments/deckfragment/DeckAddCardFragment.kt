package fr.innog.memoryflow.ui.fragments.deckfragment

import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.forEachIndexed
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import fr.innog.memoryflow.R
import fr.innog.memoryflow.core.navigation.NavKeys
import fr.innog.memoryflow.databinding.FragmentAddCardBinding
import fr.innog.memoryflow.ui.cards.DeckAddCardViewModel
import fr.innog.memoryflow.data.local.model.CardColor
import fr.innog.memoryflow.data.local.model.CardColors
import fr.innog.memoryflow.data.local.model.CardSide
import fr.innog.memoryflow.ui.utils.dp
import kotlinx.coroutines.launch
import org.json.JSONObject

@AndroidEntryPoint
class DeckAddCardFragment : Fragment() {

    var _binding : FragmentAddCardBinding? = null

    val binding get() = _binding!!

    var selectedColor : CardColor? = null
    var selectedColorView : View? = null

    private val viewModel: DeckAddCardViewModel by viewModels()

    enum class UIState {
        EDIT,
        ADD
    }

    lateinit var uiState : UIState

    private var deckId: Long = -1

    private var cardId: Long = -1

    class RichEditorBridge(private val fragment: DeckAddCardFragment, private val onChange: (String) -> Unit) {

        @JavascriptInterface
        fun pickImage() {
            fragment.openImagePicker()
        }

        @JavascriptInterface
        fun onEditorChanged(html: String) {
            Handler(Looper.getMainLooper()).post {
                onChange(html)
            }
        }
    }


    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { insertImageInEditor(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        deckId = requireArguments().getLong(NavKeys.ARG_DECK_ID)

        uiState = UIState.entries.toTypedArray()[requireArguments().getInt(NavKeys.ARG_UI_STATE)]

        if(uiState == UIState.EDIT)
        {
            cardId = requireArguments().getLong(NavKeys.ARG_CARD_ID)
            viewModel.setEditingCard(cardId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddCardBinding.inflate(inflater, container, false)

        val binding = _binding!!



        binding.richEditor.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        binding.richEditor.apply {
            // WebSettings
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            // WebView properties
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_ALWAYS

            if(uiState == UIState.ADD)
            {
                binding.title.text = "Ajouter une carte"
            }
            else
            {
                binding.title.text = "Edition de la carte"

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {

                        viewLifecycleOwner.lifecycleScope.launch {

                                val card = viewModel.currentEditingCard!!

                                loadSide(CardSide.RECTO)

                                selectColor(binding.colorPicker.getChildAt(card.cardColorIndex)!!,
                                    CardColors.colors[card.cardColorIndex])

                                if(viewModel.hasQuiz())
                                {
                                    binding.quizSwitch.isChecked = true
                                    binding.quizContainer.visibility = View.VISIBLE
                                }

                                val quizElementsUI = listOf<Pair<TextInputEditText, ImageView>>(Pair(binding.answerA, binding.starA), Pair(binding.answerB, binding.starB), Pair(binding.answerC, binding.starC), Pair(binding.answerD, binding.starD))
                                val answers = viewModel.answers

                                answers.forEachIndexed { index, answer ->
                                    val pair = quizElementsUI[index]
                                    pair.first.setText(answer.value)

                                    if(answer.isCorrect)
                                    {
                                        pair.second.setImageResource(R.drawable.star_filled)
                                        pair.second.tag = true
                                    }
                                    else
                                    {
                                        pair.second.setImageResource(R.drawable.star)
                                        pair.second.tag = false
                                    }
                                }
                        }
                    }
                }

            }


            // Fix scroll tactile + accessibilité
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    v.performClick()
                }
                v.parent?.requestDisallowInterceptTouchEvent(true)
                false
            }

        }

        binding.richEditor.loadUrl("file:///android_asset/editor.html")


        binding.quizSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateQuizDisplay(binding)
            updateButtonState()
        }


        binding.richEditor.addJavascriptInterface(
            RichEditorBridge(this) { html ->
                viewModel.setContent(html, viewModel.data.currentSide)
                updateButtonState()
            },
            "Android"
        )

        binding.cardTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener
        {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateCardDisplay(binding)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

        binding.saveButton.isEnabled = false
        binding.saveButton.setOnClickListener { view ->

            if(uiState == UIState.EDIT)
            {
                viewModel.editCard()
            }
            else
            {
                viewModel.addCard()
            }

            parentFragmentManager.popBackStack()
        }

        setupColorPicker()

        setupQuizUI()

        return binding.root
    }


    fun updateButtonState()
    {
        var enabled = true
        if(binding.quizSwitch.isChecked)
        {
            val answers = viewModel.answers

            if(viewModel.getFilledAnswerCount() < 2)
            {
                enabled = false
            }
            else if(viewModel.getCorrectAnswerCount() < 1)
            {
                enabled = false
            }
        }

        if(viewModel.isContentEmpty(CardSide.RECTO) ||viewModel.isContentEmpty(CardSide.VERSO))
        {
            enabled = false
        }

        binding.saveButton.isEnabled = enabled
    }

    fun setupQuizUI()
    {
        val _binding = binding

        binding.answerA.addTextChangedListener {
            viewModel.onQuizAnswerFilled(0,binding.answerA.text.toString())
            updateButtonState()
        }
        binding.answerB.addTextChangedListener {
            viewModel.onQuizAnswerFilled(1,binding.answerB.text.toString())
            updateButtonState()
        }
        binding.answerC.addTextChangedListener {
            viewModel.onQuizAnswerFilled(2,binding.answerC.text.toString())
            updateButtonState()
        }
        binding.answerD.addTextChangedListener {
            viewModel.onQuizAnswerFilled(3,binding.answerD.text.toString())
            updateButtonState()
        }

        _binding.quizContainer.forEachIndexed { index, view ->
            if (view is LinearLayout) {
                view.forEachIndexed { index, view ->
                    if(view is ImageView)
                    {
                        view.setOnClickListener {
                            val isSelected = view.tag as? Boolean ?: false

                            if (isSelected) {
                                view.setImageResource(R.drawable.star)
                                view.tag = false
                            } else {
                                view.setImageResource(R.drawable.star_filled)
                                view.tag = true
                            }

                            viewModel.onQuizAnswerChecked(index, !isSelected)
                            updateButtonState()
                        }
                    }
                }
            }
        }
    }


    fun createColorCircle(
        fillColor: Int,
        strokeColor: Int? = null,
        strokeWidthDp: Int = 0
    ): GradientDrawable {

        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(fillColor)

            if (strokeColor != null && strokeWidthDp > 0) {
                setStroke(strokeWidthDp.dp, strokeColor)
            }
        }
    }

    fun selectColor(view : View, cardColor: CardColor) {
        if(selectedColor != null)
        {
            selectedColorView?.background = createColorCircle(selectedColor!!.color,  ContextCompat.getColor(requireContext(), R.color.mf_text_primary), 0.dp)
        }

        selectedColorView = view
        selectedColorView?.background = createColorCircle(cardColor.color,  ContextCompat.getColor(requireContext(), R.color.mf_text_primary), 1.dp)

        selectedColor = cardColor

        viewModel.onColorSelected(CardColors.colors.indexOf(cardColor))

        binding.cardPreview.setCardBackgroundColor(cardColor.color)
    }

    fun setupColorPicker()
    {
        binding.colorPicker.removeAllViews()

        CardColors.colors.forEachIndexed { index, cardColor ->
            val view = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(28.dp, 28.dp).apply {
                    marginEnd = 12.dp
                }

                background = createColorCircle(cardColor.color, null,0)

                isClickable = true
            }

            if(index == 0) {
                selectColor(view, cardColor)
            }

            view.setOnClickListener { view ->
                selectColor(view, cardColor)
            }

            binding.colorPicker.addView(view)
        }

    }

    fun updateQuizDisplay(binding: FragmentAddCardBinding)
    {
        if(binding.quizSwitch.isChecked)
        {
            binding.quizContainer.visibility = View.VISIBLE
        }
        else
        {
            binding.quizContainer.visibility = View.GONE
        }
    }

    fun updateCardDisplay(binding: FragmentAddCardBinding)
    {
        updateQuizDisplay(binding)

        switchSide(CardSide.entries[binding.cardTabs.selectedTabPosition])
    }

    fun saveCurrentSide(onDone: () -> Unit) {
        binding.richEditor.evaluateJavascript("getContent();") { value ->

            // value = String JSON renvoyée par WebView
            val html = Gson().fromJson(value, String::class.java)

            if (viewModel.data.currentSide == CardSide.RECTO) {
                viewModel.data.rectoHtml = html
            } else {
                viewModel.data.versoHtml = html
            }

            onDone()
        }
    }

    fun loadSide(side: CardSide) {
        val html = if (side == CardSide.RECTO) viewModel.data.rectoHtml else viewModel.data.versoHtml

        binding.richEditor.evaluateJavascript(
            "setContent(${JSONObject.quote(html)})",
            null
        )
    }

    fun switchSide(newSide: CardSide) {
        saveCurrentSide {
            viewModel.setSide(newSide)

            loadSide(newSide)

            val placeholder = if (newSide == CardSide.RECTO) {
                "Question (recto)"
            } else {
                "Réponse (verso)"
            }

            setEditorContent(placeholder)
        }
    }

    fun setEditorContent(html: String)
    {
        binding.richEditor.evaluateJavascript(
            "setPlaceholder('${html}');",
            null
        )
    }

    fun openImagePicker() {
        pickImageLauncher.launch("image/*")
    }

    fun insertImageInEditor(uri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: return
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val dataUri = "data:image/png;base64,$base64"

        binding.richEditor.evaluateJavascript(
            "insertImage('$dataUri');",
            null
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {


        @JvmStatic
        fun newInstance(state : UIState, deckId: Long, cardId : Long = -1) : DeckAddCardFragment {
            return DeckAddCardFragment().apply {
                arguments = Bundle().apply {
                    putLong(NavKeys.ARG_DECK_ID, deckId)
                    putInt(NavKeys.ARG_UI_STATE, state.ordinal)
                    if(state == UIState.EDIT)
                    {
                        putLong(NavKeys.ARG_CARD_ID, cardId)
                    }
                }
            }
        }
    }
}