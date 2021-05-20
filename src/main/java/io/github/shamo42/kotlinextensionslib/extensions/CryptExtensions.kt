package io.github.shamo42.kotlinextensionslib.extensions

import okio.ByteString
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.math.BigInteger
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

fun String.shaHashHex(bit: Int): String {
    val md = MessageDigest.getInstance("SHA-$bit")
    md.update(this.toByteArray(Charsets.UTF_8))
    return md.digest().toHex()
}

fun String.shaHash(bit: Int): ByteArray {
    val md = MessageDigest.getInstance("SHA-$bit")
    md.update(this.toByteArray(Charsets.UTF_8))
    return md.digest()
}


fun String.hmacShaHexString(key: String, bit: Int): String {
    return this.hmacShaByteArray(key.toByteArray(Charsets.UTF_8), bit).toHex()
}
fun String.hmacShaHexString(key: ByteArray, bit: Int): String {
    return this.hmacShaByteArray(key, bit).toHex()
}

fun String.hmacShaByteArray(key: ByteArray, bit: Int): ByteArray {
    return this.toByteArray(Charsets.UTF_8).hmacShaByteArray(key, bit)
}

fun ByteArray.hmacShaByteArray(key: ByteArray, bit: Int): ByteArray {
    val type = "HmacSHA$bit"
    val secret = SecretKeySpec(key, type)
    return Mac.getInstance(type).let { mac ->
        mac.init(secret)
        mac.doFinal(this)
    }
}


fun String.compress(): ByteArray {
    val obj = ByteArrayOutputStream()
    val gzip = GZIPOutputStream(obj)
    gzip.write(this.toByteArray(charset("UTF-8")))
    gzip.flush()
    gzip.close()
    return obj.toByteArray()
}


fun ByteArray.decompress(): String {
    val outStr = StringBuilder()
    if (this.isEmpty()) return ""

    val gis = GZIPInputStream(ByteArrayInputStream(this))
    val bufferedReader = BufferedReader(InputStreamReader(gis, "UTF-8"))

    while (true) {
        val line = bufferedReader.readLine()?: break
        outStr.append(line)
    }

    return outStr.toString()
}

fun ByteString.decompress() = this.toByteArray().decompress()

fun ByteArray.toHex() : String{
    //todo remove String.format()
    /*val result = StringBuffer()
    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }
    return result.toString()*/

    return String.format("%064x", BigInteger(1, this))
}

