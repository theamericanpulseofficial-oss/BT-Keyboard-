package com.example.keyboard

import android.view.KeyEvent
import com.example.bluetooth.HidConsts

object AndroidKeyToHid {

    fun mapKeyCodeToHid(keyCode: Int): Byte {
        return when (keyCode) {
            KeyEvent.KEYCODE_A -> HidConsts.KEY_A
            KeyEvent.KEYCODE_B -> HidConsts.KEY_B
            KeyEvent.KEYCODE_C -> HidConsts.KEY_C
            KeyEvent.KEYCODE_D -> HidConsts.KEY_D
            KeyEvent.KEYCODE_E -> HidConsts.KEY_E
            KeyEvent.KEYCODE_F -> HidConsts.KEY_F
            KeyEvent.KEYCODE_G -> HidConsts.KEY_G
            KeyEvent.KEYCODE_H -> HidConsts.KEY_H
            KeyEvent.KEYCODE_I -> HidConsts.KEY_I
            KeyEvent.KEYCODE_J -> HidConsts.KEY_J
            KeyEvent.KEYCODE_K -> HidConsts.KEY_K
            KeyEvent.KEYCODE_L -> HidConsts.KEY_L
            KeyEvent.KEYCODE_M -> HidConsts.KEY_M
            KeyEvent.KEYCODE_N -> HidConsts.KEY_N
            KeyEvent.KEYCODE_O -> HidConsts.KEY_O
            KeyEvent.KEYCODE_P -> HidConsts.KEY_P
            KeyEvent.KEYCODE_Q -> HidConsts.KEY_Q
            KeyEvent.KEYCODE_R -> HidConsts.KEY_R
            KeyEvent.KEYCODE_S -> HidConsts.KEY_S
            KeyEvent.KEYCODE_T -> HidConsts.KEY_T
            KeyEvent.KEYCODE_U -> HidConsts.KEY_U
            KeyEvent.KEYCODE_V -> HidConsts.KEY_V
            KeyEvent.KEYCODE_W -> HidConsts.KEY_W
            KeyEvent.KEYCODE_X -> HidConsts.KEY_X
            KeyEvent.KEYCODE_Y -> HidConsts.KEY_Y
            KeyEvent.KEYCODE_Z -> HidConsts.KEY_Z

            KeyEvent.KEYCODE_1 -> HidConsts.KEY_1
            KeyEvent.KEYCODE_2 -> HidConsts.KEY_2
            KeyEvent.KEYCODE_3 -> HidConsts.KEY_3
            KeyEvent.KEYCODE_4 -> HidConsts.KEY_4
            KeyEvent.KEYCODE_5 -> HidConsts.KEY_5
            KeyEvent.KEYCODE_6 -> HidConsts.KEY_6
            KeyEvent.KEYCODE_7 -> HidConsts.KEY_7
            KeyEvent.KEYCODE_8 -> HidConsts.KEY_8
            KeyEvent.KEYCODE_9 -> HidConsts.KEY_9
            KeyEvent.KEYCODE_0 -> HidConsts.KEY_0

            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> HidConsts.KEY_ENTER
            KeyEvent.KEYCODE_ESCAPE -> HidConsts.KEY_ESC
            KeyEvent.KEYCODE_DEL -> HidConsts.KEY_BACKSPACE
            KeyEvent.KEYCODE_TAB -> HidConsts.KEY_TAB
            KeyEvent.KEYCODE_SPACE -> HidConsts.KEY_SPACE
            KeyEvent.KEYCODE_MINUS -> HidConsts.KEY_MINUS
            KeyEvent.KEYCODE_EQUALS -> HidConsts.KEY_EQUAL
            KeyEvent.KEYCODE_LEFT_BRACKET -> HidConsts.KEY_LEFTBRACE
            KeyEvent.KEYCODE_RIGHT_BRACKET -> HidConsts.KEY_RIGHTBRACE
            KeyEvent.KEYCODE_BACKSLASH -> HidConsts.KEY_BACKSLASH
            KeyEvent.KEYCODE_SEMICOLON -> HidConsts.KEY_SEMICOLON
            KeyEvent.KEYCODE_APOSTROPHE -> HidConsts.KEY_APOSTROPHE
            KeyEvent.KEYCODE_GRAVE -> HidConsts.KEY_GRAVE
            KeyEvent.KEYCODE_COMMA -> HidConsts.KEY_COMMA
            KeyEvent.KEYCODE_PERIOD -> HidConsts.KEY_DOT
            KeyEvent.KEYCODE_SLASH -> HidConsts.KEY_SLASH
            KeyEvent.KEYCODE_CAPS_LOCK -> HidConsts.KEY_CAPSLOCK

            KeyEvent.KEYCODE_F1 -> HidConsts.KEY_F1
            KeyEvent.KEYCODE_F2 -> HidConsts.KEY_F2
            KeyEvent.KEYCODE_F3 -> HidConsts.KEY_F3
            KeyEvent.KEYCODE_F4 -> HidConsts.KEY_F4
            KeyEvent.KEYCODE_F5 -> HidConsts.KEY_F5
            KeyEvent.KEYCODE_F6 -> HidConsts.KEY_F6
            KeyEvent.KEYCODE_F7 -> HidConsts.KEY_F7
            KeyEvent.KEYCODE_F8 -> HidConsts.KEY_F8
            KeyEvent.KEYCODE_F9 -> HidConsts.KEY_F9
            KeyEvent.KEYCODE_F10 -> HidConsts.KEY_F10
            KeyEvent.KEYCODE_F11 -> HidConsts.KEY_F11
            KeyEvent.KEYCODE_F12 -> HidConsts.KEY_F12

            KeyEvent.KEYCODE_SYSRQ -> HidConsts.KEY_PRINTSCREEN
            KeyEvent.KEYCODE_SCROLL_LOCK -> HidConsts.KEY_SCROLLLOCK
            KeyEvent.KEYCODE_BREAK -> HidConsts.KEY_PAUSE
            KeyEvent.KEYCODE_INSERT -> HidConsts.KEY_INSERT
            KeyEvent.KEYCODE_MOVE_HOME -> HidConsts.KEY_HOME
            KeyEvent.KEYCODE_PAGE_UP -> HidConsts.KEY_PAGEUP
            KeyEvent.KEYCODE_FORWARD_DEL -> HidConsts.KEY_DELETE
            KeyEvent.KEYCODE_MOVE_END -> HidConsts.KEY_END
            KeyEvent.KEYCODE_PAGE_DOWN -> HidConsts.KEY_PAGEDOWN
            KeyEvent.KEYCODE_DPAD_RIGHT -> HidConsts.KEY_RIGHT
            KeyEvent.KEYCODE_DPAD_LEFT -> HidConsts.KEY_LEFT
            KeyEvent.KEYCODE_DPAD_DOWN -> HidConsts.KEY_DOWN
            KeyEvent.KEYCODE_DPAD_UP -> HidConsts.KEY_UP

            KeyEvent.KEYCODE_NUM_LOCK -> HidConsts.KEY_NUMLOCK
            KeyEvent.KEYCODE_NUMPAD_DIVIDE -> HidConsts.KEY_KPSLASH
            KeyEvent.KEYCODE_NUMPAD_MULTIPLY -> HidConsts.KEY_KPASTERISK
            KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> HidConsts.KEY_KPMINUS
            KeyEvent.KEYCODE_NUMPAD_ADD -> HidConsts.KEY_KPPLUS
            KeyEvent.KEYCODE_NUMPAD_DOT -> HidConsts.KEY_KPDOT
            KeyEvent.KEYCODE_NUMPAD_0 -> HidConsts.KEY_KP0
            KeyEvent.KEYCODE_NUMPAD_1 -> HidConsts.KEY_KP1
            KeyEvent.KEYCODE_NUMPAD_2 -> HidConsts.KEY_KP2
            KeyEvent.KEYCODE_NUMPAD_3 -> HidConsts.KEY_KP3
            KeyEvent.KEYCODE_NUMPAD_4 -> HidConsts.KEY_KP4
            KeyEvent.KEYCODE_NUMPAD_5 -> HidConsts.KEY_KP5
            KeyEvent.KEYCODE_NUMPAD_6 -> HidConsts.KEY_KP6
            KeyEvent.KEYCODE_NUMPAD_7 -> HidConsts.KEY_KP7
            KeyEvent.KEYCODE_NUMPAD_8 -> HidConsts.KEY_KP8
            KeyEvent.KEYCODE_NUMPAD_9 -> HidConsts.KEY_KP9

            else -> HidConsts.KEY_NONE
        }
    }

    fun mapModifierMask(keyCode: Int): Byte {
        return when (keyCode) {
            KeyEvent.KEYCODE_CTRL_LEFT -> HidConsts.MOD_LEFT_CTRL
            KeyEvent.KEYCODE_CTRL_RIGHT -> HidConsts.MOD_RIGHT_CTRL
            KeyEvent.KEYCODE_SHIFT_LEFT -> HidConsts.MOD_LEFT_SHIFT
            KeyEvent.KEYCODE_SHIFT_RIGHT -> HidConsts.MOD_RIGHT_SHIFT
            KeyEvent.KEYCODE_ALT_LEFT -> HidConsts.MOD_LEFT_ALT
            KeyEvent.KEYCODE_ALT_RIGHT -> HidConsts.MOD_RIGHT_ALT
            KeyEvent.KEYCODE_META_LEFT -> HidConsts.MOD_LEFT_GUI
            KeyEvent.KEYCODE_META_RIGHT -> HidConsts.MOD_RIGHT_GUI
            else -> 0
        }
    }
}
