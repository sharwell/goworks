/*
 *  Copyright (c) 2012 Sam Harwell, Tunnel Vision Laboratories LLC
 *  All rights reserved.
 *
 *  The source code of this document is proprietary work, and is not licensed for
 *  distribution. For information about licensing, contact Sam Harwell at:
 *      sam@tunnelvisionlabs.com
 */
package org.tvl.goworks.editor.go.semantics;

import org.antlr.v4.runtime.RuleDependency;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.tvl.goworks.editor.go.parser.GoParser;
import org.tvl.goworks.editor.go.parser.generated.AbstractGoParser.BodyContext;

/**
 *
 * @author Sam Harwell
 */
public class SemanticAnalyzerParseTreeWalker extends ParseTreeWalker {

    private final boolean backgroundAnalysis;

    public SemanticAnalyzerParseTreeWalker(boolean backgroundAnalysis) {
        this.backgroundAnalysis = backgroundAnalysis;
    }

    @Override
    @RuleDependency(recognizer=GoParser.class, rule=GoParser.RULE_body, version=0)
    public void walk(ParseTreeListener listener, ParseTree t) {
        if (backgroundAnalysis && t instanceof BodyContext) {
            return;
        }

        super.walk(listener, t);
    }

}
