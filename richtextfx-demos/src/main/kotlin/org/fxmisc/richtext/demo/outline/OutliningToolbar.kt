package org.fxmisc.richtext.demo.outline

import com.ebadollah.editor.ParagraphStyle
import javafx.beans.binding.BooleanBinding
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.fxmisc.richtext.demo.outline.model.TextFormatting
import org.fxmisc.richtext.demo.outline.model.TextRun
import org.fxmisc.richtext.model.*
import org.reactfx.SuspendableNo
import org.reactfx.util.Tuple2
import java.io.*
import java.util.function.Function
import kotlin.streams.asSequence

class OutliningToolbar(val area: OutlineArea) : ToolBar() {

    private fun updateStyleInSelection(mixinGetter: Function<StyleSpans<TextFormatting>, TextFormatting>) {
        val selection = area.selection
        if (selection.length != 0) {
            val styles: StyleSpans<TextFormatting> = area.getStyleSpans(selection)
            val mixin = mixinGetter.apply(styles)
            val newStyles = styles.mapStyles { it.overrideWith(mixin) }
            area.setStyleSpans(selection.start, newStyles)
        }
    }

    private fun toggleBold() = updateStyleInSelection {
        TextFormatting(bold = !it.styleStream().asSequence().all { it.bold })
    }

    private fun toggleItalic() = updateStyleInSelection {
        TextFormatting(italic = !it.styleStream().asSequence().all { it.italic })
    }

    private fun toggleUnderline() = updateStyleInSelection {
        TextFormatting(italic = !it.styleStream().asSequence().all { it.underline })
    }

    private fun toggleStrikethrough() = updateStyleInSelection {
        TextFormatting(italic = !it.styleStream().asSequence().all { it.strikethrough })
    }

    private fun createButton(styleClass: String, action: Runnable, toolTip: String?): Button {
        val button = Button()
        button.styleClass.add(styleClass)
        button.onAction = EventHandler { evt: ActionEvent? ->
            action.run()
            area.requestFocus()
        }
        button.prefWidth = 25.0
        button.prefHeight = 25.0
        if (toolTip != null) {
            button.tooltip = Tooltip(toolTip)
        }
        return button
    }

    private fun updateFontSize(size: Int) {
        if (!updatingToolbar.get())
            updateStyleInSelection { TextFormatting(fontSize = size) }
    }

    private fun updateFontFamily(family: String) {
        if (!updatingToolbar.get())
            updateStyleInSelection { TextFormatting(fontFamily = family) }
    }

    private fun updateTextColor(color: Color) {
        if (!updatingToolbar.get())
            updateStyleInSelection { TextFormatting(textColor = color) }
    }

    private fun updateBackgroundColor(color: Color) {
        if (!updatingToolbar.get())
            updateStyleInSelection { TextFormatting(backgroundColor = color) }
    }

    val wrapToggle = CheckBox("Wrap").apply {
        isSelected = true
        area.wrapTextProperty().bind(selectedProperty())
    }
    val undoBtn: Button = createButton("undo", { area.undo() }, "Undo").apply {
        disableProperty().bind(area.undoAvailableProperty().map { x -> !x })
    }
    val redoBtn: Button = createButton("redo", { area.redo() }, "Redo").apply {
        disableProperty().bind(area.redoAvailableProperty().map { x -> !x })
    }

    val cutBtn: Button = createButton("cut", { area.cut() }, "Cut")
    val copyBtn: Button = createButton("copy", { area.copy() }, "Copy")
    val pasteBtn: Button = createButton("paste", { area.paste() }, "Paste")

    val boldBtn: Button = createButton("bold", { this.toggleBold() }, "Bold")
    val italicBtn: Button = createButton("italic", { this.toggleItalic() }, "Italic")
    val underlineBtn: Button = createButton("underline", { this.toggleUnderline() }, "Underline")
    val strikeBtn: Button = createButton("strikethrough", { this.toggleStrikethrough() }, "Strike Trough")

    val sizeCombo = ComboBox(
        FXCollections.observableArrayList(
            "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "16", "18",
            "20", "22", "24", "28", "32", "36", "40",
            "48", "56", "64", "72"
        )
    ).apply {
        selectionModel.select("12")
        tooltip = Tooltip("Font size")
        setOnAction { updateFontSize(value.toInt()) }
    }

    val familyCombo = ComboBox(FXCollections.observableList(TextFormatting.FONTS)).apply {
        selectionModel.select(0)
        tooltip = Tooltip("Font family")
        setOnAction { updateFontFamily(value) }
    }

    val textColorPicker = ColorPicker(Color.BLACK).apply {
        tooltip = Tooltip("Text color")
        setOnAction { updateTextColor(value) }
    }
    val backgroundColorPicker = ColorPicker().apply {
        tooltip = Tooltip("Text background")
        setOnAction { updateBackgroundColor(value) }
    }


    private val updatingToolbar = SuspendableNo()

    init {
        val selectionEmpty: BooleanBinding = object : BooleanBinding() {
            override fun computeValue(): Boolean {
                return area.getSelection().getLength() == 0
            }

            init {
                bind(area.selectionProperty())
            }
        }
        cutBtn.disableProperty().bind(selectionEmpty)
        copyBtn.disableProperty().bind(selectionEmpty)
        area.beingUpdatedProperty()
            .addListener { o: ObservableValue<out Boolean?>, old: Boolean, beingUpdated: Boolean ->
                val bold: Boolean
                val italic: Boolean
                val underline: Boolean
                val strike: Boolean
                val fontSize: Int?
                val fontFamily: String?
                val textColor: Color?
                val backgroundColor: Color?

                if (!beingUpdated) {
                    val selection: IndexRange = area.getSelection()
                    if (selection.getLength() != 0) {
                        val styles: StyleSpans<TextFormatting> = area.getStyleSpans(selection)
                        bold = styles.styleStream().asSequence().all { it.bold }
                        italic = styles.styleStream().asSequence().all { it.italic }
                        underline = styles.styleStream().asSequence().all { it.underline }
                        strike = styles.styleStream().asSequence().all { it.strikethrough }

                        fontSize = styles.styleStream().asSequence().map { it.fontSize }.distinct().singleOrNull()
                        fontFamily = styles.styleStream().asSequence().map { it.fontFamily }.distinct().singleOrNull()
                        textColor = styles.styleStream().asSequence().map { it.textColor }.distinct().singleOrNull()
                        backgroundColor =
                            styles.styleStream().asSequence().map { it.backgroundColor }.distinct().singleOrNull()
                    } else {
                        val p: Int = area.currentParagraph
                        val col: Int = area.caretColumn
                        val style: TextFormatting = area.getStyleAtPosition(p, col)
                        bold = style.bold
                        italic = style.italic
                        underline = style.underline
                        strike = style.strikethrough
                        fontSize = style.fontSize
                        fontFamily = style.fontFamily
                        textColor = style.textColor
                        backgroundColor = style.backgroundColor
                    }

                    updatingToolbar.suspendWhile {
                        if (bold) {
                            if (!boldBtn.styleClass.contains("pressed")) {
                                boldBtn.styleClass.add("pressed")
                            }
                        } else {
                            boldBtn.styleClass.remove("pressed")
                        }
                        if (italic) {
                            if (!italicBtn.styleClass.contains("pressed")) {
                                italicBtn.styleClass.add("pressed")
                            }
                        } else {
                            italicBtn.styleClass.remove("pressed")
                        }
                        if (underline) {
                            if (!underlineBtn.styleClass.contains("pressed")) {
                                underlineBtn.styleClass.add("pressed")
                            }
                        } else {
                            underlineBtn.styleClass.remove("pressed")
                        }
                        if (strike) {
                            if (!strikeBtn.styleClass.contains("pressed")) {
                                strikeBtn.styleClass.add("pressed")
                            }
                        } else {
                            strikeBtn.styleClass.remove("pressed")
                        }
                        if (fontSize != null) {
                            sizeCombo.selectionModel.select(fontSize)
                        } else {
                            sizeCombo.selectionModel.clearSelection()
                        }
                        if (fontFamily != null) {
                            familyCombo.getSelectionModel().select(fontFamily)
                        } else {
                            familyCombo.getSelectionModel().clearSelection()
                        }
                        if (textColor != null) {
                            textColorPicker.setValue(textColor)
                        }
                        backgroundColorPicker.setValue(backgroundColor)
                    }
                }
            }
        items.addAll(
            wrapToggle,
            Separator(Orientation.VERTICAL),
            undoBtn, redoBtn,
            Separator(Orientation.VERTICAL),
            cutBtn, copyBtn, pasteBtn,
            Separator(Orientation.VERTICAL),
            boldBtn, italicBtn, underlineBtn, strikeBtn,
            Separator(Orientation.VERTICAL),
            sizeCombo, familyCombo, textColorPicker, backgroundColorPicker
        )
    }
}
