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

	private String HANDLE    = "(\\w+)";
	private String FILE_PATH = "[\"\']([^\"\']+)[\"\']";
	private String ELEMENT   = "([^\\s/_>=<\\-:;,.$#!*+?{}()|\\\\]|\\[[^\\]]+\\])+";
	private String CONTINUE  = '(' + ELEMENT + "\\s+)*" + ELEMENT + '$';
	private String AS        = "\\s+(as\\s+)?";

	Regex COMMENT  = new Regex("%.*");
	Regex COMPOUND = new Regex("COMPOUND",    true);
	Regex BREAK    = new Regex("BREAK",       true);
	Regex RESERVE  = new Regex("RESERVE\\s+", true);
	Regex MODE     = new Regex("MODE\\s+",    true);
	Regex EXECUTE  = new Regex("EXECUTE\\s+", true);
	Regex IMPORT   = new Regex("IMPORT\\s+",  true);
	Regex LOAD     = new Regex("LOAD\\s+",    true);
	
	Regex CLOSE    = new Regex("CLOSE\\s+" + HANDLE + AS + FILE_PATH, true);
	Regex WRITE    = new Regex("WRITE\\s+" + HANDLE + AS + FILE_PATH, true);
	Regex OPEN     = new Regex("OPEN\\s+" + FILE_PATH + AS + HANDLE,  true);
	
	Regex RULE          = new Regex("(\\[[^]]+]|[^>])+\\s+>");
	Regex VAR_NEXT_LINE = new Regex(CONTINUE);
	Regex CONTINUATION  = new Regex("\\s*(/|or|not)", true);
	Regex QUOTES        = new Regex("[\"']");
	Regex PATH          = new Regex("[\\\\/][^/\\\\]*$");
}
