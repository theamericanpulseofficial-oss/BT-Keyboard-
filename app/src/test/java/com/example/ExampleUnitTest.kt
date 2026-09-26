package com.example

import com.example.bluetooth.HidConsts
import com.example.keyboard.AndroidKeyToHid
import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun keyMapping_isCorrect() {
        assertEquals(HidConsts.KEY_A, AndroidKeyToHid.mapKeyCodeToHid(KeyEvent.KEYCODE_A))
        assertEquals(HidConsts.KEY_ENTER, AndroidKeyToHid.mapKeyCodeToHid(KeyEvent.KEYCODE_ENTER))
        assertEquals(HidConsts.KEY_SPACE, AndroidKeyToHid.mapKeyCodeToHid(KeyEvent.KEYCODE_SPACE))
        assertEquals(HidConsts.MOD_LEFT_CTRL, AndroidKeyToHid.mapModifierMask(KeyEvent.KEYCODE_CTRL_LEFT))
    }
}
