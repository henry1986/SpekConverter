package org.daiv.spek.converter

import java.io.BufferedReader
import java.io.FileReader

fun List<String>.toUpperCase(): String {
    return joinToString("") { "${it.first().toUpperCase()}${it.drop(1)}" }
}

data class SpekConverter(val list: List<String>) {
    private val onReg = "[ ]*on\\(\"(.*)\"\\)(.*)".toRegex()
    private val itReg = "[ ]*it\\(\"(.*)\"\\)(.*)".toRegex()

    fun String.onConverter(): String {
        return replace(onReg, "$1").split(" ").toUpperCase()
    }

    fun String.inConverter(): String {
        val list = replace(itReg, "$1").split(" ")
        if (list.size < 2) {
            return list.first()
        } else {
            return (listOf(list.first()) + list.drop(1).toUpperCase()).joinToString("")
        }
    }

    fun className(): String? {
        return list.find { it.startsWith("class") || it.startsWith("internal class") }?.let { it.split(" ", ":") }
            ?.let {
                val i = it.indexOf("class")
                it.get(i + 1)
            }
    }

    fun removeClassAndSpek(): SpekConverter {
        return copy(list.filter {
            !it.startsWith("class") && !it.startsWith("internal class") && !it.contains("Spek") && !it.contains("org.jetbrains.spek.api.dsl") && !it.contains(
                "describe"
            )
        })
    }

    fun replaceDatabaseWrapperImport(): SpekConverter {
        return copy(list.map {
            if (it.contains("import org.daiv.immutable.utils.persistence.annotations.DatabaseWrapper")) {
                "import org.daiv.reflection.database.DatabaseHandler"
            } else {
                it
            }
        })
    }

    val databaseWrapperReg = "(.*)DatabaseWrapper.create\\(\"(.*)\"\\)".toRegex()

    fun String.databaseConverterConverter(): String {
        return replace(databaseWrapperReg, "$1 DatabaseHandler(\"$2\")")
    }

    fun replaceDatabaseWrapper(): SpekConverter {
        return copy(list.map {
            if (databaseWrapperReg matches it) {
                it.databaseConverterConverter()
            } else {
                it
            }
        })
    }

    fun addImport(): SpekConverter = copy(list.take(2) + "import kotlin.test.Test" + list.drop(2))

    fun renameOns(className: String): SpekConverter {
        return copy(list.map {
            if (onReg matches it) {
                "class ${it.onConverter()} : $className(){"
            } else {
                it
            }
        })
    }

    fun renameIts(): SpekConverter {
        return copy(list.map {
            if (itReg matches it) {
                "@Test fun ${it.inConverter()}(){"
            } else {
                it
            }
        })
    }

    fun print() {
        list.forEach { println(it) }
    }

    fun removeLastLine() = copy(list.dropLast(2))
}

fun main() {
    val source =
        "/home/mheinrich/Software/IdeaProjects/jpersistence/backend/jpersistence-jvm/src/test/kotlin/org/daiv/reflection/persistence/kotlin/InsertMapTest.kt"
    val file = BufferedReader(FileReader(source))
    val spekConverter = SpekConverter(file.readLines())
    val className = spekConverter.className() ?: source.split("/").last().dropLast(3)
    print("className: ")
    println(spekConverter.className())
    spekConverter.removeClassAndSpek().addImport().replaceDatabaseWrapperImport().replaceDatabaseWrapper()
        .renameOns(className).renameIts().removeLastLine().print()

}