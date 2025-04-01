package org.darkan.core

import io.github.classgraph.ClassGraph
import java.lang.reflect.Method
import java.text.NumberFormat
import java.util.*
import kotlin.math.roundToInt

object UtilsAndExtensions {

}

private const val FNV1aPrime = 16777619u
fun String.hashToShort(): Short {
    var hash = 0u
    for (char in this) {
        hash = hash xor char.code.toUInt()
        hash = (hash * FNV1aPrime) % 65536u
    }
    return hash.toShort()
}

val currentTimeTicks get() = System.currentTimeMillis() / 600L

fun String.formatPlayerNameForProtocol(): String {
    return this.lowercase().replace(" ", "_")
}

fun String.formatPlayerNameForDisplay(): String {
    return this.replace("_", " ")
        .lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}

fun generateRandomString(length: Int = 50): String {
    val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { charPool.random() }
        .joinToString("")
}

/**
 * Tick time extensions
 */

fun Double.ticksToTimeString(): String = this.roundToInt().ticksToTimeString()
fun Int.ticksToTimeString(): String = this.toLong().ticksToTimeString()
fun Long.ticksToTimeString(): String = toMillisTimeString(this * 600)

private fun toMillisTimeString(millis: Long): String {
    var seconds = (millis / 1000).toInt()
    var minutes = seconds / 60
    val hours = minutes / 60

    minutes -= hours * 60
    seconds -= (hours * 60 * 60) + (minutes * 60)

    val parts = mutableListOf<String>()
    if (hours > 0) parts += "$hours hours"
    if (minutes > 0) parts += "$minutes minutes"
    if (seconds > 0) parts += "$seconds seconds"

    return (parts.joinToString(" ").ifEmpty { "moment or two" } + ".")
}

fun Int.formatNumber() = NumberFormat.getNumberInstance(Locale.US).format(toLong())
fun Long.formatNumber() = NumberFormat.getNumberInstance(Locale.US).format(this)
fun Double.formatNumber() = NumberFormat.getNumberInstance(Locale.US).format(toLong())

/**
 * Finds all methods with a specific annotation in the given package
 */
fun getMethodsWithAnnotation(packageName: String, annotation: Class<out Annotation>): List<Method> =
    ClassGraph()
        .enableClassInfo()
        .enableMethodInfo()
        .enableAnnotationInfo()
        .acceptPackages(packageName)
        .scan().use { scanResult ->
            scanResult.allClasses
                .flatMap { it.methodInfo }
                .filter { it.hasAnnotation(annotation.name) }
                .map { it.loadClassAndGetMethod() }
        }

/**
 * Finds all classes with a specific annotation in the given package
 */
fun getClassesWithAnnotation(packageName: String, annotation: Class<out Annotation>): List<Class<*>> =
    ClassGraph()
        .enableClassInfo()
        .enableAnnotationInfo()
        .acceptPackages(packageName)
        .scan().use { scanResult ->
            scanResult.getClassesWithAnnotation(annotation.name)
                .map { it.loadClass() }
        }

/**
 * Gets all classes in the given package
 */
fun getClasses(packageName: String): List<Class<*>> =
    ClassGraph()
        .enableClassInfo()
        .acceptPackages(packageName)
        .scan().use { scanResult ->
            scanResult.allClasses
                .map { it.loadClass() }
        }

/**
 * Gets all subclasses of a specific class in the given package
 */
fun getSubClasses(packageName: String, superClass: Class<*>): List<Class<*>> =
    ClassGraph()
        .enableClassInfo()
        .acceptPackages(packageName)
        .scan().use { scanResult ->
            scanResult.getSubclasses(superClass.name)
                .map { it.loadClass() }
        }