package com.download.maps.features.regions.ui

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.download.maps.features.regions.ui.components.RegionsScreenContent
import com.download.maps.features.regions.ui.components.RegionsScreenError
import com.download.maps.features.regions.ui.components.RegionsScreenLoading
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun RegionsList(
    parentRegionId: String,
    navigate: (parentRegionId: String, parentRegionName: String) -> Unit,
    modifier: Modifier = Modifier,
    listHeader: (@Composable LazyItemScope.() -> Unit)? = null
) {
    val viewModel: RegionsListViewModel =
        hiltViewModel<RegionsListViewModel, RegionsListViewModel.Factory> {
            it.create(parentRegionId)
        }
    val state by viewModel.collectAsState()

    when (val currentState = state) {
        is RegionsListViewState.Content -> RegionsScreenContent(
            modifier = modifier,
            content = currentState,
            download = viewModel::download,
            cancel = viewModel::cancelDownload,
            navigate = navigate,
            listHeader = listHeader
        )

        RegionsListViewState.Error -> RegionsScreenError(
            modifier = modifier,
            errorMessage = stringResource(com.download.maps.common.R.string.unknown_error),
            onReload = viewModel::reload
        )

        RegionsListViewState.Loading -> RegionsScreenLoading(modifier = modifier)
    }
}
