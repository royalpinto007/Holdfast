package dev.holdfast.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChainTest {

    private fun chain(vararg notes: String): Case {
        var prev = GENESIS
        val entries = notes.mapIndexed { i, note ->
            val e = sealEntry(
                id = "e$i",
                at = 1_700_000_000_000L + i * 60_000L,
                note = note,
                photoHash = "photo$i",
                photoFile = "p$i.jpg",
                place = null,
                prev = prev,
            )
            prev = e.hash
            e
        }
        return Case("c1", "Flat 3B", "Move-in condition", 1_700_000_000_000L, entries)
    }

    @Test
    fun `an untouched chain verifies`() {
        assertEquals(Verdict.Intact, verify(chain("hallway", "kitchen", "bathroom")))
    }

    @Test
    fun `an empty case is empty rather than intact`() {
        // "Intact" on a case with nothing in it would be a comforting lie.
        assertEquals(Verdict.Empty, verify(chain()))
    }

    @Test
    fun `editing a note after the fact is caught`() {
        val case = chain("hallway", "kitchen", "bathroom")
        val tampered = case.copy(
            entries = case.entries.toMutableList().also {
                it[1] = it[1].copy(note = "kitchen, already damaged")
            },
        )
        val verdict = verify(tampered)
        assertTrue(verdict is Verdict.Broken)
        assertEquals(1, (verdict as Verdict.Broken).index)
        assertTrue(verdict.reason.contains("was changed"))
    }

    @Test
    fun `removing an entry from the middle is caught`() {
        val case = chain("hallway", "kitchen", "bathroom")
        val tampered = case.copy(entries = listOf(case.entries[0], case.entries[2]))
        val verdict = verify(tampered)
        assertTrue(verdict is Verdict.Broken)
        // The gap shows at the entry that no longer follows its predecessor.
        assertEquals(1, (verdict as Verdict.Broken).index)
    }

    @Test
    fun `reordering two entries is caught`() {
        val case = chain("hallway", "kitchen", "bathroom")
        val tampered = case.copy(
            entries = listOf(case.entries[0], case.entries[2], case.entries[1]),
        )
        assertTrue(verify(tampered) is Verdict.Broken)
    }

    @Test
    fun `inserting an entry after the fact is caught`() {
        val case = chain("hallway", "kitchen")
        val forged = sealEntry(
            id = "forged",
            at = 1_700_000_030_000L,
            note = "damage was already here",
            photoHash = "x",
            photoFile = "x.jpg",
            place = null,
            prev = case.entries[0].hash,
        )
        val tampered = case.copy(entries = listOf(case.entries[0], forged, case.entries[1]))
        // The forged entry hashes correctly on its own. It is the entry after it
        // that no longer follows, which is the whole point of chaining.
        assertTrue(verify(tampered) is Verdict.Broken)
    }

    @Test
    fun `swapping the photo file is caught when the file is present`() {
        val case = chain("hallway", "kitchen")
        val hashes = mapOf("p0.jpg" to "photo0", "p1.jpg" to "something else")
        val verdict = verify(case, hashes)
        assertTrue(verdict is Verdict.Broken)
        assertTrue((verdict as Verdict.Broken).reason.contains("photo that was replaced"))
    }

    @Test
    fun `backdating an entry is caught`() {
        val case = chain("hallway", "kitchen")
        val moved = case.entries[1].let { second ->
            sealEntry(
                id = second.id,
                at = case.entries[0].at - 60_000L,
                note = second.note,
                photoHash = second.photoHash,
                photoFile = second.photoFile,
                place = second.place,
                prev = second.prev,
            )
        }
        val tampered = case.copy(entries = listOf(case.entries[0], moved))
        val verdict = verify(tampered)
        assertTrue(verdict is Verdict.Broken)
        assertTrue((verdict as Verdict.Broken).reason.contains("dated before"))
    }

    @Test
    fun `field boundaries cannot be shifted between fields`() {
        // "ab" + "c" must not hash the same as "a" + "bc". Without a separator
        // a note ending in a space and a place starting with one would collide,
        // and the chain would quietly stop proving anything.
        val a = preimage("id", 1L, "ab", "c", null, GENESIS)
        val b = preimage("id", 1L, "a", "bc", null, GENESIS)
        assertNotEquals(sha256(a), sha256(b))
    }

    @Test
    fun `the separator is the documented control character, not a space`() {
        // Pinned deliberately. It was once a literal NUL typed into the source,
        // which is invisible in an editor and disagreed with what the export
        // file told the reader the rule was.
        assertEquals("\u001F", FIELD_SEP)
        assertTrue(preimage("a", 1L, "n", null, null, GENESIS).contains('\u001F'))
    }

    @Test
    fun `the same contents always seal to the same hash`() {
        val one = sealEntry("id", 1L, "note", "ph", "f.jpg", "Kochi", GENESIS)
        val two = sealEntry("id", 1L, "note", "ph", "f.jpg", "Kochi", GENESIS)
        assertEquals(one.hash, two.hash)
    }

    @Test
    fun `head is the last hash, or genesis when there is nothing`() {
        assertEquals(GENESIS, chain().head)
        val case = chain("one", "two")
        assertEquals(case.entries.last().hash, case.head)
    }

    @Test
    fun `sha256 matches the known digest of an empty input`() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            sha256(ByteArray(0)),
        )
    }

    @Test
    fun `short hash keeps both ends so it can be compared by eye`() {
        val h = "a".repeat(64)
        assertEquals("aaaaaa…aaaa", shortHash(h))
        assertEquals("short", shortHash("short"))
    }
}
