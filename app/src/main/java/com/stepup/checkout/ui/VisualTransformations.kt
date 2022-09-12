package com.stepup.checkout.ui

import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import com.stepup.checkout.common.plus
import com.stepup.checkout.domain.PaymentCardType
import com.stepup.checkout.domain.numberBlocks
import kotlin.math.min

@Composable
fun rememberCardNumberTransformation(type: PaymentCardType? = null): VisualTransformation =
    remember(type) { SeparatedBlockTransformation(type.numberBlocks) }

@Composable
fun rememberExpiryDateTransformation(
    hintColor: Color = LocalContentColor.current.copy(alpha = 0.3f)
): VisualTransformation = remember(hintColor) { ExpiryDateTransformation(hintColor) }

private class ExpiryDateTransformation(
    private val hintColor: Color
) : VisualTransformation {

    companion object {
        private val transformation = SeparatedBlockTransformation(listOf(2, 2), separator = '/')
    }

    private val expiryDateHint = buildAnnotatedString {
        withStyle(SpanStyle(color = hintColor)) { append("MM/YY") }
    }

    private val appendZeroMapping = object : OffsetMapping {

        override fun originalToTransformed(offset: Int): Int {
            return if (offset > 0) return offset + 1 else offset
        }

        override fun transformedToOriginal(offset: Int): Int {
            return if (offset <= 1) 0 else offset
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val appendZero = with(text) {
            !isEmpty() && !startsWith('1') && !startsWith('0')
        }
        val transformed = transformation.filter(
            buildAnnotatedString {
                if (appendZero) append('0')
                append(text)
            }
        )
        return TransformedText(
            text = buildAnnotatedString {
                append(transformed.text)
                if (length < expiryDateHint.length) {
                    append(expiryDateHint.subSequence(length, expiryDateHint.length))
                }
            },
            offsetMapping = transformed.offsetMapping +
                    if (appendZero) appendZeroMapping else OffsetMapping.Identity
        )
    }
}

/**
 * A [VisualTransformation] that will break up the incoming text into blocks
 * separated by the given [separator].
 */
internal class SeparatedBlockTransformation(
    blockLengths: List<Int>,
    private val separator: Char = ' '
) : VisualTransformation {

    private val originalRanges = mutableListOf<TextRange>()
    private val transformedRanges = mutableListOf<TextRange>()

    init {
        blockLengths.fold(0) { acc, length ->
            (acc + length).also {
                originalRanges.add(TextRange(acc, it))
                val numberOfSeparatorsSoFar = transformedRanges.size
                transformedRanges.add(
                    TextRange(
                        acc + numberOfSeparatorsSoFar,
                        // include the separator in the preceding transformed range
                        it + numberOfSeparatorsSoFar + 1
                    )
                )
            }
        }
    }

    private val mapping = object : OffsetMapping {

        override fun originalToTransformed(offset: Int): Int {
            val separatorsBefore = originalRanges.indexOfRangeContaining(offset)
            return offset + separatorsBefore
        }

        override fun transformedToOriginal(offset: Int): Int {
            val separatorsBefore = transformedRanges.indexOfRangeContaining(offset)
            return offset - separatorsBefore
        }

        private fun List<TextRange>.indexOfRangeContaining(offset: Int): Int {
            return indexOfFirst { offset in it }.takeIf { it >= 0 }
                ?: originalRanges.lastIndex.coerceAtLeast(0)
        }
    }

    override fun filter(text: AnnotatedString) = TransformedText(
        text = buildAnnotatedString {
            originalRanges
                .takeWhile { it.start < text.length }
                .forEachIndexed { index, range ->
                    append(text.subSequence(range.start, min(range.end, text.length)))
                    if (index < originalRanges.lastIndex && range.end <= text.length) {
                        append(separator.toString())
                    }
                }
            val nextIndexInText = mapping.transformedToOriginal(length)
            if (nextIndexInText < text.length) {
                append(text.subSequence(nextIndexInText, text.length))
            }
        },
        mapping
    )
}