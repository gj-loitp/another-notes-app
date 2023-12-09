

package com.roy93group.notes.di

import com.roy93group.notes.ui.home.BuildTypeBehavior
import com.roy93group.notes.ui.home.DebugBuildTypeBehavior
import dagger.Binds
import dagger.Module

@Module
abstract class BuildTypeModule {

    @get:Binds
    abstract val DebugBuildTypeBehavior.bind: BuildTypeBehavior
}
