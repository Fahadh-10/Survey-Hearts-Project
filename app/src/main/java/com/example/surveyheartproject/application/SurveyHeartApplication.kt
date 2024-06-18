package com.example.surveyheartproject.application

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class SurveyHeartApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build())
    }
}