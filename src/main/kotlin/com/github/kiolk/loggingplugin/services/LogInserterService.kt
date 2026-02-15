package com.github.kiolk.loggingplugin.services

import com.github.kiolk.loggingplugin.settings.LoggingSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*

@Service(Service.Level.PROJECT)
class LogInserterService(private val project: Project) {

    fun insertKotlinAssignmentLogs(searchScope: PsiElement, logTag: String, framework: LoggingSettings.LoggingFramework = LoggingSettings.LoggingFramework.PRINTLN) {
        val strategy = LogStrategyFactory.getStrategy(framework)
        val factory = KtPsiFactory(project)
        val assignments = PsiTreeUtil.findChildrenOfType(searchScope, KtBinaryExpression::class.java)
            .filter { it.operationToken in listOf(KtTokens.EQ, KtTokens.PLUSEQ, KtTokens.MINUSEQ, KtTokens.MULTEQ, KtTokens.DIVEQ, KtTokens.PERCEQ) }

        assignments.forEach { assignment ->
            val left = assignment.left ?: return@forEach
            val varName = left.text
            val logMessage = "$varName assigned new value: \${$varName}"
            val fullLog = strategy.createKotlinLog(factory, logTag, logMessage)
            
            if (isLogAlreadyPresent(assignment, logTag, varName)) return@forEach

            val expression = factory.createExpression(fullLog)
            insertAfterStatement(assignment, expression, factory)
        }
    }

    fun insertKotlinMethodLogs(searchScope: PsiElement, logTag: String, framework: LoggingSettings.LoggingFramework = LoggingSettings.LoggingFramework.PRINTLN) {
        val strategy = LogStrategyFactory.getStrategy(framework)
        val factory = KtPsiFactory(project)
        val functions = PsiTreeUtil.findChildrenOfType(searchScope, KtNamedFunction::class.java)
        functions.forEach { function ->
            val body = function.bodyBlockExpression ?: return@forEach
            val paramsText = function.valueParameters.joinToString(", ") { "${it.name}=\${${it.name}}" }
            val logMessage = "${function.name}($paramsText)"
            val fullLog = strategy.createKotlinLog(factory, logTag, logMessage)
            
            if (body.text.contains(logTag) && body.text.contains(function.name ?: "")) return@forEach
            
            val lBrace = body.lBrace ?: return@forEach
            val expression = factory.createExpression(fullLog)
            body.addAfter(expression, lBrace)
            body.addAfter(factory.createNewLine(), lBrace)
        }
    }

    fun insertJavaMethodLogs(searchScope: PsiElement, logTag: String, framework: LoggingSettings.LoggingFramework = LoggingSettings.LoggingFramework.PRINTLN) {
        val strategy = LogStrategyFactory.getStrategy(framework)
        val factory = JavaPsiFacade.getElementFactory(project)
        val methods = PsiTreeUtil.findChildrenOfType(searchScope, PsiMethod::class.java)
        methods.forEach { method ->
            val body = method.body ?: return@forEach
            val paramsText = method.parameterList.parameters.joinToString(", ") { "${it.name}=\" + ${it.name} + \"" }
            val logMessage = "${method.name}($paramsText)"
            val fullLog = strategy.createJavaLog(factory, logTag, logMessage)
            
            if (body.text.contains(logTag) && body.text.contains(method.name)) return@forEach
            
            val lBrace = body.lBrace ?: return@forEach
            val statement = factory.createStatementFromText(fullLog, method)
            body.addAfter(statement, lBrace)
        }
    }

    fun insertJavaAssignmentLogs(searchScope: PsiElement, logTag: String, framework: LoggingSettings.LoggingFramework = LoggingSettings.LoggingFramework.PRINTLN) {
        val strategy = LogStrategyFactory.getStrategy(framework)
        val factory = JavaPsiFacade.getElementFactory(project)
        val assignments = PsiTreeUtil.findChildrenOfType(searchScope, PsiAssignmentExpression::class.java)
        assignments.forEach { assignment ->
            val varName = assignment.lExpression.text
            val logMessage = "$varName assigned new value: \" + $varName"
            val fullLog = strategy.createJavaLog(factory, logTag, logMessage)
            
            if (isLogAlreadyPresent(assignment, logTag, varName)) return@forEach

            val statement = factory.createStatementFromText(fullLog, assignment)
            insertAfterStatement(assignment, statement)
        }
    }

    fun removeLogs(searchScope: PsiElement, logTag: String, framework: LoggingSettings.LoggingFramework = LoggingSettings.LoggingFramework.PRINTLN) {
        val strategy = LogStrategyFactory.getStrategy(framework)
        val patterns = strategy.getRemovalPatterns(logTag)
        
        if (searchScope.containingFile is PsiJavaFile) {
            val statements = PsiTreeUtil.findChildrenOfType(searchScope, PsiExpressionStatement::class.java)
            statements.filter { stmt -> patterns.any { stmt.text.contains(it) } }.forEach { it.delete() }
        } else if (searchScope.containingFile is KtFile) {
            val calls = PsiTreeUtil.findChildrenOfType(searchScope, KtCallExpression::class.java)
            val toDelete = mutableSetOf<PsiElement>()
            calls.forEach { call ->
                if (patterns.any { call.text.contains(it) }) {
                    var top: PsiElement = call
                    while (top.parent is KtDotQualifiedExpression || top.parent is KtSafeQualifiedExpression) {
                        top = top.parent
                    }
                    if (top.parent is KtBlockExpression || top.parent is KtNamedFunction) {
                        toDelete.add(top)
                    }
                }
            }
            toDelete.forEach { it.delete() }
        }
    }

    private fun isLogAlreadyPresent(element: PsiElement, logTag: String, varName: String): Boolean {
        var current = element
        while (current.parent != null && current.parent !is KtBlockExpression && current.parent !is PsiCodeBlock) {
            current = current.parent
        }
        
        var next = current.nextSibling
        while (next != null && (next is PsiWhiteSpace || next is PsiComment)) {
            next = next.nextSibling
        }
        val text = next?.text ?: ""
        return text.contains(logTag) && text.contains(varName)
    }

    private fun insertAfterStatement(statement: PsiElement, newElement: PsiElement, ktFactory: KtPsiFactory? = null) {
        var current = statement
        while (current.parent != null && current.parent !is KtBlockExpression && current.parent !is PsiCodeBlock) {
            current = current.parent
        }
        
        val parent = current.parent
        if (parent is KtBlockExpression && ktFactory != null) {
            parent.addAfter(newElement, current)
            parent.addAfter(ktFactory.createNewLine(), current)
        } else if (parent is PsiCodeBlock) {
            parent.addAfter(newElement, current)
        }
    }

    companion object {
        fun getInstance(project: Project): LogInserterService = project.getService(LogInserterService::class.java)
    }
}
