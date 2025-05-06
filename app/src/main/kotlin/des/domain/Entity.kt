package ai.moying.app.des.domain

// 仿真实体基类
abstract class Entity(val id: String) {
    abstract fun updateState(time: Double)
}