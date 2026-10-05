package com.example.engine

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Streaming converter CRX2 / CRX3 -> ZIP
 * Strips Google CRX headers without loading entire archive into RAM.
 */
object CrxConverter {

    /** "Cr24" (0x43 0x72 0x32 0x34) in Little Endian */
    private const val CRX_MAGIC = 0x34327243

    fun crxToZip(crxFile: File, outputFile: File): Boolean {
        return try {
            BufferedInputStream(FileInputStream(crxFile), 64 * 1024).use { input ->
                val head = ByteArray(8)
                if (!readFully(input, head)) return false

                // If not Cr24, check if it's already standard ZIP ("PK" = 0x50 0x4B)
                if (le32(head, 0) != CRX_MAGIC) {
                    val isZip = head[0] == 0x50.toByte() && head[1] == 0x4B.toByte()
                    return isZip && copyFile(crxFile, outputFile)
                }

                // Determine header size to skip based on CRX version
                val version = le32(head, 4)
                val toSkip: Long = when (version) {
                    3 -> { // CRX3: [magic][3][headerSize] + protobuf
                        val b = ByteArray(4)
                        if (!readFully(input, b)) return false
                        le32(b, 0).toLong() and 0xFFFFFFFFL
                    }
                    2 -> { // CRX2: [magic][2][keyLen][sigLen] + key + sig
                        val b = ByteArray(8)
                        if (!readFully(input, b)) return false
                        (le32(b, 0).toLong() and 0xFFFFFFFFL) + (le32(b, 4).toLong() and 0xFFFFFFFFL)
                    }
                    else -> return false
                }

                if (toSkip > crxFile.length()) return false
                if (!skipFully(input, toSkip)) return false

                FileOutputStream(outputFile).use { out -> input.copyTo(out) }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun le32(b: ByteArray, o: Int): Int =
        (b[o].toInt() and 0xFF) or
            ((b[o + 1].toInt() and 0xFF) shl 8) or
            ((b[o + 2].toInt() and 0xFF) shl 16) or
            ((b[o + 3].toInt() and 0xFF) shl 24)

    private fun readFully(input: InputStream, buf: ByteArray): Boolean {
        var off = 0
        while (off < buf.size) {
            val n = input.read(buf, off, buf.size - off)
            if (n < 0) return false
            off += n
        }
        return true
    }

    private fun skipFully(input: InputStream, count: Long): Boolean {
        var left = count
        while (left > 0) {
            val n = input.skip(left)
            if (n > 0) left -= n
            else {
                if (input.read() < 0) return false
                left--
            }
        }
        return true
    }

    private fun copyFile(source: File, dest: File): Boolean = try {
        source.inputStream().use { i -> dest.outputStream().use { o -> i.copyTo(o) } }
        true
    } catch (e: Exception) {
        false
    }
}
