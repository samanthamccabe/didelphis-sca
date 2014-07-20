/**
 *
 */
package org.haedus.alignment;

import org.apache.commons.io.FileUtils;
import org.haedus.datatypes.Table;
import org.haedus.datatypes.phonetic.FeatureModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author goats
 */
public class ModelCalibrator {

    public static void main(String[] args) throws IOException {
		String rootPath     = "E:/program/MCAS/";
		String lexiconPath  = rootPath + "iir.csv";
		String standardPath = rootPath + "iir_human.csv";
		String featuresPath = rootPath + "feature.table";
		String weightsPath  = rootPath + "feature.weights";

		List<String> lexicon  = FileUtils.readLines(new File(lexiconPath));
		List<String> standard = FileUtils.readLines(new File(standardPath));

		FeatureModel model = new FeatureModel(featuresPath, weightsPath);

		ModelCalibrator.genetic(lexicon, standard,model);
    }

	public static void genetic(List<String> lexicon, List<String> standard, FeatureModel model) {

		int time = Math.abs((int) System.nanoTime()/100000);

		int    initialPoolSize       =  50;
		int    numberOfGenerations   = 100;
		int    numberOfBreedingPairs =  70;
		double mutationStrength      =   1.0;
		double mutationProbability   =   0.02;

		ArrayList<Table<Float>> weightsPool = new ArrayList<Table<Float>>();
		Table<Float> optimalTable = new Table<Float>();
		Double       optimalValue = 1000.0;

		for (int i = 0; i < initialPoolSize; i++) {
            Table<Float> candidate = mutate(new Table<Float>(0f,19,19), mutationStrength,mutationProbability);
			weightsPool.add(candidate);
		}

		for (int g = 0; g < numberOfGenerations; g++) {
			//
			TreeMap<Double, Table<Float>> candidates = new TreeMap<Double, Table<Float>>();

			double sumOfScores = 0;
			for (Table<Float> aWeightsPool : weightsPool) {

				double score = 0;
				Table<Float> trialWeights = aWeightsPool;
				FeatureModel trialModel = new FeatureModel(model.getFeatureMap(), trialWeights);

				// --------------------------------------------------------------------------------
				score += AlignCognates.alignLexicon(trialModel, lexicon, standard);
				score += evaluate(trialModel);
				// --------------------------------------------------------------------------------
				candidates.put(score, trialWeights);
				sumOfScores += score;

				System.out.println(g + "\t" + score);

//				progressLog.write(g + "\t" + score + "\n");
//				collectedData.write(score + "\t" + printLinear(trialWeights) + "\n");
			}

			// First gets the min score
			double value = candidates.firstEntry().getKey();
			System.out.println("\t"+value);
			if (optimalValue > value) {
				optimalTable = candidates.firstEntry().getValue();
				optimalValue = value;
			}

			candidates = normalizeCandidates(candidates,sumOfScores);

			weightsPool = new ArrayList<Table<Float>>();
			for (int i = 0; i < numberOfBreedingPairs; i++) {
				Table<Float> left  = select(candidates);
				Table<Float> right = select(candidates);
				Table<Float> child = mutate(breed(left, right),mutationStrength, mutationProbability);
				weightsPool.add(child);
			}
		}
		System.out.println("\nOptimal: "+optimalValue+"\n"+optimalTable);
//		progressLog.close();
//		collectedData.close();
	}

	private static double evaluate(FeatureModel model) {
		double score = 0.0;

		score += (model.computeScore("p","pʰ") < model.computeScore("t","tʰ")) ? 0 : 10;
		score += (model.computeScore("t","tʰ") < model.computeScore("k","kʰ")) ? 0 : 10;
		score += (model.computeScore("k","kʰ") < model.computeScore("q","qʰ")) ? 0 : 10;

		score += (model.computeScore("h","_") < model.computeScore("s","_")) ? 0 : 20;
		score += (model.computeScore("s","_") < model.computeScore("t","_")) ? 0 : 20;

		score += (model.computeScore("k","g") < model.computeScore("t","d")) ? 0 : 10;
		score += (model.computeScore("t","d") < model.computeScore("p","b")) ? 0 : 10;

		score += (model.computeScore("p","m") < model.computeScore("p","n")) ? 0 : 10;
		score += (model.computeScore("t","n") < model.computeScore("p","n")) ? 0 : 10;
		score += (model.computeScore("k","n") < model.computeScore("p","n")) ? 0 : 10;

		score += (model.computeScore("t͜s","s") < model.computeScore("r","s")) ? 0 : 10;

		score += (model.computeScore("ʈ","ɖ") < model.computeScore("ʈ","t")) ? 0 : 10;
		score += (model.computeScore("w","u") < model.computeScore("w","v")) ? 0 : 10;
		score += (model.computeScore("j","i") < model.computeScore("j","z")) ? 0 : 10;

		score += (model.computeScore("i","y") > model.computeScore("e","ø")) ? 0 :  5;
		score += (model.computeScore("e","ø") > model.computeScore("ɛ","œ")) ? 0 :  5;
		score += (model.computeScore("ɛ","œ") > model.computeScore("a","ɶ")) ? 0 :  5;

		score += (model.computeScore("i","u") > model.computeScore("y","ɯ")) ? 0 : 20;
		score += (model.computeScore("i","u") > model.computeScore("a","ɑ")) ? 0 : 20;
		score += (model.computeScore("i","a") > model.computeScore("u","ɑ")) ? 0 : 20;

		return score;
	}

	/**
     *
     * @param left
     * @param right
     * @return
     */
    private static Table<Float> breed(Table<Float> left, Table<Float> right) {
        Table<Float> table = left.copy();
        for (int i = 0; i < left.getNumberRows(); i++) {
            for (int j = 0; j < i; j++) {
                /* Uniform Crossover */
                if (Math.random() < 0.5) {
                    float value = right.get(i, j);
                    table.set(value, i, j);
                    table.set(value, j, i);
                }
            }
        }
        return table;
    }

	private static String printLinear(Table<Float> table) {
		String s = "";

		for (int i = 0; i < table.getNumberRows(); i++ ) {
			for (int j = 0; j < i; j++) {
				s = s.concat(table.get(i,j)+"\t");
			}
		}
		return s.trim();
	}

    /**
     *
     * @param map
     * @param factor
     * @return
     */
    private static TreeMap<Double, Table<Float>> normalizeCandidates(TreeMap<Double, Table<Float>> map, double factor) {
        TreeMap<Double, Table<Float>> candidates = new TreeMap<Double, Table<Float>>();
        for (Double value : map.keySet()) {
            double normalized = value / factor;
            candidates.put(normalized, map.get(value));
        }
        return candidates;
    }

    /**
     *
     * @param table
     * @param mutationFactor
     * @return
     */
    private static Table<Float> mutate(Table<Float> table, double mutationFactor, double mutationProbability) {
        Table<Float> mutatedTable = table.copy();

        for (int i = 0; i < table.getNumberRows(); i++) {
            for (int j = 0; j <= i; j++) {
                double change = (Math.random() - 0.5) * mutationFactor;
                Float value = new Float(mutatedTable.get(i, j) + change);
				if (mutationProbability <= Math.random()) {
					if (i != j) {
						mutatedTable.set(value, i, j);
						mutatedTable.set(value, j, i);
					}
				}
            }
        }
        return mutatedTable;
    }

    /**
     *
     * @param candidates
     * @return
     */
    private static Table<Float> select(TreeMap<Double, Table<Float>> candidates) {

		Table<Float> selected = new Table<Float>();

        double cumulative = 0;
        boolean found = false;

        while (!found) {
            for (Double value : candidates.descendingKeySet()) {
				double random = Math.random();
				if (random > 0.2){
					selected = candidates.get(value);
                    found = true;
				}
            }
        }
        return selected;
    }
}
