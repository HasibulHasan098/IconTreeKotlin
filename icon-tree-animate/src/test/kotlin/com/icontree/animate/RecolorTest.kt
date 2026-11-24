package com.icontree.animate

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class RecolorTest {
    private fun sample() = JSONObject(
        """{"layers":[{"ty":4,"shapes":[{"ty":"gr","it":[
            {"ty":"fl","c":{"k":[0,0,0,1]}},{"ty":"st","c":{"k":[1,1,1,1]}},{"ty":"sh"}]}]},
            {"ty":0,"layers":[{"ty":4,"shapes":[{"ty":"fl","c":{"k":[0.5,0.5,0.5,1]}}]}]}]}"""
    )

    private fun JSONArray.doubles() = (0 until length()).map { getDouble(it) }

    @Test fun recolorsNestedFillsAndStrokes() {
        val out = recolorLottie(sample(), "#ff0000")
        val group = out.getJSONArray("layers").getJSONObject(0).getJSONArray("shapes").getJSONObject(0).getJSONArray("it")
        assertEquals(listOf(1.0, 0.0, 0.0, 1.0), group.getJSONObject(0).getJSONObject("c").getJSONArray("k").doubles())
        assertEquals(listOf(1.0, 0.0, 0.0, 1.0), group.getJSONObject(1).getJSONObject("c").getJSONArray("k").doubles())
        val nested = out.getJSONArray("layers").getJSONObject(1).getJSONArray("layers").getJSONObject(0)
            .getJSONArray("shapes").getJSONObject(0).getJSONObject("c").getJSONArray("k")
        assertEquals(listOf(1.0, 0.0, 0.0, 1.0), nested.doubles())
    }

    @Test fun doesNotMutateInput() {
        val input = sample()
        val before = input.toString()
        recolorLottie(input, "#00ff00")
        assertEquals(before, input.toString())
    }

    @Test fun expandsShortHex() {
        assertEquals(listOf(1.0, 0.0, 1.0, 1.0), hexToRgba("#f0f").toList())
    }

    @Test fun memoizesByIconAndColor() {
        val input = sample()
        val a = getRecoloredLottie("memo-test", input, "#123456")
        assertSame(a, getRecoloredLottie("memo-test", input, "#123456"))
        assertNotSame(a, getRecoloredLottie("memo-test", input, "#654321"))
    }
}
