package com.example.s87sun.synopsis

import android.graphics.*
import android.util.Range
import kotlin.math.PI
import kotlin.math.abs

enum class OneFingerInteraction {
    TRANSLATE,
    ROTATE,
}

enum class TwoFingerInteraction {
    SCALE,
    NONE,
}

class BodyPart {
    var positon: PointF = PointF()
    var scale: PointF = PointF(1f, 1f)
        set(value) {
            field = PointF(value.x, scaleRangeY.clamp(value.y))
        }
    var scaleRangeY: Range<Float> = Range(0f, Float.POSITIVE_INFINITY)
    var rotation: Float = 0f
        set(value) {
            val new = rotationRange.clamp(value)
            if (abs(new - field) < PI)
                field = new
        }
    var rotationRange: Range<Float> = Range(0f, 0f)
    var image: Bitmap? = null
    var oneFingerInteraction: OneFingerInteraction = OneFingerInteraction.ROTATE
    var twoFingerInteraction: TwoFingerInteraction = TwoFingerInteraction.NONE
    var children: List<BodyPart> = listOf()
        set(value) {
            for (child in children) {
                child.parent = null
            }
            field = value
            for (child in children) {
                assert(child.parent != null)
                child.parent = this
            }
        }

    var parent: BodyPart? = null
        private set

    val interactive: Boolean
        get() = image == null

    val hitTestVisible: Boolean
        get() = image != null

    var resetScale: Boolean = false

    companion object {
        val BOUND: RectF = RectF(-.5f, -.5f, .5f, .5f)
    }

    val transform: Matrix
        get() {
            var scaleY = scale.y
            if (resetScale) {
                var part = parent
                while (part != null) {
                    scaleY /= part.scale.y
                    part = part.parent
                }
            }
            val trans = Matrix()
            trans.preTranslate(positon.x, positon.y)
            trans.preRotate(rotation * 180f / PI.toFloat())
            trans.preScale(scale.x, scaleY)
            return trans
        }

    // find which BodyPart is touched
    fun hitTest(point: PointF) : BodyPart? {
        val p = transform.inverse().dotPoint(point)
        return children.reversed().mapNotNull { it.hitTest(p) }.firstOrNull() ?:
            if (hitTestVisible && BOUND.contains(p.x, p.y)) this else null
    }

    // absolute transformation including torso transformation
    val absTransform: Matrix
        get() {
            val trans = Matrix()
            var part: BodyPart = this
            while (true) {
                val parent = part.parent ?: break
                trans.postConcat(parent.transform)
                part = parent
            }
            return trans
        }

    // default parentTrans for torso is the identity matrix
    fun draw(canvas: Canvas, parentTrans: Matrix = Matrix()) {
        val trans = Matrix(parentTrans)
        trans.preConcat(transform)
        if (image != null) {
            val paint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 0.02f
            }
            canvas.setMatrix(trans)
            canvas.drawBitmap(image!!, null, BOUND, paint)
        }
        for (child in children) {
            child.draw(canvas, trans)
        }
    }
}

