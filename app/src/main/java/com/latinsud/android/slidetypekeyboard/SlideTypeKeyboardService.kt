package com.latinsud.android.slidetypekeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.sqrt

class SlideTypeKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard

    private var swipeStartX: Float = 0f
    private var swipeStartY: Float = 0f
    private val deleteHandler = Handler()
    private var deleteRunnable: Runnable? = null
    private var isDeleteKeyPressed = false

    private val deleteDelay = 300L // Zeitverzögerung für kontinuierliches Löschen

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.qwerty)
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)

        // Vorschau deaktivieren
        keyboardView.isPreviewEnabled = false

        keyboardView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    swipeStartX = event.x
                    swipeStartY = event.y
                }
                MotionEvent.ACTION_UP -> {
                    val swipeEndX = event.x
                    val swipeEndY = event.y
                    handleKeyPressOrSwipe(swipeStartX, swipeStartY, swipeEndX, swipeEndY)
                }
            }
            false
        }
        return keyboardView
    }

    private fun handleKeyPressOrSwipe(startX: Float, startY: Float, endX: Float, endY: Float) {
        val deltaX = endX - startX
        val deltaY = endY - startY

        val strokeLength = sqrt(deltaX * deltaX + deltaY * deltaY)
        val direction = if (abs(deltaX) > abs(deltaY)) {
            if (deltaX > 0) "rechts" else "links"
        } else {
            if (deltaY > 0) "unten" else "oben"
        }

        val touchedKey = keyboard.keys.find { key ->
            startX >= key.x && startX <= key.x + key.width &&
                    startY >= key.y && startY <= key.y + key.height
        }

        touchedKey?.let { key ->
            val keyLabel = key.label?.toString() ?: return

            if (key.codes.contains(-5)) { // DEL-Taste
                if (strokeLength < 100) deleteSurroundingText()
                return
            }

            if (key.codes.contains(10)) { // Enter-Taste
                handleEnterKey()
                return
            }

            // Zeichenverarbeitung für andere Tasten
            if (strokeLength < 100) {
                currentInputConnection.commitText(keyLabel, 1)
            } else {
                val swipeCharacter = getSwipeCharacter(keyLabel, direction)
                currentInputConnection.commitText(swipeCharacter, 1)
            }
        }
    }

    private fun handleEnterKey() {
        val inputConnection = currentInputConnection
        if (inputConnection != null) {
            val editorInfo = currentInputEditorInfo
            when {
                (editorInfo.imeOptions and android.view.inputmethod.EditorInfo.IME_ACTION_DONE) != 0 -> {
                    inputConnection.performEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_DONE)
                }
                (editorInfo.imeOptions and android.view.inputmethod.EditorInfo.IME_ACTION_SEND) != 0 -> {
                    inputConnection.performEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_SEND)
                }
                (editorInfo.imeOptions and android.view.inputmethod.EditorInfo.IME_ACTION_GO) != 0 -> {
                    inputConnection.performEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_GO)
                }
                else -> {
                    inputConnection.commitText("\n", 1)
                }
            }
        } else {
            requestHideSelf(0)
        }
    }

    private fun deleteSurroundingText() {
        currentInputConnection.deleteSurroundingText(1, 0)
    }

    private fun startContinuousDelete() {
        if (deleteRunnable == null) {
            deleteRunnable = object : Runnable {
                override fun run() {
                    currentInputConnection.deleteSurroundingText(1, 0)
                    deleteHandler.postDelayed(this, 50) // Löschen alle 50ms
                }
            }
        }
        deleteRunnable?.let { deleteHandler.postDelayed(it, deleteDelay) }
    }

    private fun stopContinuousDelete() {
        isDeleteKeyPressed = false
        deleteRunnable?.let { deleteHandler.removeCallbacks(it) }
    }

    private fun getSwipeCharacter(keyLabel: String, direction: String): String {
        return when (keyLabel) {
            "2" -> when (direction) {
                "links" -> "a"
                "oben" -> "b"
                "rechts" -> "c"
                else -> "2"
            }
            "3" -> when (direction) {
                "links" -> "d"
                "oben" -> "e"
                "rechts" -> "f"
                else -> "3"
            }
            "4" -> when (direction) {
                "links" -> "g"
                "oben" -> "h"
                "rechts" -> "i"
                else -> "4"
            }
            "5" -> when (direction) {
                "links" -> "j"
                "oben" -> "k"
                "rechts" -> "l"
                else -> "5"
            }
            "6" -> when (direction) {
                "links" -> "m"
                "oben" -> "n"
                "rechts" -> "o"
                else -> "6"
            }
            "7" -> when (direction) {
                "links" -> "p"
                "oben" -> "q"
                "rechts" -> "r"
                "unten" -> "s"
                else -> "7"
            }
            "8" -> when (direction) {
                "links" -> "t"
                "oben" -> "u"
                "rechts" -> "v"
                else -> "8"
            }
            "9" -> when (direction) {
                "links" -> "w"
                "oben" -> "x"
                "rechts" -> "y"
                "unten" -> "z"
                else -> "9"
            }
            "*" -> when (direction) {
                "links" -> "-"
                "oben" -> "/"
                "rechts" -> "_"
                "unten" -> "@"
                else -> "*"
            }
            "0" -> when (direction) {
                "links" -> "."
                "oben" -> "!"
                "rechts" -> ","
                "unten" -> "?"
                else -> "0"
            }
            else -> keyLabel
        }
    }

    override fun onPress(primaryCode: Int) {
        if (primaryCode == -5) {
            isDeleteKeyPressed = true
            startContinuousDelete()
        }
    }

    override fun onRelease(primaryCode: Int) {
        if (primaryCode == -5) {
            stopContinuousDelete()
        }
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
