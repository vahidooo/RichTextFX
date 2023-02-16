package org.fxmisc.richtext.demo.outline

import com.ebadollah.editor.ParagraphStyle
import org.fxmisc.richtext.GenericStyledArea
import org.fxmisc.richtext.demo.outline.model.BodyText
import org.fxmisc.richtext.demo.outline.model.Heading
import org.fxmisc.richtext.demo.outline.model.ParagraphLevel

class OutliningActions(
    val area: GenericStyledArea<ParagraphStyle, *, *>
) {
    private fun paragraph(index: Int): ParagraphStyle? = area.paragraphs.getOrNull(index)?.paragraphStyle

    fun promote(start: Int, end: Int): Map<Int, ParagraphStyle> {
        val allBodyText = paragraphs(start, end).all { it.level is BodyText }
        return computeNewStyles(start, end) { level ->
            if (allBodyText)
                level.promoted()
            else
                with(level) {
                    if (this is BodyText) BodyText(level = maxOf(1, value - 1)) else promoted()
                }
        }
    }

    private fun promoteToLevel(index: Int, level: Heading): Map<Int, ParagraphStyle> {
        val updates = mutableMapOf<Int, ParagraphStyle>()
        val prg = paragraph(index)
        require(prg != null && prg.level < level)

        val next = paragraph(index + 1)
        updates[index] = prg.copy(level = Heading(level.value, (next?.let { it.level < level } ?: false)))

        paragraph(index - 1)?.let { previous ->
            if (previous.level is Heading && previous.level <= level) {
                updates[index - 1] = previous.copy(level = Heading(previous.level.value, previous.level > level))
            }
        }
        updateFollowingBodyTexts(index, level.value, updates)
        return updates
    }

    fun demote(start: Int, end: Int): Map<Int, ParagraphStyle> {
        val allBodyText = paragraphs(start, end).all { it.level is BodyText }
        return computeNewStyles(start, end) { level ->
            if (allBodyText)
                level.demoted()
            else
                with(level) { if (this is BodyText) BodyText(level = value + 1) else demoted() }
        }

    }

    private fun paragraphs(start: Int, end: Int): List<ParagraphStyle> =
        area.paragraphs.subList(start, end + 1).map { it.paragraphStyle }


    private fun demoteToLevel(index: Int, level: Heading): Map<Int, ParagraphStyle> {
        val updates = mutableMapOf<Int, ParagraphStyle>()
        val prg = paragraph(index)
        require(prg != null && (prg.level is BodyText || prg.level > level))

        val next = paragraph(index + 1)
        updates[index] = prg.copy(level = Heading(level.value, (next?.let { it.level < level } ?: false)))


        paragraph(index - 1)?.let { previous ->
            if (previous.level is Heading && previous.level > level) {
                updates[index - 1] = previous.copy(level = Heading(previous.level.value, hasChild = true))
            }
        }
        updateFollowingBodyTexts(index, level.value, updates)
        return updates
    }

    private fun updatePrecedingAndFollowings(updates: MutableMap<Int, ParagraphStyle>, start: Int, end: Int) {
        with(updates.getValue(start)) {
            paragraph(start - 1)?.let { previous ->
                if (previous.level is Heading) {
                    updates[start - 1] = previous.copy(
                        level = Heading(previous.level.value, hasChild = previous.level > level)
                    )
                }
            }
        }

        with(updates.getValue(end)) {
            val next = paragraph(end + 1)
            if (level is Heading)
                updates[end] = copy(level = Heading(level.value, (next?.let { it.level < level } ?: false)))
        }

        updateFollowingBodyTexts(end, updates.getValue(end).level.value, updates)
    }

    private fun updateFollowingBodyTexts(index: Int, level: Int, updates: MutableMap<Int, ParagraphStyle>) {
        paragraph(index + 1)?.let { next ->
            if (next.level is BodyText) {
                var it = index + 1
                do {
                    updates[it] = next.copy(level = BodyText(level))
                    it++
                } while (paragraph(it)?.level is BodyText)
            }
        }
    }

    fun makeBodyText(start: Int, end: Int): Map<Int, ParagraphStyle> {
        val value = paragraph(start - 1)
            ?.let { it.level.value }
            ?: 1
        return computeNewStyles(start, end) { BodyText(value) }
    }

    private fun computeNewStyles(
        start: Int,
        end: Int,
        function: (ParagraphLevel) -> ParagraphLevel
    ): Map<Int, ParagraphStyle> {
        val updates = paragraphs(start, end)
            .asSequence()
            .mapIndexed { index, style -> (index + start) to style.copy(level = function(style.level)) }
            .toMap()
            .toMutableMap()

        updatePrecedingAndFollowings(updates, start, end)
        return updates
    }

    fun setLevel(start: Int, end: Int, level: Int): Map<Int, ParagraphStyle> =
        if (level == 0)
            makeBodyText(start, end)
        else {
            computeNewStyles(start, end) { Heading(level, false) }
        }

    fun moveUp(prgs: List<ParagraphStyle>, index: Int) {
        TODO()
    }

    fun moveDown(prgs: List<ParagraphStyle>, index: Int) {
        TODO()
    }

    fun expand(prgs: List<ParagraphStyle>, index: Int) {
        TODO()
    }

    fun collapse(prgs: List<ParagraphStyle>, index: Int) {
        TODO()
    }

    fun showLevels(level: ParagraphLevel) {}

    fun updateRemoved(prgs: List<ParagraphStyle>, level: ParagraphLevel) {
        TODO()
    }

    fun updateInserted(prgs: List<ParagraphStyle>, level: ParagraphLevel) {
        TODO()
    }
}

