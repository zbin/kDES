package ai.moying.app.des.domain

// 机器人实体
data class Robot(
    val robotId: String,
    var speed: Double = 1.0,
    var loadCapacity: Double = 100.0,
    var isBusy: Boolean = false
) : Entity(robotId) {
    override fun updateState(time: Double) {
        if (isBusy) {
            println("Robot $robotId working at time $time")
        }
    }
}