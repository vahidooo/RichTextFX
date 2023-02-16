package org.fxmisc.richtext.demo.outline.codec

import com.ebadollah.editor.ParagraphStyle
import org.fxmisc.richtext.demo.outline.model.BodyText
import org.fxmisc.richtext.demo.outline.model.Heading
import org.fxmisc.richtext.model.Codec
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException


object ParagraphStyleCodec : Codec<ParagraphStyle> {
    override fun getName(): String {
        return "paragraph-style"
    }

    @Throws(IOException::class)
    override fun encode(output: DataOutputStream, ps: ParagraphStyle) = with(output) {
        writeBoolean(ps.level is BodyText)
        writeInt(ps.level.value)
        if (ps.level is Heading) {
            writeBoolean(ps.level.hasChild)
        }
        writeBoolean(ps.hidden)
        writeBoolean(ps.collapsed)
    }

    @Throws(IOException::class)
    override fun decode(input: DataInputStream): ParagraphStyle = with(input) {
        val bodyText = readBoolean()
        ParagraphStyle(
            if (bodyText) BodyText(readInt()) else Heading(readInt(), readBoolean()),
            readBoolean(),
            readBoolean()
        )
    }
}