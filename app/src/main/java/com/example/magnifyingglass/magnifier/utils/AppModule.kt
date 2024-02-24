
package com.example.magnifyingglass.magnifier.utils

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object AppModule {

    val getModule = module {

        single { RemoteConfigClient().init(get()) }
        single { RemoteConfigRepository(get()) }
        viewModel {
            RemoteConfigViewModel(get(), get())
        }

    }
}