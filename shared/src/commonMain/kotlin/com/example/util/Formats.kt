package com.example.util

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.time.Clock
import kotlin.time.Instant

/** Dates are stored as ISO strings (yyyy-MM-dd) everywhere. */
object Dates {
    private val monthShort = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val monthLong = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

    fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    fun todayIso(): String = today().toString()

    fun parse(iso: String): LocalDate? = try {
        LocalDate.parse(iso)
    } catch (_: Exception) {
        null
    }

    fun minusDays(date: LocalDate, days: Int): LocalDate = date.minus(DatePeriod(days = days))

    fun isoDaysAgo(days: Int): String = minusDays(today(), days).toString()

    fun fromMillis(millis: Long): LocalDate =
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).date

    /** "27 Sep 2026" (or "27 Sep" when [short]). Falls back to the input on bad data. */
    fun pretty(iso: String, short: Boolean = false): String {
        val d = parse(iso) ?: return iso
        return pretty(d, short)
    }

    fun pretty(d: LocalDate, short: Boolean = false): String {
        val day = d.day.toString().padStart(2, '0')
        val m = monthShort[d.month.number - 1]
        return if (short) "$day $m" else "$day $m ${d.year}"
    }

    /** "SUNDAY, SEPTEMBER 27" style header. */
    fun longHeader(iso: String): String {
        val d = parse(iso) ?: return iso
        val dow = d.dayOfWeek.displayName()
        return "$dow, ${monthLong[d.month.number - 1]} ${d.day}"
    }

    private fun DayOfWeek.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

    /** Material3 DatePicker works in UTC millis at midnight. */
    fun isoToPickerMillis(iso: String): Long =
        (parse(iso) ?: today()).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    fun pickerMillisToIso(millis: Long): String =
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date.toString()

    fun year(): Int = today().year
}

/** One decimal place, locale-independent ("7.5", "-0.3"). */
fun Double.fmt1(): String {
    val r = (this * 10).roundToLong()
    val sign = if (r < 0) "-" else ""
    val a = abs(r)
    return "$sign${a / 10}.${a % 10}"
}

fun Int.pad2(): String = toString().padStart(2, '0')

/** Skill scores are stored as a JSON object: {"skillId": 7.5, ...}. */
object ScoreJson {
    fun decode(json: String): Map<String, Double> = try {
        Json.parseToJsonElement(json).jsonObject.mapNotNull { (k, v) ->
            v.jsonPrimitive.doubleOrNull?.let { k to it }
        }.toMap()
    } catch (_: Exception) {
        emptyMap()
    }

    fun encode(scores: Map<String, Double>): String =
        JsonObject(scores.mapValues { JsonPrimitive(it.value) }).toString()
}
