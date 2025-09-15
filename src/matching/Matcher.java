package matching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.UmlClass;
import model.UmlDiagram;

/**
 * 2つのUmlDiagramを比較し、対応するUmlClass要素をマッチングさせるクラス。
 */
public class Matcher {

    // 2つのクラスが「同じ」と見なされるための類似度のしきい値
    private static final double SIMILARITY_THRESHOLD = 0.5;

    /**
     * 2つのダイアグラム間でクラスのマッチングを行います。
     * @param diagramA 比較元となるダイアグラム
     * @param diagramB 比較先となるダイアグラム
     * @return マッチしたUmlClassのペアを格納したMap
     */
    public Map<UmlClass, UmlClass> match(UmlDiagram diagramA, UmlDiagram diagramB) {
        // 結果を格納するマップ
        Map<UmlClass, UmlClass> matches = new HashMap<>();
        
        // diagramBの中で、まだマッチしていないクラスの集合
        Set<UmlClass> unmatchedB = new HashSet<>(diagramB.getClasses());

        // --- Step 1: IDベースのマッチング ---
        // 最も確実な方法から先に行う
        for (UmlClass classA : diagramA.getClasses()) {
            UmlClass classB = diagramB.getClassById(classA.id);
            if (classB != null) {
                matches.put(classA, classB);
                unmatchedB.remove(classB); // マッチしたので集合から削除
            }
        }
        
        // --- Step 2: ヒューリスティックマッチング ---
        // IDでマッチしなかった残りのクラスに対して、類似度ベースのマッチングを行う
        for (UmlClass classA : diagramA.getClasses()) {
            // 既にIDでマッチ済みのクラスはスキップ
            if (matches.containsKey(classA)) {
                continue;
            }

            UmlClass bestMatch = null;
            double highestScore = 0.0;

            // マッチしていないdiagramBの全クラスと比較
            for (UmlClass classB : unmatchedB) {
                // 1. 名前の類似度を計算 (0.0 ~ 1.0に正規化)
                int maxLen = Math.max(classA.name.length(), classB.name.length());
                double nameDistance = UmlClass.calculateLevenshteinDistance(classA.name, classB.name);
                double nameSimilarity = (maxLen > 0) ? (1.0 - (nameDistance / maxLen)) : 1.0;
                
                // 2. 内容の類似度を計算 (Jaccard係数)
                double contentSimilarity = classA.calculateJaccardSimilarity(classB);

                // 3. 総合スコアを計算 (重み付け)
                double totalScore = 0.6 * nameSimilarity + 0.4 * contentSimilarity;

                if (totalScore > highestScore) {
                    highestScore = totalScore;
                    bestMatch = classB;
                }
            }
            
            // 最もスコアが高かったペアがしきい値を超えていれば、マッチと見なす
            if (bestMatch != null && highestScore > SIMILARITY_THRESHOLD) {
                matches.put(classA, bestMatch);
                unmatchedB.remove(bestMatch); // マッチしたので集合から削除
            }
        }
        
        return matches;
    }
}