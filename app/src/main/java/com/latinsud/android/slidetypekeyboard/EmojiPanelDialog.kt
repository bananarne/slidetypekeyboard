package com.latinsud.android.slidetypekeyboard

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EmojiPanelDialog(
    context: Context,
    private val currentTheme: Int,
    private val onEmojiSelected: (String) -> Unit
) : Dialog(context) {

    private lateinit var emojiGrid: RecyclerView
    private lateinit var categoryTabs: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var recentEmojisContainer: LinearLayout

    private var currentCategory = 0
    private val recentEmojis = mutableListOf<String>()
    private val maxRecentEmojis = 20

    // Emoji-Kategorien mit den beliebtesten Emojis
    private val emojiCategories = mapOf(
        "Smileys" to listOf(
            "😀", "😃", "😄", "😁", "😅", "😂", "🤣", "😊", "😇", "🙂",
            "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋",
            "😛", "😜", "🤪", "😝", "🤑", "🤗", "🤭", "🤫", "🤔", "🤐",
            "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬", "🤥", "😌",
            "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮", "🤧",
            "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "😎", "🤓", "🧐",
            "😕", "😟", "🙁", "☹️", "😮", "😯", "😲", "😳", "🥺", "😦",
            "😧", "😨", "😰", "😥", "😢", "😭", "😱", "😖", "😣", "😞",
            "😓", "😩", "😫", "🥱", "😤", "😡", "😠", "🤬", "😈", "👿"
        ),
        "Gesten" to listOf(
            "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤏", "✌️", "🤞", "🤟",
            "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "👍", "👎",
            "✊", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤝", "🙏", "✍️",
            "💪", "🦾", "🦿", "🦵", "🦶", "👂", "🦻", "👃", "🧠", "🦷",
            "🦴", "👀", "👁️", "👅", "👄", "💋", "👶", "🧒", "👦", "👧",
            "🧑", "👱", "👨", "🧔", "👱‍♂️", "👨‍🦰", "👨‍🦱", "👨‍🦳", "👨‍🦲", "👩",
            "👱‍♀️", "👩‍🦰", "👩‍🦱", "👩‍🦳", "👩‍🦲", "🧓", "👴", "👵", "🙍", "🙍‍♂️"
        ),
        "Tiere" to listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🦝", "🐻", "🐼", "🦘",
            "🦡", "🐨", "🐯", "🦁", "🐮", "🐷", "🐽", "🐸", "🐵", "🙈",
            "🙉", "🙊", "🐒", "🐔", "🐧", "🐦", "🐤", "🐣", "🐥", "🦆",
            "🦅", "🦉", "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🐛", "🦋",
            "🐌", "🐞", "🐜", "🦟", "🦗", "🕷️", "🕸️", "🦂", "🐢", "🐍",
            "🦎", "🦖", "🦕", "🐙", "🦑", "🦐", "🦞", "🦀", "🐡", "🐠",
            "🐟", "🐬", "🐳", "🐋", "🦈", "🐊", "🐅", "🐆", "🦓", "🦍"
        ),
        "Essen" to listOf(
            "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🍈",
            "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑", "🥦",
            "🥬", "🥒", "🌶️", "🌽", "🥕", "🥔", "🍠", "🥐", "🥖", "🍞",
            "🥨", "🥯", "🧀", "🥚", "🍳", "🥞", "🥓", "🥩", "🍗", "🍖",
            "🌭", "🍔", "🍟", "🍕", "🥪", "🥙", "🌮", "🌯", "🥗", "🥘",
            "🥫", "🍝", "🍜", "🍲", "🍛", "🍣", "🍱", "🥟", "🍤", "🍙",
            "🍚", "🍘", "🍥", "🥠", "🥮", "🍢", "🍡", "🍧", "🍨", "🍦"
        ),
        "Aktivitäten" to listOf(
            "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🥏", "🎱",
            "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🥅", "⛳", "🏹", "🎣",
            "🤿", "🥊", "🥋", "🎽", "🛹", "🛷", "⛸️", "🥌", "🎿", "⛷️",
            "🏂", "🏋️", "🤼", "🤸", "🤺", "⛹️", "🤾", "🏌️", "🏇", "🧘",
            "🏄", "🏊", "🚣", "🧗", "🚵", "🚴", "🏆", "🥇", "🥈", "🥉",
            "🏅", "🎖️", "🏵️", "🎗️", "🎫", "🎟️", "🎪", "🤹", "🎭", "🎨",
            "🎬", "🎤", "🎧", "🎼", "🎵", "🎶", "🎹", "🥁", "🎷", "🎺"
        ),
        "Objekte" to listOf(
            "📱", "📲", "💻", "⌨️", "🖥️", "🖨️", "🖱️", "🖲️", "🕹️", "🗜️",
            "💾", "💿", "📀", "📼", "📷", "📸", "📹", "🎥", "📽️", "🎞️",
            "📞", "☎️", "📟", "📠", "📺", "📻", "🎙️", "🎚️", "🎛️", "🧭",
            "⏱️", "⏲️", "⏰", "🕰️", "⌛", "⏳", "📡", "🔋", "🔌", "💡",
            "🔦", "🕯️", "🧯", "🛢️", "💸", "💵", "💴", "💶", "💷", "💰",
            "💳", "💎", "⚖️", "🧰", "🔧", "🔨", "⚒️", "🛠️", "⛏️", "🔩",
            "⚙️", "🧱", "⛓️", "🧲", "🔫", "💣", "🧨", "🔪", "🗡️", "⚔️"
        ),
        "Symbole" to listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔",
            "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "☮️",
            "✝️", "☪️", "🕉️", "☸️", "✡️", "🔯", "🕎", "☯️", "☦️", "🛐",
            "⛎", "♈", "♉", "♊", "♋", "♌", "♍", "♎", "♏", "♐",
            "♑", "♒", "♓", "🆔", "⚛️", "🉑", "☢️", "☣️", "📴", "📳",
            "🈶", "🈚", "🈸", "🈺", "🈷️", "✴️", "🆚", "💮", "🉐", "㊙️",
            "㊗️", "🈴", "🈵", "🈹", "🈲", "🅰️", "🅱️", "🆎", "🆑", "🅾️",
            "🆘", "❌", "⭕", "🛑", "⛔", "📛", "🚫", "💯", "💢", "♨️"
        ),
        "Flaggen" to listOf(
            "🏳️", "🏴", "🏴‍☠️", "🏁", "🚩", "🏳️‍🌈", "🏳️‍⚧️", "🇺🇳", "🇦🇫", "🇦🇽",
            "🇦🇱", "🇩🇿", "🇦🇸", "🇦🇩", "🇦🇴", "🇦🇮", "🇦🇶", "🇦🇬", "🇦🇷", "🇦🇲",
            "🇦🇼", "🇦🇺", "🇦🇹", "🇦🇿", "🇧🇸", "🇧🇭", "🇧🇩", "🇧🇧", "🇧🇾", "🇧🇪",
            "🇧🇿", "🇧🇯", "🇧🇲", "🇧🇹", "🇧🇴", "🇧🇦", "🇧🇼", "🇧🇷", "🇮🇴", "🇻🇬",
            "🇧🇳", "🇧🇬", "🇧🇫", "🇧🇮", "🇰🇭", "🇨🇲", "🇨🇦", "🇮🇨", "🇨🇻", "🇧🇶",
            "🇰🇾", "🇨🇫", "🇹🇩", "🇨🇱", "🇨🇳", "🇨🇽", "🇨🇨", "🇨🇴", "🇰🇲", "🇨🇬",
            "🇨🇩", "🇨🇰", "🇨🇷", "🇨🇮", "🇭🇷", "🇨🇺", "🇨🇼", "🇨🇾", "🇨🇿", "🇩🇰",
            "🇩🇯", "🇩🇲", "🇩🇴", "🇪🇨", "🇪🇬", "🇸🇻", "🇬🇶", "🇪🇷", "🇪🇪", "🇪🇹"
        )
    )

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(createDialogLayout())

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
        }

        loadRecentEmojis()
        showCategory(0)
    }

    private fun createDialogLayout(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(getThemeBackgroundColor())
            setPadding(16, 16, 16, 16)

            // Suchleiste
            searchInput = EditText(context).apply {
                hint = "Emoji suchen..."
                setTextColor(getThemeTextColor())
                setHintTextColor(getThemeSecondaryTextColor())
                setPadding(20, 15, 20, 15)
                textSize = 16f
                setBackgroundResource(android.R.drawable.edit_text)
            }
            addView(searchInput)

            // Zuletzt verwendet (horizontal scrollbar)
            TextView(context).apply {
                text = "Zuletzt verwendet"
                setTextColor(getThemeTextColor())
                setPadding(0, 10, 0, 5)
                textSize = 14f
            }.let { addView(it) }

            recentEmojisContainer = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                val scrollView = HorizontalScrollView(context)
                scrollView.addView(this)
                addView(scrollView, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ))
            }

            // Kategorie-Tabs
            categoryTabs = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 10, 0, 10)

                emojiCategories.keys.forEachIndexed { index, category ->
                    val tab = Button(context).apply {
                        text = getCategoryIcon(category)
                        textSize = 24f
                        background = null
                        setPadding(15, 5, 15, 5)
                        setOnClickListener { showCategory(index) }
                    }
                    addView(tab)
                }
            }

            val tabScrollView = HorizontalScrollView(context)
            tabScrollView.addView(categoryTabs)
            addView(tabScrollView)

            // Emoji Grid
            emojiGrid = RecyclerView(context).apply {
                layoutManager = GridLayoutManager(context, 8)
                setPadding(0, 10, 0, 10)
            }
            addView(emojiGrid, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400 // Feste Höhe für das Grid
            ))

            // Such-Listener
            searchInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    filterEmojis(s.toString())
                }
            })
        }
    }

    private fun getCategoryIcon(category: String): String {
        return when(category) {
            "Smileys" -> "😀"
            "Gesten" -> "👍"
            "Tiere" -> "🐾"
            "Essen" -> "🍔"
            "Aktivitäten" -> "⚽"
            "Objekte" -> "💡"
            "Symbole" -> "❤️"
            "Flaggen" -> "🏁"
            else -> "😀"
        }
    }

    private fun showCategory(index: Int) {
        currentCategory = index
        val categoryName = emojiCategories.keys.elementAt(index)
        val emojis = emojiCategories[categoryName] ?: emptyList()

        emojiGrid.adapter = EmojiAdapter(emojis) { emoji ->
            addToRecentEmojis(emoji)
            onEmojiSelected(emoji)
            dismiss()
        }

        // Update tab highlighting
        updateTabHighlighting(index)
    }

    private fun updateTabHighlighting(selectedIndex: Int) {
        for (i in 0 until categoryTabs.childCount) {
            val tab = categoryTabs.getChildAt(i) as Button
            if (i == selectedIndex) {
                tab.alpha = 1.0f
                tab.scaleX = 1.2f
                tab.scaleY = 1.2f
            } else {
                tab.alpha = 0.6f
                tab.scaleX = 1.0f
                tab.scaleY = 1.0f
            }
        }
    }

    private fun filterEmojis(query: String) {
        if (query.isEmpty()) {
            showCategory(currentCategory)
            return
        }

        val allEmojis = emojiCategories.values.flatten()
        val filtered = allEmojis.filter { emoji ->
            // Hier könnte man eine Emoji-Name-Datenbank einbinden
            // Für jetzt nur simple Suche
            emoji.contains(query, ignoreCase = true)
        }

        emojiGrid.adapter = EmojiAdapter(filtered) { emoji ->
            addToRecentEmojis(emoji)
            onEmojiSelected(emoji)
            dismiss()
        }
    }

    private fun addToRecentEmojis(emoji: String) {
        recentEmojis.remove(emoji) // Remove if exists
        recentEmojis.add(0, emoji) // Add to beginning

        if (recentEmojis.size > maxRecentEmojis) {
            recentEmojis.removeAt(recentEmojis.lastIndex)
        }

        saveRecentEmojis()
        updateRecentEmojisView()
    }

    private fun updateRecentEmojisView() {
        recentEmojisContainer.removeAllViews()

        recentEmojis.forEach { emoji ->
            TextView(context).apply {
                text = emoji
                textSize = 28f
                setPadding(10, 5, 10, 5)
                setOnClickListener {
                    onEmojiSelected(emoji)
                    dismiss()
                }
            }.let { recentEmojisContainer.addView(it) }
        }
    }

    private fun saveRecentEmojis() {
        val prefs = context.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("recent_emojis", recentEmojis.joinToString(",")).apply()
    }

    private fun loadRecentEmojis() {
        val prefs = context.getSharedPreferences("emoji_prefs", Context.MODE_PRIVATE)
        val saved = prefs.getString("recent_emojis", "") ?: ""
        if (saved.isNotEmpty()) {
            recentEmojis.clear()
            recentEmojis.addAll(saved.split(","))
            updateRecentEmojisView()
        }
    }

    // Theme-Farben basierend auf currentTheme
    private fun getThemeBackgroundColor(): Int {
        return when(currentTheme) {
            0 -> Color.rgb(48, 48, 48) // Dark
            1 -> Color.WHITE // Light
            2 -> Color.rgb(45, 45, 50) // Modern
            else -> Color.DKGRAY
        }
    }

    private fun getThemeTextColor(): Int {
        return when(currentTheme) {
            0 -> Color.WHITE
            1 -> Color.BLACK
            2 -> Color.rgb(200, 200, 210)
            else -> Color.WHITE
        }
    }

    private fun getThemeSecondaryTextColor(): Int {
        return when(currentTheme) {
            0 -> Color.LTGRAY
            1 -> Color.GRAY
            2 -> Color.rgb(150, 150, 160)
            else -> Color.GRAY
        }
    }

    // Emoji Adapter für RecyclerView
    inner class EmojiAdapter(
        private val emojis: List<String>,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

        inner class EmojiViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
            val textView = TextView(context).apply {
                textSize = 28f
                gravity = Gravity.CENTER
                setPadding(5, 10, 5, 10)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            return EmojiViewHolder(textView)
        }

        override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
            holder.textView.text = emojis[position]
            holder.textView.setOnClickListener {
                onItemClick(emojis[position])
            }
        }

        override fun getItemCount() = emojis.size
    }
}