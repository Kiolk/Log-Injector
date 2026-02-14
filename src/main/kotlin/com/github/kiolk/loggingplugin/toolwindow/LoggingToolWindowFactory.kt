package com.github.kiolk.loggingplugin.toolwindow

import com.github.kiolk.loggingplugin.settings.LoggingSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel

class LoggingToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val settings = LoggingSettings.getInstance(project)
        
        val mainPanel = JPanel(BorderLayout())
        
        // Top panel for settings
        val settingsPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(10)
        }

        val previewArea = JBTextArea().apply {
            isEditable = false
            font = Font("Monospaced", Font.PLAIN, 12)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Preview"),
                JBUI.Borders.empty(5)
            )
        }

        fun updatePreview() {
            val preview = StringBuilder()
            if (settings.state.trackMethodExecution) {
                preview.append("// Method Execution:\n")
                preview.append("fun someMethod(arg: String) {\n")
                preview.append("    println(\"Myfancy log: someMethod(arg=\$arg)\")\n")
                preview.append("    // ...\n")
                preview.append("}\n\n")
            }
            if (settings.state.trackAssignments) {
                preview.append("// Assignments:\n")
                preview.append("var x = 10\n")
                preview.append("println(\"Myfancy log: x assigned new value: \$x\")\n")
            }
            if (!settings.state.trackMethodExecution && !settings.state.trackAssignments) {
                preview.append("No tracking selected.")
            }
            previewArea.text = preview.toString()
        }

        val methodExecCheckbox = JBCheckBox("Track Method Execution", settings.state.trackMethodExecution).apply {
            addActionListener { 
                settings.state.trackMethodExecution = isSelected
                updatePreview()
            }
        }
        val assignmentsCheckbox = JBCheckBox("Track Assignments", settings.state.trackAssignments).apply {
            addActionListener { 
                settings.state.trackAssignments = isSelected
                updatePreview()
            }
        }

        settingsPanel.add(methodExecCheckbox)
        settingsPanel.add(assignmentsCheckbox)

        mainPanel.add(settingsPanel, BorderLayout.NORTH)
        mainPanel.add(previewArea, BorderLayout.CENTER)

        updatePreview()

        val content = ContentFactory.getInstance().createContent(mainPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
