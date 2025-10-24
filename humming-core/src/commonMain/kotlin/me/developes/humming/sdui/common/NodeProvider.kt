package me.developes.humming.sdui.common

typealias NodeProvider = suspend (MutableMap<String, String?>) -> ServerDrivenNode