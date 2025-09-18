package model;

import java.util.HashSet;
import java.util.Set;

/**
 * クラス図における「クラス」の要素を表現するクラス。
 * ID, 名前, 属性の集合、そして座標を保持します。
 */
public class UmlClass {
    public final String id;
    public String name;
    public final Set<String> attributes;
    public final int x; // ★追加: X座標
    public final int y; // ★追加: Y座標

    public UmlClass(String id, String name, int x, int y) { // ★コンストラクタを修正
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.attributes = new HashSet<>();
    }

    /**
     * このクラスと別のクラスとの間で、属性のJaccard係数を計算します。
     * @param other 比較対象のUmlClassオブジェクト
     * @return 内容の類似度 (0.0 ~ 1.0)
     */
    public double calculateJaccardSimilarity(UmlClass other) {
        Set<String> intersection = new HashSet<>(this.attributes);
        intersection.retainAll(other.attributes);

        Set<String> union = new HashSet<>(this.attributes);
        union.addAll(other.attributes);

        if (union.isEmpty()) {
            return 1.0; // 両方の属性セットが空の場合、完全に一致すると見なす
        }
        return (double) intersection.size() / union.size();
    }

    /**
     * 2つの文字列間のレーベンシュタイン距離を計算します。
     * @param s1 文字列1
     * @param s2 文字列2
     * @return 編集距離
     */
    public static int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] 
                     + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1), 
                     Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    @Override
    public String toString() {
        // ★座標も表示されるように修正
        return "Class " + id + " [" + name + "] (" + x + ", " + y + ") Attributes: " + attributes;
    }
}