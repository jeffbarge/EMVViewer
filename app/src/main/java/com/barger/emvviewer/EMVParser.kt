package com.barger.emvviewer

import java.io.ByteArrayInputStream
import java.nio.ByteBuffer

class TagInfo(val meaning: String, val wonkyAscii: Boolean) {
    constructor(meaning: String): this(meaning, false)
}

class EMVParser {
    fun parse(input: String): List<EMVTag> {
        val reader = ByteArrayInputStream(hexStringToByteArray(input))
        val result = mutableListOf<EMVTag>()

        var nextByte = reader.read()
        while (nextByte >= 0) {
            val tag = getTag(nextByte, reader)
            if (tag == -1) break
            val length = getLength(reader)
            if (length == -1) break
            val tagInfo = tagMeaningLookup[tag]
            val valBuf = ByteArray(length)
            if (reader.read(valBuf, 0, length) == -1) break
            val value: String = when (tagInfo) {
                null -> {
                    valBuf.map {
                        it.toPositiveInt().toString(16).padStart(2, '0')
                    }.joinToString("").toUpperCase()
                }
                else -> {
                    //known, let's put it together
                    if (tagInfo.wonkyAscii) {
                        String(valBuf).toUpperCase()
                    } else {
                        valBuf.map {
                            it.toPositiveInt().toString(16).padStart(2, '0')
                        }.joinToString("").toUpperCase()
                    }
                }
            }
            result.add(EMVTag(tag.toString(16), tagInfo?.meaning ?: "Unknown", value))

            nextByte = reader.read()
        }

        return result
    }

    private fun hexStringToByteArray(input: String): ByteArray {
        val len = input.length
        val bytes = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            bytes[i/2] = ((Character.digit(input[i], 16) shl 4) + Character.digit(input[i+1], 16)).toByte()
        }
        return bytes
    }

    internal fun getTag(nextByte: Int, reader: ByteArrayInputStream): Int {
        val needsAnotherByte = nextByte and 0x1f == 0x1f
        if (needsAnotherByte) {
            val i = reader.read()
            return if (i == -1) -1 else (nextByte shl 8) + i
        }
        return nextByte
    }

    internal fun getLength(reader: ByteArrayInputStream): Int {
        val nextByte = reader.read()
        return if (nextByte < 127) {
            nextByte
        } else {
            //nextByte tells us how many _more_ bytes
            val numBytes = nextByte and 0x0f
            val bytes = ByteArray(numBytes)
            val i = reader.read(bytes, 0, numBytes)
            if (i == -1) -1 else ByteBuffer.wrap(bytes).short.toInt()
        }
    }

    companion object {
        val tagMeaningLookup = mapOf(
                Pair(0x9F01, TagInfo("Acquirer Identifier")),
                Pair(0x9F40, TagInfo("Additional Terminal Capabilities")),
                Pair(0x81, TagInfo("Amount, Authorised (Binary)")),
                Pair(0x9F02, TagInfo("Amount, Authorised (Numeric)")),
                Pair(0x9F04, TagInfo("Amount, Other (Binary)")),
                Pair(0x9F03, TagInfo("Amount, Other (Numeric)")),
                Pair(0x9F3A, TagInfo("Amount, Reference Currency")),
                Pair(0x9F26, TagInfo("Application Cryptogram")),
                Pair(0x9F42, TagInfo("Application Currency Code")),
                Pair(0x9F44, TagInfo("Application Currency Exponent")),
                Pair(0x9F05, TagInfo("Application Discretionary Data")),
                Pair(0x5F25, TagInfo("Application Effective Date")),
                Pair(0x5F24, TagInfo("Application Expiration Date")),
                Pair(0x94, TagInfo("Application File Locator (AFL)")),
                Pair(0x4F, TagInfo("Application Identifier (AID) – card")),
                Pair(0x9F06, TagInfo("Application Identifier (AID) – terminal")),
                Pair(0x82, TagInfo("Application Interchange Profile")),
                Pair(0x50, TagInfo("Application Label", true)),
                Pair(0x9F12, TagInfo("Application Preferred Name", true)),
                Pair(0x5A, TagInfo("Application Primary Account Number (PAN)")),
                Pair(0x5F34, TagInfo("Application Primary Account Number (PAN) Sequence Number")),
                Pair(0x87, TagInfo("Application Priority Indicator")),
                Pair(0x9F3B, TagInfo("Application Reference Currency")),
                Pair(0x9F43, TagInfo("Application Reference Currency Exponent")),
                Pair(0x61, TagInfo("Application Template")),
                Pair(0x9F36, TagInfo("Application Transaction Counter (ATC)")),
                Pair(0x9F07, TagInfo("Application Usage Control")),
                Pair(0x9F08, TagInfo("Application Version Number")),
                Pair(0x9F09, TagInfo("Application Version Number")),
                Pair(0x89, TagInfo("Authorisation Code")),
                Pair(0x8A, TagInfo("Authorisation Response Code")),
                Pair(0x5F54, TagInfo("Bank Identifier Code (BIC)")),
                Pair(0x8C, TagInfo("Card Risk Management Data Object List 1 (CDOL1)")),
                Pair(0x8D, TagInfo("Card Risk Management Data Object List 2 (CDOL2)")),
                Pair(0x5F20, TagInfo("Cardholder Name", true)),
                Pair(0x9F0B, TagInfo("Cardholder Name Extended", true)),
                Pair(0x8E, TagInfo("Cardholder Verification Method (CVM) List")),
                Pair(0x9F34, TagInfo("Cardholder Verification Method (CVM) Results")),
                Pair(0x8F, TagInfo("Certification Authority Public Key Index")),
                Pair(0x9F22, TagInfo("Certification Authority Public Key Index")),
                Pair(0x83, TagInfo("Command Template")),
                Pair(0x9F27, TagInfo("Cryptogram Information Data")),
                Pair(0x9F45, TagInfo("Data Authentication Code")),
                Pair(0x84, TagInfo("Dedicated File (DF) Name")),
                Pair(0x9D, TagInfo("Directory Definition File (DDF) Name")),
                Pair(0x73, TagInfo("Directory Discretionary Template")),
                Pair(0x9F49, TagInfo("Dynamic Data Authentication Data Object List (DDOL)")),
                Pair(0x70, TagInfo("EMV Proprietary Template")),
                Pair(0xBF0C, TagInfo("File Control Information (FCI) Issuer Discretionary Data")),
                Pair(0xA5, TagInfo("File Control Information (FCI) Proprietary Template")),
                Pair(0x6F, TagInfo("File Control Information (FCI) Template")),
                Pair(0x9F4C, TagInfo("ICC Dynamic Number")),
                Pair(0x9F2D, TagInfo("Integrated Circuit Card (ICC) PIN Encipherment Public Key Certificate")),
                Pair(0x9F2E, TagInfo("Integrated Circuit Card (ICC) PIN Encipherment Public Key Exponent")),
                Pair(0x9F2F, TagInfo("Integrated Circuit Card (ICC) PIN Encipherment Public Key Remainder")),
                Pair(0x9F46, TagInfo("Integrated Circuit Card (ICC) Public Key Certificate")),
                Pair(0x9F47, TagInfo("Integrated Circuit Card (ICC) Public Key Exponent")),
                Pair(0x9F48, TagInfo("Integrated Circuit Card (ICC) Public Key Remainder")),
                Pair(0x9F1E, TagInfo("Interface Device (IFD) Serial Number", true)),
                Pair(0x5F53, TagInfo("International Bank Account Number (IBAN)")),
                Pair(0x9F0D, TagInfo("Issuer Action Code – Default")),
                Pair(0x9F0E, TagInfo("Issuer Action Code – Denial")),
                Pair(0x9F0F, TagInfo("Issuer Action Code – Online")),
                Pair(0x9F10, TagInfo("Issuer Application Data")),
                Pair(0x91, TagInfo("Issuer Authentication Data")),
                Pair(0x9F11, TagInfo("Issuer Code Table Index")),
                Pair(0x5F28, TagInfo("Issuer Country Code")),
                Pair(0x5F55, TagInfo("Issuer Country Code (alpha2 format)")),
                Pair(0x5F56, TagInfo("Issuer Country Code (alpha3 format)")),
                Pair(0x42, TagInfo("Issuer Identification Number (IIN)")),
                Pair(0x90, TagInfo("Issuer Public Key Certificate")),
                Pair(0x9F32, TagInfo("Issuer Public Key Exponent")),
                Pair(0x92, TagInfo("Issuer Public Key Remainder")),
                Pair(0x86, TagInfo("Issuer Script Command")),
                Pair(0x9F18, TagInfo("Issuer Script Identifier")),
                Pair(0x71, TagInfo("Issuer Script Template 1")),
                Pair(0x72, TagInfo("Issuer Script Template 2")),
                Pair(0x5F50, TagInfo("Issuer URL", true)),
                Pair(0x5F2D, TagInfo("Language Preference", true)),
                Pair(0x9F13, TagInfo("Last Online Application Transaction Counter (ATC) Register")),
                Pair(0x9F4D, TagInfo("Log Entry")),
                Pair(0x9F4F, TagInfo("Log Format")),
                Pair(0x9F14, TagInfo("Lower Consecutive Offline Limit")),
                Pair(0x9F15, TagInfo("Merchant Category Code")),
                Pair(0x9F16, TagInfo("Merchant Identifier", true)),
                Pair(0x9F4E, TagInfo("Merchant Name and Location", true)),
                Pair(0x9F17, TagInfo("Personal Identification Number (PIN) Try Counter")),
                Pair(0x9F39, TagInfo("Point-of-Service (POS) Entry Mode")),
                Pair(0x9F38, TagInfo("Processing Options Data Object List (PDOL)")),
                Pair(0x80, TagInfo("Response Message Template Format 1")),
                Pair(0x77, TagInfo("Response Message Template Format 2")),
                Pair(0x5F30, TagInfo("Service Code")),
                Pair(0x88, TagInfo("Short File Identifier (SFI)")),
                Pair(0x9F4B, TagInfo("Signed Dynamic Application Data")),
                Pair(0x93, TagInfo("Signed Static Application Data")),
                Pair(0x9F4A, TagInfo("Static Data Authentication Tag List")),
                Pair(0x9F33, TagInfo("Terminal Capabilities")),
                Pair(0x9F1A, TagInfo("Terminal Country Code")),
                Pair(0x9F1B, TagInfo("Terminal Floor Limit")),
                Pair(0x9F1C, TagInfo("Terminal Identification")),
                Pair(0x9F1D, TagInfo("Terminal Risk Management Data")),
                Pair(0x9F35, TagInfo("Terminal Type")),
                Pair(0x95, TagInfo("Terminal Verification Results")),
                Pair(0x9F1F, TagInfo("Track 1 Discretionary Data", true)),
                Pair(0x9F20, TagInfo("Track 2 Discretionary Data")),
                Pair(0x57, TagInfo("Track 2 Equivalent Data")),
                Pair(0x98, TagInfo("Transaction Certificate (TC) Hash Value")),
                Pair(0x97, TagInfo("Transaction Certificate Data Object List (TDOL)")),
                Pair(0x5F2A, TagInfo("Transaction Currency Code")),
                Pair(0x5F36, TagInfo("Transaction Currency Exponent")),
                Pair(0x9A, TagInfo("Transaction Date")),
                Pair(0x99, TagInfo("Transaction Personal Identification Number (PIN) Data")),
                Pair(0x9F3C, TagInfo("Transaction Reference Currency Code")),
                Pair(0x9F3D, TagInfo("Transaction Reference Currency Exponent")),
                Pair(0x9F41, TagInfo("Transaction Sequence Counter")),
                Pair(0x9B, TagInfo("Transaction Status Information")),
                Pair(0x9F21, TagInfo("Transaction Time")),
                Pair(0x9C, TagInfo("Transaction Type")),
                Pair(0x9F37, TagInfo("Unpredictable Number")),
                Pair(0x9F23, TagInfo("Upper Consecutive Offline Limit"))
        )
    }
}

fun Byte.toPositiveInt() = toInt() and 0xFF