package com.pinterest.ktlint.ruleset.standard.rules

import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BODY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLASS_BODY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ENUM_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.IF
import com.pinterest.ktlint.rule.engine.core.api.ElementType.KDOC
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OBJECT_KEYWORD
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SEMICOLON
import com.pinterest.ktlint.rule.engine.core.api.ElementType.THEN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHILE
import com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.SinceKtlint
import com.pinterest.ktlint.rule.engine.core.api.SinceKtlint.Status.STABLE
import com.pinterest.ktlint.rule.engine.core.api.findParentByType
import com.pinterest.ktlint.rule.engine.core.api.hasModifier
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import com.pinterest.ktlint.rule.engine.core.api.isCode
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpace20
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpaceWithNewline20
import com.pinterest.ktlint.rule.engine.core.api.lastChildLeafOrSelf20
import com.pinterest.ktlint.rule.engine.core.api.nextCodeSibling20
import com.pinterest.ktlint.rule.engine.core.api.nextLeaf
import com.pinterest.ktlint.rule.engine.core.api.parent
import com.pinterest.ktlint.rule.engine.core.api.prevCodeLeaf
import com.pinterest.ktlint.rule.engine.core.api.prevLeaf
import com.pinterest.ktlint.rule.engine.core.api.remove
import com.pinterest.ktlint.rule.engine.core.api.upsertWhitespaceAfterMe
import com.pinterest.ktlint.ruleset.standard.StandardRule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens

@SinceKtlint("0.1", STABLE)
public class NoSemicolonsRule :
    StandardRule(
        id = "no-semi",
        visitorModifiers =
            setOf(
                RunAfterRule(
                    ruleId = WRAPPING_RULE_ID,
                    mode = RunAfterRule.Mode.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED,
                ),
            ),
    ) {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType != SEMICOLON) {
            return
        }
        val nextLeaf = node.nextLeaf
        if (nextLeaf.doesNotRequirePreSemi() && isNoSemicolonRequiredAfter(node)) {
            emit(node.startOffset, "Unnecessary semicolon", true)
                .ifAutocorrectAllowed {
                    val prevLeaf = node.prevLeaf
                    node.remove()
                    if ((prevLeaf != null && prevLeaf.isWhiteSpace20) &&
                        (nextLeaf == null || nextLeaf.isWhiteSpace20)
                    ) {
                        prevLeaf.remove()
                    }
                }
        } else if (!nextLeaf.isWhiteSpace20) {
            if (node.prevLeaf.isWhiteSpaceWithNewline20) {
                return
            }
            // todo: move to a separate rule
            emit(node.startOffset + 1, "Missing spacing after \";\"", true)
                .ifAutocorrectAllowed {
                    node.upsertWhitespaceAfterMe(" ")
                }
        }
    }

    private fun ASTNode?.doesNotRequirePreSemi() =
        when {
            this == null -> {
                true
            }

            this.isWhiteSpace20 -> {
                nextLeaf {
                    it.isCode &&
                        it.findParentByType(KDOC) == null &&
                        it.findParentByType(ANNOTATION_ENTRY) == null
                }.let { nextLeaf ->
                    nextLeaf == null ||
                        // \s+ and then eof
                        (textContains('\n') && nextLeaf.elementType != KtTokens.LBRACE)
                }
            }

            else -> {
                false
            }
        }

    private fun isNoSemicolonRequiredAfter(node: ASTNode): Boolean {
        node
            .prevCodeLeaf
            ?.also { prevCodeLeaf ->
                if (prevCodeLeaf.elementType == OBJECT_KEYWORD) {
                    // https://github.com/pinterest/ktlint/issues/281
                    return false
                }
            }?.parent
            ?.run {
                if (isLoopWithoutBody()) {
                    // https://github.com/pinterest/ktlint/issues/955
                    return false
                }
                if (isIfExpressionWithoutThen()) {
                    return false
                }
            }

        // In case of an enum entry the semicolon (e.g. the node) is a direct child node of enum entry
        if (node.parent?.elementType == ENUM_ENTRY) {
            return node.isLastCodeLeafBeforeClosingOfClassBody()
        }
        if (node.isEnumClassWithoutValues()) {
            return false
        }

        return true
    }

    private fun ASTNode.isLoopWithoutBody() =
        (elementType == WHILE || elementType == FOR) &&
            findChildByType(BODY)?.firstChildNode == null

    private fun ASTNode.isIfExpressionWithoutThen() = elementType == IF && findChildByType(THEN)?.firstChildNode == null

    private fun ASTNode?.isLastCodeLeafBeforeClosingOfClassBody() = getLastCodeLeafBeforeClosingOfClassBody() == this

    private fun ASTNode?.getLastCodeLeafBeforeClosingOfClassBody() =
        this
            ?.findParentByType(CLASS_BODY)
            ?.lastChildLeafOrSelf20
            ?.prevCodeLeaf

    private fun ASTNode?.isEnumClassWithoutValues() =
        this
            ?.takeIf { !it.isLastCodeLeafBeforeClosingOfClassBody() }
            ?.findParentByType(CLASS_BODY)
            ?.takeIf { this == it.firstChildNode.nextCodeSibling20 }
            ?.findParentByType(CLASS)
            ?.hasModifier(ENUM_KEYWORD)
            ?: false
}

public val NO_SEMICOLONS_RULE_ID: RuleId = NoSemicolonsRule().ruleId
