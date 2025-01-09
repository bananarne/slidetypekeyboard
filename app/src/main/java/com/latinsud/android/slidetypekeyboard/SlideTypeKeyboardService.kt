package com.latinsud.android.slidetypekeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.view.MotionEvent
import android.view.View

class SlideTypeKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard

    private var swipeStartX: Float = 0f
    private var swipeStartY: Float = 0f
    private var isShifted = false

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.qwerty)
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)

        // Swipe-Logik hinzufügen
        keyboardView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    swipeStartX = event.x
                    swipeStartY = event.y
                }
                MotionEvent.ACTION_UP -> {
                    val swipeEndX = event.x
                    val swipeEndY = event.y
                    handleSwipe(swipeStartX, swipeStartY, swipeEndX, swipeEndY)
                }
            }
            false
        }
        return keyboardView
    }

    private fun handleSwipe(startX: Float, startY: Float, endX: Float, endY: Float) {
        val deltaX = endX - startX
        val deltaY = endY - startY

        val key = keyboardView.keyboard.keys.find { it.isInside(startX.toInt(), startY.toInt()) }
        key?.let {
            val keyLabel = key.label?.toString() ?: return
            val output = when {
                Math.abs(deltaX) > Math.abs(deltaY) -> {
                    if (deltaX > 0) getSwipeCharacter(keyLabel, "Rechts") else getSwipeCharacter(keyLabel, "Links")
                }
                Math.abs(deltaY) > Math.abs(deltaX) -> {
                    if (deltaY > 0) getSwipeCharacter(keyLabel, "Unten") else getSwipeCharacter(keyLabel, "Oben")
                }
                else -> keyLabel[0].toString()
            }
            currentInputConnection.commitText(output, 1)
        }
    }

    private fun getSwipeCharacter(keyLabel: String, direction: String): String {
        return when (keyLabel) {
            "2\nABC" -> when (direction) {
                "Links" -> "A"
                "Oben" -> "B"
                "Rechts" -> "C"
                else -> "2"
            }
            "3\nDEF" -> when (direction) {
                "Links" -> "D"
                "Oben" -> "E"
                "Rechts" -> "F"
                else -> "3"
            }
            "4\nGHI" -> when (direction) {
                "Links" -> "G"
                "Oben" -> "H"
                "Rechts" -> "I"
                else -> "4"
            }
            // Wiederhole dies für alle anderen Keys ...
            else -> keyLabel
        }
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        if (primaryCode == -6) {
            isShifted = !isShifted
            keyboard.isShifted = isShifted
            keyboardView.invalidateAllKeys()
        } else {
            currentInputConnection.commitText(primaryCode.toChar().toString(), 1)
        }
    }

    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
