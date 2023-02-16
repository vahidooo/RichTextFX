package org.fxmisc.richtext.demo.outline.model

import kotlin.math.max

abstract sealed class ParagraphLevel(
    val value: Int,
    val hasChild: Boolean
) {
    init {
        require(value > 0)
    }

//    fun demoted() = Heading(value + 1)
//
//    fun promoted() = Heading(max(1, value - 1))
//
//    fun indented() = ParagraphLevel(bodyText, value + 1)
//
//    fun unindented() = ParagraphLevel(bodyText, max(1, value - 1))

//    fun toLogicalLevel(): LogicalLevel = if (bodyText) BodyText else Heading(indentation)


    /**
     * All BodyTexts are equal and lower than each Heading.
     * In Heading, the lower value the greater.
     */
    operator fun compareTo(other: ParagraphLevel): Int {
        return when (Pair(this is BodyText, other is BodyText)) {
            false to false -> {
                this as Heading
                other as Heading
                other.value.compareTo(value)
            }
            false to true -> 1
            true to false -> -1
            else -> 0
        }
    }

//    abstract fun copy(value: Int, hasChild: Boolean )

    fun demoted(): Heading = Heading(value + 1, hasChild)
    fun promoted(): Heading = Heading(max(1, value - 1), hasChild)
}


class BodyText(level: Int) : ParagraphLevel(level, hasChild = false)

class Heading(level: Int, hasChild: Boolean) : ParagraphLevel(level, hasChild = hasChild)