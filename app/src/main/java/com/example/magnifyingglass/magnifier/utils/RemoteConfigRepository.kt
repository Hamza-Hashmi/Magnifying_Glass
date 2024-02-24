package com.example.magnifyingglass.magnifier.utils

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class RemoteConfigRepository(private val remoteData: FirebaseRemoteConfig) {
    fun getFirebaseRemoteConfig(): FirebaseRemoteConfig = remoteData
}