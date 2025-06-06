package com.example.skypeek.utils

import java.util.concurrent.ConcurrentLinkedQueue

interface PoolableObject {
    fun reset()
}

class ObjectPool<T : PoolableObject>(
    private val factory: () -> T,
    private val maxSize: Int = 50
) {
    private val pool = ConcurrentLinkedQueue<T>()

    fun acquire(): T {
        return pool.poll() ?: factory()
    }

    fun release(obj: T) {
        if (pool.size < maxSize) {
            obj.reset()
            pool.offer(obj)
        }
    }

    fun clear() {
        pool.clear()
    }
}

data class PooledSnowflake(
    var x: Float = 0f,
    var y: Float = 0f,
    var speed: Float = 0f,
    var size: Float = 0f
) : PoolableObject {
    override fun reset() {
        x = 0f
        y = 0f
        speed = 0f
        size = 0f
    }
}

data class PooledRaindrop(
    var x: Float = 0f,
    var y: Float = 0f,
    var speed: Float = 0f,
    var length: Float = 0f
) : PoolableObject {
    override fun reset() {
        x = 0f
        y = 0f
        speed = 0f
        length = 0f
    }
}

data class PooledOffset(
    var x: Float = 0f,
    var y: Float = 0f
) : PoolableObject {
    override fun reset() {
        x = 0f
        y = 0f
    }
}

object AnimationPools {
    val snowflakePool = ObjectPool({ PooledSnowflake() }, maxSize = 30)
    val raindropPool = ObjectPool({ PooledRaindrop() }, maxSize = 50)
    val offsetPool = ObjectPool({ PooledOffset() }, maxSize = 100)
    
    fun clearAll() {
        snowflakePool.clear()
        raindropPool.clear()
        offsetPool.clear()
    }
}