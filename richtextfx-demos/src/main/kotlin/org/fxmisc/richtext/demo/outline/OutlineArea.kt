package org.fxmisc.richtext.demo.outline

import com.ebadollah.editor.ParagraphStyle
import javafx.scene.Node
import javafx.scene.input.Clipboard
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination.CONTROL_DOWN
import javafx.scene.input.KeyCombination.SHIFT_DOWN
import javafx.scene.text.TextFlow
import org.fxmisc.richtext.GenericStyledArea
import org.fxmisc.richtext.StyledTextArea
import org.fxmisc.richtext.TextExt
import org.fxmisc.richtext.demo.outline.model.*
import org.fxmisc.richtext.model.*
import org.fxmisc.wellbehaved.event.EventPattern.keyPressed
import org.fxmisc.wellbehaved.event.InputMap.consume
import org.fxmisc.wellbehaved.event.InputMap.sequence
import org.fxmisc.wellbehaved.event.Nodes
import java.util.function.BiConsumer
import java.util.function.Function

class OutlineArea :
    GenericStyledArea<ParagraphStyle, TextRun, TextFormatting>(
        ParagraphStyle.DEFAULT,  // default paragraph style
        BiConsumer { paragraph: TextFlow, style: ParagraphStyle ->
            paragraph.style = style.toCss()
        },  // paragraph style setter
        TextFormatting.DEFAULT,  // default segment style
        TextRunOps,  // segment operations
        Function<StyledSegment<TextRun, TextFormatting>, Node>
        { seg: StyledSegment<TextRun, TextFormatting> ->
            createNode(
                seg
            ) { text: TextExt, style: TextFormatting ->
                text.style = style.toCss()
            }
        }) {
    val outliningActions = OutliningActions(this)

    val KEY_MAPPING = sequence(
        consume(keyPressed(KeyCodeCombination(KeyCode.TAB, SHIFT_DOWN))) { promote() },
        consume(keyPressed(KeyCodeCombination(KeyCode.TAB))) { demote() },

        consume(keyPressed(KeyCodeCombination(KeyCode.DIGIT0, CONTROL_DOWN))) { makeBodyText() },

        consume(keyPressed(KeyCodeCombination(KeyCode.DIGIT1, CONTROL_DOWN))) { setLevel(1) },
        consume(keyPressed(KeyCodeCombination(KeyCode.DIGIT2, CONTROL_DOWN))) { setLevel(2) },
        consume(keyPressed(KeyCodeCombination(KeyCode.DIGIT3, CONTROL_DOWN))) { setLevel(3) },
        consume(keyPressed(KeyCodeCombination(KeyCode.DIGIT4, CONTROL_DOWN))) { setLevel(4) },
        consume(keyPressed(KeyCodeCombination(KeyCode.DIGIT5, CONTROL_DOWN))) { setLevel(5) },
        consume(keyPressed(KeyCodeCombination(KeyCode.DIGIT6, CONTROL_DOWN))) { setLevel(6) },
        consume(keyPressed(KeyCodeCombination(KeyCode.DIGIT7, CONTROL_DOWN))) { setLevel(7) },
        consume(keyPressed(KeyCodeCombination(KeyCode.DIGIT8, CONTROL_DOWN))) { setLevel(8) },
        consume(keyPressed(KeyCodeCombination(KeyCode.DIGIT9, CONTROL_DOWN))) { setLevel(9) },

        consume(keyPressed(KeyCodeCombination(KeyCode.MINUS, CONTROL_DOWN))) { collapse() },
        consume(keyPressed(KeyCodeCombination(KeyCode.EQUALS, CONTROL_DOWN))) { expand() },
        consume(keyPressed(KeyCodeCombination(KeyCode.DIGIT0, CONTROL_DOWN, SHIFT_DOWN))) {
            showLevels(BodyText(Int.MAX_VALUE))
        },
    )

    init {
        Nodes.addInputMap(this, KEY_MAPPING)
        paragraphGraphicFactory = OutlineBulletFactory(this)
//        nodeOrientation = NodeOrientation.RIGHT_TO_LEFT
    }

//    fun foldParagraphs(startPar: Int, endPar: Int) {
//        foldParagraphs(startPar, endPar, addFoldStyle)
//    }
//
//    fun foldSelectedParagraphs() {
//        foldSelectedParagraphs(addFoldStyle)
//    }
//
//    fun foldText(start: Int, end: Int) {
//        fold(start, end, addFoldStyle)
//    }
//
//    fun unfoldParagraphs(startingFromPar: Int) {
//        unfoldParagraphs(startingFromPar, foldStyleCheck, removeFoldStyle)
//    }
//
//    fun unfoldText(startingFromPos: Int) {
//        var startingFromPos = startingFromPos
//        startingFromPos = offsetToPosition(startingFromPos, TwoDimensional.Bias.Backward).major
//        unfoldParagraphs(startingFromPos, foldStyleCheck, removeFoldStyle)
//    }

//    protected val addFoldStyle: UnaryOperator<ParStyle>
//        protected get() = UnaryOperator { pstyle: ParStyle -> pstyle.updateFold(true) }
//    protected val removeFoldStyle: UnaryOperator<ParStyle>
//        protected get() = UnaryOperator { pstyle: ParStyle -> pstyle.updateFold(false) }
//    protected val foldStyleCheck: Predicate<ParStyle>
//        protected get() = Predicate { pstyle: ParStyle -> pstyle.isFolded() }


    fun demote() {
        runAndUpdate { start, end -> outliningActions.demote(start, end) }

    }

    fun promote() {
        runAndUpdate { start, end -> outliningActions.promote(start, end) }
    }

    fun makeBodyText() {
        runAndUpdate { start, end -> outliningActions.makeBodyText(start, end) }
    }

    fun setLevel(level: Int) {
        runAndUpdate { start, end -> outliningActions.setLevel(start, end, level) }
    }


    private fun runAndUpdate(f: (Int) -> Map<Int, ParagraphStyle>) {
        if (selection.length != 0) throw RuntimeException()
        val index: Int = currentParagraph
        with(caretSelectionBind) { startParagraphIndex..endParagraphIndex }

        val updates: Map<Int, ParagraphStyle> = f(index)
        updates.forEach { (i, ps) ->
            setParagraphStyle(i, ps)
        }
    }

    private fun runAndUpdate(f: (Int, Int) -> Map<Int, ParagraphStyle>) {
        val updates: Map<Int, ParagraphStyle> =
            caretSelectionBind.let { f(it.startParagraphIndex, it.endParagraphIndex) }
        updates.forEach { (i, ps) -> setParagraphStyle(i, ps) }
    }


    fun showLevels(level: ParagraphLevel) {
        unfoldParagraphs(0, { it.level >= level }) { it.copy(hidden = false) }
    }


    fun expand(index: Int = currentParagraph) {
        val prg = paragraphs[index].paragraphStyle
        setParagraphStyle(index, prg.copy(collapsed = false))

        var it = index + 1
        while (paragraphs.getOrNull(it)?.let { it.paragraphStyle.level < prg.level } == true) {
            it++
        }

        unfoldParagraphs(index, { it.level < prg.level }) { it.copy(hidden = false) }
    }

    fun collapse(index: Int = currentParagraph) {
        val prg = paragraphs[index].paragraphStyle
        setParagraphStyle(index, prg.copy(collapsed = true))

        var it = index + 1
        while (paragraphs.getOrNull(it)?.let { it.paragraphStyle.level < prg.level } == true) {
            it++
        }

        foldParagraphs(index, it - 1) { it.copy(hidden = true) }
    }


    companion object {

        const val FILE_EXTENSION = "outline"

        private fun createNode(
            seg: StyledSegment<TextRun, TextFormatting>,
            applyStyle: BiConsumer<in TextExt, TextFormatting>
        ): Node {
            return StyledTextArea.createStyledTextNode(
                seg.segment.content,
                seg.style,
                applyStyle
            )
        }
    }
}