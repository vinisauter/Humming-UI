package me.developes.humming.sdui.container

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.developes.humming.sdui.common.ComposableContent
import me.developes.humming.sdui.common.SDLibrary
import me.developes.humming.sdui.common.fromNode

class SDContainer : SDLibrary("container") {
    init {
        addComponent("column") { node, vm ->
            val modifier = Modifier.fromNode(node)
            val padding = (node.property("padding")?.toIntOrNull() ?: 0).dp
            val paddingStart = (node.property("paddingStart")?.toIntOrNull()?.dp) ?: padding
            val paddingTop = (node.property("paddingTop")?.toIntOrNull()?.dp) ?: padding
            val paddingEnd = (node.property("paddingEnd")?.toIntOrNull()?.dp) ?: padding
            val paddingBottom = (node.property("paddingBottom")?.toIntOrNull()?.dp) ?: padding
            val reverseLayout = node.property("reverseLayout")?.toBooleanStrictOrNull() ?: false
            val userScrollEnabled =
                node.property("userScrollEnabled")?.toBooleanStrictOrNull() ?: true
            object : ComposableContent {
                @Composable
                override fun Content() {
                    LazyColumn(
                        modifier = modifier,
                        contentPadding = PaddingValues(
                            start = paddingStart,
                            top = paddingTop,
                            end = paddingEnd,
                            bottom = paddingBottom
                        ),
                        reverseLayout = reverseLayout,
                        userScrollEnabled = userScrollEnabled,
                    ) {
                        node.children?.let { childrenNodes ->
                            for (childNode in childrenNodes) {
                                item {
                                    vm.loadComponent(node = childNode)?.Content()
                                }
                            }
                        }
                    }
                }
            }
        }
        addComponent("row") { node, vm ->
            val modifier = Modifier.fromNode(node)
            val padding = (node.property("padding")?.toIntOrNull() ?: 0).dp
            val paddingStart = (node.property("paddingStart")?.toIntOrNull()?.dp) ?: padding
            val paddingTop = (node.property("paddingTop")?.toIntOrNull()?.dp) ?: padding
            val paddingEnd = (node.property("paddingEnd")?.toIntOrNull()?.dp) ?: padding
            val paddingBottom = (node.property("paddingBottom")?.toIntOrNull()?.dp) ?: padding
            val reverseLayout = node.property("reverseLayout")?.toBooleanStrictOrNull() ?: false
            val userScrollEnabled =
                node.property("userScrollEnabled")?.toBooleanStrictOrNull() ?: true
            object : ComposableContent {
                @Composable
                override fun Content() {
                    LazyRow(
                        modifier = modifier,
                        contentPadding = PaddingValues(
                            start = paddingStart,
                            top = paddingTop,
                            end = paddingEnd,
                            bottom = paddingBottom
                        ),
                        reverseLayout = reverseLayout,
                        userScrollEnabled = userScrollEnabled,
                    ) {
                        node.children?.let { childrenNodes ->
                            for (childNode in childrenNodes) {
                                item {
                                    vm.loadComponent(node = childNode)?.Content()
                                }
                            }
                        }
                    }
                }
            }
        }
        addComponent("grid") { node, vm ->
            val modifier = Modifier.fromNode(node)
            val columns = node.property("columns")?.toIntOrNull() ?: 3
            val vertical = node.property("vertical")?.toBooleanStrictOrNull() ?: true
            val padding = (node.property("padding")?.toIntOrNull() ?: 0).dp
            val paddingStart = (node.property("paddingStart")?.toIntOrNull()?.dp) ?: padding
            val paddingTop = (node.property("paddingTop")?.toIntOrNull()?.dp) ?: padding
            val paddingEnd = (node.property("paddingEnd")?.toIntOrNull()?.dp) ?: padding
            val paddingBottom = (node.property("paddingBottom")?.toIntOrNull()?.dp) ?: padding
            val reverseLayout = node.property("reverseLayout")?.toBooleanStrictOrNull() ?: false
            val userScrollEnabled =
                node.property("userScrollEnabled")?.toBooleanStrictOrNull() ?: false
            object : ComposableContent {
                @Composable
                override fun Content() {
                    if (vertical) {
                        LazyVerticalGrid(
                            modifier = modifier,
                            columns = GridCells.Fixed(columns),
                            contentPadding = PaddingValues(
                                start = paddingStart,
                                top = paddingTop,
                                end = paddingEnd,
                                bottom = paddingBottom
                            ),
                            reverseLayout = reverseLayout,
                            userScrollEnabled = userScrollEnabled
                        ) {
                            node.children?.let { childrenNodes ->
                                for (childNode in childrenNodes) {
                                    val span =
                                        childNode.property("span")?.toIntOrNull() ?: 1
                                    item(span = { GridItemSpan(span) }) {
                                        vm.loadComponent(node = childNode)?.Content()
                                    }
                                }
                            }
                        }
                    } else {
                        LazyHorizontalGrid(
                            modifier = modifier,
                            rows = GridCells.Fixed(columns),
                            contentPadding = PaddingValues(
                                start = paddingStart,
                                top = paddingTop,
                                end = paddingEnd,
                                bottom = paddingBottom
                            ),
                            reverseLayout = reverseLayout,
                            userScrollEnabled = userScrollEnabled
                        ) {
                            node.children?.let { childrenNodes ->
                                for (childNode in childrenNodes) {
                                    val span = childNode.property("span")?.toIntOrNull() ?: 1
                                    item(span = { GridItemSpan(span) }) {
                                        vm.loadComponent(node = childNode)?.Content()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        addComponent("box") { node, vm ->
            val modifier = Modifier.fromNode(node)
            val contentAlignment: Alignment =
                when (node.property("contentAlignment")) {
                    null -> Alignment.TopStart
                    "TopStart" -> Alignment.TopStart
                    "TopCenter" -> Alignment.TopCenter
                    "TopEnd" -> Alignment.TopEnd
                    "CenterStart" -> Alignment.CenterStart
                    "Center" -> Alignment.Center
                    "CenterEnd" -> Alignment.CenterEnd
                    "BottomStart" -> Alignment.BottomStart
                    "BottomCenter" -> Alignment.BottomCenter
                    "BottomEnd" -> Alignment.BottomEnd
                    else -> error("Unknown value for horizontalAlignment ${node.property("horizontalAlignment")}")
                }

            val propagateMinConstraints: Boolean =
                node.property("propagateMinConstraints")?.toBooleanStrictOrNull() ?: false
            object : ComposableContent {
                @Composable
                override fun Content() {
                    Box(
                        modifier = modifier,
                        contentAlignment = contentAlignment,
                        propagateMinConstraints = propagateMinConstraints
                    ) {
                        node.children?.let { childrenNodes ->
                            for (childNode in childrenNodes) {
                                vm.loadComponent(node = childNode)?.Content()
                            }
                        }
                    }
                }
            }
        }
    }
}