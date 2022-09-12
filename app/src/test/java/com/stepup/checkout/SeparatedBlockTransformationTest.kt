package com.stepup.checkout

import androidx.compose.ui.text.AnnotatedString
import com.stepup.checkout.ui.SeparatedBlockTransformation
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

class SeparatedBlockTransformationTest {

    @Test
    fun `when empty blocks, no change`() {
        val transformation = SeparatedBlockTransformation(emptyList())
        val text = "these are some words"
        val filtered = transformation.filter(AnnotatedString(text))
        filtered.text.toString() shouldBeEqualTo text
        filtered.offsetMapping.apply {
            originalToTransformed(0) shouldBeEqualTo 0
            originalToTransformed(3) shouldBeEqualTo 3
            originalToTransformed(4) shouldBeEqualTo 4
            originalToTransformed(5) shouldBeEqualTo 5
            originalToTransformed(8) shouldBeEqualTo 8
            originalToTransformed(16) shouldBeEqualTo 16
            originalToTransformed(235) shouldBeEqualTo 235
            transformedToOriginal(0) shouldBeEqualTo 0
            transformedToOriginal(3) shouldBeEqualTo 3
            transformedToOriginal(4) shouldBeEqualTo 4
            transformedToOriginal(5) shouldBeEqualTo 5
            transformedToOriginal(8) shouldBeEqualTo 8
            transformedToOriginal(16) shouldBeEqualTo 16
            transformedToOriginal(235) shouldBeEqualTo 235
        }
    }

    @Test
    fun `original to transformed offset mapping`() {
        val blocks = listOf("these", "are", "some", "words")
        val transformation = SeparatedBlockTransformation(blocks.map { it.length })
        val filtered = transformation.filter(AnnotatedString(blocks.joinToString("")))
        filtered.offsetMapping.apply {
            originalToTransformed(0) shouldBeEqualTo 0
            originalToTransformed(3) shouldBeEqualTo 3
            originalToTransformed(4) shouldBeEqualTo 4
            originalToTransformed(5) shouldBeEqualTo 6
            originalToTransformed(7) shouldBeEqualTo 8
            originalToTransformed(16) shouldBeEqualTo 19
            originalToTransformed(235) shouldBeEqualTo 238
        }
    }

    @Test
    fun `transformed to original offset mapping`() {
        val blocks = listOf("these", "are", "some", "words")
        val transformation = SeparatedBlockTransformation(blocks.map { it.length })
        val filtered = transformation.filter(AnnotatedString(blocks.joinToString("")))
        filtered.offsetMapping.apply {
            transformedToOriginal(0) shouldBeEqualTo 0
            transformedToOriginal(3) shouldBeEqualTo 3
            transformedToOriginal(4) shouldBeEqualTo 4
            transformedToOriginal(5) shouldBeEqualTo 5
            transformedToOriginal(6) shouldBeEqualTo 5
            transformedToOriginal(19) shouldBeEqualTo 16
            transformedToOriginal(238) shouldBeEqualTo 235
        }
    }

    @Test
    fun `when all blocks used, separator inserted correctly`() {
        val blocks = listOf("these", "are", "some", "words")
        val separator = ' '
        val transformation = SeparatedBlockTransformation(blocks.map { it.length }, separator)
        val filtered = transformation.filter(AnnotatedString(blocks.joinToString("")))
        filtered.text.toString() shouldBeEqualTo blocks.joinToString(separator.toString())
    }

    @Test
    fun `when fewer blocks used, separator inserted correctly`() {
        val blocks = listOf("these", "are", "some", "words")
        val separator = ' '
        val transformation = SeparatedBlockTransformation(blocks.map { it.length }, separator)
        val filtered = transformation.filter(AnnotatedString("thesearesom"))
        filtered.text.toString() shouldBeEqualTo "these are som"
    }

    @Test
    fun `when more blocks used, separator inserted correctly`() {
        val blocks = listOf("these", "are", "some", "words")
        val separator = ' '
        val transformation = SeparatedBlockTransformation(blocks.map { it.length }, separator)
        val filtered = transformation.filter(AnnotatedString(blocks.joinToString("") + " and more words"))
        filtered.text.toString() shouldBeEqualTo (blocks.joinToString(separator.toString()) + " and more words")
    }
}