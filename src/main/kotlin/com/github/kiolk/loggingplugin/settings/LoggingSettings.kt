package com.github.kiolk.loggingplugin.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(name = "LoggingSettings", storages = [Storage("loggingSettings.xml")])
class LoggingSettings : PersistentStateComponent<LoggingSettings.State> {
    enum class LoggingFramework(val displayName: String) {
        PRINTLN("System Println"),
        TIMBER("Timber"),
    }

    data class State(
        var trackMethodExecution: Boolean = true,
        var trackAssignments: Boolean = true,
        var logTag: String = "Myfancy log",
        var loggingFramework: LoggingFramework = LoggingFramework.PRINTLN,
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(project: Project): LoggingSettings = project.service()
    }
}
