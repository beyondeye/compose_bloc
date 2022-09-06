package com.beyondeye.kbloc.router

import kotlin.test.*

public class PathTest {
    @Test
    public fun testingPathConverter() {
        val prefixPath = "/a"
        assertEquals(Path("/a", null), Path.from(prefixPath))
        assertEquals(Path("/a", null), Path.from("a"))
    }

    @Test
    public fun relative() {
        val base = Path.from("/a/b/c/d")
        assertEquals("/a/b/c", base.relative("./").path)
        assertEquals("/a/b", base.relative("././").path)
        assertEquals("/a/b/g", base.relative("././g").path)

        assertEquals("/a/b/g?foo=bar", base.relative("././g?foo=bar").toString())
    }
}
