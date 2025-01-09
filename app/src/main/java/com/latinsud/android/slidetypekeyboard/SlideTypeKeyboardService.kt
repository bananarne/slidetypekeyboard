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
    private var isShifted = false
    private var flipFlop = false;

    private var coordinateMap: MutableMap<Pair<Pair<Int, Int>, Pair<Int, Int>>, Keyboard.Key> = mutableMapOf<Pair<Pair<Int, Int>, Pair<Int, Int>>, Keyboard.Key>();

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as KeyboardView
        keyboard = Keyboard(this, R.xml.qwerty)
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)

        keyboard.keys.forEach { key -> coordinateMap[Pair(Pair(key.x, key.y),Pair(key.width, key.height))] = key };


        // Swipe-Logik hinzufügen
        keyboardView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP || MotionEvent.ACTION_DOWN == event.action) Log.d("xx","yowwwww $event.x, $event.y");

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


        val keyData = this.coordinateMap.entries.find { (coords, _) ->
            val (pos, size) = coords;
            val (sx, sy) = pos;
            val (ex, ey) = size;

            (startX >= sx && startX <= sx + ex) && (startY >= sy && startY <= sy + ey)

        }

        val strokeLength = sqrt((endX - startX) * (endX - startX) + (endY - startY) * (endY - startY))
        val dx = endX - startX
        val dy = endY - startY

        val direction = if (abs(dx) > abs(dy)) {
            if (dx > 0) "Rechts" else "Links"
        } else {
            if (dy > 0) "Unten" else "Oben"
        }

        if (keyData != null) {
            val (_, key) = keyData
            val keyLabel = key.label?.toString() ?: return
            Log.d("xxx", "$keyLabel");
            if (strokeLength < 140) {
                currentInputConnection.commitText(keyLabel[0].toString(), 1)
            } else {
                currentInputConnection.commitText(getSwipeCharacter(keyLabel, direction), 1)
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
                else -> "4"
            }
            "6" -> when (direction) {
                "Links" -> "M"
                "Oben" -> "N"
                "Rechts" -> "O"
                else -> "4"
            }
            "7" -> when (direction) {
                "Links" -> "P"
                "Oben" -> "Q"
                "Rechts" -> "R"
                else -> "4"
            }
            "8" -> when (direction) {
                "Links" -> "S"
                "Oben" -> "T"
                "Rechts" -> "U"
                else -> "4"
            }
            "9" -> when (direction) {
                "Links" -> "V"
                "Oben" -> "W"
                "Rechts" -> "X"
                else -> "4"
            }
            // Wiederhole dies für alle anderen Keys ...
            else -> keyLabel
        }
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {

    }

    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
