package org.fxmisc.richtext.demo.outline.codec

import org.fxmisc.richtext.demo.outline.model.TextFormatting
import org.fxmisc.richtext.model.Codec
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

fun <T> Codec<Optional<T>>.encode(os: DataOutputStream, t: T?) = encode(os, Optional.ofNullable(t))

object TextFormattingCodec : Codec<TextFormatting> {

    private val OPT_STRING_CODEC = Codec.optionalCodec(Codec.STRING_CODEC)
    private val OPT_COLOR_CODEC = Codec.optionalCodec(Codec.COLOR_CODEC)

    override fun getName(): String = "text-formatting"

    override fun encode(output: DataOutputStream, tf: TextFormatting) {
        output.writeBoolean(tf.bold)
        output.writeBoolean(tf.italic)
        output.writeBoolean(tf.underline)
        output.writeBoolean(tf.strikethrough)
        output.writeInt(tf.fontSize ?: -1)
        OPT_STRING_CODEC.encode(output, tf.fontFamily)
        OPT_COLOR_CODEC.encode(output, tf.textColor)
        OPT_COLOR_CODEC.encode(output, tf.backgroundColor)
    }

    override fun decode(input: DataInputStream): TextFormatting = with(input) {
        TextFormatting(
            readBoolean(),
            readBoolean(),
            readBoolean(),
            readBoolean(),
            readInt().let { if (it == -1) null else it },
            OPT_STRING_CODEC.decode(input).get(),
            OPT_COLOR_CODEC.decode(input).get(),
            OPT_COLOR_CODEC.decode(input).get()
        )
    }
}