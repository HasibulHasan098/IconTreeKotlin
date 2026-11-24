package com.icontree.animate

import org.json.JSONArray
import org.json.JSONObject

/** Parses `#rgb` or `#rrggbb` into a Lottie `[r, g, b, a]` array (channels 0..1, alpha always 1). */
internal fun hexToRgba(hex: String): DoubleArray {
    var h = hex.removePrefix("#")
    if (h.length == 3) h = "${h[0]}${h[0]}${h[1]}${h[1]}${h[2]}${h[2]}"
    fun channel(start: Int): Double =
        h.substring(start, minOf(start + 2, h.length)).toIntOrNull(16)?.div(255.0) ?: 0.0
    return doubleArrayOf(channel(0), channel(2), channel(4), 1.0)
}

private fun colorJson(color: DoubleArray) = JSONArray().also { arr -> color.forEach { arr.put(it) } }

private fun recolorLayer(layer: JSONObject, color: DoubleArray) {
    layer.optJSONArray("layers")?.forEachObject { recolorLayer(it, color) }
    layer.optJSONArray("shapes")?.forEachObject { recolorShape(it, color) }
}

private fun recolorShape(shape: JSONObject, color: DoubleArray) {
    val ty = shape.optString("ty")
    if (ty == "fl" || ty == "st") {
        val c = shape.optJSONObject("c")
        if (c != null && c.has("k") && !c.isNull("k")) c.put("k", colorJson(color))
    }
    shape.optJSONArray("it")?.forEachObject { recolorShape(it, color) }
}

private inline fun JSONArray.forEachObject(action: (JSONObject) -> Unit) {
    for (i in 0 until length()) optJSONObject(i)?.let(action)
}

/**
 * Returns a recolored deep copy of Lottie animation data: every solid fill (`fl`) and stroke (`st`)
 * color is replaced with [color] (a hex string such as `"#ff0000"`). The input is not modified.
 */
fun recolorLottie(json: JSONObject, color: String): JSONObject {
    val clone = JSONObject(json.toString())
    val rgba = hexToRgba(color)
    clone.optJSONArray("layers")?.forEachObject { recolorLayer(it, rgba) }
    return clone
}

// Bounded LRU so scrolling through many icons/colors can't grow memory without limit.
private const val MEMO_LIMIT = 64
private val memoCache = object : LinkedHashMap<String, JSONObject>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, JSONObject>) = size > MEMO_LIMIT
}

/** Like [recolorLottie] but memoized (LRU, 64 entries) by icon ID and color. */
fun getRecoloredLottie(iconId: String, json: JSONObject, color: String): JSONObject {
    val key = "$iconId-$color"
    synchronized(memoCache) { memoCache[key]?.let { return it } }
    val result = recolorLottie(json, color)
    synchronized(memoCache) { memoCache[key] = result }
    return result
}
