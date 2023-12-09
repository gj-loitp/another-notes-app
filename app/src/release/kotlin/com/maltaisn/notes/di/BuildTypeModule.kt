

package com.roy93group.notes.di

import com.roy93group.notes.ui.home.BuildTypeBehavior
import com.roy93group.notes.ui.home.ReleaseBuildTypeBehavior
import dagger.Binds
import dagger.Module

@Module
abstract class BuildTypeModule {

    @get:Binds
    abstract val ReleaseBuildTypeBehavior.bind: BuildTypeBehavior
}
