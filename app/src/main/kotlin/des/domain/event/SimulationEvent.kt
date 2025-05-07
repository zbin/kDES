package ai.moying.app.des.domain.event

import ai.moying.app.des.domain.engine.SimulationEngine

// 仿真事件接口
interface SimulationEvent {
    val time: Double // 事件触发时间
    suspend fun process(engine: SimulationEngine) // 事件处理逻辑
}

// 示例事件：任务到达
data class TaskArrivalEvent(
    override val time: Double,
    private val taskId: String
) : SimulationEvent {
    override suspend fun process(engine: SimulationEngine) {
        println("Task $taskId arrived at ${engine.getCurrentTime()}")
        // 调度后续事件，例如任务分配给机器人
        engine.scheduleEvent(TaskAssignmentEvent(time + 1.0, taskId))
    }
}

// 示例事件：任务分配
data class TaskAssignmentEvent(
    override val time: Double,
    private val taskId: String
) : SimulationEvent {
    override suspend fun process(engine: SimulationEngine) {
        println("Task $taskId assigned to robot at ${engine.getCurrentTime()}")
    }
}
