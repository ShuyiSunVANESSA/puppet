package com.example.s87sun.synopsis

import android.graphics.Matrix
import android.graphics.PointF
import kotlin.math.abs
import kotlin.math.atan2

fun Matrix.dotPoint(p: PointF): PointF {
    val pts = floatArrayOf(p.x, p.y)
    mapPoints(pts)
    return PointF(pts[0], pts[1])
}

fun Matrix.dotVector(p: PointF): PointF {
    val pts = floatArrayOf(p.x, p.y)
    mapVectors(pts)
    return PointF(pts[0], pts[1])
}

fun Matrix.inverse(): Matrix {
    val inverse = Matrix()
    invert(inverse)
    return inverse
}

fun PointF.angleBetween(p: PointF): Float =
    atan2(x * p.y - y * p.x, x * p.x + y * p.y)

fun PointF.dot(p: PointF): Float =
    x * p.x + y * p.y

fun PointF.norm(): PointF {
    val len = length()
    return PointF(x / len, y / len)
}

fun PointF.project(p: PointF): Float =
    dot(p.norm())

fun PointF.abs(): PointF =
    PointF(abs(x), abs(y))
