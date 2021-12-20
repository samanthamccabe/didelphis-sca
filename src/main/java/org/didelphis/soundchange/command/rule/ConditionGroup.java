package org.didelphis.soundchange.command.rule;

import org.didelphis.language.automata.Regex;
import org.didelphis.language.automata.matching.Match;
import org.didelphis.language.parsing.ParseException;
import org.didelphis.language.phonetic.SequenceFactory;
import org.didelphis.language.phonetic.sequences.Sequence;
import org.didelphis.soundchange.VariableStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConditionGroup implements Condition {

	private static final Regex NOT = new Regex("\\s*not\\s*", true);
	private static final Regex OR  = new Regex("\\s*or\\s*", true);

	private final VariableStore variables;
	private final SequenceFactory factory;

	private final Collection<Condition> conditions;
	private final Collection<Condition> exceptions;

	public ConditionGroup(
			VariableStore variables,
			SequenceFactory factory,
			String condtionString
	) {
		this.variables = variables;
		this.factory = factory;

		conditions = new ArrayList<>();
		exceptions = new ArrayList<>();

		parseCondition(condtionString);
	}

	@Override
	public String toString() {
		String condition = conditions.stream()
				.map(Objects::toString)
				.collect(Collectors.joining(" or "));
		String exception = exceptions.stream()
				.map(Objects::toString)
				.map(string -> "not " + string)
				.collect(Collectors.joining());
		return condition + exception;
	}

	@Override
	public boolean isMatch(Sequence word, int index) {
		return isMatch(word, index, index + 1);
	}

	@Override
	public boolean isMatch(Sequence word, int startIndex, int endIndex) {


		// this must equal `false` in initially
		boolean conditionMatch = false;
		Iterator<Condition> cI = conditions.iterator();
		if (cI.hasNext()) {
			while (cI.hasNext() && !conditionMatch) {
				Condition condition = cI.next();
				conditionMatch = condition.isMatch(word, startIndex, endIndex);
			}
		} else {
			conditionMatch = true;
		}


		boolean exceptionMatch = false;
		Iterator<Condition> eI = exceptions.iterator();
		if (eI.hasNext()) {
			while (eI.hasNext() && !exceptionMatch) {
				Condition exception = eI.next();
				exceptionMatch = exception.isMatch(word, startIndex, endIndex);
			}
		}
		return conditionMatch && !exceptionMatch;
	}

	private void parseCondition(String conditionString) {
		Match<String> notMatcher = NOT.match(conditionString);
		if (notMatcher.matches()) {
			// if there is no regular condition
			// Takes the first one off, and splits on the restde
			for (String clause : NOT.split(conditionString)) {
				Match<String> orMatch = OR.find(clause);
				if (orMatch.matches()) {
					throw new ParseException("OR not allowed following a NOT");
				}

				String trim = clause.trim();
				if (!trim.isEmpty()) {
					exceptions.add(new ConditionClause(trim, variables, factory));
				}
			}

		} else if (NOT.find(conditionString).matches()) {
			List<String> split = NOT.split(conditionString, 1);
			String conditionClauses = split.get(0);
			String exceptionClauses = split.get(1);

			for (String con : OR.split(conditionClauses, -1)) {
				conditions.add(new ConditionClause(con, variables, factory));
			}

			for (String exc : NOT.split(exceptionClauses, -1)) {
				exceptions.add(new ConditionClause(exc, variables, factory));
			}

		} else {
			for (String s : OR.split(conditionString, -1)) {
				if (s.trim().isEmpty()){
					throw new ParseException("Dangling OR");
				}
				conditions.add(new ConditionClause(s, variables, factory));
			}
		}
	}
}
