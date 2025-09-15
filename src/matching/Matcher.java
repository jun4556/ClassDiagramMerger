package matching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.UmlClass;
import model.UmlDiagram;

/**
 * 2つのUmlDiagramを比較し、対応するUmlClass要素をマッチングさせるクラス。（最終版）
 */
public class Matcher {

    // しきい値は0.2を維持
    private static final double SIMILARITY_THRESHOLD = 0.2;

    public Map<UmlClass, UmlClass> match(UmlDiagram diagramA, UmlDiagram diagramB) {
        Map<UmlClass, UmlClass> matches = new HashMap<>();
        Set<UmlClass> unmatchedB = new HashSet<>(diagramB.getClasses());

        for (UmlClass classA : diagramA.getClasses()) {
            UmlClass bestMatch = null;
            double highestScore = 0.0;

            for (UmlClass classB : unmatchedB) {
                // 1. 名前の類似度
                int maxLen = Math.max(classA.name.length(), classB.name.length());
                double nameDistance = UmlClass.calculateLevenshteinDistance(classA.name, classB.name);
                double nameSimilarity = (maxLen > 0) ? (1.0 - (nameDistance / maxLen)) : 1.0;
                
                // 2. ★★★ 改良点 ★★★
                //    双方向で属性の類似度を計算する
                double contentSimilarity = calculateSymmetricalContentSimilarity(classA, classB);

                // 3. 総合スコア
                double totalScore = 0.5 * nameSimilarity + 0.5 * contentSimilarity;

                if (totalScore > highestScore) {
                    highestScore = totalScore;
                    bestMatch = classB;
                }
            }
            
            if (bestMatch != null && highestScore > SIMILARITY_THRESHOLD) {
                matches.put(classA, bestMatch);
                unmatchedB.remove(bestMatch);
            }
        }
        
        return matches;
    }
    
    /**
     * A->B と B->A の両方から属性類似度を計算し、その平均を返します。
     */
    private double calculateSymmetricalContentSimilarity(UmlClass classA, UmlClass classB) {
        double scoreAB = calculateDirectedContentSimilarity(classA, classB); // AからBを見る
        double scoreBA = calculateDirectedContentSimilarity(classB, classA); // BからAを見る
        
        return (scoreAB + scoreBA) / 2.0; // 両方の平均を取る
    }

    /**
     * Aの各属性について、B内で最も似ている属性を見つけ、その類似度の平均を返します。(片方向)
     */
    private double calculateDirectedContentSimilarity(UmlClass classA, UmlClass classB) {
        Set<String> attrsA = classA.attributes;
        Set<String> attrsB = classB.attributes;

        if (attrsA.isEmpty() && attrsB.isEmpty()) return 1.0;
        if (attrsA.isEmpty() || attrsB.isEmpty()) return 0.0;

        double totalBestMatchScore = 0.0;
        for (String attrA : attrsA) {
            double highestSimilarityForAttrA = 0.0;
            for (String attrB : attrsB) {
                int maxLen = Math.max(attrA.length(), attrB.length());
                double distance = UmlClass.calculateLevenshteinDistance(attrA, attrB);
                double similarity = (maxLen > 0) ? (1.0 - (distance / maxLen)) : 1.0;
                
                if (similarity > highestSimilarityForAttrA) {
                    highestSimilarityForAttrA = similarity;
                }
            }
            totalBestMatchScore += highestSimilarityForAttrA;
        }

        return totalBestMatchScore / attrsA.size();
    }
}