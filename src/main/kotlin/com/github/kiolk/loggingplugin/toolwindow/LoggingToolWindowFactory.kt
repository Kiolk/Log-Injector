package com.github.kiolk.loggingplugin.toolwindow

import com.github.kiolk.loggingplugin.services.LogStrategyFactory
import com.github.kiolk.loggingplugin.settings.LoggingSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class LoggingToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val settings = LoggingSettings.getInstance(project)
        
        val mainPanel = JPanel(BorderLayout())
        
        // Top panel for settings
        val settingsPanel = JPanel(GridBagLayout()).apply {
            border = JBUI.Borders.empty(10)
        }

        val constraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
            gridx = 0
            gridy = 0
            anchor = GridBagConstraints.WEST
            insets = JBUI.insets(2)
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
            val state = settings.state
            val logTag = state.logTag
            val strategy = LogStrategyFactory.getStrategy(state.loggingFramework)
            val preview = StringBuilder()
            
            val ktFactory = org.jetbrains.kotlin.psi.KtPsiFactory(project)

            val kotlinImport = strategy.getKotlinImport()
            if (kotlinImport != null) {
                preview.append("import $kotlinImport\n\n")
            }

            if (state.trackMethodExecution) {
                preview.append("// Method Execution:\n")
                preview.append("fun someMethod(arg: String) {\n")
                val logLine = strategy.createKotlinLog(ktFactory, logTag, "someMethod(arg=\$arg)")
                preview.append("    $logLine\n")
                preview.append("    // ...\n")
                preview.append("}\n\n")
            }
            if (state.trackAssignments) {
                preview.append("// Assignments:\n")
                preview.append("var x = 10\n")
                val logLine = strategy.createKotlinLog(ktFactory, logTag, "x assigned new value: \$x")
                preview.append("$logLine\n")
            }
            if (!state.trackMethodExecution && !state.trackAssignments) {
                preview.append("No tracking selected.")
            }
            previewArea.text = preview.toString()
        }

        val frameworkModel = CollectionComboBoxModel(LoggingSettings.LoggingFramework.entries)
        val frameworkCombo = ComboBox(frameworkModel).apply {
            renderer = SimpleListCellRenderer.create("") { it.displayName }
            selectedItem = settings.state.loggingFramework
            addActionListener {
                settings.state.loggingFramework = selectedItem as LoggingSettings.LoggingFramework
                updatePreview()
            }
        }

        val tagField = JBTextField(settings.state.logTag).apply {
            document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent) { settings.state.logTag = text; updatePreview() }
                override fun removeUpdate(e: DocumentEvent) { settings.state.logTag = text; updatePreview() }
                override fun changedUpdate(e: DocumentEvent) { settings.state.logTag = text; updatePreview() }
            })
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

        settingsPanel.add(JBLabel("Logging System:"), constraints)
        constraints.gridy++
        settingsPanel.add(frameworkCombo, constraints)
        constraints.gridy++
        settingsPanel.add(JBLabel("Log Tag:"), constraints)
        constraints.gridy++
        settingsPanel.add(tagField, constraints)
        constraints.gridy++
        settingsPanel.add(methodExecCheckbox, constraints)
        constraints.gridy++
        settingsPanel.add(assignmentsCheckbox, constraints)

        mainPanel.add(settingsPanel, BorderLayout.NORTH)
        mainPanel.add(previewArea, BorderLayout.CENTER)

        updatePreview()

        val content = ContentFactory.getInstance().createContent(mainPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
