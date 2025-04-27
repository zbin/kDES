package ai.moying.app

import ai.moying.utils.Printer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

fun main() = runBlocking {
    val engine = SimulationEngine(this)
    val config = SimulationConfig(robotCount = 5, taskFrequency = 2.0)
    config.generateTaskArrivalEvents(engine)

    val model = SimulationModel()
    repeat(config.robotCount) { model.addEntity(Robot("Robot-$it")) }

    engine.run(config.simulationDuration)
    println("Simulation completed. Log:\n${engine.getEventLog().joinToString("\n")}")
}


// 仿真事件接口
interface SimulationEvent {
    val time: Double // 事件触发时间
    suspend fun process(engine: SimulationEngine) // 事件处理逻辑
}

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

// 多方案配置管理
class SimulationScenarioManager {
    private val scenarios = mutableMapOf<String, SimulationConfig>()

    fun addScenario(name: String, config: SimulationConfig) {
        scenarios[name] = config
    }

    fun getScenario(name: String): SimulationConfig? = scenarios[name]
}

data class SimulationResult(
    val scenarioName: String,
    val robotUtilization: Double,
    val averageTaskCompletionTime: Double,
    val throughput: Int
)

class ResultAnalyzer {
    private val results = mutableListOf<SimulationResult>()

    fun addResult(result: SimulationResult) {
        results.add(result)
    }

    fun compareResults(): String {
        return results.joinToString("\n") { result ->
            "${result.scenarioName}: Utilization=${result.robotUtilization}, Throughput=${result.throughput}"
        }
    }
}

class OptimizationAdvisor {
    fun analyzeBottlenecks(result: SimulationResult): String {
        return if (result.robotUtilization > 0.9) {
            "Increase robot count by 10% to reduce utilization."
        } else {
            "Current configuration is acceptable."
        }
    }
}

// 仿真实体基类
abstract class Entity(val id: String) {
    abstract fun updateState(time: Double)
}

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

// 事件定义
data class EventDefinition(
    val type: String,
    val triggerCondition: (Double, List<Entity>) -> Boolean,
    val action: (Double, List<Entity>) -> SimulationEvent?
)

// 仿真模型管理类
class SimulationModel {
    private val entities = mutableListOf<Entity>()
    private val eventDefinitions = mutableListOf<EventDefinition>()

    fun addEntity(entity: Entity) {
        entities.add(entity)
    }

    fun addEventDefinition(definition: EventDefinition) {
        eventDefinitions.add(definition)
    }

    fun generateEvents(time: Double, engine: SimulationEngine) {
        for (def in eventDefinitions) {
            if (def.triggerCondition(time, entities)) {
                def.action(time, entities)?.let { engine.scheduleEvent(it) }
            }
        }
    }
}

