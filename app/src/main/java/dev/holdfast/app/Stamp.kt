package dev.holdfast.app

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Times, written the way a record should write them.
 *
 * Everything is absolute. "2 hours ago" is friendly and useless here: the whole
 * point of the record is when a thing happened, and a relative time changes
 * meaning every time somebody reads it.
 */
object Stamp {
    private val zone: ZoneId get() = ZoneId.systemDefault()

    private val fullFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm:ss", Locale.UK)
    private val shortFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.UK)
    private val dayFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.UK)

    fun full(at: Long): String =
        fullFmt.format(Instant.ofEpochMilli(at).atZone(zone)) + " " + zoneLabel()

    fun short(at: Long): String = shortFmt.format(Instant.ofEpochMilli(at).atZone(zone))

    fun day(at: Long): String = dayFmt.format(Instant.ofEpochMilli(at).atZone(zone))

    /** The zone is part of the claim, so it is always shown on the full form. */
    private fun zoneLabel(): String = zone.id

    /** How long a case has been running, for the header. */
    fun spanDays(from: Long, to: Long): Long =
        ((to - from) / 86_400_000L).coerceAtLeast(0)
}
