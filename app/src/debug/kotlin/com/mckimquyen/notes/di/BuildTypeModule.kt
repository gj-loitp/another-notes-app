

package com.mckimquyen.notes.di

import com.mckimquyen.notes.ui.home.BuildTypeBehavior
import com.mckimquyen.notes.ui.home.DebugBuildTypeBehavior
import dagger.Binds
import dagger.Module

@Module
abstract class BuildTypeModule {

    @get:Binds
    abstract val DebugBuildTypeBehavior.bind: BuildTypeBehavior
}
