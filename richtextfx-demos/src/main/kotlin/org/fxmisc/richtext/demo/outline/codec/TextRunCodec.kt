package org.fxmisc.richtext.demo.outline.codec

import org.fxmisc.richtext.demo.outline.model.TextRun
import org.fxmisc.richtext.model.Codec
import java.io.DataInputStream
import java.io.DataOutputStream

object TextRunCodec : Codec<TextRun> {
    override fun getName(): String = "text-run"

    override fun encode(os: DataOutputStream, t: TextRun) =
        Codec.STRING_CODEC.encode(os, t.content)

    override fun decode(input: DataInputStream): TextRun =
        TextRun(Codec.STRING_CODEC.decode(input))
}