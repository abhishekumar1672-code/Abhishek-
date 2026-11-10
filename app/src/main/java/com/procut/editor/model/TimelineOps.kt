package com.procut.editor.model

import java.util.UUID

object TimelineOps {

    fun findClip(project: Project, clipId: String): Clip? {
        return project.timeline.clips.find { it.id == clipId }
    }

    fun clipsAt(project: Project, timeUs: TimeUs): List<Clip> {
        return project.timeline.clips.filter { clip ->
            timeUs >= clip.timelineStartUs && timeUs < (clip.timelineStartUs + clip.durationUs)
        }
    }

    fun trackOf(project: Project, clip: Clip): Track? {
        return project.timeline.tracks.find { it.id == clip.trackId }
    }

    fun totalDurationUs(project: Project): TimeUs {
        return project.durationUs
    }

    fun addClip(project: Project, clip: Clip): Project {
        val updatedClips = project.timeline.clips + clip
        val updatedTimeline = project.timeline.copy(clips = updatedClips)
        return project.copy(
            timeline = updatedTimeline,
            modifiedAt = System.currentTimeMillis()
        )
    }

    fun removeClip(project: Project, clipId: String): Project {
        val updatedClips = project.timeline.clips.filterNot { it.id == clipId }
        val updatedTimeline = project.timeline.copy(clips = updatedClips)
        return project.copy(
            timeline = updatedTimeline,
            modifiedAt = System.currentTimeMillis()
        )
    }

    fun moveClip(project: Project, clipId: String, newTrackId: String, newStartUs: TimeUs): Project {
        val updatedClips = project.timeline.clips.map { clip ->
            if (clip.id == clipId) {
                clip.copy(trackId = newTrackId, timelineStartUs = newStartUs.coerceAtLeast(0L))
            } else {
                clip
            }
        }
        return project.copy(
            timeline = project.timeline.copy(clips = updatedClips),
            modifiedAt = System.currentTimeMillis()
        )
    }

    fun splitClip(project: Project, clipId: String, splitTimeUs: TimeUs): Project {
        val target = findClip(project, clipId) ?: return project
        val clipStart = target.timelineStartUs
        val clipEnd = clipStart + target.durationUs

        if (splitTimeUs <= clipStart || splitTimeUs >= clipEnd) {
            return project // Cannot split outside clip boundaries
        }

        val firstPartDurationUs = splitTimeUs - clipStart
        val secondPartDurationUs = clipEnd - splitTimeUs

        val firstPart = target.copy(
            durationUs = firstPartDurationUs,
            sourceOutUs = target.sourceInUs + (firstPartDurationUs * target.speed).toLong()
        )

        val secondPart = target.copy(
            id = UUID.randomUUID().toString(),
            timelineStartUs = splitTimeUs,
            durationUs = secondPartDurationUs,
            sourceInUs = firstPart.sourceOutUs,
            name = "${target.name} (Part 2)"
        )

        val updatedClips = project.timeline.clips.map { clip ->
            if (clip.id == clipId) firstPart else clip
        } + secondPart

        return project.copy(
            timeline = project.timeline.copy(clips = updatedClips),
            modifiedAt = System.currentTimeMillis()
        )
    }

    fun replaceClip(project: Project, newClip: Clip): Project {
        val updatedClips = project.timeline.clips.map { clip ->
            if (clip.id == newClip.id) newClip else clip
        }
        return project.copy(
            timeline = project.timeline.copy(clips = updatedClips),
            modifiedAt = System.currentTimeMillis()
        )
    }

    fun updateClipTransform(project: Project, clipId: String, transform: Transform): Project {
        val updatedClips = project.timeline.clips.map { clip ->
            if (clip.id == clipId) clip.copy(transform = transform) else clip
        }
        return project.copy(
            timeline = project.timeline.copy(clips = updatedClips),
            modifiedAt = System.currentTimeMillis()
        )
    }

    fun addTrack(project: Project, type: TrackType, name: String? = null): Project {
        val trackCount = project.timeline.tracks.count { it.type == type }
        val defaultName = when (type) {
            TrackType.VIDEO -> "V${trackCount + 1}"
            TrackType.AUDIO -> "A${trackCount + 1}"
            TrackType.SUBTITLE -> "Sub${trackCount + 1}"
        }
        val newTrack = Track(
            id = UUID.randomUUID().toString(),
            type = type,
            index = project.timeline.tracks.size,
            name = name ?: defaultName
        )
        return project.copy(
            timeline = project.timeline.copy(tracks = project.timeline.tracks + newTrack),
            modifiedAt = System.currentTimeMillis()
        )
    }
}
