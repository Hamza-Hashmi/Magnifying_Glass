package com.example.magnifyingglass.magnifier.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.magnifyingglass.magnifier.R
import com.google.gson.Gson

class RemoteConfigViewModel(application: Application, private val repository: RemoteConfigRepository) :
    AndroidViewModel(application) {
    private val TAG = "RemoteViewModel"
    var remoteConfig: MutableLiveData<RemoteConfig> = MutableLiveData()

    fun getRemoteConfigSplash(context: Activity) {
        repository.getFirebaseRemoteConfig().fetchAndActivate()
            .addOnCompleteListener(context) { task ->
                if (task.isSuccessful) {
                    remoteConfig.value = Gson().fromJson(repository.getFirebaseRemoteConfig()
                        .getString(context.getString(
                            R.string.remoteTopic)), RemoteConfig::class.java)

               }
            }
            .addOnFailureListener {

            }
    }

    fun getRemoteConfig(context: Context): RemoteConfig? {
        return Gson().fromJson(repository.getFirebaseRemoteConfig()
            .getString(context.getString(R.string.remoteTopic)), RemoteConfig::class.java)
    }
}