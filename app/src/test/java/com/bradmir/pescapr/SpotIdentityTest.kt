package com.bradmir.pescapr

import org.junit.Assert.assertEquals
import org.junit.Test

class SpotIdentityTest {
    private val remoteSpot = SpotEntity(
        nombre = "Remote name",
        descripcion = "Remote description",
        latitud = 18.4,
        longitud = -66.1,
        firestoreId = "stable-firestore-id"
    )

    @Test
    fun `existing firestore row keeps its local identity when updated`() {
        val existing = remoteSpot.copy(id = 17, nombre = "Old name")

        val updated = remoteSpot.withLocalIdentityFrom(existing)

        assertEquals(17, updated.id)
        assertEquals("stable-firestore-id", updated.firestoreId)
        assertEquals("Remote name", updated.nombre)
    }

    @Test
    fun `canonical duplicate is the oldest local row`() {
        val duplicateIds = listOf(37, 21, 33, 25)

        assertEquals(21, duplicateIds.minOrNull())
    }

    @Test
    fun `repointing duplicate references preserves every record`() {
        val canonicalId = 21
        val duplicateIds = setOf(25, 33, 37)
        val recordSpotIds = listOf(25, 21, 33, 8, 37)

        val repointed = recordSpotIds.map { spotId ->
            if (spotId in duplicateIds) canonicalId else spotId
        }

        assertEquals(listOf(21, 21, 21, 8, 21), repointed)
        assertEquals(recordSpotIds.size, repointed.size)
    }

    @Test
    fun `repeated remote replacement remains idempotent`() {
        val existing = remoteSpot.copy(id = 21, nombre = "Old name")

        val first = remoteSpot.withLocalIdentityFrom(existing)
        val second = remoteSpot.withLocalIdentityFrom(first)

        assertEquals(first, second)
        assertEquals(21, second.id)
    }
}
