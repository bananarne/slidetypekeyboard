package com.latinsud.android.slidetypekeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlin.math.abs
import kotlin.math.sqrt

class SlideTypeKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var keyboard: Keyboard

    private var swipeStartX: Float = 0f
    private var swipeStartY: Float = 0f
    private val deleteHandler = Handler()
    private var deleteRunnable: Runnable? = null
    private var isCapsLockEnabled = false

    private val deleteDelay = 300L

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

            if (key.codes.contains(-6)) { // ALT-Taste -> Capslock
                toggleCapsLock()
                return
            }
            if (key.codes.contains(10)) { // Enter-Taste
                handleEnterKey()
                return
            }
            if (strokeLength < 100) {
                val output = if (isCapsLockEnabled) keyLabel.uppercase() else keyLabel
                currentInputConnection.commitText(output, 1)
            } else {
                val swipeCharacter = getSwipeCharacter(keyLabel, direction)
                currentInputConnection.commitText(swipeCharacter, 1)
            }
        }
    }

    private fun toggleCapsLock() {
        isCapsLockEnabled = !isCapsLockEnabled
        updateKeyboardCaps()
    }

    private fun updateKeyboardCaps() {
        keyboard.keys.forEach { key ->
            val label = key.label?.toString()
            if (label != null && label.length == 1 && label[0].isLetter()) {
                key.label = if (isCapsLockEnabled) label.uppercase() else label.lowercase()
            }
        }
        keyboardView.invalidateAllKeys()
    }

    private fun deleteSurroundingText() {
        currentInputConnection.deleteSurroundingText(1, 0)
    }

    private fun startContinuousDelete() {
        if (deleteRunnable == null) {
            deleteRunnable = object : Runnable {
                override fun run() {
                    currentInputConnection.deleteSurroundingText(1, 0)
                    deleteHandler.postDelayed(this, 50)
                }
            }
        }
        deleteRunnable?.let { deleteHandler.postDelayed(it, deleteDelay) }
    }

    private fun stopContinuousDelete() {
        deleteRunnable?.let { deleteHandler.removeCallbacks(it) }
    }

    private fun handleEnterKey() {
        val inputConnection = currentInputConnection
        val editorInfo = currentInputEditorInfo

        if (inputConnection == null) {
            Log.e("KeyboardHandler", "InputConnection ist null")
            return
        }

        if (editorInfo == null) {
            Log.e("KeyboardHandler", "EditorInfo ist null")
            inputConnection.commitText("\n", 1) // Fallback
            return
        }

        // Debugging-Logs für Kontextinformationen
        Log.d("KeyboardHandler", "IME Options: ${editorInfo.imeOptions}")
        Log.d("KeyboardHandler", "Input Type: ${editorInfo.inputType}")
        Log.d("KeyboardHandler", "Package: ${editorInfo.packageName}")

        // Priorisierung von IME_ACTION_SEND vor IME_ACTION_DONE
        val actionPerformed = when {
            (editorInfo.imeOptions and EditorInfo.IME_ACTION_SEND) != 0 -> {
                Log.d("KeyboardHandler", "IME_ACTION_SEND erkannt")
                inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND)
            }
            (editorInfo.imeOptions and EditorInfo.IME_ACTION_DONE) != 0 -> {
                Log.d("KeyboardHandler", "IME_ACTION_DONE erkannt")
                inputConnection.performEditorAction(EditorInfo.IME_ACTION_DONE)
            }
            else -> {
                Log.w("KeyboardHandler", "Keine definierte Aktion erkannt, Fallback auf Zeilenumbruch")
                false
            }
        }

        // Fallback auf Zeilenumbruch, wenn keine Aktion sichtbar verarbeitet wird
        if (!actionPerformed) {
            Log.w("KeyboardHandler", "Aktion fehlgeschlagen, Fallback auf Zeilenumbruch")
            inputConnection.commitText("\n", 1)
        } else {
            Log.d("KeyboardHandler", "Aktion erfolgreich ausgeführt")
        }
    }

    private fun getSwipeCharacter(keyLabel: String, direction: String): String {
        return when (keyLabel) {
            "2" -> when (direction) {
                "links" -> if (isCapsLockEnabled) "A" else "a"
                "oben" -> if (isCapsLockEnabled) "B" else "b"
                "rechts" -> if (isCapsLockEnabled) "C" else "c"
                else -> "2"
            }
            "3" -> when (direction) {
                "links" -> if (isCapsLockEnabled) "D" else "d"
                "oben" -> if (isCapsLockEnabled) "E" else "e"
                "rechts" -> if (isCapsLockEnabled) "F" else "f"
                else -> "3"
            }
            "4" -> when (direction) {
                "links" -> if (isCapsLockEnabled) "G" else "g"
                "oben" -> if (isCapsLockEnabled) "H" else "h"
                "rechts" -> if (isCapsLockEnabled) "I" else "i"
                else -> "4"
            }
            "5" -> when (direction) {
                "links" -> if (isCapsLockEnabled) "J" else "j"
                "oben" -> if (isCapsLockEnabled) "K" else "k"
                "rechts" -> if (isCapsLockEnabled) "L" else "l"
                else -> "5"
            }
            "6" -> when (direction) {
                "links" -> if (isCapsLockEnabled) "M" else "m"
                "oben" -> if (isCapsLockEnabled) "N" else "n"
                "rechts" -> if (isCapsLockEnabled) "O" else "o"
                else -> "6"
            }
            "7" -> when (direction) {
                "links" -> if (isCapsLockEnabled) "P" else "p"
                "oben" -> if (isCapsLockEnabled) "Q" else "q"
                "rechts" -> if (isCapsLockEnabled) "R" else "r"
                "unten" -> if (isCapsLockEnabled) "S" else "s"
                else -> "7"
            }
            "8" -> when (direction) {
                "links" -> if (isCapsLockEnabled) "T" else "t"
                "oben" -> if (isCapsLockEnabled) "U" else "u"
                "rechts" -> if (isCapsLockEnabled) "V" else "v"
                else -> "8"
            }
            "9" -> when (direction) {
                "links" -> if (isCapsLockEnabled) "W" else "w"
                "oben" -> if (isCapsLockEnabled) "X" else "x"
                "rechts" -> if (isCapsLockEnabled) "Y" else "y"
                "unten" -> if (isCapsLockEnabled) "Z" else "z"
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
