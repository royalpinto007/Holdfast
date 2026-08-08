package dev.holdfast.app

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

/**
 * Where cases live: this app's private storage, and nowhere else.
 *
 * Plain JSON on disk rather than a database. A case is a few dozen entries at
 * most, the whole file is rewritten on every change anyway, and the format
 * being readable matters: someone should be able to open an export in a text
 * editor and check it by hand. A schema nobody can inspect is a poor foundation
 * for a record whose only job is to be checkable.
 */
class Vault(private val context: Context) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val root: File get() = File(context.filesDir, "cases").apply { mkdirs() }

    private fun caseDir(caseId: String) = File(root, caseId).apply { mkdirs() }
    private fun caseFile(caseId: String) = File(caseDir(caseId), "case.json")

    fun list(): List<Case> =
        root.listFiles()
            ?.mapNotNull { dir -> runCatching { read(dir.name) }.getOrNull() }
            ?.sortedByDescending { it.entries.lastOrNull()?.at ?: it.openedAt }
            ?: emptyList()

    fun read(caseId: String): Case? {
        val f = caseFile(caseId)
        if (!f.exists()) return null
        return runCatching { json.decodeFromString<Case>(f.readText()) }.getOrNull()
    }

    fun create(title: String, what: String, now: Long): Case {
        val case = Case(
            id = UUID.randomUUID().toString().take(8),
            title = title.trim(),
            what = what.trim(),
            openedAt = now,
        )
        write(case)
        return case
    }

    fun write(case: Case) {
        caseFile(case.id).writeText(json.encodeToString(case))
    }

    fun delete(caseId: String) {
        caseDir(caseId).deleteRecursively()
    }

    fun photoFile(caseId: String, name: String) = File(caseDir(caseId), name)

    /** A new file to point the camera at. Named by time so the folder sorts. */
    fun newPhotoTarget(caseId: String, now: Long): File =
        photoFile(caseId, "$now.jpg")

    /**
     * Seal a new entry onto the end of a case.
     *
     * The photo is hashed from the bytes actually on disk, not from whatever
     * the caller believed it wrote, so the sealed hash always describes the
     * file that is really there.
     */
    fun append(case: Case, note: String, photo: File?, place: String?, now: Long): Case {
        val photoHash = photo?.takeIf { it.exists() }?.let { sha256(it.readBytes()) }
        val entry = sealEntry(
            id = UUID.randomUUID().toString().take(8),
            at = now,
            note = note.trim(),
            photoHash = photoHash,
            photoFile = photo?.name,
            place = place?.takeIf { it.isNotBlank() },
            prev = case.head,
        )
        val updated = case.copy(entries = case.entries + entry)
        write(updated)
        return updated
    }

    /** Hash every photo present on disk, so verification checks the real files. */
    fun photoHashes(case: Case): Map<String, String> =
        case.entries.mapNotNull { it.photoFile }.distinct().mapNotNull { name ->
            val f = photoFile(case.id, name)
            if (f.exists()) name to sha256(f.readBytes()) else null
        }.toMap()

    /**
     * A case as a single text file, written for a human on the other side.
     *
     * The point of an export is that somebody who does not have this app can
     * still check it: the hashes are printed in full and the rule for
     * recomputing them is stated at the top.
     */
    fun exportText(case: Case): String = buildString {
        appendLine("HOLDFAST RECORD")
        appendLine("case ${case.id}: ${case.title}")
        appendLine(case.what)
        appendLine("opened ${Stamp.full(case.openedAt)}")
        appendLine("entries ${case.entries.size}")
        appendLine()
        appendLine("Each entry's hash is SHA-256 over its fields joined by ASCII 31,")
        appendLine("the unit separator, in this order:")
        appendLine("  id, time in milliseconds, note, photo hash, place, previous hash")
        appendLine("A missing photo hash or place is written as a single dash.")
        appendLine("The separator is a control character so it cannot occur inside a note.")
        appendLine("The first entry follows $GENESIS.")
        appendLine()
        case.entries.forEachIndexed { i, e ->
            appendLine("── ${i + 1} ──────────────────────────────────────")
            appendLine("time   ${Stamp.full(e.at)}  (${e.at})")
            appendLine("note   ${e.note.ifBlank { "-" }}")
            appendLine("photo  ${e.photoFile ?: "-"}")
            appendLine("sha    ${e.photoHash ?: "-"}")
            appendLine("place  ${e.place ?: "-"}")
            appendLine("prev   ${e.prev}")
            appendLine("hash   ${e.hash}")
            appendLine()
        }
        // The app calls this the seal. Both words are here so a reader can match
        // the file against the screen it came from.
        appendLine("head, the latest seal: ${case.head}")
    }

    fun writeExport(case: Case): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val f = File(dir, "holdfast-${case.id}.txt")
        f.writeText(exportText(case))
        return f
    }
}
