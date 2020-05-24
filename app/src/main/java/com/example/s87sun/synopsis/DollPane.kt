package com.example.s87sun.synopsis

import android.content.Context
import android.graphics.*
import android.util.Range
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.minus
import androidx.core.graphics.plus
import kotlin.collections.HashMap
import kotlin.math.*


class DollPane(ctx: Context) : View(ctx) {
    private lateinit var torso: BodyPart
    private var touchedBodyPart: BodyPart? = null
    // Map pointer ID to touched points
    private val pointerPos: MutableMap<Int, PointF> = HashMap()
    private var lastPosition: PointF? = null
    private var lastRotation: Float? = null
    private var lastScale: PointF? = null

    init {
        reset()
    }

    private fun arm(side: Float) =
        // upper arm
        BodyPart().apply {
            positon = PointF(85f * side, -80f)
            scale = PointF(-side, 1f)
            rotationRange = Range(-PI.toFloat(), PI.toFloat())
            children = listOf(
                // lower arm
                BodyPart().apply {
                    positon = PointF(0f, 110f)
                    rotationRange = Range(-135f / 180f * PI.toFloat(), 135f / 180f * PI.toFloat())
                    children = listOf(
                        // model
                        BodyPart().apply {
                            image = BitmapFactory.decodeResource(resources, R.drawable.lower_arm)
                            positon = PointF(0f, 55f)
                            scale = PointF(50f, 130f)
                        },
                        // hand
                        BodyPart().apply {
                            positon = PointF(0f, 110f)
                            rotationRange = Range(-35f / 180f * PI.toFloat(), 35f / 180f * PI.toFloat())
                            children = listOf(
                                // model
                                BodyPart().apply {
                                    image = BitmapFactory.decodeResource(resources, R.drawable.hand)
                                    positon = PointF(0f, 15f)
                                    scale = PointF(60f, 70f)
                                }
                            )
                        }
                    )
                },
                // model
                BodyPart().apply {
                    image = BitmapFactory.decodeResource(resources, R.drawable.upper_arm)
                    positon = PointF(0f, 45f)
                    scale = PointF(60f, 150f)
                }
            )
        }

    private fun leg(side: Float) =
        // upper leg
        BodyPart().apply {
            positon = PointF(40f * side, 110f)
            scale = PointF(-side, 1f)
            rotationRange = Range(-90f / 180f * PI.toFloat(), 90f / 180f * PI.toFloat())
            scaleRangeY = Range(0.75f, 1.5f)
            twoFingerInteraction = TwoFingerInteraction.SCALE
            children = listOf(
                // lower leg
                BodyPart().apply {
                    positon = PointF(0f, 160f)
                    rotationRange = Range(-90f / 180f * PI.toFloat(), 90f / 180f * PI.toFloat())
                    scaleRangeY = Range(0.75f, 1.5f)
                    twoFingerInteraction = TwoFingerInteraction.SCALE
                    children = listOf(
                        // model
                        BodyPart().apply {
                            image = BitmapFactory.decodeResource(resources, R.drawable.lower_leg)
                            positon = PointF(0f, 75f)
                            scale = PointF(60f, 200f)
                        },
                        // foot
                        BodyPart().apply {
                            positon = PointF(0f, 150f)
                            rotationRange = Range(-35f / 180f * PI.toFloat(), 35f / 180f * PI.toFloat())
                            children = listOf(
                                // model
                                BodyPart().apply {
                                    image = BitmapFactory.decodeResource(resources, R.drawable.foot)
                                    positon = PointF(-30f, 30f)
                                    scale = PointF(120f, 80f)
                                    resetScale = true
                                }
                            )
                        }
                    )
                },
                // model
                BodyPart().apply {
                    image = BitmapFactory.decodeResource(resources, R.drawable.upper_leg)
                    positon = PointF(0f, 70f)
                    scale = PointF(70f, 200f)
                }
            )
        }

    fun reset() {
        torso = BodyPart().apply {
            positon = PointF(900f, 600f)
            oneFingerInteraction = OneFingerInteraction.TRANSLATE
            children = listOf(
                // left leg
                leg(-1f),
                // right leg
                leg(1f),
                // model
                BodyPart().apply {
                    image = BitmapFactory.decodeResource(resources, R.drawable.body)
                    scale = PointF(200f, 250f)
                },
                // head
                BodyPart().apply {
                    positon = PointF(0f, -110f)
                    rotationRange = Range(-50f / 180f * PI.toFloat(), 50f / 180f * PI.toFloat())
                    children = listOf(
                        // model
                        BodyPart().apply {
                            image = BitmapFactory.decodeResource(resources, R.drawable.face)
                            positon = PointF(0f, -50f)
                            scale = PointF(90f, 130f)
                        }
                    )
                },
                // left arm
                arm(-1f),
                // right arm
                arm(1f)
            )
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        torso.draw(canvas!!)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        fun select(point: PointF) {
            var part = torso.hitTest(point) ?: return
            while (!part.interactive) {
                part = part.parent!!
            }
            touchedBodyPart = part
            lastPosition = part.positon
            lastRotation = part.rotation
            lastScale = part.scale
        }

        event!!
        val newGlo = PointF(event.getX(event.actionIndex), event.getY(event.actionIndex))
        val pointerId = event.getPointerId(event.actionIndex)
        when (event.actionMasked) {
            // first finger down
            MotionEvent.ACTION_DOWN -> {
                pointerPos[pointerId] = newGlo
                select(newGlo)
            }
            // second to N finger down
            MotionEvent.ACTION_POINTER_DOWN -> {
                pointerPos[pointerId] = newGlo
                if (pointerPos.size != 2)
                    return true
                val it = pointerPos.values.iterator()
                val a = it.next()
                val b = it.next()
                // mid point of two finger
                select(PointF((a.x + b.x) / 2, (a.y + b.y) / 2))
            }
            MotionEvent.ACTION_MOVE -> {
                val oldGlo = pointerPos[pointerId]!!
                pointerPos[pointerId] = newGlo

                val part = touchedBodyPart ?: return true

                // invert the transformation for the touched part
                val inverse = part.absTransform.inverse()
                val oldLoc = inverse.dotPoint(oldGlo)
                val newLoc = inverse.dotPoint(newGlo)

                when (pointerPos.size) {
                    // 1 finger handles translate/rotate
                    1 -> when (part.oneFingerInteraction) {
                        OneFingerInteraction.TRANSLATE -> {
                            val position = lastPosition!! + newLoc - oldLoc
                            part.positon = position
                            lastPosition = position
                        }
                        OneFingerInteraction.ROTATE -> {
                            // a, b are the original and current position for 1 finger
                            val a = oldLoc - part.positon
                            val b = newLoc - part.positon
                            // calculate the angle formed by a~origin and b~origin
                            val rotation = lastRotation!! + a.angleBetween(b)
                            part.rotation = rotation
                            lastRotation = rotation
                        }
                    }
                    2 -> when (part.twoFingerInteraction) {
                        TwoFingerInteraction.NONE -> {}
                        TwoFingerInteraction.SCALE -> {
                            // currently stable finger position
                            val altPair = pointerPos.asIterable().first { it.key != pointerId }
                            val altIdx = event.findPointerIndex(altPair.key)
                            // get x,y position of the new and old point using altindex
                            val altNewGlo = PointF(event.getX(altIdx), event.getY(altIdx))
                            val altOldGlo = altPair.value
                            val altNewLoc = inverse.dotPoint(altNewGlo)
                            val altOldLoc = inverse.dotPoint(altOldGlo)

                            println("oldloc"+altOldLoc)
                            println("newloc"+altNewLoc)
                            val axis = PointF(-sin(part.rotation), cos(part.rotation))
                            // a, b are previous and current projection to y-axis of the touched part
                            val a = (oldLoc - altOldLoc).dot(axis)
                            val b = (newLoc - altNewLoc).dot(axis)
                            val scale = PointF(lastScale!!.x, lastScale!!.y * b / a)
                            part.scale = scale
                            lastScale = scale
                        }
                    }
                }

                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                touchedBodyPart = null
                lastRotation = null
                lastScale = null
                pointerPos.remove(pointerId)
            }
        }
        return true
    }

}

