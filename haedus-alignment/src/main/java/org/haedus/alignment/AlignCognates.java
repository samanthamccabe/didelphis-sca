package org.haedus.alignment;

import org.apache.commons.io.FileUtils;
import org.haedus.datatypes.Table;
import org.haedus.datatypes.phonetic.Alignment;
import org.haedus.datatypes.phonetic.FeatureModel;
import org.haedus.datatypes.phonetic.Segment;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.VariableStore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class AlignCognates {

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {

        // For now, we just want to load some strings and split them into segments.
        String rootPath     = "E:/program/MCAS/";
        String lexiconPath  = rootPath + "iir.csv";
        String featuresPath = rootPath + "feature.table";
        String weightsPath  = rootPath + "feature_optimal.weights";
        String outputPath   = rootPath + "iir_aligned_optimal.csv";

		List<String> lexicon = FileUtils.readLines(new File(lexiconPath));

        FeatureModel model = new FeatureModel(featuresPath, weightsPath);
        Map<Alignment,Integer> correspondences = new HashMap<Alignment,Integer>();

		List<Alignment> alignments = alignLexicon(model, lexicon, correspondences);

		FileUtils.writeLines(new File(outputPath), alignments);
    }

    public static Alignment alignSequences(Sequence left, Sequence right,
            FeatureModel table, Map<Alignment,Integer> correspondences) {

        int m = left.size();
        int n = right.size();

        Table<Alignment> matrix = new Table<Alignment>(new Alignment(),m, n);

        Alignment startingAlignment = new Alignment();
        startingAlignment.add(table.get("#"), table.get("#"));
        matrix.set(startingAlignment, 0, 0);

        for (int i = 1; i < m; i++) {   // Fill Left
            Alignment current = new Alignment(matrix.get(i - 1, 0));
            current.add(left.get(i), table.gap());
            current.score(table);
            matrix.set(current, i, 0);
        }
        for (int j = 1; j < n; j++) {   // Fill Right
            Alignment current = new Alignment(matrix.get(0, j - 1));
            current.add(table.gap(), right.get(j));
            current.score(table);
            matrix.set(current, 0, j);
        }
        for (int i = 1; i < m; i++) {
            Segment a = left.get(i);
            for (int j = 1; j < n; j++) {
                Segment b = right.get(j);
                Alignment selected = findMinimum(table, matrix, i, j, a, b, correspondences);
                Alignment pair     = selected.getLast();
                matrix.set(selected, i, j);
                updateCorrespondences(correspondences, pair);
            }
        }
        return matrix.get(m - 1, n - 1);
    }

    private static void updateCorrespondences(Map<Alignment, Integer> map, Alignment pair) {
        if (map.containsKey(pair)) {
            int count = map.get(pair);
            count++;
            map.put(pair,count);
        } else {
            map.put(pair,1);
        }
    }

    private static Alignment findMinimum(FeatureModel table, Table<Alignment> matrix,
            int i, int j, Segment a, Segment b, Map<Alignment,Integer> correspondences) {
        Segment g = table.gap();
        Alignment change = new Alignment(matrix.get( i-1, j-1 ));
        Alignment insert = new Alignment(matrix.get( i-1, j   ));
        Alignment delete = new Alignment(matrix.get( i  , j-1 ));

        Alignment changePair = new Alignment(a,b);
        Alignment insertPair = new Alignment(a,g);
        Alignment deletePair = new Alignment(g,b);

        change.add(changePair);
        insert.add(insertPair);
        delete.add(deletePair);

        double changeScore = change.score(table) / surprisal(changePair,correspondences);
        double insertScore = insert.score(table) / surprisal(insertPair,correspondences);
        double deleteScore = delete.score(table) / surprisal(deletePair,correspondences);

        // Rather than have a MIN function, the tree sorts
        NavigableMap<Double,Alignment> candidates = new TreeMap<Double, Alignment>();
        candidates.put(changeScore, change);
        candidates.put(insertScore, insert);
        candidates.put(deleteScore, delete);
        return candidates.firstEntry().getValue();
    }

    private static double surprisal(Alignment a, Map<Alignment, Integer> c) {
        double surprisal = 1.0;
        if (c.containsKey(a)) {
            surprisal = Math.log10(c.get(a));
        }
        return surprisal;
    }

    public static List<Alignment> alignLexicon(FeatureModel model, Collection<String> lexicon,
            Map<Alignment,Integer> correspondences) {

        List<Alignment> alignedLexicon = new ArrayList<Alignment>(lexicon.size());

        // And then process the file
	    for (String l : lexicon) {
		    String[] line = l.split("\t");
		    // Process and initialize our word pairs
			Sequence left  = new Sequence("#" + line[0], model, new VariableStore());
			Sequence right = new Sequence("#" + line[1], model, new VariableStore());
		    Alignment result = alignSequences(left, right, model, correspondences);
		    alignedLexicon.add(result);
	    }
        return alignedLexicon;
    }

    public static double alignLexicon(FeatureModel model, List<String> lexicon, List<String> standard) {
        double score = 0;

        // And then process the file
        for (int i = 0; i < lexicon.size(); i++) {
            String l = lexicon.get(i);
            String s = standard.get(i);

            String[] line = l.split("\t");
            String[] stan = s.split("\t");

            // Process and initialize our word pairs
            Sequence left  = new Sequence("#" + line[0]);
            Sequence right = new Sequence("#" + line[1]);

            Sequence leftStandard  = new Sequence("#" + stan[0].replaceAll(" ", ""));
            Sequence rightStandard = new Sequence("#" + stan[1].replaceAll(" ", ""));

            Alignment result = alignSequences(left, right, model, new HashMap<Alignment,Integer>());
            Alignment human = new Alignment(leftStandard, rightStandard);

            // Perform Evaluation
            double value = 0;
            if (!result.equals(human)) {
                value = 1;
            }
            score += value;
        }
        return score;
    }
}
