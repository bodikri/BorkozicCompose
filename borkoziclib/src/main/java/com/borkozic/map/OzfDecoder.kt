package com.borkozic.map

import java.io.File

class OzfDecoder {

    companion object {
        init {
            System.loadLibrary("ozfdecoder")
        }
    }

    private var nativePointer: Long = 0

    external fun openImageNative(path: String): Long
    external fun closeImageNative(ptr: Long)
    external fun getTileNative(
        ptr: Long,
        type: Int,
        key: Int,
        depth: Int,
        offset: Int,
        index: Int,
        width: Int,
        height: Int,
        palette: ByteArray
    ): IntArray?

    fun openImage(file: File): Boolean {
        nativePointer = openImageNative(file.absolutePath)
        return nativePointer != 0L
    }

    fun closeImage() {
        if (nativePointer != 0L) {
            closeImageNative(nativePointer)
            nativePointer = 0
        }
    }

    fun getTile(
        type: Int,
        key: Int,
        depth: Int,
        offset: Int,
        index: Int,
        width: Int,
        height: Int,
        palette: ByteArray
    ): IntArray? {
        if (nativePointer == 0L) return null
        return getTileNative(nativePointer, type, key, depth, offset, index, width, height, palette)
    }
}