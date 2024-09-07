package com.maltaisn.notes.ui.home

import com.mckimquyen.notes.ui.home.BuildTypeBehavior
import com.mckimquyen.notes.ui.home.HomeVM
import javax.inject.Inject

class ReleaseBuildTypeBehavior @Inject constructor() : BuildTypeBehavior {

    override suspend fun doExtraAction(viewModel: HomeVM) = Unit
}
