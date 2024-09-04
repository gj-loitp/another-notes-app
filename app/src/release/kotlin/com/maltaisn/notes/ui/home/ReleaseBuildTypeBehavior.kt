

package com.mckimquyen.notes.ui.home

import javax.inject.Inject

class ReleaseBuildTypeBehavior @Inject constructor() : BuildTypeBehavior {

    override suspend fun doExtraAction(viewModel: HomeViewModel) = Unit
}
