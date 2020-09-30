package org.daiv.spek.converter

import kotlin.test.Test
import kotlin.test.assertEquals

class SpekConverterTest {
    @Test
    fun testSplit() {
        val list = listOf(
            "class ClassX:",
            "class ClassX :",
            "class ClassX: Spek",
            "class ClassX :Spek",
            "class ClassX : Spek",
            "class ClassX :"
        )
        list.map { listOf(it) }.map { SpekConverter(list).className() }.forEach {
            assertEquals("ClassX", it)
        }
    }

    @Test
    fun removeSpekAndClass() {
        val list =
            listOf("class ", "   Spek  ", ":Spek", ": Spek", "spe", "ek", "org.jetbrains.spek.api.dsl", "describe")
        val res = SpekConverter(list).removeClassAndSpek()
        assertEquals(listOf("spe", "ek"), res.list)
    }

    @Test
    fun renameOns() {
        val list = listOf("on(\"refactor simple object\")", "   on(\"refactor simple Object\") {", "onData", "on ar")
        val spek = SpekConverter(list).renameOns("SpekConverterTestClass")
        println("spek: $spek")
        assertEquals(
            listOf(
                 "class RefactorSimpleObject : SpekConverterTestClass(){",
                "class RefactorSimpleObject : SpekConverterTestClass(){",
                "onData",
                "on ar"
            ), spek.list
        )
    }

    @Test
    fun regexTest() {
        val list = listOf("on(\"this is\")")
        val reg = "on\\(\"(.*)\"\\)".toRegex()
        list.map {
            it.replace(reg, "$1").split(" ").toUpperCase()
//                .replace(" ", "").let { "class $it" }
        }.forEach { println(it) }
    }

    @Test
    fun findLowerCase() {
        val list = listOf("  anelr", "  drNa", "Barne", "Zanrue", "zarn")
        val reg = "(^[a-z].*)".toRegex()
        list.map {
            it.trim()
        }.map {
            if (reg matches it) {
                "${it.first().toUpperCase()}${it.drop(1)}"
            } else {
                it
            }
        }.forEach { println("$it") }
    }

    @Test
    fun inConverter(){
        val list = listOf("it(\"rename Table\") {", "it(\"rename\") {", "it(\"rename Table axrd\") {")
        SpekConverter(list).renameIts().print()
    }
}