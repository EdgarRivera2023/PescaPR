package com.bradmir.pescapr.ui

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoastalMorphologyInteractionTest {
    @Test
    fun metadataNormalization_omitsBlankAndUnknownValues() {
        assertNull(normalizeMorphologyValue(null))
        assertNull(normalizeMorphologyValue("  "))
        assertNull(normalizeMorphologyValue("unknown"))
        assertEquals("Arrecife", normalizeMorphologyValue("  Arrecife  "))
    }

    @Test
    fun polygonSelection_excludesPointsOutsideAndInsideHoles() {
        val feature = metadata("reef")
        val polygon = MorphologyPolygonData(
            id = "reef-polygon",
            outerBoundary = square(0.0, 0.0, 10.0),
            holes = listOf(square(4.0, 4.0, 2.0)),
            metadata = feature
        )
        val data = MorphologyParsedData(polygons = listOf(polygon))

        assertEquals(feature, findMorphologyFeatureAt(LatLng(2.0, 2.0), data))
        assertNull(findMorphologyFeatureAt(LatLng(5.0, 5.0), data))
        assertNull(findMorphologyFeatureAt(LatLng(12.0, 12.0), data))
    }

    @Test
    fun overlappingPolygons_selectSmallestContainingFeature() {
        val large = metadata("large")
        val small = metadata("small")
        val data = MorphologyParsedData(
            polygons = listOf(
                MorphologyPolygonData("large", square(0.0, 0.0, 10.0), metadata = large),
                MorphologyPolygonData("small", square(1.0, 1.0, 2.0), metadata = small)
            )
        )

        assertEquals(small, findMorphologyFeatureAt(LatLng(2.0, 2.0), data))
    }

    @Test
    fun separatePolygonParts_canResolveToSameFeature() {
        val feature = metadata("multi")
        val data = MorphologyParsedData(
            polygons = listOf(
                MorphologyPolygonData("multi-1", square(0.0, 0.0, 1.0), metadata = feature),
                MorphologyPolygonData("multi-2", square(3.0, 3.0, 1.0), metadata = feature)
            )
        )

        assertEquals(feature, findMorphologyFeatureAt(LatLng(3.5, 3.5), data))
    }

    @Test
    fun lineSelection_usesToleranceAndChoosesNearestLine() {
        val near = metadata("near")
        val far = metadata("far")
        val data = MorphologyParsedData(
            lines = listOf(
                MorphologyLineData(
                    "far",
                    listOf(LatLng(18.0, -66.0), LatLng(18.01, -66.0)),
                    far
                ),
                MorphologyLineData(
                    "near",
                    listOf(LatLng(18.0, -66.0001), LatLng(18.01, -66.0001)),
                    near
                )
            )
        )

        assertEquals(
            near,
            findMorphologyFeatureAt(
                LatLng(18.005, -66.00012),
                data,
                lineToleranceMeters = 30.0
            )
        )
        assertNull(
            findMorphologyFeatureAt(
                LatLng(18.005, -66.001),
                data,
                lineToleranceMeters = 30.0
            )
        )
    }

    private fun square(latitude: Double, longitude: Double, size: Double) = listOf(
        LatLng(latitude, longitude),
        LatLng(latitude, longitude + size),
        LatLng(latitude + size, longitude + size),
        LatLng(latitude + size, longitude),
        LatLng(latitude, longitude)
    )

    private fun metadata(id: String) = MorphologyFeatureMetadata(
        id = id,
        nameEs = id,
        nameEn = null,
        structureType = null,
        bottomType = null,
        targetSpecies = null,
        fishingStrategyEs = null,
        fishingStrategyEn = null,
        notes = null,
        bestTide = null,
        hazardsEs = null,
        hazardsEn = null,
        geometrySource = null,
        fishingSource = null,
        geometryConfidence = null,
        fishingConfidence = null
    )
}
