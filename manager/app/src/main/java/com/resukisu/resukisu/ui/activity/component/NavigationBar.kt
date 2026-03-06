package com.resukisu.resukisu.ui.activity.component

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailColors
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.resukisu.resukisu.ui.MainActivity
import com.resukisu.resukisu.ui.component.LiquidGlassNavBar
import com.resukisu.resukisu.ui.screen.BottomBarDestination
import com.resukisu.resukisu.ui.theme.ThemeConfig
import com.resukisu.resukisu.ui.util.LocalHandlePageChange
import com.resukisu.resukisu.ui.util.LocalSelectedPage
import com.resukisu.resukisu.ui.util.LocalWallpaperBackdrop
import com.resukisu.resukisu.ui.util.getKpmModuleCount
import com.resukisu.resukisu.ui.util.getModuleCount
import com.resukisu.resukisu.ui.util.getSuperuserCount
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavigationBar(
    destinations: List<BottomBarDestination>,
    hazeState: HazeState?,
    isBottomBar: Boolean
) {
    val activity = LocalContext.current as MainActivity

    val isHideOtherInfo by activity.settingsStateFlow
        .map { it.isHideOtherInfo }
        .collectAsState(initial = false)

    val page = LocalSelectedPage.current
    val handlePageChange = LocalHandlePageChange.current

    var superuserCountSaved by rememberSaveable { mutableIntStateOf(0) }
    var moduleCountSaved by rememberSaveable { mutableIntStateOf(0) }
    var kpmModuleCountSaved by rememberSaveable { mutableIntStateOf(0) }

    val superuserCount by produceState(initialValue = superuserCountSaved) {
        withContext(Dispatchers.IO) {
            value = getSuperuserCount()
            superuserCountSaved = value
        }
    }
    val moduleCount by produceState(initialValue = moduleCountSaved) {
        withContext(Dispatchers.IO) {
            value = getModuleCount()
            moduleCountSaved = value
        }
    }
    val kpmModuleCount by produceState(initialValue = kpmModuleCountSaved) {
        withContext(Dispatchers.IO) {
            value = getKpmModuleCount()
            kpmModuleCountSaved = value
        }
    }

    val modifier = Modifier.windowInsetsPadding(
        WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
    )

    if (isBottomBar) {
        if (ThemeConfig.backgroundImageLoaded) {
            // Liquid glass navigation bar
            val backdrop = LocalWallpaperBackdrop.current
            LiquidGlassNavBar(
                selectedIndex = page,
                destinations = destinations,
                backdrop = backdrop,
                onTabSelected = handlePageChange,
                superuserCount = superuserCount,
                moduleCount = moduleCount,
                kpmModuleCount = kpmModuleCount,
                isHideOtherInfo = isHideOtherInfo,
                modifier = modifier
            )
        } else {
            val hazeStyle = HazeStyle(
                backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f),
                tint = HazeTint(Color.Transparent)
            )
            val hazeModifier = if (hazeState != null) {
                modifier.hazeEffect(hazeState) {
                    style = hazeStyle
                    blurRadius = 30.dp
                    noiseFactor = 0f
                }
            } else modifier

            FlexibleBottomAppBar(
                modifier = hazeModifier,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                destinations.forEachIndexed { index, destination ->
                    BottomBarNavigationItem(
                        isSelected = index == page,
                        destination = destination,
                        onClick = { handlePageChange(index) },
                        kpmModuleCount = kpmModuleCount,
                        superuserCount = superuserCount,
                        moduleCount = moduleCount,
                        isHideOtherInfo = isHideOtherInfo,
                    )
                }
            }
        }
    } else {
        WideNavigationRail(
            modifier = modifier,
            colors = WideNavigationRailColors(
                containerColor = if (ThemeConfig.backgroundImageLoaded) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modalContainerColor = WideNavigationRailDefaults.colors().modalContainerColor,
                modalScrimColor = WideNavigationRailDefaults.colors().modalScrimColor,
                modalContentColor = WideNavigationRailDefaults.colors().modalContentColor,
            ),
        ) {
            destinations.forEachIndexed { index, destination ->
                NavigationRailItem(
                    isSelected = index == page,
                    destination = destination,
                    onClick = { handlePageChange(index) },
                    kpmModuleCount = kpmModuleCount,
                    superuserCount = superuserCount,
                    moduleCount = moduleCount,
                    isHideOtherInfo = isHideOtherInfo,
                )
            }
        }
    }
}

@Composable
private fun NavigationRailItem(
    isSelected: Boolean,
    destination: BottomBarDestination,
    onClick: () -> Unit,
    kpmModuleCount: Int,
    superuserCount: Int,
    moduleCount: Int,
    isHideOtherInfo: Boolean
) {
    WideNavigationRailItem(
        railExpanded = false,
        selected = isSelected,
        onClick = onClick,
        icon = {
            BadgedBox(
                badge = {
                    DestinationBadge(
                        dest = destination,
                        superUser = superuserCount,
                        module = moduleCount,
                        kpm = kpmModuleCount,
                        isHideOtherInfo = isHideOtherInfo,
                    )
                }
            ) {
                if (isSelected) {
                    Icon(destination.iconSelected, stringResource(destination.label))
                } else {
                    Icon(destination.iconNotSelected, stringResource(destination.label))
                }
            }
        },
        label = {
            Text(
                stringResource(destination.label),
                style = MaterialTheme.typography.labelMedium
            )
        },
    )
}

@Composable
private fun RowScope.BottomBarNavigationItem(
    isSelected: Boolean,
    destination: BottomBarDestination,
    onClick: () -> Unit,
    kpmModuleCount: Int,
    superuserCount: Int,
    moduleCount: Int,
    isHideOtherInfo: Boolean
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            BadgedBox(
                badge = {
                    DestinationBadge(
                        dest = destination,
                        superUser = superuserCount,
                        module = moduleCount,
                        kpm = kpmModuleCount,
                        isHideOtherInfo = isHideOtherInfo,
                    )
                }
            ) {
                if (isSelected) {
                    Icon(destination.iconSelected, stringResource(destination.label))
                } else {
                    Icon(destination.iconNotSelected, stringResource(destination.label))
                }
            }
        },
        label = {
            Text(
                stringResource(destination.label),
                style = MaterialTheme.typography.labelMedium
            )
        },
        alwaysShowLabel = false
    )
}

@Composable
private fun DestinationBadge(
    dest: BottomBarDestination,
    superUser: Int,
    module: Int,
    kpm: Int,
    isHideOtherInfo: Boolean,
) {
    val count = when (dest) {
        BottomBarDestination.Kpm -> kpm
        BottomBarDestination.SuperUser -> superUser
        BottomBarDestination.Module -> module
        else -> 0
    }

    AnimatedVisibility(
        visible = count > 0 && !isHideOtherInfo,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Badge(
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(count.toString())
        }
    }
}