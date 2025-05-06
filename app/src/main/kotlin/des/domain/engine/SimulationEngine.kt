package ai.moying.app.des.domain.engine

import ai.moying.app.des.domain.event.SimulationEvent
import ai.moying.app.des.domain.event.TaskArrivalEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.PriorityQueue
import java.util.Random

// 仿真引擎核心类
class SimulationEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val eventQueue = PriorityQueue<SimulationEvent>(compareBy { it.time })
    private var currentTime: Double = 0.0
    private var running: Boolean = false
    private val eventLog = mutableListOf<String>()

    // 添加事件到队列
    fun scheduleEvent(event: SimulationEvent) {
        eventQueue.add(event)
    }

    // 运行仿真
    fun run(maxTime: Double) = scope.launch {
        running = true
        while (running && eventQueue.isNotEmpty() && currentTime < maxTime) {
            val event = eventQueue.poll() ?: break
            currentTime = event.time
            event.process(this@SimulationEngine)
            eventLog.add("[$currentTime] Processed event: ${event.javaClass.simpleName}")
        }
        running = false
    }

    // 获取当前仿真时间
    fun getCurrentTime(): Double = currentTime

    // 暂停仿真
    fun pause() {
        running = false
    }

    // 获取事件日志
    fun getEventLog(): List<String> = eventLog.toList()
}


// 仿真参数配置类
data class SimulationConfig(
    val robotCount: Int = 10,
    val taskFrequency: Double = 5.0, // 每分钟任务到达频率
    val simulationDuration: Double = 60.0, // 仿真总时长（分钟）
    val randomSeed: Long = 42
) {
    fun generateTaskArrivalEvents(engine: SimulationEngine) {
        val random = Random(randomSeed)
        var time = 0.0
        while (time < simulationDuration) {
            // 使用泊松分布模拟任务到达间隔
            val interval = -Math.log(1.0 - random.nextDouble()) / taskFrequency
            time += interval
            if (time < simulationDuration) {
                engine.scheduleEvent(TaskArrivalEvent(time, "Task-${random.nextInt(1000)}"))
            }
        }
    }
}

