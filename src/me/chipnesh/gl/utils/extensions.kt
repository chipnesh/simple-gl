@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.chipnesh.gl.utils

import io.ktor.application.Application

fun Application.getProperty(name: String) = environment.config
    .propertyOrNull(name)
    ?.getString()
    ?.toLowerCase()
    ?.trim()
