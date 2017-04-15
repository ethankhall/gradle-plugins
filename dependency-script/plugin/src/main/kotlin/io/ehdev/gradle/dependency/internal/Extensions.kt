package io.ehdev.gradle.dependency.internal

import java.io.File
import java.math.BigInteger
import java.security.DigestInputStream
import java.security.MessageDigest

fun File.md5Digest(): ByteArray {
    val md = MessageDigest.getInstance("MD5")
    DigestInputStream(inputStream(), md).use {
        val buf = ByteArray(1024)
        while (it.read(buf) != -1) {}
        it.close()
    }
    return md.digest()
}

fun toHex(bytes: ByteArray): String {
    val bi = BigInteger(1, bytes)
    return String.format("%0" + (bytes.size shl 1) + "X", bi)
}