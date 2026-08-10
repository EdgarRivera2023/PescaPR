package com.bradmir.pescapr.ui

import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject

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
) {
    val isEmpty: Boolean
        get() = polygons.isEmpty() && lines.isEmpty()
}

internal class CoastalMorphologyParser {
    fun parse(jsonText: String): MorphologyParsedData {
        val root = JSONObject(jsonText)
        require(root.optString("type") == "FeatureCollection") {
            "Root GeoJSON type must be FeatureCollection"
        }
        val features = root.optJSONArray("features")
            ?: throw IllegalArgumentException("GeoJSON features array is missing")
        val polygons = mutableListOf<MorphologyPolygonData>()
        val lines = mutableListOf<MorphologyLineData>()

        for (index in 0 until features.length()) {
            try {
                parseFeature(features.getJSONObject(index), index, polygons, lines)
            } catch (_: Exception) {
                // A single malformed or unsupported feature must not discard the valid dataset.
            }
        }

        return MorphologyParsedData(polygons, lines).also {
            require(!it.isEmpty) { "GeoJSON contains no usable morphology geometry" }
        }
    }

    private fun parseFeature(
        feature: JSONObject,
        index: Int,
        polygons: MutableList<MorphologyPolygonData>,
        lines: MutableList<MorphologyLineData>
    ) {
        val geometry = feature.optJSONObject("geometry") ?: return
        val type = geometry.optString("type")
        val coordinates = geometry.optJSONArray("coordinates") ?: return
        val properties = feature.optJSONObject("properties") ?: JSONObject()
        val fid = properties.optionalText("fid") ?: index.toString()
        val metadata = properties.toMorphologyMetadata(fid)

        when (type) {
            "Polygon" -> addPolygon("$fid-$index", coordinates, metadata, polygons)
            "MultiPolygon" -> {
                for (part in 0 until coordinates.length()) {
                    addPolygon(
                        "$fid-$index-$part",
                        coordinates.getJSONArray(part),
                        metadata,
                        polygons
                    )
                }
            }
            "LineString" -> addLine("$fid-$index", coordinates, metadata, lines)
            "MultiLineString" -> {
                for (part in 0 until coordinates.length()) {
                    addLine(
                        "$fid-$index-$part",
                        coordinates.getJSONArray(part),
                        metadata,
                        lines
                    )
                }
            }
        }
    }

    private fun addPolygon(
        id: String,
        coordinates: JSONArray,
        metadata: MorphologyFeatureMetadata,
        polygons: MutableList<MorphologyPolygonData>
    ) {
        if (coordinates.length() == 0) return
        val outer = parseCoordinates(coordinates.getJSONArray(0))
        if (outer.size < 3) return
        val holes = (1 until coordinates.length()).mapNotNull { holeIndex ->
            parseCoordinates(coordinates.getJSONArray(holeIndex)).takeIf { it.size >= 3 }
        }
        polygons.add(MorphologyPolygonData(id, outer, holes, metadata))
    }

    private fun addLine(
        id: String,
        coordinates: JSONArray,
        metadata: MorphologyFeatureMetadata,
        lines: MutableList<MorphologyLineData>
    ) {
        val points = parseCoordinates(coordinates)
        if (points.size >= 2) lines.add(MorphologyLineData(id, points, metadata))
    }

    private fun parseCoordinates(jsonArray: JSONArray): List<LatLng> {
        return (0 until jsonArray.length()).map { index ->
            val point = jsonArray.getJSONArray(index)
            val longitude = point.getDouble(0)
            val latitude = point.getDouble(1)
            require(latitude.isFinite() && latitude in -90.0..90.0)
            require(longitude.isFinite() && longitude in -180.0..180.0)
            LatLng(latitude, longitude)
        }
    }

    private fun JSONObject.optionalText(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return normalizeMorphologyValue(optString(key))
    }

    private fun JSONObject.optionalTextList(key: String): String? {
        if (!has(key) || isNull(key)) return null
        val array = optJSONArray(key)
        if (array != null) {
            return (0 until array.length())
                .mapNotNull { normalizeMorphologyValue(array.optString(it)) }
                .joinToString(", ")
                .takeIf(String::isNotEmpty)
        }
        return optionalText(key)
    }

    private fun JSONObject.toMorphologyMetadata(fallbackId: String) =
        MorphologyFeatureMetadata(
            id = optionalText("id") ?: fallbackId,
            nameEs = optionalText("name_es"),
            nameEn = optionalText("name_en"),
            structureType = optionalText("type"),
            bottomType = optionalText("bottom_type"),
            targetSpecies = optionalTextList("target_species"),
            fishingStrategyEs = optionalText("fishing_strategy_es"),
            fishingStrategyEn = optionalText("fishing_strategy_en"),
            notes = optionalText("notes"),
            bestTide = optionalText("best_tide"),
            hazardsEs = optionalText("hazards_es"),
            hazardsEn = optionalText("hazards_en"),
            geometrySource = optionalText("geometry_source"),
            fishingSource = optionalText("fishing_source"),
            geometryConfidence = optionalText("confidence_geometry"),
            fishingConfidence = optionalText("confidence_fishing")
        )
}
