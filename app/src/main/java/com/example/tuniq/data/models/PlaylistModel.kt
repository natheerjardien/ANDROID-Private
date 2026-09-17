package com.example.tuniq.data.models

// JetBrains (2026) outlines the use of Lists within data classes for one-to-many relationships.

data class PlaylistModel(
    val playlistId: String = "",
    val title: String = "",
    val description: String = "",
    val creatorId: String = "",
    val isCollaborative: Boolean = false,
    val collaboratorIds: List<String> = emptyList(),
    val tracks: List<SongModel> = emptyList()
)

/* Reference List:

 * JetBrains, 2026. Data Classes. (Version 2.0) [Source code] Available at: < https://kotlinlang.org/docs/data-classes.html > [Accessed 17 September 2026].

*/