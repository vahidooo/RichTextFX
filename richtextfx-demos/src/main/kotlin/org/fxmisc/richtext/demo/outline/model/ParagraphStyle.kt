package com.ebadollah.editor

import org.fxmisc.richtext.demo.outline.model.Heading
import org.fxmisc.richtext.demo.outline.model.ParagraphLevel

/**
 * [indentation] can be calculated from [level] and the previous paragraph as following:
 * if [level] is not BodyText then [indentation] is same as level
 * else there are two cases:
 *     if the previous paragraph is BodyText then [indentation] must be same as the previous paragraph
 *     else [indentation] must be one step forward than the previous
 *
 * it implies that changing [level] of a paragraph may cause change [indentation] of
 *          itself and the following BodyText paragraphs
 *
 *
 *
 * [hasChild] can also be calculated from [level] and the next paragraph as following:
 * if [level] is BodyText then [hasChild] is obviously false
 * else if the next paragraph is in lower significant level then [hasChild] must be true
 * otherwise it must be false
 *
 * it implies that changing [level] of a paragraph may cause change [hasChild] of
 *              itself and the preceding Heading paragraph
 */

//sealed interface LogicalLevel {
//
//}
//
//object BodyText : LogicalLevel
//class Heading(val value: Int) : LogicalLevel


data class ParagraphStyle(
    val level: ParagraphLevel,
    val hidden: Boolean = false,
    val collapsed: Boolean = false
) {
    fun toCss(): String = buildString {
        if (hidden)
            append("visibility: collapse;");

    }

    companion object {
        val DEFAULT = ParagraphStyle(
            level = Heading(1, false),
            hidden = false,
            collapsed = false
        )
    }
}

