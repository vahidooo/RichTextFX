package org.fxmisc.richtext.demo.outline

import com.ebadollah.editor.ParagraphStyle
import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.ContentDisplay
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import org.fxmisc.richtext.demo.outline.model.BodyText
import org.fxmisc.richtext.demo.outline.model.Heading
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import org.kordamp.ikonli.javafx.FontIcon
import java.util.function.IntFunction

class OutlineBulletFactory(private var area: OutlineArea) : IntFunction<Node> {

    private val DEFAULT_FONT = Font.font("monospace", FontPosture.ITALIC, 13.0)

    init {
        area.getParagraphs().sizeProperty().addListener { ob: ObservableValue<out Int>?, ov: Int, nv: Int ->
            if (nv <= ov) Platform.runLater { deleteParagraphCheck() } else Platform.runLater { insertParagraphCheck() }
        }
    }

    override fun apply(value: Int): Node? {
        val ps = area.getParagraph(value).paragraphStyle
        return createGraphic(ps, value)
    }

    private fun createGraphic(ps: ParagraphStyle, idx: Int): Node {
        val foldIndicator = Label("  ")
        VBox.setVgrow(foldIndicator, Priority.ALWAYS)
        foldIndicator.maxHeight = Double.MAX_VALUE
        foldIndicator.alignment = Pos.TOP_LEFT
        foldIndicator.font = DEFAULT_FONT
        if (area.paragraphs.size > idx + 1) {
            if (ps.collapsed) {
                foldIndicator.onMouseClicked = EventHandler { area.expand(idx) }
//                foldIndicator.styleClass.add("fold-indicator")
                foldIndicator.cursor = Cursor.HAND
//                foldIndicator.text = "+ "
            }
        }
        if (ps.hidden.not()) {
            foldIndicator.graphic = createBullet(ps, idx)
            foldIndicator.contentDisplay = ContentDisplay.RIGHT
        }
        return VBox(0.0, foldIndicator)
    }

    private fun createBullet(ps: ParagraphStyle, idx: Int): Node {
        val result = when (ps.level) {
            is BodyText -> {
                FontIcon(FontAwesomeSolid.CIRCLE).apply {
                    iconSize = (Font.getDefault().size / 2).toInt()
                    iconColor = Color.GRAY
                }
            }
            is Heading -> {
                if (ps.level.hasChild)
                    FontIcon(FontAwesomeSolid.PLUS_CIRCLE).apply {
                        iconColor =
                            if (ps.collapsed) Color.DIMGRAY
                            else Color.GRAY
                    }
                else
                    FontIcon(FontAwesomeSolid.MINUS_CIRCLE).apply {
                        iconColor = Color.GRAY
                    }
            }
        }

        val bullet = Label(" ", result)
        val indentation: Double = when (ps.level) {
            is BodyText -> ps.level.value + BODY_TEXT_INDENTATION_DIFFERENCE
            is Heading -> ps.level.value.toDouble()
        }
        bullet.padding = Insets(0.0, 0.0, 0.0, indentation * INDENTATION_WIDTH)
        bullet.contentDisplay = ContentDisplay.LEFT
        return bullet
    }

    private fun deleteParagraphCheck() {
        var p = area.getCurrentParagraph()
        // Was the deleted paragraph in the viewport ?
        if (p >= area.firstVisibleParToAllParIndex() && p <= area.lastVisibleParToAllParIndex()) {
            val col = area.getCaretColumn()
            // Delete was pressed on an empty paragraph, and so the cursor is now at the start of the next paragraph.
            if (col == 0) {
                // Check if the now current paragraph is folded.
                if (area.getParagraph(p).paragraphStyle.hidden) {
                    p = Math.max(p - 1, 0) // Adjust to previous paragraph.
                    area.recreateParagraphGraphic(p) // Show fold/unfold indicator on previous paragraph.
                    area.moveTo(p, 0) // Move cursor to previous paragraph.
                }
            } else if (col == area.getParagraph(p).length()) {
                area.recreateParagraphGraphic(p) // Shows fold/unfold indicator on current paragraph if needed.
            }
            // In all other cases the paragraph graphic is created/updated automatically.
        }
    }

    private fun insertParagraphCheck() {
        val p = area.getCurrentParagraph()
        // Is the inserted paragraph in the viewport ?
        if (p > area.firstVisibleParToAllParIndex() && p <= area.lastVisibleParToAllParIndex()) {
            // Check limits, as p-1 and p+1 are accessed ...
            if (p > 0 && p + 1 < area.getParagraphs().size) {
                // Now check if the inserted paragraph is before a folded block ?
                if (area.getParagraph(p + 1).paragraphStyle.hidden) {
                    area.recreateParagraphGraphic(p - 1) // Remove the unfold indicator.
                }
            }
        }
    }

    companion object {
        const val INDENTATION_WIDTH = 20
        const val BODY_TEXT_INDENTATION_DIFFERENCE = +0.7
    }

}