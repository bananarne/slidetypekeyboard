package com.latinsud.android.slidetypekeyboard

import android.content.Context
import android.graphics.*
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.util.AttributeSet
import android.view.MotionEvent

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
    
    private var pressedKey: Keyboard.Key? = null
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
            3 -> { // Neon Theme
                bgColor = Color.BLACK
                borderColor = Color.rgb(0, 255, 255) // Cyan
                numberColor = Color.rgb(0, 255, 0) // Bright Green
                letterColor = Color.rgb(255, 0, 255) // Magenta
                specialColor = Color.rgb(255, 255, 0) // Yellow
                keyTextColor = Color.rgb(0, 255, 255) // Cyan
            }
            4 -> { // Ocean Theme
                bgColor = Color.rgb(25, 42, 86) // Deep Blue
                borderColor = Color.rgb(70, 130, 180) // Steel Blue
                numberColor = Color.rgb(135, 206, 250) // Light Sky Blue
                letterColor = Color.rgb(240, 248, 255) // Alice Blue
                specialColor = Color.rgb(0, 191, 255) // Deep Sky Blue
                keyTextColor = Color.rgb(240, 248, 255) // Alice Blue
            }
            5 -> { // Sunset Theme
                bgColor = Color.rgb(139, 69, 19) // Saddle Brown
                borderColor = Color.rgb(255, 140, 0) // Dark Orange
                numberColor = Color.rgb(255, 215, 0) // Gold
                letterColor = Color.rgb(255, 255, 224) // Light Yellow
                specialColor = Color.rgb(255, 69, 0) // Red Orange
                keyTextColor = Color.rgb(255, 255, 224) // Light Yellow
            }
            6 -> { // Forest Theme
                bgColor = Color.rgb(34, 139, 34) // Forest Green
                borderColor = Color.rgb(85, 107, 47) // Dark Olive Green
                numberColor = Color.rgb(173, 255, 47) // Green Yellow
                letterColor = Color.rgb(240, 255, 240) // Honeydew
                specialColor = Color.rgb(50, 205, 50) // Lime Green
                keyTextColor = Color.rgb(240, 255, 240) // Honeydew
            }
            7 -> { // Purple Theme
                bgColor = Color.rgb(75, 0, 130) // Indigo
                borderColor = Color.rgb(138, 43, 226) // Blue Violet
                numberColor = Color.rgb(186, 85, 211) // Medium Orchid
                letterColor = Color.rgb(221, 160, 221) // Plum
                specialColor = Color.rgb(255, 0, 255) // Magenta
                keyTextColor = Color.rgb(221, 160, 221) // Plum
            }
        }

        // Paint-Objekte aktualisieren
        numberPaint.color = numberColor
        letterPaint.color = letterColor
        specialCharPaint.color = specialColor
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val key = findKeyAtPosition(event.x, event.y)
                pressedKey = key
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                pressedKey = null
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun findKeyAtPosition(x: Float, y: Float): Keyboard.Key? {
        return keyboard?.keys?.find { key ->
            x >= key.x && x <= key.x + key.width &&
                    y >= key.y && y <= key.y + key.height
        }
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
                    49 -> drawKey1(canvas, key) // Zahl 1
                    in 50..57 -> drawNumberKey(canvas, key) // Zahlen 2-9
                    48, 42 -> drawSpecialNumberKey(canvas, key) // 0 und *
                    -6 -> drawCapsKey(canvas, key) // CAPS key
                    else -> drawSimpleKey(canvas, key) // Alle anderen
                }
            }
        } catch (e: Exception) {
            // Fallback zum Standard-Layout
            super.onDraw(canvas)
        }
    }

    private fun drawKeyBackground(canvas: Canvas, key: Keyboard.Key) {
        val isPressed = key == pressedKey
        val keyRect = RectF(
            key.x.toFloat() + 2,
            key.y.toFloat() + 2,
            (key.x + key.width).toFloat() - 2,
            (key.y + key.height).toFloat() - 2
        )

        val cornerRadius = if (currentTheme == 2) 12f else 8f
        
        // Shadow effect (drawn first, behind the key)
        val shadowRect = RectF(
            keyRect.left + 4f,
            keyRect.top + 4f,
            keyRect.right + 4f,
            keyRect.bottom + 4f
        )
        
        val shadowPaint = Paint().apply {
            color = Color.argb(80, 0, 0, 0) // Semi-transparent black shadow
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        
        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint)
        
        // Gradient background for 3D effect
        val baseColor = if (isPressed) lightenColor(bgColor, 0.4f) else bgColor
        val gradientPaint = Paint().apply {
            shader = LinearGradient(
                keyRect.left, keyRect.top,
                keyRect.left, keyRect.bottom,
                intArrayOf(
                    if (isPressed) darkenColor(baseColor, 0.1f) else lightenColor(baseColor, 0.3f), // Inverted when pressed
                    baseColor, // Highlighted color when pressed
                    if (isPressed) lightenColor(baseColor, 0.2f) else darkenColor(baseColor, 0.3f) // Inverted when pressed
                ),
                null,
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Highlight on top edge for 3D effect - adjust based on press state
        val highlightPaint = Paint().apply {
            color = if (isPressed) darkenColor(baseColor, 0.3f) else lightenColor(baseColor, 0.5f)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        
        // Dark line on bottom edge for depth - adjust based on press state  
        val depthPaint = Paint().apply {
            color = if (isPressed) lightenColor(baseColor, 0.2f) else darkenColor(baseColor, 0.4f)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }

        // Draw the key with gradient background
        canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, gradientPaint)
        
        // Add highlight at top
        val highlightRect = RectF(keyRect.left, keyRect.top, keyRect.right, keyRect.top + keyRect.height() * 0.3f)
        canvas.drawRoundRect(highlightRect, cornerRadius, cornerRadius, highlightPaint)
        
        // Add depth line at bottom
        val depthRect = RectF(keyRect.left, keyRect.bottom - 8f, keyRect.right, keyRect.bottom)
        canvas.drawRoundRect(depthRect, cornerRadius, cornerRadius, depthPaint)
        
        // Draw border last
        canvas.drawRoundRect(keyRect, cornerRadius, cornerRadius, borderPaint)
    }
    
    private fun lightenColor(color: Int, factor: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val a = Color.alpha(color)
        
        val newR = (r + (255 - r) * factor).coerceIn(0f, 255f).toInt()
        val newG = (g + (255 - g) * factor).coerceIn(0f, 255f).toInt()
        val newB = (b + (255 - b) * factor).coerceIn(0f, 255f).toInt()
        
        return Color.argb(a, newR, newG, newB)
    }
    
    private fun darkenColor(color: Int, factor: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val a = Color.alpha(color)
        
        val newR = (r * (1 - factor)).coerceIn(0f, 255f).toInt()
        val newG = (g * (1 - factor)).coerceIn(0f, 255f).toInt()
        val newB = (b * (1 - factor)).coerceIn(0f, 255f).toInt()
        
        return Color.argb(a, newR, newG, newB)
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
            canvas.drawText(letter, centerX, centerY - 65f, letterPaint)
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
            canvas.drawText(it, centerX, centerY - 65f, specialCharPaint)
        }
        specials["rechts"]?.let {
            canvas.drawText(it, centerX + 60f, centerY + 8f, specialCharPaint)
        }
        specials["unten"]?.let {
            canvas.drawText(it, centerX, centerY + 65f, specialCharPaint)
        }
    }

    private fun drawKey1(canvas: Canvas, key: Keyboard.Key) {
        val centerX = key.x + key.width / 2f
        val centerY = key.y + key.height / 2f
        
        // Display "1" with same style as other numbers
        canvas.drawText("1", centerX, centerY + 8f, numberPaint)
        
        if (isSpecialCharMode) {
            // Special characters for key 1
            val specials = mapOf(
                "links" to "[",
                "oben" to "#",
                "rechts" to "]",
                "unten" to "&"
            )
            
            specials["links"]?.let {
                canvas.drawText(it, centerX - 60f, centerY + 8f, specialCharPaint)
            }
            specials["oben"]?.let {
                canvas.drawText(it, centerX, centerY - 65f, specialCharPaint)
            }
            specials["rechts"]?.let {
                canvas.drawText(it, centerX + 60f, centerY + 8f, specialCharPaint)
            }
            specials["unten"]?.let {
                canvas.drawText(it, centerX, centerY + 65f, specialCharPaint)
            }
        }
    }

    private fun drawCapsKey(canvas: Canvas, key: Keyboard.Key) {
        val centerX = key.x + key.width / 2f
        val centerY = key.y + key.height / 2f
        
        val textPaint = Paint().apply {
            color = keyTextColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        
        // Main center symbol
        val centerSymbol = if (isSpecialCharMode) "abc" else "⁉️"
        textPaint.textSize = 42f
        canvas.drawText(centerSymbol, centerX, centerY + 8f, textPaint)
        
        if (!isSpecialCharMode) {
            // Up arrow for caps lock (smaller)
            textPaint.textSize = 30f
            canvas.drawText("↑", centerX, centerY - 50f, textPaint)
            
            // Down arrow for lowercase (smaller)
            canvas.drawText("↓", centerX, centerY + 50f, textPaint)
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
            canvas.drawText(it, centerX, centerY - 65f, letterPaint)
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
            51 -> "3"
            -5 -> "⌫" // Backspace icon
            52 -> "4"
            53 -> "5"
            54 -> "6"
            -1 -> "😀"
            55 -> "7"
            56 -> "8"
            57 -> "9"
            42 -> "*"
            48 -> "0"
            32 -> "⎵" // Space bar symbol
            10 -> "↵" // Enter symbol
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