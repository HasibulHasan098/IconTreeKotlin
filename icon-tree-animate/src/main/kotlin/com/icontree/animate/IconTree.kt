package com.icontree.animate

import android.content.Context
import org.json.JSONObject

/** Icon identifiers are plain strings such as `"heart"`; see [iconIds] for the full list. */
typealias IconId = String

/** Local icon index, search and loading. Nothing here touches the network. */
object IconTree {
    /** Directory inside the library's assets that holds the bundled icon JSON files. */
    internal const val ASSET_DIR = "icontree"

    /** All bundled icon IDs. */
    val ids: List<IconId> get() = iconIds

    /**
     * Returns icon IDs containing any query term (case-insensitive). Commas and whitespace both
     * separate terms, matching `npx icontree search`.
     */
    fun search(query: String): List<IconId> {
        val terms = query.trim().lowercase().split(Regex("[\\s,]+")).filter { it.isNotEmpty() }
        if (terms.isEmpty()) return emptyList()
        return iconIds.filter { id ->
            val normalized = id.lowercase()
            terms.any { normalized.contains(it) }
        }
    }

    /** Loads the raw (uncolored) Lottie JSON for [id], or null when the ID is unknown. Blocking I/O. */
    fun loadRaw(context: Context, id: IconId): JSONObject? {
        return runCatching {
            context.applicationContext.assets.open("$ASSET_DIR/$id.json").use {
                JSONObject(it.readBytes().toString(Charsets.UTF_8))
            }
        }.getOrNull()
    }

    /** Loads [id] and recolors it to [color] (hex), memoized. Null when the ID is unknown. Blocking. */
    fun loadRecolored(context: Context, id: IconId, color: String): JSONObject? =
        loadRaw(context, id)?.let { getRecoloredLottie(id, it, color) }
}
