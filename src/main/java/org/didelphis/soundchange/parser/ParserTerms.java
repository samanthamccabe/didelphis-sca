package org.didelphis.soundchange.parser;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.didelphis.language.automata.Regex;

/**
 * Class {@code ParserTerms}
 *
 * A holder for patterns used to parse scripts
 * 
 * @author Samantha Fiona McCabe
 */
@UtilityClass
@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
public class ParserTerms {

	public final Regex KEYWORDS = new Regex("BREAK|RESERVE|MODE|EXECUTE|IMPORT|LOAD|CLOSE|WRITE|OPEN", true);
	private final String HANDLE    = "(\\w+)";
	private final String FILE_PATH = "[\"\']([^\"\']+)[\"\']";
	private final String ELEMENT   = "([^\\s/_>=<\\-:;,.$#!*+?{}()|\\\\]|\\[[^\\]]+\\])+";
	private final String CONTINUE  = '(' + ELEMENT + "\\s+)*" + ELEMENT + '$';
	private final String AS        = "\\s+(as\\s+)?";

	public final Regex SPECIAL  = new Regex("[-=/_><;:,.$#!*+?{}()|\\\\]");
	public final Regex COMMENT  = new Regex("%.*");
	public final Regex COMPOUND = new Regex("COMPOUND",    true);
	public final Regex BREAK    = new Regex("BREAK",       true);
	public final Regex RESERVE  = new Regex("RESERVE\\s+", true);
	public final Regex MODE     = new Regex("MODE\\s+",    true);
	public final Regex EXECUTE  = new Regex("EXECUTE\\s+", true);
	public final Regex IMPORT   = new Regex("IMPORT\\s+",  true);
	public final Regex LOAD     = new Regex("LOAD\\s+",    true);
	public final Regex CLOSE    = new Regex("CLOSE\\s+" + HANDLE + AS + FILE_PATH, true);
	public final Regex WRITE    = new Regex("WRITE\\s+" + HANDLE + AS + FILE_PATH, true);
	public final Regex OPEN     = new Regex("OPEN\\s+" + FILE_PATH + AS + HANDLE,  true);
	public final Regex RULE          = new Regex("(\\[[^]]+]|[^>])+\\s+>");
	public final Regex VAR_NEXT_LINE = new Regex(CONTINUE);
	public final Regex CONTINUATION  = new Regex("\\s*(/|or|not)", true);
	public final Regex QUOTES        = new Regex("[\"']");
	public final Regex PATH          = new Regex("(.*[\\\\/])([^/\\\\]*)$");
}
