/*
 * [The "BSD license"]
 *  Copyright (c) 2011 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.antlr.works.editor.grammar.parser;

import java.util.ArrayList;
import java.util.List;
import org.antlr.grammar.v3.ANTLRParser;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.MissingTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.UnwantedTokenException;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.tool.ANTLRErrorListener;
import org.antlr.tool.GrammarAST;
import org.antlr.tool.GrammarSyntaxMessage;
import org.antlr.tool.Message;
import org.antlr.tool.ToolMessage;

public class ANTLRErrorProvidingParser extends ANTLRParser {
    
    private final List<SyntaxError> syntaxErrors = new ArrayList<SyntaxError>();

    public ANTLRErrorProvidingParser(TokenStream input) {
        super(input);
    }

    public List<SyntaxError> getSyntaxErrors() {
        return syntaxErrors;
    }

    public String getCurrentRuleName() {
        return currentRuleName;
    }

    @Override
    public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        //String header = getErrorHeader(e);
        String message = getErrorMessage(e, tokenNames);
        syntaxErrors.add(new SyntaxError(e, message));

        super.displayRecognitionError(tokenNames, e);
    }

    public static class SyntaxError {
        private final RecognitionException exception;
        private final String message;

        public SyntaxError(RecognitionException exception, String message) {
            this.exception = exception;
            this.message = message;
        }

        public RecognitionException getException() {
            return exception;
        }

        public String getMessage() {
            return message;
        }
    }

    public static final class ErrorListener implements ANTLRErrorListener {

        @Override
        public void info(String string) {
        }

        @Override
        public void error(Message msg) {
            if (msg instanceof GrammarSyntaxMessage) {
                GrammarSyntaxMessage syntaxMessage = (GrammarSyntaxMessage)msg;
                Token token = syntaxMessage.offendingToken;
                if (token == null)
                    return;

                if (!(syntaxMessage.exception.input instanceof ANTLRParserTokenStream))
                    return;

                ANTLRParserTokenStream stream = (ANTLRParserTokenStream)syntaxMessage.exception.input;
                ANTLRErrorProvidingParser parser = stream.getParser();
                if (parser == null)
                    return;

                parser.syntaxErrors.add(new SyntaxError(syntaxMessage.exception, msg.toString()));
            }
        }

        @Override
        public void warning(Message msg) {
        }

        @Override
        public void error(ToolMessage tm) {
        }
    }

    private static class GrammarASTErrorNode extends GrammarAST {
        public IntStream input;
        public Token start;
        public Token stop;
        public RecognitionException trappedException;

        public GrammarASTErrorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
            super(stop);
            //Console.Out.WriteLine( "start: " + start + ", stop: " + stop );
            if ( stop == null ||
                 ( stop.getTokenIndex() < start.getTokenIndex() &&
                  stop.getType() != Token.EOF) ) {
                // sometimes resync does not consume a token (when LT(1) is
                // in follow set.  So, stop will be 1 to left to start. adjust.
                // Also handle case where start is the first token and no token
                // is consumed during recovery; LT(-1) will return null.
                stop = start;
            }
            this.input = input;
            this.start = start;
            this.stop = stop;
            this.trappedException = e;
        }

        @Override
        public boolean isNil() { return false; }

        @Override
        public String getText()
        {
            String badText = null;
            if (start instanceof Token) {
                int i = start.getTokenIndex();
                int j = stop.getTokenIndex();
                if (stop.getType() == Token.EOF) {
                    j = ((TokenStream)input).size();
                }
                badText = ((TokenStream)input).toString(i, j);
            } else if (start instanceof Tree) {
                badText = ((TreeNodeStream)input).toString(start, stop);
            } else {
                // people should subclass if they alter the tree type so this
                // next one is for sure correct.
                badText = "<unknown>";
            }
            return badText;
        }

        @Override
        public void setText(String value) { }

        @Override
        public int getType() { return Token.INVALID_TOKEN_TYPE; }

        @Override
        public void setType(int value) { }

        @Override
        public String toString()
        {
            if (trappedException instanceof MissingTokenException)
            {
                return "<missing type: " +
                       ( (MissingTokenException)trappedException ).getMissingType() +
                       ">";
            } else if (trappedException instanceof UnwantedTokenException) {
                return "<extraneous: " +
                       ( (UnwantedTokenException)trappedException ).getUnexpectedToken() +
                       ", resync=" + getText() + ">";
            } else if (trappedException instanceof MismatchedTokenException) {
                return "<mismatched token: " + trappedException.token + ", resync=" + getText() + ">";
            } else if (trappedException instanceof NoViableAltException) {
                return "<unexpected: " + trappedException.token +
                       ", resync=" + getText() + ">";
            }
            return "<error: " + getText() + ">";
        }
    }

    public static class grammar_Adaptor extends CommonTreeAdaptor {
        private final ANTLRErrorProvidingParser parser;

        public grammar_Adaptor(ANTLRErrorProvidingParser parser) {
            this.parser = parser;
        }

        @Override
        public Object create(Token payload) {
            GrammarAST t = new GrammarAST(payload);
            if (parser != null)
                t.enclosingRuleName = parser.getCurrentRuleName();
            return t;
        }

        @Override
        public Object errorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
            GrammarAST t = new GrammarASTErrorNode(input, start, stop, e);
            if (parser != null)
                t.enclosingRuleName = parser.getCurrentRuleName();

            return t;
        }
    }
}
