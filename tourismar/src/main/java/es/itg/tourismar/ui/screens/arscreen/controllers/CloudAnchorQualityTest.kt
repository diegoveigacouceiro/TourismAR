package es.itg.tourismar.ui.screens.arscreen.controllers
import com.google.ar.core.Pose
import com.google.ar.core.Session
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.material.setColor
import io.github.sceneview.node.CylinderNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class QualityChecker(
    private val anyQualityThresholdReached: () -> Unit,
    private val bothQualityThresholdsReached: () -> Unit,
    private val anyQualityThresholdCooldownMillis: Long = 30000
) {
    private var anyQualityReached = false
    private var lastQualityThresholdReachedTime: Long = 0

    private val TOLERANCE = 0.1f
    var framesInQuadrant1 = 0
    var framesInQuadrant2 = 0
    var framesInQuadrant3 = 0
    var framesInQuadrant4 = 0

    private var running = true
    private val bucleMutex: Mutex = Mutex()

    suspend fun stop() {
        bucleMutex.withLock {
            running = false
        }
    }

    suspend fun checkQualityAndProceed(
        session: Session,
        arSceneController: ARSceneController,
        anchorNode: AnchorNode,
        numberOfFrames: Int = 15
    ) {
        val uniquePoses = mutableSetOf<Pose>()
        var framesWithGoodQuality = 0
        addQualityIndicator(arSceneController, anchorNode)

        bucleMutex.withLock {
            while (running) {
                val pose = arSceneController.cameraNode.pose

                if (pose != null) {
                    val quality = session.estimateFeatureMapQualityForHosting(pose)

                    val isPoseValid = uniquePoses.none { storedPose ->
                        abs(storedPose.tx() - pose.tx()) <= TOLERANCE &&
                                abs(storedPose.tz() - pose.tz()) <= TOLERANCE
                    }

                    if (quality == Session.FeatureMapQuality.GOOD && isPoseValid) {
                        uniquePoses.add(pose)
                        framesWithGoodQuality++

                        val angleToAnchor = angleRelativeToAnchor(pose, anchorNode.worldPosition)
                        when (determineQuadrant(angleToAnchor)) {
                            1 -> framesInQuadrant1++
                            2 -> framesInQuadrant2++
                            3 -> framesInQuadrant3++
                            4 -> framesInQuadrant4++
                        }

                        anchorNode.childNodes.firstOrNull()?.let { parentNode ->
                            val visibleCylinders = parentNode.childNodes.filterIsInstance<CylinderNode>().filter { it.isVisible }
                            val invisibleCylinders = parentNode.childNodes.filterIsInstance<CylinderNode>().filterNot { it.isVisible }

                            val closestInvisibleCylinder = invisibleCylinders.minByOrNull { cylinderNode ->
                                calculateDistance(pose, cylinderNode.worldPosition)
                            }
                            closestInvisibleCylinder?.isVisible = true

                            when {
                                framesWithGoodQuality < 7 -> {
                                    visibleCylinders.forEach {
                                        it.materialInstance.setColor(android.graphics.Color.RED)
                                    }
                                }
                                framesWithGoodQuality < 14 -> {
                                    visibleCylinders.forEach {
                                        it.materialInstance.setColor(android.graphics.Color.YELLOW)
                                    }
                                }
                                else -> {
                                    if (isQualitySufficient()){
                                        visibleCylinders.forEach {
                                            it.materialInstance.setColor(android.graphics.Color.GREEN)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    delay(100)
                }

                if (isQualitySufficient() && framesWithGoodQuality >= numberOfFrames) {
                    bothQualityThresholdsReached()
                    stop()
                } else if (isQualitySufficient() || framesWithGoodQuality >= numberOfFrames) {
                    if (!anyQualityReached) {
                        anyQualityThresholdReached()
                        anyQualityReached = true
                        lastQualityThresholdReachedTime = System.currentTimeMillis()
                        CoroutineScope(coroutineContext).launch {
                            delay(anyQualityThresholdCooldownMillis)
                            anyQualityReached = false
                        }
                    }
                }
            }
        }
    }

    private fun calculateDistance(pose: Pose, cylinderPosition: Float3): Float {
        val dx = pose.tx() - cylinderPosition.x
        val dz = pose.tz() - cylinderPosition.z
        return sqrt(dx * dx + dz * dz)
    }

    private fun addQualityIndicator(arSceneController: ARSceneController, anchorNode: AnchorNode) {
        val parentX = anchorNode.childNodes.first().position.x
        val cylinderY = anchorNode.childNodes.first().position.y
        val parentZ = anchorNode.childNodes.first().position.z
        val radius = 0.3f
        val angleStep = (2 * Math.PI / 15).toFloat()

        for (i in 0 until 15) {
            val angle = angleStep * i
            val cylinderX = parentX + radius * cos(angle.toDouble()).toFloat()
            val cylinderZ = parentZ + radius * sin(angle.toDouble()).toFloat()

            val cylinderPosition = Float3(cylinderX, cylinderY, cylinderZ)

            val material = arSceneController.materialLoader.createColorInstance(color = android.graphics.Color.WHITE)
            CylinderNode(arSceneController.engine, 0.05f, 0.3f, cylinderPosition, 24, material).let {
                it.isVisible=false
                anchorNode.childNodes.first().childNodes += it
            }
        }
    }

    private fun angleRelativeToAnchor(pose: Pose, anchorPosition: Float3): Float {
        val dx = pose.tx() - anchorPosition.x
        val dz = pose.tz() - anchorPosition.z
        return kotlin.math.atan2(dz, dx)
    }

    private fun determineQuadrant(angle: Float): Int {
        val normalizedAngle = (angle + 2 * PI.toFloat()) % (2 * PI.toFloat())
        return when {
            normalizedAngle < PI.toFloat() / 2 -> 1
            normalizedAngle < PI.toFloat() -> 2
            normalizedAngle < 3 * PI.toFloat() / 2 -> 3
            else -> 4
        }
    }

    private fun isQualitySufficient():Boolean{
        return framesInQuadrant1 >= 2 && framesInQuadrant2 >= 2 &&
                framesInQuadrant3 >= 2 && framesInQuadrant4 >= 2
    }
}
