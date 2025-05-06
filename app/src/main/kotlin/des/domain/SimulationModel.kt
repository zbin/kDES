package ai.moying.app.des.domain

import ai.moying.app.des.domain.engine.SimulationEngine

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