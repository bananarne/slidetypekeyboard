package com.latinsud.android.slidetypekeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlin.math.abs
import kotlin.math.sqrt

class SlideTypeKeyboardService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: CustomKeyboardView
    private lateinit var keyboard: Keyboard

    private var swipeStartX: Float = 0f
    private var swipeStartY: Float = 0f
    private val deleteHandler = Handler()
    private var deleteRunnable: Runnable? = null
    private var isCapsLockEnabled = false
    private var isSpecialCharMode = false

    // Key-Locking State
    private var currentPressedKey: Keyboard.Key? = null
    private var isKeyLocked = false

    // Auto-Umlaut Buffer
    private var lastTwoChars = ""

    // Theme System
    private var currentTheme = 0
    private var longPressStartTime = 0L
    private val longPressThreshold = 500L

    private val deleteDelay = 300L

    override fun onCreateInputView(): View {
        keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null) as CustomKeyboardView
        keyboard = Keyboard(this, R.xml.qwerty)
        keyboardView.keyboard = keyboard
        keyboardView.setOnKeyboardActionListener(this)

        keyboardView.isPreviewEnabled = false

        keyboardView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    swipeStartX = event.x
                    swipeStartY = event.y

                    val touchedKey = findKeyAtPosition(event.x, event.y)
                    if (touchedKey != null) {
                        currentPressedKey = touchedKey
                        isKeyLocked = true
                    }
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    false
                }
                MotionEvent.ACTION_UP -> {
                    if (isKeyLocked && currentPressedKey != null) {
                        val swipeEndX = event.x
                        val swipeEndY = event.y

                        val deltaX = swipeEndX - swipeStartX
                        val deltaY = swipeEndY - swipeStartY
                        val strokeLength = sqrt(deltaX * deltaX + deltaY * deltaY)

                        if (strokeLength >= 100) {
                            handleKeyPressOrSwipe(swipeStartX, swipeStartY, swipeEndX, swipeEndY)

                            isKeyLocked = false
                            currentPressedKey = null
                            return@setOnTouchListener true
                        }
                    }

                    isKeyLocked = false
                    currentPressedKey = null
                    false
                }
                MotionEvent.ACTION_CANCEL -> {
                    isKeyLocked = false
                    currentPressedKey = null
                    false
                }
                else -> false
            }
        }

        return keyboardView
    }

    private fun findKeyAtPosition(x: Float, y: Float): Keyboard.Key? {
        return keyboard.keys.find { key ->
            x >= key.x && x <= key.x + key.width &&
                    y >= key.y && y <= key.y + key.height
        }
    }

    private fun handleKeyPressOrSwipe(startX: Float, startY: Float, endX: Float, endY: Float) {
        val deltaX = endX - startX
        val deltaY = endY - startY
        val strokeLength = sqrt(deltaX * deltaX + deltaY * deltaY)

        if (strokeLength < 100) return

        val direction = if (abs(deltaX) > abs(deltaY)) {
            if (deltaX > 0) "rechts" else "links"
        } else {
            if (deltaY > 0) "unten" else "oben"
        }

        val touchedKey = currentPressedKey ?: return
        val originalKeyLabel = getOriginalKeyLabelFromCode(touchedKey.codes[0])

        if (touchedKey.codes.contains(-6)) {
            when (direction) {
                "oben" -> enableCapsLock()
                "unten" -> disableCapsLock()
            }
            return
        }

        val swipeCharacter = if (isSpecialCharMode) {
            getSpecialCharacter(originalKeyLabel, direction)
        } else {
            getSwipeCharacter(originalKeyLabel, direction)
        }
        commitTextWithUmlautCheck(swipeCharacter)
    }

    private fun getOriginalKeyLabelFromCode(code: Int): String {
        return when (code) {
            49 -> "1"
            50 -> "2"
            51 -> "3"
            52 -> "4"
            53 -> "5"
            54 -> "6"
            55 -> "7"
            56 -> "8"
            57 -> "9"
            48 -> "0"
            42 -> "*"
            else -> code.toChar().toString()
        }
    }

    private fun commitTextWithUmlautCheck(text: String) {
        currentInputConnection.commitText(text, 1)

        lastTwoChars += text
        if (lastTwoChars.length > 2) {
            lastTwoChars = lastTwoChars.takeLast(2)
        }

        checkAndReplaceUmlauts()
    }

    private fun checkAndReplaceUmlauts() {
        val replacement = when (lastTwoChars) {
            "ae" -> "ä"
            "oe" -> "ö"
            "ue" -> "ü"
            "Ae" -> "Ä"
            "Oe" -> "Ö"
            "Ue" -> "Ü"
            else -> null
        }

        if (replacement != null) {
            currentInputConnection.deleteSurroundingText(2, 0)
            currentInputConnection.commitText(replacement, 1)
            lastTwoChars = ""
        }
    }

    private fun toggleSpecialCharMode() {
        isSpecialCharMode = !isSpecialCharMode
        keyboardView.isSpecialCharMode = isSpecialCharMode
    }

    private fun enableCapsLock() {
        isCapsLockEnabled = true
        keyboardView.isCapsLockEnabled = isCapsLockEnabled
    }

    private fun openEmojiKeyboard() {
        try {
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            inputMethodManager.showInputMethodPicker()
        } catch (e: Exception) {
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.data = android.net.Uri.parse("content://com.android.inputmethod.latin/emoji")
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e2: Exception) {
                currentInputConnection.commitText("😀", 1)
            }
        }
    }

    private fun cycleTheme() {
        currentTheme = (currentTheme + 1) % 3
        keyboardView.currentTheme = currentTheme

        val themeName = when (currentTheme) {
            0 -> "Dark"
            1 -> "Light"
            2 -> "Modern"
            else -> "Unknown"
        }
        android.widget.Toast.makeText(this, "Theme: $themeName", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun disableCapsLock() {
        isCapsLockEnabled = false
        keyboardView.isCapsLockEnabled = isCapsLockEnabled
    }

    private fun deleteSurroundingText() {
        currentInputConnection.deleteSurroundingText(1, 0)
        if (lastTwoChars.isNotEmpty()) {
            lastTwoChars = lastTwoChars.dropLast(1)
        }
    }

    private fun startContinuousDelete() {
        if (deleteRunnable == null) {
            deleteRunnable = object : Runnable {
                override fun run() {
                    deleteSurroundingText()
                    deleteHandler.postDelayed(this, 50)
                }
            }
        }
        deleteRunnable?.let { deleteHandler.postDelayed(it, deleteDelay) }
    }

    private fun stopContinuousDelete() {
        deleteRunnable?.let { deleteHandler.removeCallbacks(it) }
    }

    private fun isEnterSearch(): Boolean {
        val editorInfo = currentInputEditorInfo ?: return false
        val inputType = editorInfo.inputType and InputType.TYPE_MASK_VARIATION

        return inputType == InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT ||
                inputType == InputType.TYPE_TEXT_VARIATION_FILTER ||
                (editorInfo.imeOptions and EditorInfo.IME_ACTION_SEARCH) != 0 ||
                (editorInfo.imeOptions and EditorInfo.IME_ACTION_SEND) != 0
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
            inputConnection.commitText("\n", 1)
            return
        }

        if (isEnterSearch()) {
            val actionPerformed = when {
                (editorInfo.imeOptions and EditorInfo.IME_ACTION_SEARCH) != 0 -> {
                    inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
                }
                (editorInfo.imeOptions and EditorInfo.IME_ACTION_SEND) != 0 -> {
                    inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEND)
                }
                (editorInfo.imeOptions and EditorInfo.IME_ACTION_DONE) != 0 -> {
                    inputConnection.performEditorAction(EditorInfo.IME_ACTION_DONE)
                }
                else -> false
            }

            if (!actionPerformed) {
                inputConnection.commitText("\n", 1)
            }
        } else {
            inputConnection.commitText("\n", 1)
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
                "unten" -> "ß"
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

    private fun getSpecialCharacter(keyLabel: String, direction: String): String {
        return when (keyLabel) {
            "1" -> when (direction) {
                "oben" -> "#"
                "rechts" -> "]"
                "links" -> "["
                "unten" -> "&"
                "tap" -> "1"
                else -> "1"
            }
            "2" -> when (direction) {
                "oben" -> "="
                "rechts" -> ")"
                "links" -> "("
                "unten" -> "+"
                "tap" -> "2"
                else -> "2"
            }
            "3" -> when (direction) {
                "oben" -> "'"
                "rechts" -> "´"
                "links" -> "`"
                "unten" -> "\""
                "tap" -> "3"
                else -> "3"
            }
            "4" -> when (direction) {
                "oben" -> ":"
                "rechts" -> "/"
                "links" -> "\\"
                "unten" -> ";"
                "tap" -> "4"
                else -> "4"
            }
            "5" -> when (direction) {
                "oben" -> "±"
                "rechts" -> "×"
                "links" -> "~"
                "unten" -> "÷"
                "tap" -> "5"
                else -> "5"
            }
            "6" -> when (direction) {
                "oben" -> "•"
                "unten" -> "°"
                "tap" -> "6"
                else -> "6"
            }
            "7" -> when (direction) {
                "oben" -> "£"
                "rechts" -> "€"
                "unten" -> "$"
                "tap" -> "7"
                else -> "7"
            }
            "8" -> when (direction) {
                "oben" -> "^"
                "rechts" -> ">"
                "links" -> "<"
                "tap" -> "8"
                else -> "8"
            }
            "9" -> when (direction) {
                "oben" -> "¡"
                "rechts" -> "¿"
                "links" -> "|"
                "unten" -> "%"
                "tap" -> "9"
                else -> "9"
            }
            "*" -> when (direction) {
                "tap" -> "*"
                else -> "*"
            }
            "0" -> when (direction) {
                "tap" -> "0"
                else -> "0"
            }
            else -> keyLabel
        }
    }

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        when (primaryCode) {
            -5 -> deleteSurroundingText()
            -6 -> toggleSpecialCharMode()
            -1 -> openEmojiKeyboard() // SYM-Taste öffnet Emoji-Keyboard
            10 -> handleEnterKey()
            32 -> currentInputConnection.commitText(" ", 1)
            in 48..57 -> {
                val number = getOriginalKeyLabelFromCode(primaryCode)
                val output = if (isSpecialCharMode) {
                    getSpecialCharacter(number, "tap")
                } else {
                    number
                }
                commitTextWithUmlautCheck(output)
            }
            42 -> {
                val output = if (isSpecialCharMode) {
                    getSpecialCharacter("*", "tap")
                } else {
                    "*"
                }
                commitTextWithUmlautCheck(output)
            }
            else -> {
                val char = primaryCode.toChar().toString()
                val output = if (isCapsLockEnabled) char.uppercase() else char
                commitTextWithUmlautCheck(output)
            }
        }
    }

    override fun onPress(primaryCode: Int) {
        when (primaryCode) {
            -5 -> startContinuousDelete()
            -1 -> longPressStartTime = System.currentTimeMillis()
        }
    }

    override fun onRelease(primaryCode: Int) {
        when (primaryCode) {
            -5 -> stopContinuousDelete()
            -1 -> {
                val pressDuration = System.currentTimeMillis() - longPressStartTime
                if (pressDuration >= longPressThreshold) {
                    cycleTheme()
                }
            }
        }
    }

    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}