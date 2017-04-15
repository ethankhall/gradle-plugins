package io.ehdev.gradle.dependency.internal

import java.math.BigInteger
import java.security.MessageDigest

fun String.md5Digest(): ByteArray {
    val md = MessageDigest.getInstance("MD5")
    return md.digest(this.toByteArray())
}

fun toHex(bytes: ByteArray): String {
    val bi = BigInteger(1, bytes)
    return String.format("%0" + (bytes.size shl 1) + "X", bi)
}