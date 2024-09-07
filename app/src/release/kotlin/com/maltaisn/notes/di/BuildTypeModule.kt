

package com.mckimquyen.notes.di

import com.mckimquyen.notes.ui.home.BuildTypeBehavior
import com.maltaisn.notes.ui.home.ReleaseBuildTypeBehavior
import dagger.Binds
import dagger.Module

@Module
abstract class BuildTypeModule {

    @get:Binds
    abstract val ReleaseBuildTypeBehavior.bind: BuildTypeBehavior
}
