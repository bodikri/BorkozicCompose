package com.borkozic.library.map.online

import androidx.collection.LruCache

/**
 * Кеш за тайлове в паметта с фиксиран капацитет и LRU политика.
 * @param capacity максимален брой тайлове, които могат да се съхраняват.
 */
@Suppress("unused")
class TileCache(private val capacity: Int) {
    // Използваме LruCache от AndroidX, който е thread-safe и оптимизиран
    private val lruCache = object : LruCache<Long, Tile>(capacity) {
        override fun sizeOf(key: Long, value: Tile): Int {
            // Приблизителен размер на тайла (може да се подобри)
            return if (value.bitmap != null) 1 else 0
        }

        override fun entryRemoved(evicted: Boolean, key: Long, oldValue: Tile, newValue: Tile?) {
            // При изваждане от кеша, рециклираме Bitmap, за да освободим памет
            oldValue.bitmap?.recycle()
            oldValue.bitmap = null
        }
    }

    /**
     * Добавя тайл в кеша.
     * @param tile тайл за добавяне
     */
    fun put(tile: Tile) {
        lruCache.put(tile.key, tile)
    }

    /**
     * Връща тайл по ключ, ако съществува.
     * @param key ключ (обикновено от Tile.getKey())
     * @return тайлът или null
     */
    fun get(key: Long): Tile? = lruCache[key]

    /**
     * Проверява дали ключ съществува в кеша.
     */
    fun containsKey(key: Long): Boolean = lruCache[key] != null

    /**
     * Изчиства целия кеш и рециклира всички битмапи.
     */
    fun clear() {
        lruCache.evictAll()
    }

    /**
     * Унищожава кеша (извиква clear).
     */
    fun destroy() {
        clear()
    }
}