package com.example.keyboard

import com.example.bluetooth.HidConsts

enum class KeyType {
    NORMAL,
    MODIFIER,
    ACTION,
    FUNCTION,
    ARROW,
    NUMPAD
}

data class KeyDefinition(
    val id: String,
    val primaryLabel: String,
    val shiftLabel: String? = null,
    val usageCode: Byte = HidConsts.KEY_NONE,
    val modifierMask: Byte = 0,
    val keyType: KeyType = KeyType.NORMAL,
    val weight: Float = 1.0f
)

object KeyboardLayouts {

    // --- Function Row ---
    val FUNCTION_ROW = listOf(
        KeyDefinition("ESC", "ESC", usageCode = HidConsts.KEY_ESC, keyType = KeyType.ACTION, weight = 1.2f),
        KeyDefinition("F1", "F1", usageCode = HidConsts.KEY_F1, keyType = KeyType.FUNCTION),
        KeyDefinition("F2", "F2", usageCode = HidConsts.KEY_F2, keyType = KeyType.FUNCTION),
        KeyDefinition("F3", "F3", usageCode = HidConsts.KEY_F3, keyType = KeyType.FUNCTION),
        KeyDefinition("F4", "F4", usageCode = HidConsts.KEY_F4, keyType = KeyType.FUNCTION),
        KeyDefinition("F5", "F5", usageCode = HidConsts.KEY_F5, keyType = KeyType.FUNCTION),
        KeyDefinition("F6", "F6", usageCode = HidConsts.KEY_F6, keyType = KeyType.FUNCTION),
        KeyDefinition("F7", "F7", usageCode = HidConsts.KEY_F7, keyType = KeyType.FUNCTION),
        KeyDefinition("F8", "F8", usageCode = HidConsts.KEY_F8, keyType = KeyType.FUNCTION),
        KeyDefinition("F9", "F9", usageCode = HidConsts.KEY_F9, keyType = KeyType.FUNCTION),
        KeyDefinition("F10", "F10", usageCode = HidConsts.KEY_F10, keyType = KeyType.FUNCTION),
        KeyDefinition("F11", "F11", usageCode = HidConsts.KEY_F11, keyType = KeyType.FUNCTION),
        KeyDefinition("F12", "F12", usageCode = HidConsts.KEY_F12, keyType = KeyType.FUNCTION),
        KeyDefinition("PRTSC", "PrtSc", usageCode = HidConsts.KEY_PRINTSCREEN, keyType = KeyType.ACTION, weight = 1.1f),
        KeyDefinition("PAUSE", "Pause", usageCode = HidConsts.KEY_PAUSE, keyType = KeyType.ACTION, weight = 1.1f)
    )

    // --- Main Rows ---
    val ROW_NUMBER = listOf(
        KeyDefinition("GRAVE", "`", "~", HidConsts.KEY_GRAVE),
        KeyDefinition("1", "1", "!", HidConsts.KEY_1),
        KeyDefinition("2", "2", "@", HidConsts.KEY_2),
        KeyDefinition("3", "3", "#", HidConsts.KEY_3),
        KeyDefinition("4", "4", "$", HidConsts.KEY_4),
        KeyDefinition("5", "5", "%", HidConsts.KEY_5),
        KeyDefinition("6", "6", "^", HidConsts.KEY_6),
        KeyDefinition("7", "7", "&", HidConsts.KEY_7),
        KeyDefinition("8", "8", "*", HidConsts.KEY_8),
        KeyDefinition("9", "9", "(", HidConsts.KEY_9),
        KeyDefinition("0", "0", ")", HidConsts.KEY_0),
        KeyDefinition("MINUS", "-", "_", HidConsts.KEY_MINUS),
        KeyDefinition("EQUAL", "=", "+", HidConsts.KEY_EQUAL),
        KeyDefinition("BACKSPACE", "Bksp", usageCode = HidConsts.KEY_BACKSPACE, keyType = KeyType.ACTION, weight = 1.8f)
    )

    val ROW_QWERTY = listOf(
        KeyDefinition("TAB", "Tab", usageCode = HidConsts.KEY_TAB, keyType = KeyType.ACTION, weight = 1.4f),
        KeyDefinition("Q", "Q", usageCode = HidConsts.KEY_Q),
        KeyDefinition("W", "W", usageCode = HidConsts.KEY_W),
        KeyDefinition("E", "E", usageCode = HidConsts.KEY_E),
        KeyDefinition("R", "R", usageCode = HidConsts.KEY_R),
        KeyDefinition("T", "T", usageCode = HidConsts.KEY_T),
        KeyDefinition("Y", "Y", usageCode = HidConsts.KEY_Y),
        KeyDefinition("U", "U", usageCode = HidConsts.KEY_U),
        KeyDefinition("I", "I", usageCode = HidConsts.KEY_I),
        KeyDefinition("O", "O", usageCode = HidConsts.KEY_O),
        KeyDefinition("P", "P", usageCode = HidConsts.KEY_P),
        KeyDefinition("LBRACKET", "[", "{", HidConsts.KEY_LEFTBRACE),
        KeyDefinition("RBRACKET", "]", "}", HidConsts.KEY_RIGHTBRACE),
        KeyDefinition("BACKSLASH", "\\", "|", HidConsts.KEY_BACKSLASH, weight = 1.4f)
    )

    val ROW_HOME = listOf(
        KeyDefinition("CAPS", "Caps", usageCode = HidConsts.KEY_CAPSLOCK, keyType = KeyType.MODIFIER, weight = 1.7f),
        KeyDefinition("A", "A", usageCode = HidConsts.KEY_A),
        KeyDefinition("S", "S", usageCode = HidConsts.KEY_S),
        KeyDefinition("D", "D", usageCode = HidConsts.KEY_D),
        KeyDefinition("F", "F", usageCode = HidConsts.KEY_F),
        KeyDefinition("G", "G", usageCode = HidConsts.KEY_G),
        KeyDefinition("H", "H", usageCode = HidConsts.KEY_H),
        KeyDefinition("J", "J", usageCode = HidConsts.KEY_J),
        KeyDefinition("K", "K", usageCode = HidConsts.KEY_K),
        KeyDefinition("L", "L", usageCode = HidConsts.KEY_L),
        KeyDefinition("SEMICOLON", ";", ":", HidConsts.KEY_SEMICOLON),
        KeyDefinition("QUOTE", "'", "\"", HidConsts.KEY_APOSTROPHE),
        KeyDefinition("ENTER", "Enter", usageCode = HidConsts.KEY_ENTER, keyType = KeyType.ACTION, weight = 2.0f)
    )

    val ROW_BOTTOM = listOf(
        KeyDefinition("LSHIFT", "Shift", modifierMask = HidConsts.MOD_LEFT_SHIFT, keyType = KeyType.MODIFIER, weight = 2.2f),
        KeyDefinition("Z", "Z", usageCode = HidConsts.KEY_Z),
        KeyDefinition("X", "X", usageCode = HidConsts.KEY_X),
        KeyDefinition("C", "C", usageCode = HidConsts.KEY_C),
        KeyDefinition("V", "V", usageCode = HidConsts.KEY_V),
        KeyDefinition("B", "B", usageCode = HidConsts.KEY_B),
        KeyDefinition("N", "N", usageCode = HidConsts.KEY_N),
        KeyDefinition("M", "M", usageCode = HidConsts.KEY_M),
        KeyDefinition("COMMA", ",", "<", HidConsts.KEY_COMMA),
        KeyDefinition("DOT", ".", ">", HidConsts.KEY_DOT),
        KeyDefinition("SLASH", "/", "?", HidConsts.KEY_SLASH),
        KeyDefinition("RSHIFT", "Shift", modifierMask = HidConsts.MOD_RIGHT_SHIFT, keyType = KeyType.MODIFIER, weight = 2.2f)
    )

    val ROW_SPACE = listOf(
        KeyDefinition("LCTRL", "Ctrl", modifierMask = HidConsts.MOD_LEFT_CTRL, keyType = KeyType.MODIFIER, weight = 1.3f),
        KeyDefinition("FN", "Fn", keyType = KeyType.MODIFIER, weight = 1.1f),
        KeyDefinition("WIN", "Win", modifierMask = HidConsts.MOD_LEFT_GUI, keyType = KeyType.MODIFIER, weight = 1.1f),
        KeyDefinition("LALT", "Alt", modifierMask = HidConsts.MOD_LEFT_ALT, keyType = KeyType.MODIFIER, weight = 1.3f),
        KeyDefinition("SPACE", "Space", usageCode = HidConsts.KEY_SPACE, keyType = KeyType.NORMAL, weight = 5.0f),
        KeyDefinition("RALT", "Alt", modifierMask = HidConsts.MOD_RIGHT_ALT, keyType = KeyType.MODIFIER, weight = 1.3f),
        KeyDefinition("FN2", "Fn", keyType = KeyType.MODIFIER, weight = 1.1f),
        KeyDefinition("RCTRL", "Ctrl", modifierMask = HidConsts.MOD_RIGHT_CTRL, keyType = KeyType.MODIFIER, weight = 1.3f)
    )

    // --- Navigation & Arrows Cluster ---
    val NAV_ROW_1 = listOf(
        KeyDefinition("INS", "Ins", usageCode = HidConsts.KEY_INSERT, keyType = KeyType.ACTION),
        KeyDefinition("HOME", "Home", usageCode = HidConsts.KEY_HOME, keyType = KeyType.ACTION),
        KeyDefinition("PGUP", "PgUp", usageCode = HidConsts.KEY_PAGEUP, keyType = KeyType.ACTION)
    )

    val NAV_ROW_2 = listOf(
        KeyDefinition("DEL", "Del", usageCode = HidConsts.KEY_DELETE, keyType = KeyType.ACTION),
        KeyDefinition("END", "End", usageCode = HidConsts.KEY_END, keyType = KeyType.ACTION),
        KeyDefinition("PGDN", "PgDn", usageCode = HidConsts.KEY_PAGEDOWN, keyType = KeyType.ACTION)
    )

    val ARROW_ROW_1 = listOf(
        KeyDefinition("SPACER_UP_L", "", weight = 1.0f),
        KeyDefinition("UP", "▲", usageCode = HidConsts.KEY_UP, keyType = KeyType.ARROW, weight = 1.0f),
        KeyDefinition("SPACER_UP_R", "", weight = 1.0f)
    )

    val ARROW_ROW_2 = listOf(
        KeyDefinition("LEFT", "◄", usageCode = HidConsts.KEY_LEFT, keyType = KeyType.ARROW),
        KeyDefinition("DOWN", "▼", usageCode = HidConsts.KEY_DOWN, keyType = KeyType.ARROW),
        KeyDefinition("RIGHT", "►", usageCode = HidConsts.KEY_RIGHT, keyType = KeyType.ARROW)
    )

    // --- Numeric Keypad ---
    val NUMPAD_ROW_0 = listOf(
        KeyDefinition("NUMLOCK", "Num", usageCode = HidConsts.KEY_NUMLOCK, keyType = KeyType.NUMPAD),
        KeyDefinition("KPSLASH", "/", usageCode = HidConsts.KEY_KPSLASH, keyType = KeyType.NUMPAD),
        KeyDefinition("KPAST", "*", usageCode = HidConsts.KEY_KPASTERISK, keyType = KeyType.NUMPAD),
        KeyDefinition("KPMINUS", "-", usageCode = HidConsts.KEY_KPMINUS, keyType = KeyType.NUMPAD)
    )

    val NUMPAD_ROW_1 = listOf(
        KeyDefinition("KP7", "7", usageCode = HidConsts.KEY_KP7, keyType = KeyType.NUMPAD),
        KeyDefinition("KP8", "8", usageCode = HidConsts.KEY_KP8, keyType = KeyType.NUMPAD),
        KeyDefinition("KP9", "9", usageCode = HidConsts.KEY_KP9, keyType = KeyType.NUMPAD),
        KeyDefinition("KPPLUS", "+", usageCode = HidConsts.KEY_KPPLUS, keyType = KeyType.NUMPAD)
    )

    val NUMPAD_ROW_2 = listOf(
        KeyDefinition("KP4", "4", usageCode = HidConsts.KEY_KP4, keyType = KeyType.NUMPAD),
        KeyDefinition("KP5", "5", usageCode = HidConsts.KEY_KP5, keyType = KeyType.NUMPAD),
        KeyDefinition("KP6", "6", usageCode = HidConsts.KEY_KP6, keyType = KeyType.NUMPAD),
        KeyDefinition("SPACER_PLUS", "", weight = 0.0f) // placeholder if needed
    )

    val NUMPAD_ROW_3 = listOf(
        KeyDefinition("KP1", "1", usageCode = HidConsts.KEY_KP1, keyType = KeyType.NUMPAD),
        KeyDefinition("KP2", "2", usageCode = HidConsts.KEY_KP2, keyType = KeyType.NUMPAD),
        KeyDefinition("KP3", "3", usageCode = HidConsts.KEY_KP3, keyType = KeyType.NUMPAD),
        KeyDefinition("KPENTER", "↵", usageCode = HidConsts.KEY_KPENTER, keyType = KeyType.ACTION)
    )

    val NUMPAD_ROW_4 = listOf(
        KeyDefinition("KP0", "0", usageCode = HidConsts.KEY_KP0, keyType = KeyType.NUMPAD, weight = 2.0f),
        KeyDefinition("KPDOT", ".", usageCode = HidConsts.KEY_KPDOT, keyType = KeyType.NUMPAD, weight = 1.0f),
        KeyDefinition("SPACER_ENTER", "", weight = 1.0f)
    )
}
