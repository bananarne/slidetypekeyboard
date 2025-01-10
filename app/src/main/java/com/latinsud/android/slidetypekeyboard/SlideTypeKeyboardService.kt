package com.latinsud.android.slidetypekeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.sqrt

class SlideTypeKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard

    private var swipeStartX: Float = 0f
    private var swipeStartY: Float = 0f

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

        val strokeLength = sqrt(deltaX * deltaX + deltaY * deltaY)
        val direction = if (abs(deltaX) > abs(deltaY)) {
            if (deltaX > 0) "Rechts" else "Links"
        } else {
            if (deltaY > 0) "Unten" else "Oben"
        }

        val touchedKey = keyboard.keys.find { key ->
            startX >= key.x && startX <= key.x + key.width &&
                    startY >= key.y && startY <= key.y + key.height
        }

        touchedKey?.let { key ->
            val keyLabel = key.label?.toString() ?: return
            if (strokeLength < 100) {
                if (keyLabel == "DEL") {
                    currentInputConnection.deleteSurroundingText(1, 0) // Löscht den letzten Buchstaben
                } else {
                    currentInputConnection.commitText(keyLabel, 1)
                }
            } else {
                val swipeCharacter = getSwipeCharacter(keyLabel, direction)
                currentInputConnection.commitText(swipeCharacter, 1)
            }
        }
    }

    private fun getSwipeCharacter(keyLabel: String, direction: String): String {
        return when (keyLabel) {
            "2" -> when (direction) {
                "Links" -> "A"
                "Oben" -> "B"
                "Rechts" -> "C"
                else -> "2"
            }
            "3" -> when (direction) {
                "Links" -> "D"
                "Oben" -> "E"
                "Rechts" -> "F"
                else -> "3"
            }
            "4" -> when (direction) {
                "Links" -> "G"
                "Oben" -> "H"
                "Rechts" -> "I"
                else -> "4"
            }
            "5" -> when (direction) {
                "Links" -> "J"
                "Oben" -> "K"
                "Rechts" -> "L"
                else -> "5"
            }
            "6" -> when (direction) {
                "Links" -> "M"
                "Oben" -> "N"
                "Rechts" -> "O"
                else -> "6"
            }
            "7" -> when (direction) {
                "Links" -> "P"
                "Oben" -> "Q"
                "Rechts" -> "R"
                "Unten" -> "S"
                else -> "7"
            }
            "8" -> when (direction) {
                "Links" -> "T"
                "Oben" -> "U"
                "Rechts" -> "V"
                else -> "8"
            }
            "9" -> when (direction) {
                "Links" -> "W"
                "Oben" -> "X"
                "Rechts" -> "Y"
                "Unten" -> "Z"
                else -> "9"
            }
            "*" -> when (direction) {
                "Links" -> "-"
                "Oben" -> "/"
                "Rechts" -> "_"
                "Unten" -> "@"
                else -> "*"
            }
            "0" -> when (direction) {
                "Links" -> "."
                "Oben" -> "!"
                "Rechts" -> ","
                "Unten" -> "?"
                else -> "0"
            }
            else -> keyLabel
        }
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
