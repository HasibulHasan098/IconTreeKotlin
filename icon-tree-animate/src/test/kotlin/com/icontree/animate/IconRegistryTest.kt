package com.icontree.animate

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class IconRegistryTest {
    // Gradle runs unit tests with the module directory as the working directory.
    private val iconDir = File("../icons")

    @Test fun registryMatchesIconFolder() {
        val onDisk = iconDir.listFiles { f -> f.name.endsWith(".json") }!!
            .map { it.name.replace(Regex("(\\.json)+$"), "") }
        assertEquals(946, onDisk.size)
        assertEquals(onDisk.toSet(), iconIds.toSet())
        assertEquals(iconIds.size, iconIds.toSet().size)
    }

    @Test fun everyIconIsValidLottie() {
        for (id in iconIds) {
            val json = JSONObject(File(iconDir, "$id.json").readText())
            assertTrue("$id has no layers", json.has("layers"))
        }
    }

    @Test fun searchMatchesAnyTermLikeCli() {
        val hits = IconTree.search("arrow right")
        assertTrue("arrow-right" in hits)
        assertTrue("heart" in IconTree.search("heart,star"))
        assertTrue("star" in IconTree.search("heart,star"))
        assertTrue(IconTree.search("zzzz-no-such-icon").isEmpty())
        assertTrue(IconTree.search("  ").isEmpty())
        assertTrue("HEART" !in hits && "heart" in IconTree.search("HEART"))
    }
}
