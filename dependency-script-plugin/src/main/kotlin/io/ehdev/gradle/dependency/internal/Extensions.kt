package io.ehdev.gradle.dependency.internal

import java.math.BigInteger
import java.security.MessageDigest

fun String.md5Digest(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(this.toByteArray())

    return BigInteger(1, digest).toString(16)
}