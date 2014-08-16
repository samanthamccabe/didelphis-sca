package org.haedus.soundchange;

import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;
import org.haedus.soundchange.exceptions.RuleFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: goats
 * Date: 4/7/13
 * Time: 5:40 PM
 */
public class Rule {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(Rule.class);

	private final String         ruleText;
	private final List<Sequence> source;
	private final List<Sequence> target;
	private final Condition      condition;
	private final VariableStore  variableStore;
	private final FeatureModel   featureModel;

	public Rule(String rule) throws RuleFormatException
	{
		this(rule, new VariableStore(), true);
	}

	public Rule(String rule, FeatureModel model, VariableStore variables, boolean useSegmentation) throws RuleFormatException {
		ruleText      = rule;
		variableStore = variables;
		featureModel  = new FeatureModel();
		source        = new ArrayList<Sequence>();
		target        = new ArrayList<Sequence>();

		String transform;
		// Check-and-parse for conditions
		if (ruleText.contains("/")) {
			String[] array = ruleText.split("/");
			if (array.length <= 1) {
				throw new RuleFormatException("Condition was empty!");
			} else {
				transform = array[0].trim();
				condition = new Condition(array[1].trim(), variableStore, model);
			}
		} else {
			transform = ruleText;
			condition = new Condition();
		}
		parseTransform(transform, useSegmentation);
	}

	public Rule(String rule, VariableStore variables, boolean useSegmentation) throws RuleFormatException {
		this(rule, new FeatureModel(), variables, useSegmentation);
	}

	public List<Sequence> getTarget() {
		return Collections.unmodifiableList(target);
	}

	public List<Sequence> getSource() {
		return Collections.unmodifiableList(source);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Sequence sequence : getSource()) {
			sb.append(sequence.toStringClean());
			sb.append(" ");
		}
		sb.append("> ");
		for (Sequence sequence : getTarget()) {
			sb.append(sequence.toStringClean());
			sb.append(" ");
		}
		sb.append("/ ");
		sb.append(condition.toString());

		return sb.toString();
	}

	public void execute(SoundChangeApplier sca) {
		for (List<Sequence> lexicon : sca.getLexicons()) {

			for (int i = 0; i < lexicon.size(); i++) {
				Sequence word = lexicon.get(i);
				lexicon.set(i, apply(word));
			}
		}
	}

	public Sequence apply(Sequence input) {
//		Sequence output = input.copy();
		Sequence output = new Sequence(input);

		assert(output.equals(input));
		
		for (int index = 0; index < output.size(); index++) {
			
			for (int i = 0; i < source.size(); i++) {
                Sequence sourceSequence = source.get(i);
                Sequence targetSequence = target.get(i);

                if (index < output.size()) {
                	Sequence subsequence = output.getSubsequence(index);        
                    if (subsequence.startsWith(sourceSequence)) {
                        int size = sourceSequence.size();

                        if (condition.isEmpty() || condition.isMatch(output, index, index + size)) {
                            output.remove(index, index + size);
                            if (!targetSequence.equals(new Sequence("0")))
                                output.insert(targetSequence, index);
                        }
	                    if (i < source.size() - 1) {
		                    index = index + 1;
	                    }
                    }
                }
			}
		}
		return output;
	}

	private List<String> toList(String string) {
		List<String> list = new ArrayList<String>();
		if (!string.isEmpty()) {
			string = string.trim();
			Collections.addAll(list, string.split("\\s+"));
		}
		return list;
	}

	private void parseTransform(String transform, boolean useSegmentation) throws RuleFormatException {
		if (transform.contains(">")) {
			String[] array = transform.split("\\s*>\\s*");

			if (array.length <= 1) {
				throw new RuleFormatException("Malformed transformation! " + transform);
			} else {
				List<String> s = toList(array[0]);
				List<String> t = toList(array[1]);

				balanceTransform(s, t);

				for (int i = 0; i < s.size(); i++) {
					List<Sequence> expandedSource = variableStore.expandVariables(s.get(i), useSegmentation);
					List<Sequence> expandedTarget = variableStore.expandVariables(t.get(i), useSegmentation);

					if (expandedTarget.size() < expandedSource.size()) {
						Sequence last = expandedTarget.get(expandedTarget.size() - 1);
						while (expandedTarget.size() < expandedSource.size()) {
							expandedTarget.add(last);
						}
					}
					source.addAll(expandedSource);
					target.addAll(expandedTarget);
				}
			}
		} else {
			throw new RuleFormatException("Rule missing \">\" sign! " + ruleText);
		}
	}

	private void balanceTransform(List<String> s, List<String> t) throws RuleFormatException {
		if (t.size() > s.size()) {
			throw new RuleFormatException("Source/Target size error! " + s + " < " + t);
		}

		if (t.size() < s.size()) {
			if (t.size() == 1) {
				String first = t.get(0);
				while (t.size() < s.size()) {
					t.add(first);
				}
			} else {
				throw new RuleFormatException("Source/Target size error! " + s + " > " + t + " and target size is greater than 1!");
			}
		}
	}
}
