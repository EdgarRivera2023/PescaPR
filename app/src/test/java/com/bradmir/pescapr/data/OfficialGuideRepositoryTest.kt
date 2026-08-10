package com.bradmir.pescapr.data

import com.bradmir.pescapr.data.model.FichaPez
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficialGuideRepositoryTest {

    @Test
    fun `maps Firestore fields and preserves document id`() {
        val mapped = mapOfficialGuideDocument(
            OfficialGuideDocument(
                id = "firestore-id",
                fields = mapOf(
                    "id" to "embedded-wrong-id",
                    "nombreCientifico" to "Lutjanus analis",
                    "nombreComun" to "Pargo",
                    "caracteristicas" to listOf("Rasgo uno"),
                    "fotosUrls" to listOf("https://example.test/fish.jpg")
                )
            ),
            emptyMap()
        )

        requireNotNull(mapped)
        assertEquals("firestore-id", mapped.id)
        assertEquals("Lutjanus analis", mapped.nombreCientifico)
        assertEquals(listOf("Rasgo uno"), mapped.caracteristicas)
        assertEquals(listOf("https://example.test/fish.jpg"), mapped.fotosUrls)
    }

    @Test
    fun `rejects malformed records without an id or usable name`() {
        assertEquals(
            null,
            mapOfficialGuideDocument(
                OfficialGuideDocument("", mapOf("nombreComun" to "Pargo")),
                emptyMap()
            )
        )
        assertEquals(
            null,
            mapOfficialGuideDocument(
                OfficialGuideDocument("valid-id", mapOf("fotosUrls" to emptyList<String>())),
                emptyMap()
            )
        )
    }

    @Test
    fun `uses Firestore thumbnail before bundled thumbnail`() {
        val bundled = FichaPez(id = "fish-id", localThumbResName = "bundled_thumb")
        val firestoreThumb = mapOfficialGuideDocument(
            OfficialGuideDocument(
                "fish-id",
                mapOf(
                    "nombreComun" to "Pargo",
                    "localThumbResName" to "remote_thumb"
                )
            ),
            mapOf(bundled.id to bundled)
        )
        val bundledThumb = mapOfficialGuideDocument(
            OfficialGuideDocument("fish-id", mapOf("nombreComun" to "Pargo")),
            mapOf(bundled.id to bundled)
        )

        assertEquals("remote_thumb", firestoreThumb?.localThumbResName)
        assertEquals("bundled_thumb", bundledThumb?.localThumbResName)
    }

    @Test
    fun `uses bundled fallback until usable Firestore data replaces it`() {
        val bundled = listOf(FichaPez(id = "bundled", nombreComun = "Bundled"))
        val remote = listOf(FichaPez(id = "remote", nombreComun = "Remote"))
        val reducer = OfficialGuideSnapshotReducer(bundled)

        assertSame(bundled, reducer.onSnapshot(emptyList()))
        assertSame(remote, reducer.onSnapshot(remote))
        assertSame(remote, reducer.onError())
        assertSame(remote, reducer.onSnapshot(emptyList()))
    }

    @Test
    fun `empty bundled and malformed Firestore remain safely empty`() {
        val reducer = OfficialGuideSnapshotReducer(emptyList())

        assertTrue(reducer.onSnapshot(emptyList()).isEmpty())
        assertTrue(reducer.onError().isEmpty())
    }
}
