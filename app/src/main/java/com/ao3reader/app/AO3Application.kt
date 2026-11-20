package com.ao3reader.app

import android.app.Application
import com.ao3reader.app.data.repository.PreferenceRepository
import com.ao3reader.app.data.repository.WorkRepository

class AO3Application : Application() {

    lateinit var preferenceRepository: PreferenceRepository
        private set

    lateinit var workRepository: WorkRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferenceRepository = PreferenceRepository(this)
        workRepository = WorkRepository(this)
    }

    companion object {
        lateinit var instance: AO3Application
            private set
    }
}
