/*
 *  Copyright (c) 2012 Sam Harwell, Tunnel Vision Laboratories LLC
 *  All rights reserved.
 *
 *  The source code of this document is proprietary work, and is not licensed for
 *  distribution. For information about licensing, contact Sam Harwell at:
 *      sam@tunnelvisionlabs.com
 */
package org.tvl.goworks.editor.go.completion;

import com.tvl.spi.editor.completion.CompletionProvider;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.antlr.netbeans.editor.classification.TokenTag;
import org.antlr.netbeans.editor.tagging.TaggedPositionRegion;
import org.antlr.netbeans.editor.tagging.Tagger;
import org.antlr.netbeans.editor.text.DocumentSnapshot;
import org.antlr.netbeans.editor.text.NormalizedSnapshotPositionRegionCollection;
import org.antlr.netbeans.editor.text.OffsetRegion;
import org.antlr.netbeans.editor.text.SnapshotPositionRegion;
import org.antlr.netbeans.editor.text.VersionedDocumentUtilities;
import org.antlr.netbeans.parsing.spi.ParserData;
import org.antlr.netbeans.parsing.spi.ParserDataOptions;
import org.antlr.netbeans.parsing.spi.ParserTaskManager;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.works.editor.antlr4.completion.AbstractCompletionProvider;
import org.antlr.works.editor.antlr4.completion.AbstractCompletionQuery;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.tvl.goworks.editor.GoEditorKit;
import org.tvl.goworks.editor.go.GoParserDataDefinitions;
import org.tvl.goworks.editor.go.parser.GoLexer;

/**
 *
 * @author Sam Harwell
 */
@MimeRegistration(mimeType=GoEditorKit.GO_MIME_TYPE, service=CompletionProvider.class)
@NbBundle.Messages({
    "GCP-imported-items=",
    "GCP-instance-members="
})
public class GoCompletionProvider extends AbstractCompletionProvider {
    // -J-Dorg.tvl.goworks.editor.go.completion.GoCompletionProvider.level=FINE
    private static final Logger LOGGER = Logger.getLogger(GoCompletionProvider.class.getName());

    private static String goCompletionAutoPopupTriggers = ".";
    private static String goCompletionSelectors = "{}[]()<>.,:;+-*/%&|^!~=?@#'\"\\ ";

    public static void incompleteCompletionSupport() {
        assert false : "Editor code completion for Go is not yet complete.";
    }

    public static void incompleteCompletionSupport(String message) {
        assert false : message;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        if (typedText == null || typedText.length() != 1) {
            return 0;
        }

        boolean triggered = getCompletionAutoPopupTriggers().indexOf(typedText.charAt(0)) >= 0;
        if (triggered || (autoPopupOnIdentifierPart() && GoCompletionQuery.isIdentifierPart(typedText))) {
            int offset = component.getSelectionStart() - 1;
            Token contextToken = getContext(component, offset);
            if (contextToken == null) {
                return 0;
            }

            if (!triggered) {
                // the caret must be at the end of the identifier. note that the
                // offset is already 1 position before the caret, so no need to
                // add 1 to contextToken.getStopIndex().
                if (offset != contextToken.getStopIndex()) {
                    return 0;
                }

                // only trigger for the first character of the identifier
                if (contextToken.getStopIndex() > contextToken.getStartIndex()) {
                    return 0;
                }
            }

            boolean allowInStrings = false;
            if (isGoContext(contextToken, offset, allowInStrings)) {
                return COMPLETION_QUERY_TYPE | AUTO_QUERY_TYPE;
            }
        }

        return 0;
    }

    @Override
    protected AbstractCompletionQuery createCompletionQuery(int queryType, int caretOffset, boolean extend) {
        return new GoCompletionQuery(this, queryType, caretOffset, true, extend);
    }

    @Override
    public boolean autoPopupOnIdentifierPart() {
        return true;
    }

    @Override
    public String getCompletionAutoPopupTriggers() {
        return goCompletionAutoPopupTriggers;
    }

    @Override
    public String getCompletionSelectors() {
        return goCompletionSelectors;
    }

    @Override
    public Token getContext(JTextComponent component, int offset) {
        return getContext(component.getDocument(), offset);
    }

    @Override
    public Token getContext(Document document, int offset) {
        Parameters.notNull("document", document);

        if (document instanceof AbstractDocument) {
            ((AbstractDocument)document).readLock();
        }

        try {
//            try {
                ParserTaskManager taskManager = Lookup.getDefault().lookup(ParserTaskManager.class);
                DocumentSnapshot snapshot = VersionedDocumentUtilities.getVersionedDocument(document).getCurrentSnapshot();
                Future<ParserData<Tagger<TokenTag<Token>>>> futureTokensData = taskManager.getData(snapshot, GoParserDataDefinitions.LEXER_TOKENS, EnumSet.of(ParserDataOptions.SYNCHRONOUS));
                if (futureTokensData == null) {
                    return null;
                }

                Tagger<TokenTag<Token>> tagger;
                try {
                    tagger = futureTokensData.get().getData();
                    if (tagger == null) {
                        return null;
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    LOGGER.log(Level.WARNING, "An exception occurred while getting tokens.", ex);
                    return null;
                }

                // get the token(s) at the cursor position, with affinity both directions
                OffsetRegion region = OffsetRegion.fromBounds(Math.max(0, offset - 1), Math.min(snapshot.length(), offset + 1));
                Iterable<TaggedPositionRegion<TokenTag<Token>>> tags = tagger.getTags(new NormalizedSnapshotPositionRegionCollection(new SnapshotPositionRegion(snapshot, region)));

                // TODO: cache tokens
//                ANTLRInputStream input = new ANTLRInputStream(document.getText(0, document.getLength()));
//                GoLexer lexer = new GoLexer(input);
//                CommonTokenStream tokenStream = new CommonTokenStream(lexer);
                Token token = null;
//                for (token = tokenStream.LT(1); token != null && token.getType() != Token.EOF; token = tokenStream.LT(1)) {
//                    tokenStream.consume();
//                    if (token.getStartIndex() <= offset && token.getStopIndex() >= offset) {
//                        break;
//                    }
//                }
                for (TaggedPositionRegion<TokenTag<Token>> taggedRegion : tags) {
                    if (taggedRegion.getTag().getToken().getChannel() != Lexer.DEFAULT_TOKEN_CHANNEL) {
                        continue;
                    }

                    token = taggedRegion.getTag().getToken();
                    if (token.getStartIndex() <= offset && token.getStopIndex() >= offset) {
                        break;
                    }
                }

                if (token == null) {
                    // try again without skipping off-channel tokens
                    for (TaggedPositionRegion<TokenTag<Token>> taggedRegion : tags) {
                        token = taggedRegion.getTag().getToken();
                        if (token.getStartIndex() <= offset && token.getStopIndex() >= offset) {
                            break;
                        }
                    }
                }

                return token;
                //List<Token> tokens;
//            } catch (BadLocationException ex) {
//                Exceptions.printStackTrace(ex);
//                return null;
//            }
        } finally {
            if (document instanceof AbstractDocument) {
                ((AbstractDocument)document).readUnlock();
            }
        }
    }

    @Override
    public boolean isContext(Token token, int offset, int queryType) {
        return isGoContext(token, offset, false);
    }

    /*package*/ boolean isGoContext(JTextComponent component, int offset, boolean allowInStrings) {
        return isGoContext(getContext(component, offset), offset, allowInStrings);
    }

    /*package*/ static boolean isGoContext(Token token, int offset, boolean allowInStrings) {
        if (token == null) {
            return false;
        }

        switch (token.getType()) {
        case GoLexer.COMMENT:
            return false;

        case GoLexer.CharLiteral:
        case GoLexer.StringLiteral:
            return allowInStrings;

        case GoLexer.WS:
        case GoLexer.NEWLINE:
            return true;

        default:
            return token.getChannel() == Lexer.DEFAULT_TOKEN_CHANNEL;
        }
    }

}
