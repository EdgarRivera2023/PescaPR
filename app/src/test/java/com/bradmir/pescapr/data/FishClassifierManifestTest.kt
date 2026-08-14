package com.bradmir.pescapr.data

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FishClassifierManifestTest {

    @Test
    fun `frozen manifest is contiguous unique and matches bundled guide ids`() {
        val manifest = JSONObject(readMainFile("assets/fish_classifier_manifest.json"))
        val guide = JSONArray(readMainFile("res/raw/oficial_guide.json"))
        val classes = manifest.getJSONArray("classes")

        val expectedClassCount = manifest.getInt("expectedClassCount")
        assertEquals(39, expectedClassCount)
        assertEquals(expectedClassCount, classes.length())

        val classIndices = mutableListOf<Int>()
        val manifestIds = mutableListOf<String>()
        val manifestScientificNamesById = mutableMapOf<String, String>()
        repeat(classes.length()) { position ->
            val classifierClass = classes.getJSONObject(position)
            classIndices += classifierClass.getInt("index")
            manifestIds += classifierClass.getString("fichaPezId")
            manifestScientificNamesById[classifierClass.getString("fichaPezId")] =
                classifierClass.getString("scientificName").trim()
        }

        assertEquals("Class indices must be unique", classIndices.size, classIndices.toSet().size)
        assertEquals("Class indices must begin at zero and be contiguous", classIndices.indices.toList(), classIndices)
        assertTrue("Classifier IDs must not be empty", manifestIds.all { it.isNotBlank() })
        assertEquals("Classifier IDs must be unique", manifestIds.size, manifestIds.toSet().size)
        assertEquals(
            "Frozen v1 ordering must be ordinal fichaPezId order",
            manifestIds.sorted(),
            manifestIds
        )

        val guideIds = mutableListOf<String>()
        val guideScientificNamesById = mutableMapOf<String, String>()
        repeat(guide.length()) {
            val record = guide.getJSONObject(it)
            val id = record.getString("id")
            guideIds += id
            guideScientificNamesById[id] = record.getString("nombreCientifico").trim()
        }
        assertEquals("Bundled guide IDs must be unique", guideIds.size, guideIds.toSet().size)
        assertEquals("Manifest and bundled guide IDs must match exactly", guideIds.toSet(), manifestIds.toSet())
        assertTrue(
            "Every classifier ID must resolve to exactly one bundled FichaPez record",
            manifestIds.all { id -> guideIds.count { it == id } == 1 }
        )
        assertEquals(
            "Manifest scientific-name audit metadata must match the bundled guide by FichaPez.id",
            guideScientificNamesById,
            manifestScientificNamesById
        )
    }

    @Test
    fun `bundled classifier catalog has 39 valid records`() {
        val guide = JSONArray(readMainFile("res/raw/oficial_guide.json"))
        assertEquals(39, guide.length())

        val ids = mutableListOf<String>()
        val scientificNames = mutableListOf<String>()
        repeat(guide.length()) {
            val record = guide.getJSONObject(it)
            ids += record.getString("id")
            scientificNames += record.getString("nombreCientifico")
        }

        assertFalse(ids.any { it.isBlank() })
        assertEquals(ids.size, ids.toSet().size)
        assertFalse(scientificNames.any { it.isBlank() })
        assertEquals(
            "Scientific names must remain unique after trimming incidental whitespace",
            scientificNames.size,
            scientificNames.map(String::trim).toSet().size
        )
    }

    private fun readMainFile(relativePath: String): String {
        val workingDirectory = Paths.get(System.getProperty("user.dir"))
        val candidates = listOf(
            workingDirectory.resolve("src/main").resolve(relativePath),
            workingDirectory.resolve("app/src/main").resolve(relativePath)
        )
        val path: Path = candidates.firstOrNull(Files::isRegularFile)
            ?: error("Could not find src/main/$relativePath from $workingDirectory")
        return String(Files.readAllBytes(path), StandardCharsets.UTF_8)
    }
}
