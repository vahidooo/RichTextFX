package org.fxmisc.richtext.demo.outline.model

import javafx.scene.paint.Color
import org.fxmisc.richtext.model.TextOps
import java.util.*

class TextRun(val content: String)

data class TextFormatting(
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val fontSize: Int? = null,
    val fontFamily: String? = null,
    val textColor: Color? = null,
    val backgroundColor: Color? = null,
) {
    fun toCss(): String = buildString {
        append("-fx-font-weight: ${if (bold) "bold" else "normal"};")
        append("-fx-font-style: ${if (italic) "italic" else "normal"};")
        append("-fx-underline: ${if (underline) "true" else "false"};")
        append("-fx-strikethrough: ${if (strikethrough) "true" else "false"};")
        fontSize?.let { append("-fx-font-size: $it pt;") }
        fontFamily?.let { append("-fx-font-family: '$it';") }
        textColor?.let { append("-fx-fill: ${cssColor(it)};") }
        backgroundColor?.let { append("-rtfx-background-color: ${cssColor(it)};") }
    }

    fun cssColor(color: Color): String {
        val red = (color.red * 255).toInt()
        val green = (color.green * 255).toInt()
        val blue = (color.blue * 255).toInt()
        val opacity = (color.opacity * 255).toInt()
        return "rgba($red, $green, $blue, $opacity)"
    }

    fun overrideWith(mixin: TextFormatting): TextFormatting {
        return TextFormatting(
            bold = mixin.bold || bold,
            italic = mixin.italic || italic,
            underline = mixin.underline || underline,
            strikethrough = mixin.strikethrough || strikethrough,
            fontSize = mixin.fontSize ?: fontSize,
            fontFamily = mixin.fontFamily ?: fontFamily,
            textColor = mixin.textColor ?: textColor,
            backgroundColor = mixin.backgroundColor ?: backgroundColor
        )
    }

    companion object {
        val DEFAULT = TextFormatting(fontSize = 12, fontFamily = "Serif", textColor = Color.BLACK)
        val FONTS = listOf("B Lotus", "B Badr")
    }
}

object TextRunOps : TextOps<TextRun, TextFormatting> {
    override fun length(seg: TextRun): Int = seg.content.length

    override fun charAt(seg: TextRun, index: Int) = seg.content[index]

    override fun getText(seg: TextRun): String = seg.content

    override fun subSequence(seg: TextRun, start: Int, end: Int) = TextRun(seg.content.substring(start, end))

    override fun subSequence(seg: TextRun, start: Int): TextRun = TextRun(seg.content.substring(start))

    override fun joinSeg(currentSeg: TextRun, nextSeg: TextRun): Optional<TextRun> =
        Optional.of(TextRun(currentSeg.content + nextSeg.content))

    override fun createEmptySeg(): TextRun = create("")

    override fun create(text: String): TextRun = TextRun(text)

}