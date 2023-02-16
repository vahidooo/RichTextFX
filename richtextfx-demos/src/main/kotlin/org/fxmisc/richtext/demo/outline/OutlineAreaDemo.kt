package org.fxmisc.richtext.demo.outline

import com.ebadollah.editor.ParagraphStyle
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.GenericStyledArea
import org.fxmisc.richtext.demo.outline.codec.ParagraphStyleCodec
import org.fxmisc.richtext.demo.outline.codec.TextFormattingCodec
import org.fxmisc.richtext.demo.outline.codec.TextRunCodec
import org.fxmisc.richtext.demo.outline.model.TextFormatting
import org.fxmisc.richtext.demo.outline.model.TextRun
import org.fxmisc.richtext.demo.richtext.RichTextDemo
import org.fxmisc.richtext.model.Codec

class OutlineAreaDemo : Application() {
    override fun start(stage: Stage) {
        val area = OutlineArea().apply {
            isWrapText = true
            setStyleCodecs(
                ParagraphStyleCodec,
                Codec.styledSegmentCodec(TextRunCodec, TextFormattingCodec)
            )
        }

        val vsPane = VirtualizedScrollPane<GenericStyledArea<ParagraphStyle, TextRun, TextFormatting>>(area)
        val vbox = VBox()
        VBox.setVgrow(vsPane, Priority.ALWAYS)

        vbox.children.addAll(
            OutliningToolbar(area),
            EditingToolbar(area, stage),
            vsPane
        )
        val scene = Scene(vbox, 600.0, 400.0).apply {
            stylesheets.add(RichTextDemo::class.java.getResource("rich-text.css").toExternalForm())
        }
        stage.scene = scene
        area.requestFocus()
        stage.title = "Rich Text Demo"

        stage.show()
//        ScenicView.show(scene)
    }
}


fun main(args: Array<String>) {
    Application.launch(OutlineAreaDemo::class.java, *args)
}