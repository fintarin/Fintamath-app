package com.fintamath

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.fintamath.calculator.CalculatorProcessor
import com.fintamath.history.HistoryFragment
import com.fintamath.history.HistoryStorage
import com.fintamath.keyboard.Keyboard
import com.fintamath.keyboard.KeyboardView
import com.fintamath.keyboard_controller.KeyboardActionListener
import com.fintamath.keyboard_controller.KeyboardSwitcher
import com.fintamath.keyboard_controller.KeyboardType
import com.fintamath.mathview.MathSolutionView
import com.fintamath.mathview.MathTextView
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {

    private lateinit var inTextLayout: ViewGroup
    private lateinit var inText: MathTextView
    private lateinit var solution: MathSolutionView

    private lateinit var calculatorProcessor: CalculatorProcessor

    private lateinit var historyFragment: HistoryFragment

    private val saveToHistoryDelay: Long = 2000
    private var saveToHistoryTask: TimerTask? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inTextLayout = findViewById(R.id.in_text_layout)
        inText = findViewById(R.id.in_text)
        solution = findViewById(R.id.solution)

        calculatorProcessor = CalculatorProcessor (
            { outTexts(it) },
            { startLoading() }
        )

        initKeyboards()
        initFragments()

        inTextLayout.setOnTouchListener { _, event -> touchInText(event) }
        inText.setOnFocusChangeListener { _, state -> callOnInTextFocusChange(state) }

        inText.requestFocus()
    }

    private fun initKeyboards() {
        val keyboards = hashMapOf<KeyboardType, Pair<KeyboardView, Keyboard>>(
            KeyboardType.MainKeyboard to
                    Pair(
                        findViewById(R.id.main_keyboard_view),
                        Keyboard(this, R.xml.keyboard_main)
                    ),
            KeyboardType.LettersKeyboard to
                    Pair(
                        findViewById(R.id.letters_keyboard_view),
                        Keyboard(this, R.xml.keyboard_letters)
                    ),
            KeyboardType.FunctionsKeyboard to
                    Pair(
                        findViewById(R.id.functions_keyboard_view),
                        Keyboard(this, R.xml.keyboard_functions)
                    ),
            KeyboardType.AnalysisKeyboard to
                    Pair(
                        findViewById(R.id.analysis_keyboard_view),
                        Keyboard(this, R.xml.keyboard_analysis)
                    ),
            KeyboardType.LogicKeyboard to
                    Pair(
                        findViewById(R.id.logic_keyboard_view),
                        Keyboard(this, R.xml.keyboard_logic)
                    )
        )

        val currentKeyboardType = KeyboardType.values().first()
        val keyboardSwitcher = KeyboardSwitcher(keyboards, currentKeyboardType)

        val listeners = mutableMapOf<KeyboardType, KeyboardActionListener>()
        for (type in KeyboardType.values()) {
            listeners[type] =
                KeyboardActionListener(calculatorProcessor, keyboardSwitcher, inText)
        }

        keyboards.forEach { (key: KeyboardType, value: Pair<KeyboardView, Keyboard>) ->
            value.first.keyboard = value.second
            value.first.setOnKeyboardActionListener(listeners[key])
        }
    }

    private fun initFragments() {
        historyFragment = HistoryFragment()
        historyFragment.onCalculate = { inText.text = it }
    }

    private fun outTexts(it: List<String>) {
        runOnUiThread {
            saveToHistoryTask?.cancel()
            saveToHistoryTask = null

            if (!inText.isComplete) {
                solution.showIncompleteInput()
            } else if (it.isEmpty() || it.first() == "") {
                solution.hideCurrentView()
            } else if (it.first() == getString(R.string.invalid_input)) {
                solution.showInvalidInput()
            } else {
                solution.showSolution(it)
                saveToHistoryTask = Timer().schedule(saveToHistoryDelay) { callOnSaveToHistory() }
            }
        }
    }

    private fun startLoading() {
        runOnUiThread {
            solution.showLoading()
        }
    }

    private fun touchInText(event: MotionEvent): Boolean {
        return inText.dispatchTouchEvent(MotionEvent.obtain(
            event.downTime, event.eventTime, event.action, event.x, 0f, event.metaState
        ))
    }

    private fun callOnInTextFocusChange(state: Boolean) {
        if (!state) {
            inText.requestFocus()
        }
    }

    private fun callOnSaveToHistory() {
        runOnUiThread {
            HistoryStorage.save(inText.text)
            saveToHistoryTask?.cancel()
            saveToHistoryTask = null
        }
    }

    fun showOptionsMenu(view: View) {
        val popup = PopupMenu(this, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.options_menu, popup.menu)
        popup.show()
    }

    fun showHistoryFragment(view: View) {
        saveToHistoryTask?.cancel()
        saveToHistoryTask?.run()
        saveToHistoryTask = null

        inText.clearFocus()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(android.R.id.content, historyFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun showCameraFragment(view: View) {
        // TODO: implement this
    }
}
 