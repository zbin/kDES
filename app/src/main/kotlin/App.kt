package ai.moying.app

import ai.moying.app.des.domain.Robot
import ai.moying.app.des.domain.SimulationModel
import ai.moying.app.des.domain.engine.SimulationConfig
import ai.moying.app.des.domain.engine.SimulationEngine
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val engine = SimulationEngine(this)
    val config = SimulationConfig(robotCount = 5, taskFrequency = 2.0)
    config.generateTaskArrivalEvents(engine)

    val model = SimulationModel()
    repeat(config.robotCount) { model.addEntity(Robot("Robot-$it")) }

    engine.run(config.simulationDuration)
    println("Simulation completed. Log:\n${engine.getEventLog().joinToString("\n")}")
}


