package com.procut.editor.data

import com.procut.editor.model.AspectRatio
import com.procut.editor.model.Clip
import com.procut.editor.model.ClipType
import com.procut.editor.model.Project
import com.procut.editor.model.Timeline
import com.procut.editor.model.Track
import com.procut.editor.model.TrackType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface ProjectRepository {
    fun getAllProjects(): Flow<List<Project>>
    suspend fun getProjectById(id: String): Project?
    suspend fun saveProject(project: Project)
    suspend fun createProject(name: String, aspectRatio: AspectRatio, frameRate: Int): Project
    suspend fun duplicateProject(id: String): Project?
    suspend fun renameProject(id: String, newName: String)
    suspend fun deleteProject(id: String)
    suspend fun createFromTemplate(templateId: String, newName: String): Project?
    suspend fun saveAsTemplate(projectId: String)
}

class InMemoryProjectRepository : ProjectRepository {

    private val _projects = MutableStateFlow<List<Project>>(createInitialProjects())

    override fun getAllProjects(): Flow<List<Project>> = _projects.asStateFlow()

    override suspend fun getProjectById(id: String): Project? {
        return _projects.value.find { it.id == id }
    }

    override suspend fun saveProject(project: Project) {
        val current = _projects.value.toMutableList()
        val index = current.indexOfFirst { it.id == project.id }
        if (index >= 0) {
            current[index] = project.copy(modifiedAt = System.currentTimeMillis())
        } else {
            current.add(0, project)
        }
        _projects.value = current
    }

    override suspend fun createProject(name: String, aspectRatio: AspectRatio, frameRate: Int): Project {
        val (width, height) = when (aspectRatio) {
            AspectRatio.RATIO_16_9 -> 1920 to 1080
            AspectRatio.RATIO_9_16 -> 1080 to 1920
            AspectRatio.RATIO_1_1 -> 1080 to 1080
            AspectRatio.RATIO_4_5 -> 1080 to 1350
            AspectRatio.RATIO_21_9 -> 2560 to 1080
        }

        val videoTrack1 = Track(id = UUID.randomUUID().toString(), type = TrackType.VIDEO, index = 0, name = "V1")
        val audioTrack1 = Track(id = UUID.randomUUID().toString(), type = TrackType.AUDIO, index = 1, name = "A1")

        val newProject = Project(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Untitled Project" },
            frameRate = frameRate,
            width = width,
            height = height,
            aspectRatio = aspectRatio,
            timeline = Timeline(
                tracks = listOf(videoTrack1, audioTrack1),
                clips = emptyList()
            )
        )
        saveProject(newProject)
        return newProject
    }

    override suspend fun duplicateProject(id: String): Project? {
        val original = getProjectById(id) ?: return null
        val duplicated = original.copy(
            id = UUID.randomUUID().toString(),
            name = "${original.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        saveProject(duplicated)
        return duplicated
    }

    override suspend fun renameProject(id: String, newName: String) {
        val project = getProjectById(id) ?: return
        saveProject(project.copy(name = newName.trim()))
    }

    override suspend fun deleteProject(id: String) {
        _projects.value = _projects.value.filterNot { it.id == id }
    }

    override suspend fun createFromTemplate(templateId: String, newName: String): Project? {
        val template = getProjectById(templateId) ?: return null
        val newProj = template.copy(
            id = UUID.randomUUID().toString(),
            name = newName.ifBlank { "${template.name} Project" },
            isTemplate = false,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        saveProject(newProj)
        return newProj
    }

    override suspend fun saveAsTemplate(projectId: String) {
        val proj = getProjectById(projectId) ?: return
        val template = proj.copy(
            id = UUID.randomUUID().toString(),
            name = "${proj.name} [Template]",
            isTemplate = true
        )
        saveProject(template)
    }

    private companion object {
        fun createInitialProjects(): List<Project> {
            val vTrack1 = Track(id = "v1", type = TrackType.VIDEO, index = 0, name = "V1")
            val vTrack2 = Track(id = "v2", type = TrackType.VIDEO, index = 1, name = "V2")
            val aTrack1 = Track(id = "a1", type = TrackType.AUDIO, index = 2, name = "A1")

            val sampleClip1 = Clip(
                id = "c1",
                trackId = "v1",
                type = ClipType.VIDEO,
                name = "A-Roll Intro.mp4",
                timelineStartUs = 0L,
                durationUs = 6_000_000L,
                sourceOutUs = 6_000_000L
            )
            val sampleClip2 = Clip(
                id = "c2",
                trackId = "v1",
                type = ClipType.VIDEO,
                name = "Drone Shot B-Roll.mp4",
                timelineStartUs = 6_000_000L,
                durationUs = 8_000_000L,
                sourceOutUs = 8_000_000L
            )
            val sampleClip3 = Clip(
                id = "c3",
                trackId = "a1",
                type = ClipType.AUDIO,
                name = "Cinematic Ambient.wav",
                timelineStartUs = 0L,
                durationUs = 14_000_000L,
                sourceOutUs = 14_000_000L,
                volume = 0.85f
            )
            val sampleClip4 = Clip(
                id = "c4",
                trackId = "v2",
                type = ClipType.TEXT,
                name = "Lower Third Title",
                textContent = "PRODUCER / DIRECTOR",
                timelineStartUs = 2_000_000L,
                durationUs = 4_000_000L
            )

            val p1 = Project(
                id = "proj-1",
                name = "Cinematic Showreel 4K",
                frameRate = 30,
                width = 1920,
                height = 1080,
                aspectRatio = AspectRatio.RATIO_16_9,
                timeline = Timeline(
                    tracks = listOf(vTrack1, vTrack2, aTrack1),
                    clips = listOf(sampleClip1, sampleClip2, sampleClip3, sampleClip4)
                )
            )

            val p2V1 = Track(id = "p2_v1", type = TrackType.VIDEO, index = 0, name = "V1")
            val p2A1 = Track(id = "p2_a1", type = TrackType.AUDIO, index = 1, name = "A1")
            val shortsClip = Clip(
                id = "sc1",
                trackId = "p2_v1",
                type = ClipType.VIDEO,
                name = "Vertical Teaser.mp4",
                timelineStartUs = 0L,
                durationUs = 15_000_000L,
                sourceOutUs = 15_000_000L
            )
            val p2 = Project(
                id = "proj-2",
                name = "Reels Travel Vlog #12",
                frameRate = 60,
                width = 1080,
                height = 1920,
                aspectRatio = AspectRatio.RATIO_9_16,
                timeline = Timeline(
                    tracks = listOf(p2V1, p2A1),
                    clips = listOf(shortsClip)
                )
            )

            return listOf(p1, p2)
        }
    }
}
