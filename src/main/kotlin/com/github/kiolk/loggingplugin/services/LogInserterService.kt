package com.github.kiolk.loggingplugin.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*

@Service(Service.Level.PROJECT)
class LogInserterService(private val project: Project) {

    fun insertKotlinAssignmentLogs(searchScope: PsiElement, logTag: String) {
        val factory = KtPsiFactory(project)
        val assignments = PsiTreeUtil.findChildrenOfType(searchScope, KtBinaryExpression::class.java)
            .filter { it.operationToken in listOf(KtTokens.EQ, KtTokens.PLUSEQ, KtTokens.MINUSEQ, KtTokens.MULTEQ, KtTokens.DIVEQ, KtTokens.PERCEQ) }

        assignments.forEach { assignment ->
            val left = assignment.left ?: return@forEach
            val varName = left.text
            val logMessage = "$logTag: $varName assigned new value: \${$varName}"
            
            if (isLogAlreadyPresent(assignment, logMessage)) return@forEach

            val logStatement = "println(\"$logMessage\")"
            val expression = factory.createExpression(logStatement)

            insertAfterStatement(assignment, expression, factory)
        }
    }

    fun insertKotlinMethodLogs(searchScope: PsiElement, logTag: String) {
        val factory = KtPsiFactory(project)
        val functions = PsiTreeUtil.findChildrenOfType(searchScope, KtNamedFunction::class.java)
        functions.forEach { function ->
            val body = function.bodyBlockExpression ?: return@forEach
            val paramsText = function.valueParameters.joinToString(", ") { "${it.name}=\${${it.name}}" }
            val logMessage = "$logTag: ${function.name}($paramsText)"
            
            if (body.text.contains(logMessage)) return@forEach
            
            val lBrace = body.lBrace ?: return@forEach
            val expression = factory.createExpression("println(\"$logMessage\")")
            body.addAfter(expression, lBrace)
            body.addAfter(factory.createNewLine(), lBrace)
        }
    }

    fun insertJavaMethodLogs(searchScope: PsiElement, logTag: String) {
        val factory = JavaPsiFacade.getElementFactory(project)
        val methods = PsiTreeUtil.findChildrenOfType(searchScope, PsiMethod::class.java)
        methods.forEach { method ->
            val body = method.body ?: return@forEach
            val paramsText = method.parameterList.parameters.joinToString(", ") { "${it.name}=\" + ${it.name} + \"" }
            val logMessage = "$logTag: ${method.name}($paramsText)"
            
            if (body.text.contains(logMessage)) return@forEach
            
            val lBrace = body.lBrace ?: return@forEach
            val statement = factory.createStatementFromText("System.out.println(\"$logMessage\");", method)
            body.addAfter(statement, lBrace)
        }
    }

    fun insertJavaAssignmentLogs(searchScope: PsiElement, logTag: String) {
        val factory = JavaPsiFacade.getElementFactory(project)
        val assignments = PsiTreeUtil.findChildrenOfType(searchScope, PsiAssignmentExpression::class.java)
        assignments.forEach { assignment ->
            val varName = assignment.lExpression.text
            val logMessage = "$logTag: $varName assigned new value: \" + $varName"
            
            if (isLogAlreadyPresent(assignment, logMessage)) return@forEach

            val logStatement = "System.out.println(\"$logMessage\");"
            val statement = factory.createStatementFromText(logStatement, assignment)
            
            insertAfterStatement(assignment, statement)
        }
    }

    private fun isLogAlreadyPresent(element: PsiElement, logContent: String): Boolean {
        var current = element
        while (current.parent != null && current.parent !is KtBlockExpression && current.parent !is PsiCodeBlock) {
            current = current.parent
        }
        
        var next = current.nextSibling
        while (next != null && (next is PsiWhiteSpace || next is PsiComment)) {
            next = next.nextSibling
        }
        return next?.text?.contains(logContent) == true
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
