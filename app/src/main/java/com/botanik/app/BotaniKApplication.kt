package com.botanik.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dagger.hilt.android.HiltAndroidApp

/**
 * Application de Botani-k.
 *
 * Implementa [ImageLoaderFactory] para que Coil use el [ImageLoader] definido
 * aquí en lugar de crear el suyo por defecto. Esto permite registrar el
 * decodificador SVG, necesario para mostrar los placeholders del catálogo
 * (que `placehold.co` sirve como SVG) y cualquier otro recurso vectorial.
 */
@HiltAndroidApp
class BotaniKApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .crossfade(true)
            .build()
}
