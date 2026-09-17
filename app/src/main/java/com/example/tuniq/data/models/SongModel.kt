package com.example.tuniq.data.models

// JetBrains (2026) demonstrates how to create data classes to hold JSON payload data efficiently.

data class SongModel(
    val trackId: String = "",
    val title: String = "",
    val artistName: String = "",
    val albumName: String = "",
    val durationMs: Int = 0,
    val coverArtUrl: String = "",
    val audioStreamUrl: String = "",
    val localFilePath: String? = null,
    val isLiked: Boolean = false,
    val bitrateKbps: Int = 160
)

/* Reference List:

 * JetBrains, 2026. Data Classes. (Version 2.0) [Source code] Available at: < https://kotlinlang.org/docs/data-classes.html > [Accessed 17 September 2026].

*/