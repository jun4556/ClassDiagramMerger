package matching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import model.UmlClass;
import model.UmlDiagram;
import model.UmlRelationship;

/**
 * 2つのUmlDiagramを比較し、対応するUmlClass要素をマッチングさせるクラス。（デバッグ版）
 * 名前、属性、座標、関連の4つの観点から類似度を評価し、その過程を出力します。
 */
public class Matcher {

    private static final double SIMILARITY_THRESHOLD = 0.2;
    private static final double NAME_WEIGHT = 0.4;
    private static final double CONTENT_WEIGHT = 0.3;
    private static final double POSITION_WEIGHT = 0.15;
    private static final double RELATION_WEIGHT = 0.15;

    // ★★★ デバッグ出力のON/OFFを切り替えるフラグを追加 ★★★
    private static final boolean DEBUG_MODE = true;

    public Map<UmlClass, UmlClass> match(UmlDiagram diagramA, UmlDiagram diagramB) {
        Map<UmlClass, UmlClass> matches = new HashMap<>();
        Set<UmlClass> unmatchedB = new HashSet<>(diagramB.getClasses());
        double maxDistance = calculateMaxDistance(diagramA, diagramB);

        if (DEBUG_MODE) {
            System.out.println("\n--- Starting Matcher ---");
            System.out.println("Max distance for position normalization: " + String.format("%.2f", maxDistance));
        }

        for (UmlClass classA : diagramA.getClasses()) {
            UmlClass bestMatch = null;
            double highestScore = 0.0;

            if (DEBUG_MODE) {
                System.out.println("\n----- Matching for Class A: '" + classA.name + "' -----");
            }

            for (UmlClass classB : unmatchedB) {
                // 1. 名前の類似度
                double nameSimilarity = calculateNameSimilarity(classA, classB);
                
                // 2. 属性内容の類似度
                double contentSimilarity = calculateSymmetricalContentSimilarity(classA, classB);

                // 3. 位置関係の類似度
                double positionSimilarity = calculatePositionSimilarity(classA, classB, maxDistance);

                // 4. 参照関係の類似度
                double relationSimilarity = calculateRelationSimilarity(classA, classB, diagramA, diagramB);

                // 5. 総合スコア
                double totalScore = (nameSimilarity * NAME_WEIGHT) +
                                    (contentSimilarity * CONTENT_WEIGHT) +
                                    (positionSimilarity * POSITION_WEIGHT) +
                                    (relationSimilarity * RELATION_WEIGHT);

                // ★★★ ここからが追加したデバッグ出力 ★★★
                if (DEBUG_MODE) {
                    System.out.println("  Comparing with Class B: '" + classB.name + "'");
                    System.out.println(String.format("    - Name Sim:       %.2f (%.2f * %.2f)", nameSimilarity * NAME_WEIGHT, nameSimilarity, NAME_WEIGHT));
                    System.out.println(String.format("    - Content Sim:    %.2f (%.2f * %.2f)", contentSimilarity * CONTENT_WEIGHT, contentSimilarity, CONTENT_WEIGHT));
                    System.out.println(String.format("    - Position Sim:   %.2f (%.2f * %.2f)", positionSimilarity * POSITION_WEIGHT, positionSimilarity, POSITION_WEIGHT));
                    System.out.println(String.format("    - Relation Sim:   %.2f (%.2f * %.2f)", relationSimilarity * RELATION_WEIGHT, relationSimilarity, RELATION_WEIGHT));
                    System.out.println(String.format("    -> Total Score:   %.4f", totalScore));
                }
                // ★★★ デバッグ出力ここまで ★★★

                if (totalScore > highestScore) {
                    highestScore = totalScore;
                    bestMatch = classB;
                }
            }
            
            if (bestMatch != null && highestScore > SIMILARITY_THRESHOLD) {
                if (DEBUG_MODE) {
                    System.out.println("  => Best match found: '" + bestMatch.name + "' with score " + String.format("%.4f", highestScore));
                }
                matches.put(classA, bestMatch);
                unmatchedB.remove(bestMatch);
            } else if (DEBUG_MODE) {
                 System.out.println("  => No suitable match found (highest score: " + String.format("%.4f", highestScore) + ")");
            }
        }
        
        return matches;
    }
    
    // (これ以下のメソッドは前回の回答から変更ありません)
    private double calculateNameSimilarity(UmlClass classA, UmlClass classB) {
        int maxLen = Math.max(classA.name.length(), classB.name.length());
        double nameDistance = UmlClass.calculateLevenshteinDistance(classA.name, classB.name);
        return (maxLen > 0) ? (1.0 - (nameDistance / maxLen)) : 1.0;
    }

    private double calculateSymmetricalContentSimilarity(UmlClass classA, UmlClass classB) {
        double scoreAB = calculateDirectedContentSimilarity(classA, classB);
        double scoreBA = calculateDirectedContentSimilarity(classB, classA);
        return (scoreAB + scoreBA) / 2.0;
    }

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
    
    private double calculatePositionSimilarity(UmlClass classA, UmlClass classB, double maxDistance) {
        double distance = Math.sqrt(Math.pow(classA.x - classB.x, 2) + Math.pow(classA.y - classB.y, 2));
        if (maxDistance == 0) return 1.0;
        return 1.0 - (distance / maxDistance);
    }

    private double calculateRelationSimilarity(UmlClass classA, UmlClass classB, UmlDiagram diagramA, UmlDiagram diagramB) {
        Set<String> incomingA = getConnectedClassNames(diagramA.getIncomingRelationships(classA.id), diagramA, true);
        Set<String> outgoingA = getConnectedClassNames(diagramA.getOutgoingRelationships(classA.id), diagramA, false);
        Set<String> incomingB = getConnectedClassNames(diagramB.getIncomingRelationships(classB.id), diagramB, true);
        Set<String> outgoingB = getConnectedClassNames(diagramB.getOutgoingRelationships(classB.id), diagramB, false);
        double incomingSimilarity = calculateJaccardSimilarity(incomingA, incomingB);
        double outgoingSimilarity = calculateJaccardSimilarity(outgoingA, outgoingB);
        return (incomingSimilarity + outgoingSimilarity) / 2.0;
    }

    private Set<String> getConnectedClassNames(List<UmlRelationship> relations, UmlDiagram diagram, boolean isIncoming) {
        return relations.stream().map(rel -> {
            String connectedClassId = isIncoming ? rel.sourceClassId : rel.targetClassId;
            UmlClass connectedClass = diagram.getClassById(connectedClassId);
            return (connectedClass != null) ? connectedClass.name : "";
        }).collect(Collectors.toSet());
    }

    private double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 0.0; 
        }
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        if (union.isEmpty()) {
            return 0.0;
        }
        return (double) intersection.size() / union.size();
    }
    
    private double calculateMaxDistance(UmlDiagram diagramA, UmlDiagram diagramB) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (UmlClass cls : diagramA.getClasses()) {
            minX = Math.min(minX, cls.x);
            minY = Math.min(minY, cls.y);
            maxX = Math.max(maxX, cls.x);
            maxY = Math.max(maxY, cls.y);
        }
        for (UmlClass cls : diagramB.getClasses()) {
            minX = Math.min(minX, cls.x);
            minY = Math.min(minY, cls.y);
            maxX = Math.max(maxX, cls.x);
            maxY = Math.max(maxY, cls.y);
        }
        return Math.sqrt(Math.pow(maxX - minX, 2) + Math.pow(maxY - minY, 2));
    }
}