package com.roshan.sandworks

import android.app.Application
import com.roshan.sandworks.data.local.SandWorksDatabase
import com.roshan.sandworks.data.repository.SandWorksRepository

class SandWorksApp : Application() {

    lateinit var database: SandWorksDatabase
        private set

    lateinit var repository: SandWorksRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = SandWorksDatabase.getInstance(this)
        repository = SandWorksRepository(database)
    }
}
