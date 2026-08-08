package dev.holdfast.app

import kotlinx.serialization.Serializable
import java.security.MessageDigest

/**
 * The record, and why it is a chain.
 *
 * Every "timestamped photo" app on the store burns the date into the pixels.
 * That is a caption, not evidence: anyone can type any date into any image, and
 * the person on the other side of a dispute knows it.
 *
 * Holdfast links entries instead. Each one hashes the photo bytes together with
 * its note, its time, and the hash of the entry before it. Change a photo,
 * reword a note, reorder two entries or quietly insert one after the fact, and
 * every hash from that point on stops matching. You cannot fix it up, because
 * fixing it up means recomputing a chain you already exported.
 *
 * This does not make a record admissible anywhere, and the app never claims it
 * does. It makes tampering detectable, which is a smaller and honest promise.
 */

const val GENESIS = "0000000000000000000000000000000000000000000000000000000000000000"

/**
 * What separates the fields inside an entry's hash input.
 *
 * ASCII 31, the unit separator. Written as an escape rather than typed, because
 * the first version of this used a literal control character pasted into the
 * source: invisible in an editor, and it silently disagreed with the rule the
 * export file printed. A separator you cannot see is a separator you cannot
 * check.
 *
 * It has to be a character that cannot appear in a note or a place name, or
 * "ab" + "c" and "a" + "bc" would hash the same and the chain would stop
 * proving anything.
 */
const val FIELD_SEP = "\u001F"

@Serializable
data class Entry(
    val id: String,
    /** Milliseconds since the epoch, taken when the entry was sealed. */
    val at: Long,
    val note: String,
    /** SHA-256 of the photo bytes, or null for a note-only entry. */
    val photoHash: String?,
    /** Relative filename inside the case folder. */
    val photoFile: String?,
    /** Coarse location, when the user chose to attach one. */
    val place: String?,
    /** Hash of the entry before this one. */
    val prev: String,
    /** Hash of this entry's own contents, including [prev]. */
    val hash: String,
)

@Serializable
data class Case(
    val id: String,
    val title: String,
    val what: String,
    val openedAt: Long,
    val entries: List<Entry> = emptyList(),
) {
    val head: String get() = entries.lastOrNull()?.hash ?: GENESIS
}

fun sha256(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

fun sha256(text: String): String = sha256(text.toByteArray(Charsets.UTF_8))

/**
 * The bytes an entry's hash is taken over.
 *
 * Field-separated with a character that cannot occur in the fields themselves,
 * because "ab" + "c" and "a" + "bc" must not hash the same. Concatenating
 * user-supplied strings without a separator is the classic way a hash chain
 * quietly stops proving anything.
 */
internal fun preimage(
    id: String,
    at: Long,
    note: String,
    photoHash: String?,
    place: String?,
    prev: String,
): String = listOf(id, at.toString(), note, photoHash ?: "-", place ?: "-", prev)
    .joinToString(FIELD_SEP)

fun sealEntry(
    id: String,
    at: Long,
    note: String,
    photoHash: String?,
    photoFile: String?,
    place: String?,
    prev: String,
): Entry {
    val hash = sha256(preimage(id, at, note, photoHash, place, prev))
    return Entry(
        id = id,
        at = at,
        note = note,
        photoHash = photoHash,
        photoFile = photoFile,
        place = place,
        prev = prev,
        hash = hash,
    )
}

sealed interface Verdict {
    data object Intact : Verdict

    /** The first entry that does not check out, and what is wrong with it. */
    data class Broken(val index: Int, val entryId: String, val reason: String) : Verdict

    data object Empty : Verdict
}

/**
 * Walk the chain and report the first thing that does not hold.
 *
 * Reports the earliest failure rather than a count, because the earliest one is
 * where the record stopped being trustworthy; everything after it is suspect
 * whether or not it hashes correctly.
 */
fun verify(case: Case, photoHashes: Map<String, String> = emptyMap()): Verdict {
    if (case.entries.isEmpty()) return Verdict.Empty

    var expectedPrev = GENESIS
    case.entries.forEachIndexed { index, entry ->
        if (entry.prev != expectedPrev) {
            return Verdict.Broken(index, entry.id, "does not follow the entry before it")
        }
        val recomputed = sha256(
            preimage(entry.id, entry.at, entry.note, entry.photoHash, entry.place, entry.prev),
        )
        if (recomputed != entry.hash) {
            return Verdict.Broken(index, entry.id, "its contents changed after it was sealed")
        }
        // Time must not run backwards. An entry dated before the one it follows
        // is the shape of a record written to suit an argument afterwards.
        val previousAt = case.entries.getOrNull(index - 1)?.at
        if (previousAt != null && entry.at < previousAt) {
            return Verdict.Broken(index, entry.id, "is dated before the entry it follows")
        }
        // When the photo is present, check the file still hashes to what was sealed.
        val file = entry.photoFile
        val sealedPhoto = entry.photoHash
        if (file != null && sealedPhoto != null) {
            val actual = photoHashes[file]
            if (actual != null && actual != sealedPhoto) {
                return Verdict.Broken(index, entry.id, "its photo was replaced")
            }
        }
        expectedPrev = entry.hash
    }
    return Verdict.Intact
}

/** A short, readable form of a hash, for showing a person. */
fun shortHash(hash: String): String =
    if (hash.length <= 12) hash else "${hash.take(6)}…${hash.takeLast(4)}"
