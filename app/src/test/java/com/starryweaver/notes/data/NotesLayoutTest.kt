package com.starryweaver.notes.data

import org.junit.Assert.assertEquals
import org.junit.Test

class NotesLayoutTest {

    @Test
    fun missingValueFallsBackToSingle() {
        assertEquals(NotesLayout.SINGLE, NotesLayout.fromStorage(null))
    }

    @Test
    fun parsesStoredValues() {
        assertEquals(NotesLayout.SINGLE, NotesLayout.fromStorage("single"))
        assertEquals(NotesLayout.DOUBLE, NotesLayout.fromStorage("double"))
    }

    @Test
    fun unknownValueFallsBackToSingle() {
        assertEquals(NotesLayout.SINGLE, NotesLayout.fromStorage("grid"))
        assertEquals(NotesLayout.SINGLE, NotesLayout.fromStorage(""))
    }

    @Test
    fun storageValuesAreStable() {
        assertEquals("single", NotesLayout.SINGLE.storageValue)
        assertEquals("double", NotesLayout.DOUBLE.storageValue)
    }
}
