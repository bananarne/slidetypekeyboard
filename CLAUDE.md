# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SlideType Keyboard is an Android Input Method Editor (IME) implementing a T9-style keyboard with swipe gestures and emoji support. The keyboard uses a 4x4 grid layout where each key contains multiple letters that can be accessed through swipe gestures.

## Build Commands

```bash
# Build the application
./gradlew build

# Install debug version on device
./gradlew installDebug

# Run tests
./gradlew check

# Run connected device tests
./gradlew connectedCheck

# Clean build
./gradlew clean

# Uninstall from device
./gradlew uninstallDebug
```

## Architecture

### Core Components

**SlideTypeKeyboardService** (`app/src/main/java/com/latinsud/android/slidetypekeyboard/SlideTypeKeyboardService.kt`)
- Main InputMethodService implementation
- Handles swipe gesture recognition for character selection
- Manages caps lock, special character modes, and theme switching
- Implements auto-umlaut conversion for German characters
- Controls key locking mechanism for precise input

**CustomKeyboardView** (`app/src/main/java/com/latinsud/android/slidetypekeyboard/CustomKeyboardView.kt`)
- Custom KeyboardView rendering the T9-style keyboard
- Handles theme system with multiple color schemes
- Manages visual feedback for pressed keys
- Renders numbers and letters on each key with custom colors

**EmojiSwipeBar** (`app/src/main/java/com/latinsud/android/slidetypekeyboard/EmojiSwipeBar.kt`)
- Horizontal scrollable emoji selector
- Integrated above the main keyboard layout

### Key Implementation Details

- **Keyboard Layout**: Defined in `app/src/main/res/xml/qwerty.xml` as a 4x4 grid
- **Theme System**: Activated by long-pressing the emoji key (key code -7)
- **Character Input**: Swipe gestures on number keys select different letters
- **Special Modes**: 
  - SYM key (code -1) toggles special character mode
  - CAPS key (code -6) enables caps lock
  - Long press on keys enables key locking for precision input

### Android Configuration

- **Target SDK**: 30
- **Min SDK**: 24
- **Compile SDK**: 36
- **Build Tools**: 36.0.0
- **JVM Target**: Java 11
- **Kotlin**: 1.9.0

## Project Structure

```
app/src/main/
├── java/com/latinsud/android/slidetypekeyboard/
│   ├── SlideTypeKeyboardService.kt  # Main IME service
│   ├── CustomKeyboardView.kt        # Keyboard rendering
│   ├── EmojiSwipeBar.kt            # Emoji selector
│   ├── EmojiPanelDialog.kt         # Emoji panel dialog
│   └── MainActivity.kt              # Settings/launcher activity
├── res/
│   ├── xml/
│   │   ├── qwerty.xml              # Keyboard layout definition
│   │   └── method.xml              # IME metadata
│   └── layout/
│       └── keyboard_with_emoji_bar.xml  # Main keyboard container
└── AndroidManifest.xml
```

## Development Notes

- The keyboard service requires `BIND_INPUT_METHOD` permission
- View binding and data binding are disabled in build configuration
- RecyclerView dependency is included for future list implementations
- The app includes Material Design components library