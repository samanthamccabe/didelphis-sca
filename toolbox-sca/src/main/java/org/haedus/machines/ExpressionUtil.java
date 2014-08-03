package org.haedus.machines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goats on 5/11/2014.
 */
public class ExpressionUtil {

	public static String getGML(Expression root) {

		StringBuilder sb = new StringBuilder("graph [");
		Map<Expression, Integer> idMap = new HashMap<Expression, Integer>();
		List<Expression> list = new ArrayList<Expression>();

		list.add(root);
		addExpressinsToList(list, root);

		for (int i = 0; i < list.size(); i ++) {
			Expression expression = list.get(i);
			idMap.put(expression, i);
			sb.append(getGraphNode(i, expression));
		}

		for (Map.Entry<Expression, Integer> entry : idMap.entrySet()) {
			Expression expression = entry.getKey();
			int index = entry.getValue();

			for (Expression sub : expression.getSubExpressions(true)) {
				Integer targetId = idMap.get(sub);
				sb.append(makeEdge(index, targetId));
			}
		}

		sb.append("\n]");

		return sb.toString();
	}

	private static void addExpressinsToList(List<Expression> list, Expression exp) {
		for (Expression sub : exp.getSubExpressions(true)) {
			list.add(sub);
			addExpressinsToList(list, sub);
		}
	}

	private static String getGraphNode(int index, Expression ex) {
		StringBuilder sb = new StringBuilder("\n\t");
		sb.append("node [");
		sb.append("\n\t\t");
		sb.append("id ");
		sb.append(index);
		sb.append("\n\t\t");
		sb.append("label \"");

		if (ex.isNegative()) {
			sb.append("!");
		}

		if (ex.isParallel()) {
			sb.append("{}");
		} else {
			sb.append(ex.getSegment());
			sb.append(ex.getSymbol());
		}


		sb.append("\"");

		sb.append("\n\t]");
		return sb.toString();
	}

	private static String makeEdge(int source, int target)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n\tedge [");
		sb.append("\n\t\t");
		sb.append("source ");
		sb.append(source);
		sb.append("\n\t\t");
		sb.append("target ");
		sb.append(target);
		sb.append("\n\t]");

		return sb.toString();
	}
}
