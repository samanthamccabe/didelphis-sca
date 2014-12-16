package org.haedus.datatypes.tables;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Author: goats
 * Created: 12/14/2014
 */
public class WeightMatrix extends SymmetricTable<Double> {

	private static final NumberFormat FORMAT_PREC = new DecimalFormat("0.0000");
	private static final NumberFormat FORMAT      = new DecimalFormat("0.00");

	public WeightMatrix(Double defaultValue, int n) {
		super(defaultValue, n);
	}

	public WeightMatrix(WeightMatrix other) {
		super(other.getNumberRows(), other.getNumberColumns());
		array.addAll(other.array);
	}

	@Override
	public String getPrettyTable() {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getNumberRows(); i++) {
			for (int j = 0; j <= i;j ++) {
				Double value = get(i, j);
				String format = FORMAT.format(value);
				if (!format.startsWith("-")) {
					sb.append(" "+format);
				} else {
					sb.append(format);
				}
				if (j < i) {
					sb.append("\t");
				}
			}
			if (i < getNumberRows() -1) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public String getLinear() {
		StringBuilder sb = new StringBuilder();

		for (Double value : array) {
			String format = FORMAT_PREC.format(value);
			if (!format.startsWith("-")) {
				sb.append(" "+format);
			} else {
				sb.append(format);
			}
			sb.append("\t");
		}

		return sb.toString().trim();
	}
}
