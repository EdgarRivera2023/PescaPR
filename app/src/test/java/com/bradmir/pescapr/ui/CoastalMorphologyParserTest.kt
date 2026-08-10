package com.bradmir.pescapr.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class CoastalMorphologyParserTest {
    private val parser = CoastalMorphologyParser()

    @Test
    fun parsesSupportedGeometryAndNormalizesMetadata() {
        val parsed = parser.parse(
            featureCollection(
                polygonFeature("reef", name = "Arrecife", bottom = "UNKNOWN"),
                lineFeature("edge")
            )
        )

        assertEquals(1, parsed.polygons.size)
        assertEquals(1, parsed.lines.size)
        assertEquals("Arrecife", parsed.polygons.single().metadata.displayName)
        assertNull(parsed.polygons.single().metadata.bottomType)
    }

    @Test
    fun malformedFeatureIsSkippedWhenAnotherFeatureIsValid() {
        val malformed = """{"type":"Feature","properties":{"id":"bad"},"geometry":{"type":"Polygon","coordinates":[[[999,999]]]}}"""

        val parsed = parser.parse(featureCollection(malformed, polygonFeature("valid")))

        assertEquals(1, parsed.polygons.size)
        assertEquals("valid", parsed.polygons.single().metadata.id)
    }

    @Test
    fun rejectsWrongRootEmptyAndUnsupportedDatasets() {
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse("""{"type":"Polygon","features":[]}""")
        }
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse(featureCollection())
        }
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse(
                featureCollection(
                    """{"type":"Feature","properties":{},"geometry":{"type":"Point","coordinates":[-66,18]}}"""
                )
            )
        }
    }
}

internal fun featureCollection(vararg features: String): String =
    """{"type":"FeatureCollection","features":[${features.joinToString(",")}]}"""

internal fun polygonFeature(
    id: String,
    name: String = id,
    bottom: String = "ROCK"
): String = """
    {
      "type":"Feature",
      "properties":{"id":"$id","fid":"$id","name_es":"$name","bottom_type":"$bottom"},
      "geometry":{"type":"Polygon","coordinates":[[[-66.01,18.00],[-66.00,18.00],[-66.00,18.01],[-66.01,18.01],[-66.01,18.00]]]}
    }
""".trimIndent()

internal fun lineFeature(id: String): String = """
    {
      "type":"Feature",
      "properties":{"id":"$id","fid":"$id"},
      "geometry":{"type":"LineString","coordinates":[[-66.01,18.00],[-66.00,18.01]]}
    }
""".trimIndent()
