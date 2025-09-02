package com.latinsud.android.slidetypekeyboard

import android.content.Context
import android.graphics.*
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet

class CustomKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : KeyboardView(context, attrs) {

    var currentTheme = 0
        set(value) {
            field = value
            updateThemeColors()
            invalidate()
        }

    // Theme-Farben werden dynamisch gesetzt
    private var bgColor = Color.DKGRAY
    private var borderColor = Color.GRAY
    private var numberColor = Color.YELLOW
    private var letterColor = Color.WHITE
    private var specialColor = Color.CYAN
    private var keyTextColor = Color.WHITE

    var isSpecialCharMode = false
        set(value) {
            field = value
            invalidate()
        }

    var isCapsLockEnabled = false
        set(value) {
            field = value
            invalidate()
        }

    private val numberPaint = Paint().apply {
        color = Color.YELLOW
        textAlign = Paint.Align.CENTER
        textSize = 75f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val letterPaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 61f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val specialCharPaint = Paint().apply {
        color = Color.CYAN
        textAlign = Paint.Align.CENTER
        textSize = 55f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    init {
        updateThemeColors()
    }

    private fun updateThemeColors() {
        when (currentTheme) {
            0 -> { // Dark Theme
                bgColor = Color.DKGRAY
                borderColor = Color.GRAY
                numberColor = Color.YELLOW
                letterColor = Color.WHITE
                specialColor = Color.CYAN
                keyTextColor = Color.WHITE
            }
            1 -> { // Light Theme
                bgColor = Color.WHITE
                borderColor = Color.DKGRAY
                numberColor = Color.rgb(255, 140, 0) // Orange
                letterColor = Color.BLACK
                specialColor = Color.rgb(0, 100, 200) // Blau
                keyTextColor = Color.BLACK
            }
            2 -> { // Modern Theme
                bgColor = Color.rgb(45, 45, 50)
                borderColor = Color.rgb(80, 80, 90)
                numberColor = Color.rgb(0, 200, 150) // Türkis
                letterColor = Color.rgb(200, 200, 210)
                specialColor = Color.rgb(255, 100, 150) // Pink
                keyTextColor = Color.rgb(200, 200, 210)
            }
        }

        // Paint-Objekte aktualisieren
        numberPaint.color = numberColor
        letterPaint.color = letterColor
        specialCharPaint.color = specialColor
    }

    override fun onDraw(canvas: Canvas) {
        // Komplett eigenes Drawing
        drawCustomLayout(canvas)
    }

    private fun drawCustomLayout(canvas: Canvas) {
        if (keyboard?.keys == null) return

        try {
            for (key in keyboard.keys) {
                // Tastenhintergrund
                drawKeyBackground(canvas, key)

                // Inhalt je nach Taste
                when (key.codes[0]) {
                    in 50..57 -> drawNumberKey(canvas, key) // Zahlen 2-9
                    48, 42 -> drawSpecialNumberKey(canvas, key) // 0 und *
                    else -> drawSimpleKey(canvas, key) // Alle anderen
                }
            }
        } catch (e: Exception) {
            // Fallback zum Standard-Layout
            super.onDraw(canvas)
        }
    }

    private fun drawKeyBackground(canvas: Canvas, key: Keyboard.Key) {
        val keyRect = RectF(
            key.x.toFloat() + 2,
            key.y.toFloat() + 2,
            (key.x + key.width).toFloat() - 2,
            (key.y + key.height).toFloat() - 2
        )

        val backgroundPaint = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
        }

        val borderPaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val cornerRadius = if (currentTheme == 2) 12f else 8f

        canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, backgroundPaint)
        canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, borderPaint)
    }

    private fun drawNumberKey(canvas: Canvas, key: Keyboard.Key) {
        val centerX = key.x + key.width / 2f
        val centerY = key.y + key.height / 2f
        val number = key.codes[0].toChar().toString()

        // Gelbe Zahl in der Mitte
        canvas.drawText(number, centerX, centerY + 8f, numberPaint)

        if (isSpecialCharMode) {
            drawSpecialChars(canvas, number, centerX, centerY)
        } else {
            drawLetters(canvas, number, centerX, centerY)
        }
    }

    private fun drawLetters(canvas: Canvas, number: String, centerX: Float, centerY: Float) {
        val letters = getLettersForNumber(number)

        // Links
        if (letters.isNotEmpty()) {
            val letter = if (isCapsLockEnabled) letters[0].uppercase() else letters[0]
            canvas.drawText(letter, centerX - 60f, centerY + 8f, letterPaint)
        }

        // Oben
        if (letters.size > 1) {
            val letter = if (isCapsLockEnabled) letters[1].uppercase() else letters[1]
            canvas.drawText(letter, centerX, centerY - 50f, letterPaint)
        }

        // Rechts
        if (letters.size > 2) {
            val letter = if (isCapsLockEnabled) letters[2].uppercase() else letters[2]
            canvas.drawText(letter, centerX + 60f, centerY + 8f, letterPaint)
        }

        // Unten (für 7 und 9)
        if (letters.size > 3) {
            val letter = if (isCapsLockEnabled) letters[3].uppercase() else letters[3]
            canvas.drawText(letter, centerX, centerY + 65f, letterPaint)
        }
    }

    private fun drawSpecialChars(canvas: Canvas, number: String, centerX: Float, centerY: Float) {
        val specials = getSpecialCharsForNumber(number)

        specials["links"]?.let {
            canvas.drawText(it, centerX - 60f, centerY + 8f, specialCharPaint)
        }
        specials["oben"]?.let {
            canvas.drawText(it, centerX, centerY - 50f, specialCharPaint)
        }
        specials["rechts"]?.let {
            canvas.drawText(it, centerX + 60f, centerY + 8f, specialCharPaint)
        }
        specials["unten"]?.let {
            canvas.drawText(it, centerX, centerY + 65f, specialCharPaint)
        }
    }

    private fun drawSpecialNumberKey(canvas: Canvas, key: Keyboard.Key) {
        val centerX = key.x + key.width / 2f
        val centerY = key.y + key.height / 2f
        val number = key.codes[0].toChar().toString()

        // Gelbe Zahl in der Mitte
        canvas.drawText(number, centerX, centerY + 8f, numberPaint)

        // Swipe-Optionen für * und 0
        val swipeChars = getSwipeCharsForKey(number)
        swipeChars["links"]?.let {
            canvas.drawText(it, centerX - 60f, centerY + 8f, letterPaint)
        }
        swipeChars["oben"]?.let {
            canvas.drawText(it, centerX, centerY - 50f, letterPaint)
        }
        swipeChars["rechts"]?.let {
            canvas.drawText(it, centerX + 60f, centerY + 8f, letterPaint)
        }
        swipeChars["unten"]?.let {
            canvas.drawText(it, centerX, centerY + 65f, letterPaint)
        }
    }

    private fun drawSimpleKey(canvas: Canvas, key: Keyboard.Key) {
        val centerX = key.x + key.width / 2f
        val centerY = key.y + key.height / 2f

        val textPaint = Paint().apply {
            color = keyTextColor // Verwendet Theme-Farbe
            textAlign = Paint.Align.CENTER
            textSize = 42f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val label = when (key.codes[0]) {
            49 -> "1"
            51 -> "3"
            -5 -> "DEL"
            52 -> "4"
            53 -> "5"
            54 -> "6"
            -1 -> "😀"
            55 -> "7"
            56 -> "8"
            57 -> "9"
            -6 -> {
                when {
                    isSpecialCharMode -> "SYM"
                    isCapsLockEnabled -> "ON"
                    else -> "off"
                }
            }
            42 -> "*"
            48 -> "0"
            32 -> "SPACE"
            10 -> "ENTER"
            else -> key.label?.toString() ?: ""
        }

        canvas.drawText(label, centerX, centerY + 8f, textPaint)
    }

    private fun getLettersForNumber(number: String): List<String> {
        return when (number) {
            "2" -> listOf("a", "b", "c")
            "3" -> listOf("d", "e", "f")
            "4" -> listOf("g", "h", "i")
            "5" -> listOf("j", "k", "l")
            "6" -> listOf("m", "n", "o")
            "7" -> listOf("p", "q", "r", "s")
            "8" -> listOf("t", "u", "v", "ß")
            "9" -> listOf("w", "x", "y", "z")
            else -> emptyList()
        }
    }

    private fun getSpecialCharsForNumber(number: String): Map<String, String> {
        return when (number) {
            "2" -> mapOf("links" to "(", "oben" to "=", "rechts" to ")", "unten" to "+")
            "3" -> mapOf("links" to "`", "oben" to "'", "rechts" to "´", "unten" to "\"")
            "4" -> mapOf("links" to "\\", "oben" to ":", "rechts" to "/", "unten" to ";")
            "5" -> mapOf("links" to "~", "oben" to "±", "rechts" to "×", "unten" to "÷")
            "6" -> mapOf("oben" to "•", "unten" to "°")
            "7" -> mapOf("oben" to "£", "rechts" to "€", "unten" to "$")
            "8" -> mapOf("links" to "<", "oben" to "^", "rechts" to ">")
            "9" -> mapOf("links" to "|", "oben" to "¡", "rechts" to "¿", "unten" to "%")
            else -> emptyMap()
        }
    }

    private fun getSwipeCharsForKey(key: String): Map<String, String> {
        return when (key) {
            "0" -> mapOf("links" to ".", "oben" to "!", "rechts" to ",", "unten" to "?")
            "*" -> mapOf("links" to "-", "oben" to "/", "rechts" to "_", "unten" to "@")
            else -> emptyMap()
        }
    }
}