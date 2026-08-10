package com.bradmir.pescapr.ui

import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.cos

internal data class MorphologyFeatureMetadata(
    val id: String,
    val nameEs: String?,
    val nameEn: String?,
    val structureType: String?,
    val bottomType: String?,
    val targetSpecies: String?,
    val fishingStrategyEs: String?,
    val fishingStrategyEn: String?,
    val notes: String?,
    val bestTide: String?,
    val hazardsEs: String?,
    val hazardsEn: String?,
    val geometrySource: String?,
    val fishingSource: String?,
    val geometryConfidence: String?,
    val fishingConfidence: String?
) {
    val displayName: String
        get() = nameEs ?: nameEn ?: id
}

internal fun normalizeMorphologyValue(value: String?): String? = value
    ?.trim()
    ?.takeIf { it.isNotEmpty() && !it.equals("UNKNOWN", ignoreCase = true) }

internal data class MorphologyPolygonData(
    val id: String,
    val outerBoundary: List<LatLng>,
    val holes: List<List<LatLng>> = emptyList(),
    val metadata: MorphologyFeatureMetadata
)

internal data class MorphologyLineData(
    val id: String,
    val points: List<LatLng>,
    val metadata: MorphologyFeatureMetadata
)

internal data class MorphologyParsedData(
    val polygons: List<MorphologyPolygonData> = emptyList(),
    val lines: List<MorphologyLineData> = emptyList()
)

internal fun findMorphologyFeatureAt(
    tap: LatLng,
    data: MorphologyParsedData,
    lineToleranceMeters: Double = 35.0
): MorphologyFeatureMetadata? {
    val containingPolygon = data.polygons
        .asSequence()
        .filter { polygonContains(tap, it) }
        .minByOrNull { polygonArea(it.outerBoundary) }

    if (containingPolygon != null) return containingPolygon.metadata

    return data.lines
        .asSequence()
        .map { it to distanceToLineMeters(tap, it.points) }
        .filter { (_, distance) -> distance <= lineToleranceMeters }
        .minByOrNull { (_, distance) -> distance }
        ?.first
        ?.metadata
}

private fun polygonContains(point: LatLng, polygon: MorphologyPolygonData): Boolean {
    if (!ringContains(point, polygon.outerBoundary)) return false
    return polygon.holes.none { ringContains(point, it) }
}

private fun ringContains(point: LatLng, ring: List<LatLng>): Boolean {
    if (ring.size < 3) return false
    var inside = false
    var previous = ring.last()

    for (current in ring) {
        if (pointOnSegment(point, previous, current)) return true

        val crossesLatitude = (current.latitude > point.latitude) !=
            (previous.latitude > point.latitude)
        if (crossesLatitude) {
            val crossingLongitude =
                (previous.longitude - current.longitude) *
                    (point.latitude - current.latitude) /
                    (previous.latitude - current.latitude) +
                    current.longitude
            if (point.longitude < crossingLongitude) inside = !inside
        }
        previous = current
    }
    return inside
}

private fun pointOnSegment(point: LatLng, start: LatLng, end: LatLng): Boolean {
    val cross = (point.latitude - start.latitude) * (end.longitude - start.longitude) -
        (point.longitude - start.longitude) * (end.latitude - start.latitude)
    if (abs(cross) > 1e-10) return false

    return point.latitude in minOf(start.latitude, end.latitude)..maxOf(start.latitude, end.latitude) &&
        point.longitude in minOf(start.longitude, end.longitude)..maxOf(start.longitude, end.longitude)
}

private fun polygonArea(points: List<LatLng>): Double {
    if (points.size < 3) return Double.MAX_VALUE
    var twiceArea = 0.0
    for (index in points.indices) {
        val next = points[(index + 1) % points.size]
        twiceArea += points[index].longitude * next.latitude - next.longitude * points[index].latitude
    }
    return abs(twiceArea) / 2.0
}

private fun distanceToLineMeters(point: LatLng, points: List<LatLng>): Double {
    if (points.size < 2) return Double.MAX_VALUE
    return points.zipWithNext().minOf { (start, end) ->
        distanceToSegmentMeters(point, start, end)
    }
}

private fun distanceToSegmentMeters(point: LatLng, start: LatLng, end: LatLng): Double {
    val metersPerDegreeLatitude = 111_320.0
    val metersPerDegreeLongitude = metersPerDegreeLatitude * cos(Math.toRadians(point.latitude))
    val pointX = point.longitude * metersPerDegreeLongitude
    val pointY = point.latitude * metersPerDegreeLatitude
    val startX = start.longitude * metersPerDegreeLongitude
    val startY = start.latitude * metersPerDegreeLatitude
    val endX = end.longitude * metersPerDegreeLongitude
    val endY = end.latitude * metersPerDegreeLatitude
    val deltaX = endX - startX
    val deltaY = endY - startY
    val lengthSquared = deltaX * deltaX + deltaY * deltaY

    if (lengthSquared == 0.0) return Math.hypot(pointX - startX, pointY - startY)

    val projection = (((pointX - startX) * deltaX + (pointY - startY) * deltaY) /
        lengthSquared).coerceIn(0.0, 1.0)
    val nearestX = startX + projection * deltaX
    val nearestY = startY + projection * deltaY
    return Math.hypot(pointX - nearestX, pointY - nearestY)
}
