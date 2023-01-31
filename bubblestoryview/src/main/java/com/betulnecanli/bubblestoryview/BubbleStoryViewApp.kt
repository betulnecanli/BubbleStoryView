package com.betulnecanli.bubblestoryview

import android.app.Application
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache

class BubbleStoryViewApp : Application() {
    // This class extends the Application class in Android

    override fun onCreate() {
        super.onCreate()

        // Create an instance of LeastRecentlyUsedCacheEvictor with a max cache size of 90 MB
        val leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(90 * 1024 * 1024)

        // Create an instance of ExoDatabaseProvider
        val databaseProvider: DatabaseProvider = ExoDatabaseProvider(this)

        // Check if the simpleCache object is null, and if so, create a new instance of SimpleCache
        if (simpleCache == null) {
            simpleCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, databaseProvider)
        }
    }

    companion object {
        // A static field to hold an instance of SimpleCache
        var simpleCache: SimpleCache? = null
    }
}