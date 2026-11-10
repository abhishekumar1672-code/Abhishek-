package com.procut.editor

import com.procut.editor.model.AspectRatio
import com.procut.editor.model.BlendMode
import com.procut.editor.model.Clip
import com.procut.editor.model.ClipType
import com.procut.editor.model.EffectInstance
import com.procut.editor.model.Keyframe
import com.procut.editor.model.KeyframeInterpolation
import com.procut.editor.model.ParamValue
import com.procut.editor.model.Project
import com.procut.editor.model.Timeline
import com.procut.editor.model.TimelineOps
import com.procut.editor.model.Track
import com.procut.editor.model.TrackType
import com.procut.editor.model.Transform
import com.procut.editor.util.TimecodeFormatter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectModelAndOpsTest {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    @Test
    fun testSerializationRoundTrip_allClipTypesAndKeyframes() {
        val trackVideo = Track(id = "v1", type = TrackType.VIDEO, index = 0, name = "V1")
        val trackAudio = Track(id = "a1", type = TrackType.AUDIO, index = 1, name = "A1")

        val keyframes = mapOf(
            "opacity" to listOf(
                Keyframe(timeUs = 0L, value = ParamValue.FloatVal(0f), interpolation = KeyframeInterpolation.LINEAR),
                Keyframe(timeUs = 1_000_000L, value = ParamValue.FloatVal(1f), interpolation = KeyframeInterpolation.BEZIER)
            ),
            "blendColor" to listOf(
                Keyframe(timeUs = 500_000L, value = ParamValue.ColorVal(0xFF3B82F6), interpolation = KeyframeInterpolation.HOLD)
            )
        )

        val clips = ClipType.entries.mapIndexed { index, type ->
            Clip(
                id = "clip-$index",
                trackId = if (type == ClipType.AUDIO) "a1" else "v1",
                type = type,
                name = "Clip ${type.name}",
                timelineStartUs = index * 2_000_000L,
                durationUs = 2_000_000L,
                blendMode = BlendMode.OVERLAY,
                transform = Transform(scaleX = 1.2f, scaleY = 1.2f, rotation = 15f),
                effects = listOf(
                    EffectInstance(
                        id = "eff-1",
                        effectId = "gaussian_blur",
                        params = mapOf("radius" to ParamValue.FloatVal(8.5f))
                    )
                ),
                keyframes = keyframes
            )
        }

        val project = Project(
            id = "proj-test",
            name = "Test Complete Project",
            frameRate = 30,
            width = 1920,
            height = 1080,
            aspectRatio = AspectRatio.RATIO_16_9,
            timeline = Timeline(
                tracks = listOf(trackVideo, trackAudio),
                clips = clips
            )
        )

        val serialized = json.encodeToString(project)
        assertTrue(serialized.contains("gaussian_blur"))

        val deserialized = json.decodeFromString<Project>(serialized)
        assertEquals(project.id, deserialized.id)
        assertEquals(project.name, deserialized.name)
        assertEquals(clips.size, deserialized.timeline.clips.size)
        assertEquals(project.timeline.durationUs, deserialized.timeline.durationUs)

        val firstClip = deserialized.timeline.clips.first()
        assertEquals(BlendMode.OVERLAY, firstClip.blendMode)
        assertEquals(2, firstClip.keyframes["opacity"]?.size)
    }

    @Test
    fun testTimelineOps_splitClip() {
        val track = Track(id = "v1", type = TrackType.VIDEO, index = 0, name = "V1")
        val clip = Clip(
            id = "c1",
            trackId = "v1",
            name = "Original.mp4",
            timelineStartUs = 0L,
            durationUs = 10_000_000L,
            sourceInUs = 0L,
            sourceOutUs = 10_000_000L
        )
        val project = Project(
            id = "p1",
            name = "Split Test",
            timeline = Timeline(tracks = listOf(track), clips = listOf(clip))
        )

        // Split at 4 seconds (4_000_000 us)
        val splitProject = TimelineOps.splitClip(project, "c1", 4_000_000L)
        assertEquals(2, splitProject.timeline.clips.size)

        val part1 = splitProject.timeline.clips.find { it.id == "c1" }
        assertNotNull(part1)
        assertEquals(4_000_000L, part1!!.durationUs)
        assertEquals(4_000_000L, part1.sourceOutUs)

        val part2 = splitProject.timeline.clips.find { it.id != "c1" }
        assertNotNull(part2)
        assertEquals(4_000_000L, part2!!.timelineStartUs)
        assertEquals(6_000_000L, part2.durationUs)
        assertEquals(4_000_000L, part2.sourceInUs)
    }

    @Test
    fun testTimelineOps_clipsAtAndRemoveClip() {
        val track = Track(id = "v1", type = TrackType.VIDEO, index = 0, name = "V1")
        val clip1 = Clip(id = "c1", trackId = "v1", timelineStartUs = 0L, durationUs = 5_000_000L)
        val clip2 = Clip(id = "c2", trackId = "v1", timelineStartUs = 4_000_000L, durationUs = 6_000_000L)

        val project = Project(
            id = "p1",
            name = "ClipsAt Test",
            timeline = Timeline(tracks = listOf(track), clips = listOf(clip1, clip2))
        )

        // At 2 seconds, only c1 is active
        val at2s = TimelineOps.clipsAt(project, 2_000_000L)
        assertEquals(1, at2s.size)
        assertEquals("c1", at2s[0].id)

        // At 4.5 seconds, both c1 and c2 are active
        val at45s = TimelineOps.clipsAt(project, 4_500_000L)
        assertEquals(2, at45s.size)

        // Remove c1
        val removed = TimelineOps.removeClip(project, "c1")
        assertEquals(1, removed.timeline.clips.size)
        assertNull(TimelineOps.findClip(removed, "c1"))
        assertNotNull(TimelineOps.findClip(removed, "c2"))
    }

    @Test
    fun testTimecodeFormatter() {
        // 0 microseconds
        assertEquals("00:00:00:00", TimecodeFormatter.formatTimecode(0L, 30))

        // 1 second at 30 fps
        assertEquals("00:00:01:00", TimecodeFormatter.formatTimecode(1_000_000L, 30))

        // 1 hour, 2 minutes, 3 seconds, 15 frames at 30 fps
        // 3600 + 120 + 3 = 3723 seconds + 0.5 sec = 3723.5 sec
        val timeUs = (3723L * 1_000_000L) + 500_000L
        assertEquals("01:02:03:15", TimecodeFormatter.formatTimecode(timeUs, 30))

        // Duration format
        assertEquals("01:02:03", TimecodeFormatter.formatDuration(timeUs))
        assertEquals("00:45", TimecodeFormatter.formatDuration(45_000_000L))
    }
}
