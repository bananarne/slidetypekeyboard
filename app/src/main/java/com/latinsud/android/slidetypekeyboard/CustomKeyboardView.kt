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

    var isSpecialCharMode = false
        set(value) {
            field = value
            invalidate() // Automatisches Neuzeichnen
        }

    var isCapsLockEnabled = false
        set(value) {
            field = value
            invalidate() // Automatisches Neuzeichnen
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
        textSize = 60f
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

    override fun onDraw(canvas: Canvas) {
        // NUR unser Custom-Drawing, kein Standard-Layout
        drawCustomLayout(canvas)
    }

    private fun drawCustomLayout(canvas: Canvas) {
        if (keyboard?.keys == null) return

        try {
            for (key in keyboard.keys) {
                // Tastenhintergrund zeichnen
                drawKeyBackground(canvas, key)

                // Inhalt je nach Taste
                when (key.codes[0]) {
                    in 50..57 -> drawNumberKey(canvas, key) // Zahlen 2-9
                    48, 42 -> drawSpecialNumberKey(canvas, key) // 0 und *
                    49 -> drawSimpleKey(canvas, key, "1") // 1 bleibt einfach
                    else -> drawSimpleKey(canvas, key) // Alle anderen Tasten
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
            color = Color.DKGRAY
            style = Paint.Style.FILL
        }

        val borderPaint = Paint().apply {
            color = Color.GRAY
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        canvas.drawRoundRect(keyRect, 8f, 8f, backgroundPaint)
        canvas.drawRoundRect(keyRect, 8f, 8f, borderPaint)
    }

    private fun drawNumberKey(canvas: Canvas, key: Keyboard.Key) {
        val centerX = key.x + key.width / 2f
        val centerY = key.y + key.height / 2f
        val number = key.codes[0].toChar().toString()

        // Gelbe Zahl in der Mitte
        canvas.drawText(number, centerX, centerY + 8f, numberPaint)

        if (isSpecialCharMode) {
            // Sonderzeichen anzeigen
            drawSpecialChars(canvas, number, centerX, centerY)
        } else {
            // Buchstaben anzeigen
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

        // Unten (nur für 7 und 9)
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

    private fun getLettersForNumber(number: String): List<String> {
        return when (number) {
            "2" -> listOf("a", "b", "c")
            "3" -> listOf("d", "e", "f")
            "4" -> listOf("g", "h", "i")
            "5" -> listOf("j", "k", "l")
            "6" -> listOf("m", "n", "o")
            "7" -> listOf("p", "q", "r", "s")
            "8" -> listOf("t", "u", "v")
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

    private fun isEnterSearch(): Boolean {
        // Einfache Implementierung - das wird vom Service übernommen
        return false
    }

    private fun drawSimpleKey(canvas: Canvas, key: Keyboard.Key, customLabel: String? = null) {
        val centerX = key.x + key.width / 2f
        val centerY = key.y + key.height / 2f

        val textPaint = Paint().apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 42f  // 50% größer als vorher (28f)
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        // Label verwenden - entweder custom oder aus der XML/Code
        val label = customLabel ?: when (key.codes[0]) {
            49 -> "1"
            51 -> "3"
            -5 -> "DEL"
            52 -> "4"
            53 -> "5"
            54 -> "6"
            -1 -> "SYM"
            55 -> "7"
            56 -> "8"
            57 -> "9"
            -6 -> {
                // CAPS-Taste zeigt aktuellen Status an - mit DEBUG
                when {
                    isSpecialCharMode -> "SYM" // War ABC
                    isCapsLockEnabled -> "ON" // War CAPS
                    else -> "off" // War caps
                }
            }
            42 -> "*"
            48 -> "0"
            32 -> "SPACE"
            10 -> {
                // Enter-Taste zeigt Kontext an
                if (isEnterSearch()) "SEND" else "ENTER"
            }
            else -> key.label?.toString() ?: ""
        }

        canvas.drawText(label, centerX, centerY + 8f, textPaint)
    }
}