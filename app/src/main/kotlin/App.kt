package ai.moying.app

//import ai.moying.app.des.domain.Robot
import ai.moying.app.des.domain.SimulationModel
import ai.moying.app.des.domain.engine.SimulationConfig
import ai.moying.app.des.domain.engine.SimulationEngine
import kotlinx.coroutines.runBlocking

//fun main() = runBlocking {
//    val engine = SimulationEngine(this)
//    val config = SimulationConfig(robotCount = 5, taskFrequency = 2.0)
//    config.generateTaskArrivalEvents(engine)
//
//    val model = SimulationModel()
//    repeat(config.robotCount) { model.addEntity(Robot("Robot-$it")) }
//
//    engine.run(config.simulationDuration)
//    println("Simulation completed. Log:\n${engine.getEventLog().joinToString("\n")}")
//}


import java.util.PriorityQueue
import kotlin.random.Random

data class Robot(
    val id: String,
    var position: Pair<Double, Double> = Pair(0.0, 0.0),
    var isBusy: Boolean = false,
    val speed: Double = 1.0 // 单位/时间
)

data class Task(
    val id: String,
    val location: Pair<Double, Double>,
    val duration: Double
)

data class Event(
    val time: Double,
    val type: String, // "TaskArrival", "MoveEnd", "TaskStart", "TaskEnd"
    val robotId: String? = null,
    val taskId: String? = null
) : Comparable<Event> {
    override fun compareTo(other: Event): Int = time.compareTo(other.time)
}

class RobotSimulator {
    private var currentTime: Double = 0.0
    private val eventQueue = PriorityQueue<Event>()
    private val robots = mutableMapOf<String, Robot>()
    private val tasks = mutableMapOf<String, Task>()
    private val availableTasks = mutableListOf<String>()
    private val robotTasks = mutableMapOf<String, String>() // robotId -> taskId

    fun addRobot(robot: Robot) {
        robots[robot.id] = robot
    }

    fun addTask(task: Task) {
        tasks[task.id] = task
        availableTasks.add(task.id)
        schedule(Event(currentTime, "TaskArrival", taskId = task.id))
    }

    fun schedule(event: Event) {
        eventQueue.add(event)
    }

    fun run(until: Double) {
        while (eventQueue.isNotEmpty() && currentTime <= until) {
            val event = eventQueue.poll() ?: break
            currentTime = event.time
            when (event.type) {
                "TaskArrival" -> handleTaskArrival(event.taskId)
                "MoveEnd" -> handleMoveEnd(event.robotId, event.taskId)
                "TaskStart" -> handleTaskStart(event.robotId, event.taskId)
                "TaskEnd" -> handleTaskEnd(event.robotId, event.taskId)
            }
        }
    }

    private fun handleTaskArrival(taskId: String?) {
        taskId ?: return
        println("任务 $taskId 到达于时间 $currentTime")
        assignTaskToRobot(taskId)
    }

    private fun assignTaskToRobot(taskId: String) {
        val availableRobot = robots.values.find { !it.isBusy }
        if (availableRobot != null) {
            availableRobot.isBusy = true
            robotTasks[availableRobot.id] = taskId
            availableTasks.remove(taskId)
            val task = tasks[taskId]!!
            val distance = calculateDistance(availableRobot.position, task.location)
            val travelTime = distance / availableRobot.speed
            schedule(Event(currentTime + travelTime, "MoveEnd", availableRobot.id, taskId))
            println("机器人 ${availableRobot.id} 被分配任务 $taskId，移动时间 $travelTime")
        }
    }

    private fun handleMoveEnd(robotId: String?, taskId: String?) {
        robotId ?: return
        taskId ?: return
        val robot = robots[robotId] ?: return
        val task = tasks[taskId] ?: return
        robot.position = task.location
        println("机器人 $robotId 到达任务位置 $taskId 于时间 $currentTime")
        schedule(Event(currentTime, "TaskStart", robotId, taskId))
    }

    private fun handleTaskStart(robotId: String?, taskId: String?) {
        robotId ?: return
        taskId ?: return
        val task = tasks[taskId] ?: return
        println("机器人 $robotId 开始执行任务 $taskId 于时间 $currentTime")
        schedule(Event(currentTime + task.duration, "TaskEnd", robotId, taskId))
    }

    private fun handleTaskEnd(robotId: String?, taskId: String?) {
        robotId ?: return
        taskId ?: return
        val robot = robots[robotId] ?: return
        robot.isBusy = false
        robotTasks.remove(robotId)
        println("机器人 $robotId 完成任务 $taskId 于时间 $currentTime")
        if (availableTasks.isNotEmpty()) {
            assignTaskToRobot(availableTasks.first())
        }
    }

    private fun calculateDistance(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double {
        return Math.sqrt(Math.pow(p2.first - p1.first, 2.0) + Math.pow(p2.second - p1.second, 2.0))
    }
}

fun main() {
    val simulator = RobotSimulator()
    simulator.addRobot(Robot("R1", Pair(0.0, 0.0)))
    simulator.addRobot(Robot("R2", Pair(0.0, 0.0)))
    simulator.addTask(Task("T1", Pair(5.0, 5.0), 3.0))
    simulator.addTask(Task("T2", Pair(10.0, 10.0), 2.0))
    simulator.run(100.0)
}