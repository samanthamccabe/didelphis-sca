/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.tables;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * Author: Samantha Fiona Morrigan McCabe
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

	public List<Double> getBackingList() {
		return array;
	}

	@Override
	public String getPrettyTable() {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getNumberRows(); i++) {
			for (int j = 0; j <= i;j ++) {
				Double value = get(i, j);
				String format = FORMAT.format(value);
				if (!format.startsWith("-")) {
					sb.append(" ").append(format);
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
				sb.append(" ").append(format);
			} else {
				sb.append(format);
			}
			sb.append("\t");
		}

		return sb.toString().trim();
	}
}
