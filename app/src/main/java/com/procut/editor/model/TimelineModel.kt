package com.procut.editor.model

import kotlinx.serialization.Serializable

typealias TimeUs = Long

@Serializable
enum class AspectRatio(val ratioWidth: Int, val ratioHeight: Int, val displayName: String) {
    RATIO_16_9(16, 9, "16:9"),
    RATIO_9_16(9, 16, "9:16"),
    RATIO_1_1(1, 1, "1:1"),
    RATIO_4_5(4, 5, "4:5"),
    RATIO_21_9(21, 9, "21:9");

    val floatRatio: Float
        get() = ratioWidth.toFloat() / ratioHeight.toFloat()
}

@Serializable
enum class TrackType {
    VIDEO,
    AUDIO,
    SUBTITLE
}

@Serializable
enum class ClipType {
    VIDEO,
    AUDIO,
    IMAGE,
    TEXT,
    ADJUSTMENT,
    NULL_OBJECT,
    SOLID,
    COMPOUND,
    SHAPE
}

@Serializable
enum class KeyframeInterpolation {
    LINEAR,
    BEZIER,
    HOLD,
    CONTINUOUS_BEZIER
}

@Serializable
sealed class ParamValue {
    @Serializable
    data class FloatVal(val value: Float) : ParamValue()

    @Serializable
    data class IntVal(val value: Int) : ParamValue()

    @Serializable
    data class BoolVal(val value: Boolean) : ParamValue()

    @Serializable
    data class ColorVal(val argb: Long) : ParamValue()

    @Serializable
    data class PointVal(val x: Float, val y: Float) : ParamValue()

    @Serializable
    data class EnumVal(val name: String) : ParamValue()

    @Serializable
    data class StringVal(val value: String) : ParamValue()
}

@Serializable
data class Keyframe(
    val timeUs: TimeUs,
    val value: ParamValue,
    val interpolation: KeyframeInterpolation = KeyframeInterpolation.LINEAR,
    val bezierInX: Float = 0f,
    val bezierInY: Float = 0f,
    val bezierOutX: Float = 0f,
    val bezierOutY: Float = 0f
)

@Serializable
data class Transform(
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotation: Float = 0f,
    val anchorX: Float = 0.5f,
    val anchorY: Float = 0.5f,
    val skew: Float = 0f,
    val flipH: Boolean = false,
    val flipV: Boolean = false,
    val cropL: Float = 0f,
    val cropT: Float = 0f,
    val cropR: Float = 0f,
    val cropB: Float = 0f
)

@Serializable
data class EffectInstance(
    val id: String,
    val effectId: String,
    val enabled: Boolean = true,
    val params: Map<String, ParamValue> = emptyMap(),
    val maskId: String? = null
)

@Serializable
data class AudioEffectInstance(
    val id: String,
    val effectId: String,
    val enabled: Boolean = true,
    val params: Map<String, ParamValue> = emptyMap()
)

@Serializable
data class Marker(
    val id: String,
    val timeUs: TimeUs,
    val label: String,
    val note: String = "",
    val colorIndex: Int = 0
)

@Serializable
enum class BlendMode(val displayName: String) {
    NORMAL("Normal"),
    DISSOLVE("Dissolve"),
    DANCING_DISSOLVE("Dancing Dissolve"),
    DARKEN("Darken"),
    MULTIPLY("Multiply"),
    COLOR_BURN("Color Burn"),
    CLASSIC_COLOR_BURN("Classic Color Burn"),
    LINEAR_BURN("Linear Burn"),
    DARKER_COLOR("Darker Color"),
    ADD("Add"),
    LIGHTEN("Lighten"),
    SCREEN("Screen"),
    COLOR_DODGE("Color Dodge"),
    CLASSIC_COLOR_DODGE("Classic Color Dodge"),
    LINEAR_DODGE("Linear Dodge"),
    LIGHTER_COLOR("Lighter Color"),
    OVERLAY("Overlay"),
    SOFT_LIGHT("Soft Light"),
    HARD_LIGHT("Hard Light"),
    LINEAR_LIGHT("Linear Light"),
    VIVID_LIGHT("Vivid Light"),
    PIN_LIGHT("Pin Light"),
    HARD_MIX("Hard Mix"),
    DIFFERENCE("Difference"),
    CLASSIC_DIFFERENCE("Classic Difference"),
    EXCLUSION("Exclusion"),
    SUBTRACT("Subtract"),
    DIVIDE("Divide"),
    HUE("Hue"),
    SATURATION("Saturation"),
    COLOR("Color"),
    LUMINOSITY("Luminosity")
}

@Serializable
data class Clip(
    val id: String,
    val trackId: String,
    val type: ClipType = ClipType.VIDEO,
    val name: String = "Clip",
    val sourceUri: String = "",
    val sourceInUs: TimeUs = 0L,
    val sourceOutUs: TimeUs = 5_000_000L,
    val timelineStartUs: TimeUs = 0L,
    val durationUs: TimeUs = 5_000_000L,
    val speed: Float = 1.0f,
    val reversed: Boolean = false,
    val freezeFrame: Boolean = false,
    val parentNullId: String? = null,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val opacity: Float = 1.0f,
    val transform: Transform = Transform(),
    val effects: List<EffectInstance> = emptyList(),
    val audioEffects: List<AudioEffectInstance> = emptyList(),
    val keyframes: Map<String, List<Keyframe>> = emptyMap(),
    val transitionIn: String? = null,
    val transitionOut: String? = null,
    val matteType: String? = null,
    val matteSourceClipId: String? = null,
    val volume: Float = 1.0f,
    val textContent: String = "",
    val solidColor: Long = 0xFF3B82F6
)

@Serializable
data class Track(
    val id: String,
    val type: TrackType = TrackType.VIDEO,
    val index: Int = 0,
    val name: String = "Track",
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val isLocked: Boolean = false,
    val isVisible: Boolean = true,
    val volume: Float = 1.0f,
    val height: Float = 48.0f
)

@Serializable
data class Timeline(
    val tracks: List<Track> = emptyList(),
    val clips: List<Clip> = emptyList()
) {
    val durationUs: TimeUs
        get() = clips.maxOfOrNull { it.timelineStartUs + it.durationUs } ?: 0L
}

@Serializable
data class Project(
    val id: String,
    val name: String,
    val frameRate: Int = 30,
    val width: Int = 1920,
    val height: Int = 1080,
    val aspectRatio: AspectRatio = AspectRatio.RATIO_16_9,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val timeline: Timeline = Timeline(),
    val markers: List<Marker> = emptyList(),
    val isTemplate: Boolean = false
) {
    val durationUs: TimeUs
        get() = timeline.durationUs
}
