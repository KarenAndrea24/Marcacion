package com.example.marcacion.customview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView

class SafePreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var previewView: PreviewView? = null
        private set

    init {
        if (!isInEditMode) {
            previewView = PreviewView(context, attrs).also { pv ->
                pv.layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
                addView(pv)
            }
        }
    }

    // Exponer la propiedad surfaceProvider delegada al PreviewView interno
    val surfaceProvider: Preview.SurfaceProvider?
        get() = previewView?.surfaceProvider

}
