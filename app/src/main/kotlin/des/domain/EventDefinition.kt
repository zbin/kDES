package ai.moying.app.des.domain

import ai.moying.app.des.domain.event.SimulationEvent

// 事件定义
data class EventDefinition(
    val type: String,
    val triggerCondition: (Double, List<Entity>) -> Boolean,
    val action: (Double, List<Entity>) -> SimulationEvent?
)