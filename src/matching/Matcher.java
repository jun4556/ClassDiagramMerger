package matching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.UmlClass;
import model.UmlDiagram;

/**
 * 2つのUmlDiagramを比較し、対応するUmlClass要素をマッチングさせるクラス。
 * クラス間の「距離」が小さいほど、類似性が高いと判断する。
 */
public class Matcher {

    // --- 実験パラメータ（ここを調整して実験を行う） ---
    private static final double DISTANCE_THRESHOLD = 250.0; // この距離以下のペアをマッチング候補とする
    private static final double W_NAME = 1.0;   // 名前の距離に対する重み
    private static final double W_ATTR = 0.5;   // 属性の距離に対する重み
    private static final double W_COORD = 1.0;  // 座標の距離に対する重み
    // ---

    private static final boolean DEBUG_MODE = true;

    public Map<UmlClass, UmlClass> match(UmlDiagram diagramA, UmlDiagram diagramB) {
        Map<UmlClass, UmlClass> matches = new HashMap<>();
        Set<UmlClass> unmatchedB = new HashSet<>(diagramB.getClasses());

        if (DEBUG_MODE) {
            System.out.println("\n--- Starting Matcher (Distance-based) ---");
            System.out.println(String.format("Parameters: THRESHOLD=%.2f, W_NAME=%.2f, W_ATTR=%.2f, W_COORD=%.2f",
                DISTANCE_THRESHOLD, W_NAME, W_ATTR, W_COORD));
        }

        for (UmlClass classA : diagramA.getClasses()) {
            UmlClass bestMatch = null;
            double lowestDistance = Double.MAX_VALUE;

            if (DEBUG_MODE) {
                System.out.println("\n----- Matching for Class A: '" + classA.name + "' -----");
            }

            for (UmlClass classB : unmatchedB) {
                // 1. 名前の距離
                double nameDistance = calculateNameDistance(classA, classB);

                // 2. 属性の距離
                double attrDistance = calculateAttributeDistance(classA, classB);

                // 3. 座標の距離
                double coordDistance = calculateCoordinateDistance(classA, classB);

                // 4. 総合距離
                double totalDistance = (nameDistance * W_NAME) +
                                       (attrDistance * W_ATTR) +
                                       (coordDistance * W_COORD);

                if (DEBUG_MODE) {
                    System.out.println("  Comparing with Class B: '" + classB.name + "'");
                    System.out.println(String.format("    - Name Dist:      %.2f (%.2f * %.2f)", nameDistance * W_NAME, nameDistance, W_NAME));
                    System.out.println(String.format("    - Attribute Dist: %.2f (%.2f * %.2f)", attrDistance * W_ATTR, attrDistance, W_ATTR));
                    System.out.println(String.format("    - Coordinate Dist:%.2f (%.2f * %.2f)", coordDistance * W_COORD, coordDistance, W_COORD));
                    System.out.println(String.format("    -> Total Dist:    %.4f", totalDistance));
                }

                if (totalDistance < lowestDistance) {
                    lowestDistance = totalDistance;
                    bestMatch = classB;
                }
            }

            if (bestMatch != null && lowestDistance < DISTANCE_THRESHOLD) {
                if (DEBUG_MODE) {
                    System.out.println("  => Best match found: '" + bestMatch.name + "' with distance " + String.format("%.4f", lowestDistance));
                }
                matches.put(classA, bestMatch);
                unmatchedB.remove(bestMatch);
            } else if (DEBUG_MODE) {
                System.out.println("  => No suitable match found (lowest distance: " + String.format("%.4f", lowestDistance) + ")");
            }
        }

        return matches;
    }

    /**
     * 2つのクラス名間のレーベンシュタイン距離を計算する。
     */
    private double calculateNameDistance(UmlClass classA, UmlClass classB) {
        return UmlClass.calculateLevenshteinDistance(classA.name, classB.name);
    }

    /**
     * 2つのクラスの属性セット間の距離を計算する。
     * 各属性について、相手のセット内で最も近い属性との距離を合計する。
     */
    private double calculateAttributeDistance(UmlClass classA, UmlClass classB) {
        double totalDistance = 0.0;
        totalDistance += calculateDirectedAttributeDistance(classA, classB);
        totalDistance += calculateDirectedAttributeDistance(classB, classA);
        return totalDistance;
    }

    private double calculateDirectedAttributeDistance(UmlClass fromClass, UmlClass toClass) {
        double directedDistance = 0.0;
        for (String attrFrom : fromClass.attributes) {
            double minDistanceForAttr = Double.MAX_VALUE;
            if (toClass.attributes.isEmpty()) {
                minDistanceForAttr = attrFrom.length(); // 相手が空なら、自身の文字数がそのまま距離
            } else {
                for (String attrTo : toClass.attributes) {
                    int dist = UmlClass.calculateLevenshteinDistance(attrFrom, attrTo);
                    if (dist < minDistanceForAttr) {
                        minDistanceForAttr = dist;
                    }
                }
            }
            directedDistance += minDistanceForAttr;
        }
        return directedDistance;
    }

    /**
     * 2つのクラスの座標間のユークリッド距離を計算する。
     */
    private double calculateCoordinateDistance(UmlClass classA, UmlClass classB) {
        return Math.sqrt(Math.pow(classA.x - classB.x, 2) + Math.pow(classA.y - classB.y, 2));
    }
}