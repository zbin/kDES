package ai.moying.app.des.domain.solutions

import ai.moying.app.SimulationConfig

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

