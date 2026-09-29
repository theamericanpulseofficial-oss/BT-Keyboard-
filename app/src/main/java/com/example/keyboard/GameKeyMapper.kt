package com.example.keyboard

import com.example.bluetooth.HidConsts

/**
 * Game Profiles and Key Mapping Engine for Gaming via Bluetooth HID.
 * Optimized for ultra-low latency action games (FPS, Shooters, Racing, RPGs).
 */
data class GameKeyBinding(
    val actionName: String,
    val description: String,
    val defaultHidCode: Byte,
    var mappedHidCode: Byte = defaultHidCode,
    val defaultModifier: Byte = 0,
    var mappedModifier: Byte = defaultModifier
)

enum class GameGenre(val displayName: String) {
    FPS_SHOOTER("FPS / Shooter (WASD + Shoot)"),
    RACING("Racing / Driving"),
    MOBA_ACTION("MOBA / Action (QWER)"),
    CUSTOM("Custom Mapping")
}

class GameKeyMapper {

    var currentGenre: GameGenre = GameGenre.FPS_SHOOTER
    var mouseSensitivity: Float = 1.0f
    var isRawInputEnabled: Boolean = true

    // Active key mappings for physical and virtual gaming inputs
    private val keyRemaps = mutableMapOf<Byte, Byte>()

    init {
        loadPreset(GameGenre.FPS_SHOOTER)
    }

    fun loadPreset(genre: GameGenre) {
        currentGenre = genre
        keyRemaps.clear()
        when (genre) {
            GameGenre.FPS_SHOOTER -> {
                // Standard FPS controls: WASD, Space, Shift, Ctrl, R, F, E, 1, 2, 3
                // No special remapping needed by default as HID keys map 1:1
            }
            GameGenre.RACING -> {
                // Up/Down/Left/Right mapped to WASD
                keyRemaps[HidConsts.KEY_UP] = HidConsts.KEY_W
                keyRemaps[HidConsts.KEY_DOWN] = HidConsts.KEY_S
                keyRemaps[HidConsts.KEY_LEFT] = HidConsts.KEY_A
                keyRemaps[HidConsts.KEY_RIGHT] = HidConsts.KEY_D
            }
            GameGenre.MOBA_ACTION -> {
                // Skills on Q, W, E, R, Items on 1, 2, 3, 4, 5, 6
            }
            GameGenre.CUSTOM -> {
                // User-defined
            }
        }
    }

    /**
     * Map an incoming HID key code to its remapped gaming HID key code
     */
    fun getMappedKey(originalHidCode: Byte): Byte {
        return keyRemaps[originalHidCode] ?: originalHidCode
    }

    /**
     * Set a custom remapping from one HID key to another
     */
    fun setKeyRemap(sourceKey: Byte, targetKey: Byte) {
        if (sourceKey == targetKey) {
            keyRemaps.remove(sourceKey)
        } else {
            keyRemaps[sourceKey] = targetKey
        }
    }

    fun resetRemaps() {
        keyRemaps.clear()
        loadPreset(currentGenre)
    }

    fun getActiveRemaps(): Map<Byte, Byte> = keyRemaps.toMap()
}
