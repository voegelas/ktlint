package com.pinterest.ktlint.ruleset.standard.rules

import com.pinterest.ktlint.logger.api.initKtLintKLogger
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision
import com.pinterest.ktlint.rule.engine.core.api.AutocorrectDecision.NO_AUTOCORRECT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ANNOTATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.ARROW
import com.pinterest.ktlint.rule.engine.core.api.ElementType.BLOCK
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CLOSING_QUOTE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.COMMA
import com.pinterest.ktlint.rule.engine.core.api.ElementType.CONDITION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DESTRUCTURING_DECLARATION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.DOT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.EOL_COMMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FOR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUN
import com.pinterest.ktlint.rule.engine.core.api.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.rule.engine.core.api.ElementType.GT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LBRACKET
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LITERAL_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LONG_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LPAR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.LT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.OBJECT_LITERAL
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RBRACE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RBRACKET
import com.pinterest.ktlint.rule.engine.core.api.ElementType.RPAR
import com.pinterest.ktlint.rule.engine.core.api.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SUPER_TYPE_CALL_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SUPER_TYPE_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.TYPE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.TYPE_PARAMETER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.TYPE_PARAMETER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.TYPE_PROJECTION
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.rule.engine.core.api.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHEN_ENTRY
import com.pinterest.ktlint.rule.engine.core.api.ElementType.WHITE_SPACE
import com.pinterest.ktlint.rule.engine.core.api.IndentConfig
import com.pinterest.ktlint.rule.engine.core.api.IndentConfig.Companion.DEFAULT_INDENT_CONFIG
import com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule
import com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule.Mode.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.SinceKtlint
import com.pinterest.ktlint.rule.engine.core.api.SinceKtlint.Status.STABLE
import com.pinterest.ktlint.rule.engine.core.api.TokenSets
import com.pinterest.ktlint.rule.engine.core.api.children20
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.EditorConfig
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_SIZE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.INDENT_STYLE_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY
import com.pinterest.ktlint.rule.engine.core.api.editorconfig.MAX_LINE_LENGTH_PROPERTY_OFF
import com.pinterest.ktlint.rule.engine.core.api.firstChildLeafOrSelf20
import com.pinterest.ktlint.rule.engine.core.api.hasNewLineInClosedRange
import com.pinterest.ktlint.rule.engine.core.api.ifAutocorrectAllowed
import com.pinterest.ktlint.rule.engine.core.api.indent20
import com.pinterest.ktlint.rule.engine.core.api.isPartOf
import com.pinterest.ktlint.rule.engine.core.api.isPartOfComment20
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpace20
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpaceWithNewline20
import com.pinterest.ktlint.rule.engine.core.api.isWhiteSpaceWithoutNewline20
import com.pinterest.ktlint.rule.engine.core.api.lastChildLeafOrSelf20
import com.pinterest.ktlint.rule.engine.core.api.leavesForwardsIncludingSelf
import com.pinterest.ktlint.rule.engine.core.api.nextCodeLeaf
import com.pinterest.ktlint.rule.engine.core.api.nextCodeSibling20
import com.pinterest.ktlint.rule.engine.core.api.nextLeaf
import com.pinterest.ktlint.rule.engine.core.api.nextSibling
import com.pinterest.ktlint.rule.engine.core.api.nextSibling20
import com.pinterest.ktlint.rule.engine.core.api.parent
import com.pinterest.ktlint.rule.engine.core.api.prevCodeLeaf
import com.pinterest.ktlint.rule.engine.core.api.prevCodeSibling20
import com.pinterest.ktlint.rule.engine.core.api.prevLeaf
import com.pinterest.ktlint.rule.engine.core.api.prevSibling
import com.pinterest.ktlint.rule.engine.core.api.prevSibling20
import com.pinterest.ktlint.rule.engine.core.api.upsertWhitespaceAfterMe
import com.pinterest.ktlint.rule.engine.core.api.upsertWhitespaceBeforeMe
import com.pinterest.ktlint.ruleset.standard.StandardRule
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtSuperTypeList
import org.jetbrains.kotlin.psi.psiUtil.leaves
import org.jetbrains.kotlin.psi.psiUtil.siblings

private val LOGGER = KotlinLogging.logger {}.initKtLintKLogger()

/**
 * This rule inserts missing newlines (e.g. between parentheses of a multi-line function call). This logic previously
 * was part of the IndentationRule (phase 1).
 *
 * Whenever a linebreak is inserted, this rules assumes that the parent node it indented correctly. So the indentation
 * is fixed with respect to indentation of the parent. This is just a simple best effort for the case that the
 * indentation rule is not run.
 */
@SinceKtlint("0.45", STABLE)
public class WrappingRule :
    StandardRule(
        id = "wrapping",
        usesEditorConfigProperties =
            setOf(
                INDENT_SIZE_PROPERTY,
                INDENT_STYLE_PROPERTY,
                MAX_LINE_LENGTH_PROPERTY,
            ),
        visitorModifiers =
            setOf(
                RunAfterRule(
                    ruleId = ANNOTATION_RULE_ID,
                    mode = REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED,
                ),
            ),
    ) {
    private var line = 1
    private var indentConfig = DEFAULT_INDENT_CONFIG
    private var maxLineLength: Int = MAX_LINE_LENGTH_PROPERTY.defaultValue

    override fun beforeFirstNode(editorConfig: EditorConfig) {
        line = 1
        indentConfig =
            IndentConfig(
                indentStyle = editorConfig[INDENT_STYLE_PROPERTY],
                tabWidth = editorConfig[INDENT_SIZE_PROPERTY],
            )
        maxLineLength = editorConfig.maxLineLength()
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        when (node.elementType) {
            BLOCK -> beforeVisitBlock(node, emit)
            LPAR, LBRACKET -> rearrangeBlock(node, emit)
            SUPER_TYPE_LIST -> rearrangeSuperTypeList(node, emit)
            VALUE_PARAMETER_LIST, VALUE_ARGUMENT_LIST -> rearrangeValueList(node, emit)
            TYPE_ARGUMENT_LIST, TYPE_PARAMETER_LIST -> rearrangeTypeArgumentList(node, emit)
            ARROW -> rearrangeArrow(node, emit)
            WHITE_SPACE -> line += node.text.count { it == '\n' }
            CLOSING_QUOTE -> rearrangeClosingQuote(node, emit)
        }
    }

    private fun beforeVisitBlock(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        require(node.elementType == BLOCK)

        val lbrace =
            node
                .getStartOfBlock()
                ?.takeIf { it.elementType == LBRACE }
                ?: return
        if (lbrace.followedByNewline() ||
            lbrace.followedByEolComment() ||
            lbrace.followedByFunctionLiteralParameterList() ||
            lbrace.isPartOf(LONG_STRING_TEMPLATE_ENTRY)
        ) {
            // String template inside raw string literal may exceed the maximum line length
            return
        }

        node
            .takeUnless { it.firstChildLeafOrSelf20.elementType == EOL_COMMENT }
            ?.getEndOfBlock()
            ?.takeIf { it.elementType == RBRACE }
            ?.let { rbrace ->
                if (hasNewLineInClosedRange(lbrace, rbrace)) {
                    requireNewlineAfterLeaf(lbrace, emit)
                }
            }

        if (maxLineLength != MAX_LINE_LENGTH_PROPERTY_OFF) {
            val lengthUntilBeginOfLine =
                node
                    .leaves(false)
                    .takeWhile { !it.isWhiteSpaceWithNewline20 }
                    .sumOf { it.textLength }
            val lengthUntilEndOfLine =
                node
                    .firstChildLeafOrSelf20
                    .leavesForwardsIncludingSelf
                    .takeWhile { !it.isWhiteSpaceWithNewline20 }
                    .sumOf { it.textLength }
            if (lengthUntilBeginOfLine + lengthUntilEndOfLine > maxLineLength) {
                requireNewlineAfterLeaf(lbrace, emit)
            }
        }
    }

    private fun ASTNode.followedByEolComment() =
        null !=
            leaves()
                .takeWhile { it.isWhiteSpaceWithoutNewline20 || it.elementType == EOL_COMMENT }
                .firstOrNull { it.elementType == EOL_COMMENT }

    private fun ASTNode.followedByFunctionLiteralParameterList() =
        VALUE_PARAMETER_LIST ==
            takeIf { parent?.elementType == FUNCTION_LITERAL }
                ?.nextCodeSibling20
                ?.elementType

    private fun rearrangeBlock(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        val closingElementType = MATCHING_RTOKEN_MAP[node.elementType]
        var newlineInBetween = false
        var parameterListInBetween = false
        var numberOfArgs = 0
        var firstArg: ASTNode? = null
        // matching ), ] or }
        val closingElement =
            node.nextSibling {
                val isValueArgument = it.elementType == VALUE_ARGUMENT
                val hasLineBreak = if (isValueArgument) it.hasLineBreak(LAMBDA_EXPRESSION, FUN) else it.hasLineBreak()
                newlineInBetween = newlineInBetween || hasLineBreak
                parameterListInBetween = parameterListInBetween || it.elementType == VALUE_PARAMETER_LIST
                if (isValueArgument) {
                    numberOfArgs++
                    firstArg = it
                }
                it.elementType == closingElementType
            }!!
        if (
            !newlineInBetween ||
            // keep { p ->
            // }
            (node.elementType == LBRACE && parameterListInBetween) ||
            // keep ({
            // }) and (object : C {
            // })
            (
                numberOfArgs == 1 &&
                    firstArg
                        ?.firstChildNode
                        ?.elementType
                        ?.let { it == OBJECT_LITERAL || it == LAMBDA_EXPRESSION } == true
            )
        ) {
            return
        }
        if (node.isPartOfForLoopConditionWithMultilineExpression()) {
            // keep:
            // for (foo in listOf(
            //     "foo-1",
            //     "foo-2"
            // )) { ... }
            // but reject:
            // for (
            //     foo in listOf(
            //         "foo-1",
            //         "foo-2"
            //     )
            // ) { ... }
            return
        }
        if (!node
                .nextCodeLeaf
                ?.prevLeaf {
                    // Skip comments, whitespace, and empty nodes
                    !it.isPartOfComment20 &&
                        !it.isWhiteSpaceWithoutNewline20 &&
                        it.textLength > 0
                }.isWhiteSpaceWithNewline20 &&
            // IDEA quirk:
            // if (true &&
            //     true
            // ) {
            // }
            // instead of
            // if (
            //     true &&
            //     true
            // ) {
            // }
            node.nextSibling20?.elementType != CONDITION
        ) {
            requireNewlineAfterLeaf(node, emit)
        }
        if (!closingElement.prevLeaf.isWhiteSpaceWithNewline20) {
            requireNewlineBeforeLeaf(closingElement, emit, indentConfig.parentIndentOf(node))
        }
    }

    private fun rearrangeSuperTypeList(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        val entries = (node.psi as KtSuperTypeList).entries
        if (
            node.textContains('\n') &&
            entries.size > 1 &&
            // e.g.
            //
            // class A : B, C,
            //     D
            // or
            // class A : B, C({
            // }), D
            //
            // but not
            //
            // class A : B, C, D({
            // })
            !(
                entries.dropLast(1).all { it.elementType == SUPER_TYPE_ENTRY } &&
                    entries.last().elementType == SUPER_TYPE_CALL_ENTRY
            )
        ) {
            // put space after :
            if (!node.prevLeaf.isWhiteSpaceWithNewline20) {
                val colon = node.prevCodeLeaf!!
                if (
                    !colon.prevLeaf.isWhiteSpaceWithNewline20 &&
                    colon.prevCodeLeaf.let { it?.elementType != RPAR || !it.prevLeaf.isWhiteSpaceWithNewline20 }
                ) {
                    requireNewlineAfterLeaf(colon, emit)
                }
            }
            // put entries on separate lines
            for (c in node.children20) {
                if (c.elementType == COMMA &&
                    !c.nextSibling20.isWhiteSpaceWithNewline20 &&
                    !c.isFollowedByCommentOnSameLine()
                ) {
                    requireNewlineAfterLeaf(
                        nodeAfterWhichNewlineIsRequired = c,
                        emit = emit,
                        indent = node.indent20,
                    )
                }
            }
        }
    }

    private fun ASTNode.isFollowedByCommentOnSameLine() =
        nextLeaf { !it.isWhiteSpaceWithoutNewline20 }
            ?.isPartOfComment20 == true

    private fun rearrangeValueList(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        for (c in node.children20) {
            val hasLineBreak =
                when (c.elementType) {
                    VALUE_ARGUMENT -> c.hasLineBreak(LAMBDA_EXPRESSION, FUN)
                    VALUE_PARAMETER, ANNOTATION -> c.hasLineBreak()
                    else -> false
                }
            if (hasLineBreak) {
                // rearrange
                //
                // a, b, value(
                // ), c, d
                //
                // to
                //
                // a, b,
                // value(
                // ),
                // c, d

                // insert \n in front of multi-line value
                val prevSibling = c.prevSibling { !it.isWhiteSpace20 }
                if (
                    prevSibling?.elementType == COMMA &&
                    !prevSibling.nextSibling20.isWhiteSpaceWithNewline20
                ) {
                    requireNewlineAfterLeaf(prevSibling, emit)
                }
                // insert \n after multi-line value
                val nextSibling = c.nextSibling { !it.isWhiteSpace20 }
                val hasDestructuringDeclarationAsLastValueParameter =
                    c.isLastValueParameter() && c.firstChildNode.elementType == DESTRUCTURING_DECLARATION
                if (
                    nextSibling?.elementType == COMMA &&
                    !hasDestructuringDeclarationAsLastValueParameter &&
                    !nextSibling.nextSibling20.isWhiteSpaceWithNewline20 &&
                    // value(
                    // ), // a comment
                    // c, d
                    nextSibling.nextSibling20?.nextSibling20?.let { !TokenSets.COMMENTS.contains(it.elementType) } != false
                ) {
                    requireNewlineAfterLeaf(nextSibling, emit)
                }
            }
        }
    }

    private fun ASTNode.isLastValueParameter() =
        elementType == VALUE_PARAMETER &&
            siblings().none { it.elementType == VALUE_PARAMETER }

    private fun rearrangeTypeArgumentList(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.textContains('\n')) {
            // Each type projection must be preceded with a whitespace containing a newline
            node
                .children20
                .filter { it.elementType == TYPE_PROJECTION || it.elementType == TYPE_PARAMETER }
                .forEach { typeProjection ->
                    typeProjection
                        .prevSibling { !it.isPartOfComment20 }
                        .let { prevSibling ->
                            if (prevSibling?.elementType == LT || prevSibling.isWhiteSpaceWithoutNewline20) {
                                emit(typeProjection.startOffset, "A newline was expected before '${typeProjection.text}'", true)
                                    .ifAutocorrectAllowed {
                                        typeProjection.upsertWhitespaceBeforeMe(indentConfig.siblingIndentOf(node))
                                    }
                            }
                        }
                }

            // After the last type projection a whitespace containing a newline must exist
            node
                .findChildByType(GT)
                ?.let { closingAngle ->
                    val prevSibling = closingAngle.prevSibling { !it.isPartOfComment20 }
                    if (!prevSibling.isWhiteSpaceWithNewline20) {
                        emit(closingAngle.startOffset, "A newline was expected before '${closingAngle.text}'", true)
                            .ifAutocorrectAllowed {
                                closingAngle.upsertWhitespaceBeforeMe(indentConfig.siblingIndentOf(node))
                            }
                    }
                }
        }
    }

    private fun rearrangeClosingQuote(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        node
            .parent
            ?.takeIf { it.elementType == STRING_TEMPLATE }
            ?.let { it.psi as KtStringTemplateExpression }
            ?.takeIf { it.isMultiLine() }
            ?.takeIf { it.isFollowedByTrimIndent() || it.isFollowedByTrimMargin() }
            ?.takeUnless { node.prevSibling20?.text.isNullOrBlank() }
            ?.let {
                // rewriting
                // """
                //     text
                // _""".trimIndent()
                // to
                // """
                //     text
                // _
                // """.trimIndent()
                emit(
                    node.startOffset,
                    "Missing newline before \"\"\"",
                    true,
                ).also { autocorrectDecision ->
                    LOGGER.trace {
                        "$line: ${if (autocorrectDecision == NO_AUTOCORRECT) "would have" else ""} inserted newline before (closing) \"\"\""
                    }
                }.ifAutocorrectAllowed {
                    node as LeafPsiElement
                    node.rawInsertBeforeMe(LeafPsiElement(LITERAL_STRING_TEMPLATE_ENTRY, "\n"))
                }
            }
    }

    private fun mustBeFollowedByNewline(node: ASTNode): Boolean {
        // find EOL token (last token before \n)
        // if token is in lTokenSet
        //     find matching rToken
        //     return true if there is no newline after the rToken
        // return false
        val nextCodeSibling = node.nextCodeSibling20 // e.g. BINARY_EXPRESSION
        var lToken = nextCodeSibling?.nextLeaf { it.isWhiteSpaceWithNewline20 }?.prevCodeLeaf
        if (lToken != null && lToken.elementType !in LTOKEN_SET) {
            // special cases:
            // x = y.f({ z ->
            // })
            // x = y.f(0, 1,
            // 2, 3)
            lToken = lToken.prevLeaf { it.elementType in LTOKEN_SET || it == node }
        }
        if (lToken != null && lToken.elementType in LTOKEN_SET) {
            val rElementType = MATCHING_RTOKEN_MAP[lToken.elementType]
            val rToken = lToken.nextSibling { it.elementType == rElementType }
            return rToken?.parent == lToken.parent
        }
        return nextCodeSibling?.textContains('\n') == false
    }

    private fun rearrangeArrow(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        val parent = node.parent!!
        if (
            // check
            // `{ p -> ... }`
            // and
            // `when { m -> ... }`
            // only
            parent.elementType.let { it != FUNCTION_LITERAL && it != WHEN_ENTRY } ||
            // ... and only if expression after -> spans multiple lines
            !parent.textContains('\n') ||
            // permit
            // when {
            //     m -> 0 + d({
            //     })
            // }
            (parent.elementType == WHEN_ENTRY && mustBeFollowedByNewline(node)) ||
            // permit
            // when (this) {
            //     in 0x1F600..0x1F64F, // Emoticons
            //     0x200D // Zero-width Joiner
            //     -> true
            // }
            (parent.elementType == WHEN_ENTRY && node.prevLeaf.isWhiteSpaceWithNewline20)
        ) {
            return
        }
        if (!node.nextCodeLeaf?.prevLeaf.isWhiteSpaceWithNewline20) {
            requireNewlineAfterLeaf(node, emit)
        }
        val r = node.nextSibling { it.elementType == RBRACE } ?: return
        if (!r.prevLeaf.isWhiteSpaceWithNewline20) {
            requireNewlineBeforeLeaf(r, emit, node.indent20)
        }
    }

    private fun requireNewlineBeforeLeaf(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
        indent: String,
    ) {
        emit(
            node.startOffset - 1,
            """Missing newline before "${node.text}"""",
            true,
        ).also { autocorrectDecision ->
            LOGGER.trace {
                "$line: " + (if (autocorrectDecision == NO_AUTOCORRECT) "would have " else "") + "inserted newline before ${node.text}"
            }
        }.ifAutocorrectAllowed {
            node.upsertWhitespaceBeforeMe(indent)
        }
    }

    private fun requireNewlineAfterLeaf(
        nodeAfterWhichNewlineIsRequired: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
        indent: String? = null,
        nodeToFix: ASTNode = nodeAfterWhichNewlineIsRequired,
    ) {
        emit(
            nodeAfterWhichNewlineIsRequired.startOffset + 1,
            """Missing newline after "${nodeAfterWhichNewlineIsRequired.text}"""",
            true,
        ).also { autocorrectDecision ->
            LOGGER.trace {
                "$line: ${if (autocorrectDecision == NO_AUTOCORRECT) "would have" else ""} inserted newline after ${nodeAfterWhichNewlineIsRequired.text}"
            }
        }.ifAutocorrectAllowed {
            val tempIndent = indent ?: (indentConfig.childIndentOf(nodeToFix))
            nodeToFix.upsertWhitespaceAfterMe(tempIndent)
        }
    }

    private fun KtStringTemplateExpression.isMultiLine(): Boolean {
        for (child in node.children20) {
            if (child.elementType == LITERAL_STRING_TEMPLATE_ENTRY) {
                val v = child.text
                if (v == "\n") {
                    return true
                }
            }
        }
        return false
    }

    private fun ASTNode.hasLineBreak(vararg ignoreElementTypes: IElementType): Boolean {
        if (isWhiteSpaceWithNewline20) return true
        return if (ignoreElementTypes.isEmpty()) {
            textContains('\n')
        } else {
            elementType !in ignoreElementTypes &&
                children20.any { c -> c.textContains('\n') && c.elementType !in ignoreElementTypes }
        }
    }

    private fun KtStringTemplateExpression.isFollowedByTrimIndent() = isFollowedBy("trimIndent()")

    private fun KtStringTemplateExpression.isFollowedByTrimMargin() = isFollowedBy("trimMargin()")

    private fun KtStringTemplateExpression.isFollowedBy(callExpressionName: String) =
        this
            .node
            .nextSibling { it.elementType != DOT }
            .let { it?.elementType == CALL_EXPRESSION && it.text == callExpressionName }

    /**
     *  Allow for-statement in which only the expression contains a newline:
     *     for (foo in listOf(
     *         "foo-1",
     *         "foo-2"
     *     )) { ... }
     * but reject:
     *     for (
     *         foo in listOf(
     *             "foo-1",
     *             "foo-2"
     *         )
     *     ) { ... }
     */
    private fun ASTNode.isPartOfForLoopConditionWithMultilineExpression(): Boolean {
        if (parent?.elementType != FOR) {
            return false
        }
        if (this.elementType != LPAR) {
            return parent?.findChildByType(LPAR)!!.isPartOfForLoopConditionWithMultilineExpression()
        }
        require(elementType == LPAR) {
            "Node should be the LPAR of the FOR loop"
        }

        var node: ASTNode? = this
        while (node != null && node.elementType != RPAR) {
            if (node.isWhiteSpaceWithNewline20) {
                return false
            }
            node = node.nextSibling20
        }
        return true
    }

    override fun afterVisitChildNodes(
        node: ASTNode,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> AutocorrectDecision,
    ) {
        if (node.elementType == BLOCK) {
            val lbrace =
                node
                    .getStartOfBlock()
                    ?.takeIf { it.elementType == LBRACE }
                    ?: return
            node
                .getEndOfBlock()
                ?.takeIf { it.elementType == RBRACE }
                ?.takeUnless { it.isPrecededByNewline() }
                ?.let { rbrace ->
                    if (hasNewLineInClosedRange(lbrace, rbrace)) {
                        requireNewlineBeforeLeaf(
                            rbrace,
                            emit,
                            indentConfig.parentIndentOf(node),
                        )
                    }
                }
        }
    }

    private fun ASTNode.followedByNewline() = nextLeaf.isWhiteSpaceWithNewline20

    private fun ASTNode.isPrecededByNewline() = prevLeaf.isWhiteSpaceWithNewline20

    private fun ASTNode.getStartOfBlock() =
        if (parent?.elementType == FUNCTION_LITERAL) {
            parent!!.findChildByType(LBRACE)
        } else {
            firstChildLeafOrSelf20
                .let { node ->
                    if (node.elementType == LBRACE) {
                        // WHEN-entry block have LBRACE and RBRACE as first and last elements
                        node
                    } else {
                        // Other blocks have LBRACE and RBRACE as siblings of the block
                        node.prevCodeSibling20
                    }
                }
        }

    private fun ASTNode.getEndOfBlock() =
        if (parent?.elementType == FUNCTION_LITERAL) {
            parent!!.findChildByType(RBRACE)
        } else {
            lastChildLeafOrSelf20
                .let { node ->
                    if (node.elementType == RBRACE) {
                        // WHEN-entry block have LBRACE and RBRACE as first and last elements
                        node
                    } else {
                        // Other blocks have LBRACE and RBRACE as siblings of the block
                        node.nextCodeSibling20
                    }
                }
        }

    private companion object {
        private val LTOKEN_SET = TokenSet.create(LPAR, LBRACE, LBRACKET, LT)
        private val RTOKEN_SET = TokenSet.create(RPAR, RBRACE, RBRACKET, GT)
        private val MATCHING_RTOKEN_MAP =
            LTOKEN_SET
                .types
                .zip(RTOKEN_SET.types)
                .toMap()
    }
}

public val WRAPPING_RULE_ID: RuleId = WrappingRule().ruleId
