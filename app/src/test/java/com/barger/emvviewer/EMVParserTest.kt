package com.barger.emvviewer

import org.junit.Test

import org.junit.Assert.*
import java.io.ByteArrayInputStream

class EMVParserTest {

    @Test
    fun getTagSingleByte() {
        val input = byteArrayOf(
                0x4F.toByte(),
                0x0E.toByte(),
                0xA0.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x04.toByte(),
                0x10.toByte(),
                0x10.toByte())
        val reader = ByteArrayInputStream(input)
        val nextByte = reader.read()
        val tag = EMVParser().getTag(nextByte, reader)
        assertEquals(0x4f, tag)
    }

    @Test
    fun getTagTwoBytes() {
        val input = byteArrayOf(
                0x9F.toByte(),
                0x06.toByte(),
                0x07.toByte(),
                0xA0.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x04.toByte(),
                0x10.toByte(),
                0x10.toByte())
        val reader = ByteArrayInputStream(input)
        val nextByte = reader.read()
        val tag = EMVParser().getTag(nextByte, reader)
        assertEquals(0x9f06, tag)
    }

    @Test
    fun getLengthSingleByte() {
        val input = byteArrayOf(
                0x4F.toByte(),
                0x0E.toByte(),
                0xA0.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x00.toByte(),
                0x04.toByte(),
                0x10.toByte(),
                0x10.toByte())
        val reader = ByteArrayInputStream(input)
        val nextByte = reader.read()
        val parser = EMVParser()
        val tag = parser.getTag(nextByte, reader)
        val length = parser.getLength(reader)
        assertEquals(14, length)
    }

    @Test
    fun getLengthMultipleBytes() {
        val input = byteArrayOf(
                0xC2.toByte(),
                0x82.toByte(),
                0x01.toByte(),
                0x68.toByte(),
                0xd9.toByte(),
                0xde.toByte(),
                0x28.toByte())
        val reader = ByteArrayInputStream(input)
        val nextByte = reader.read()
        val parser = EMVParser()
        val tag = parser.getTag(nextByte, reader)
        val length = parser.getLength(reader)
        assertEquals(0x168, length)
    }
}
