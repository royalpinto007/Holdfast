package dev.holdfast.app

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class SeedTest {
    @Test
    fun `a chain built to the documented rule verifies`() {
        val text = javaClass.classLoader!!.getResourceAsStream("case.json")!!
            .bufferedReader().readText()
        val case = Json { ignoreUnknownKeys = true }.decodeFromString<Case>(text)
        val v = verify(case)
        if (v is Verdict.Broken) {
            val e = case.entries[v.index]
            val pre = preimage(e.id, e.at, e.note, e.photoHash, e.place, e.prev)
            throw AssertionError(
                "broken at ${v.index}: ${v.reason}\n" +
                    "preimage=[$pre]\n" +
                    "recomputed=${sha256(pre)}\nstored=${e.hash}\nprev=${e.prev}",
            )
        }
        assertEquals(Verdict.Intact, v)
    }
}
