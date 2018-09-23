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

	private String HANDLE    = "([A-Z0-9_]+)";
	private String FILE_PATH = "[\"\']([^\"\']+)[\"\']";
	private String ELEMENT   = "([^\\s/_>=<\\-:;,.$#!*+?{}()|\\\\]|\\[[^]]+])+";
	private String AS        = "\\s+(as\\s+)?";

	Regex COMMENT  = new Regex("%.*");
	Regex COMPOUND = new Regex("COMPOUND",    0x02);
	Regex BREAK    = new Regex("BREAK",       0x02);
	Regex RESERVE  = new Regex("RESERVE\\s+", 0x02);
	Regex MODE     = new Regex("MODE\\s+",    0x02);
	Regex EXECUTE  = new Regex("EXECUTE\\s+", 0x02);
	Regex IMPORT   = new Regex("IMPORT\\s+",  0x02);
	Regex LOAD     = new Regex("LOAD\\s+",    0x02);
	
	Regex CLOSE    = new Regex("CLOSE\\s+" + HANDLE + AS + FILE_PATH, 0x02);
	Regex WRITE    = new Regex("WRITE\\s+" + HANDLE + AS + FILE_PATH, 0x02);
	Regex OPEN     = new Regex("OPEN\\s+" + FILE_PATH + AS + HANDLE,  0x02);
	
	Regex RULE          = new Regex("(\\[[^]]+]|[^>])+\\s+>");
	Regex VAR_NEXT_LINE = new Regex('(' + ELEMENT + "\\s+)*" + ELEMENT + '$');
	Regex CONTINUATION  = new Regex("\\s*(/|or|not)", 0x02);
	Regex QUOTES        = new Regex("[\"']");
	Regex PATH          = new Regex("[\\\\/][^/\\\\]*$");
}
