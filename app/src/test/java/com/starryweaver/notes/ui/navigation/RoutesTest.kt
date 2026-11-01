package com.starryweaver.notes.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutesTest {

    @Test
    fun editWithoutFreshFlag() {
        assertEquals("edit/abc?fresh=false", Routes.edit("abc"))
    }

    @Test
    fun editWithFreshFlag() {
        assertEquals("edit/abc?fresh=true", Routes.edit("abc", fresh = true))
    }

    @Test
    fun fixedRoutesAreStable() {
        assertEquals("main", Routes.MAIN)
        assertEquals("trash", Routes.TRASH)
        assertEquals("about", Routes.ABOUT)
        assertEquals("appearance", Routes.APPEARANCE)
        assertEquals("edit/new", Routes.NEW_EDIT)
    }
}
