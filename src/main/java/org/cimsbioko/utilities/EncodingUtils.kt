package org.cimsbioko.utilities

object EncodingUtils {

    private val hexArray = "0123456789abcdef".toCharArray()

    /**
     * Converts the given byte array to its hexadecimal equivalent.
     *
     * @param bytes the array to convert
     * @return the array contents encoded as a base-16 string
     */
    @JvmStatic
    fun toHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v: Int = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }
}