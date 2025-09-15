package diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 2つの差分リスト（例：base vs A と base vs B）を比較し、
 * 変更の競合（コンフリクト）を検出するクラス。
 */
public class ConflictDetector {

    /**
     * 2つの差分リストを受け取り、競合している差分のリストを返します。
     * @param diffsA 比較元Aの差分リスト
     * @param diffsB 比較元Bの差分リスト
     * @return 競合内容を説明する文字列のリスト
     */
    public List<String> detectConflicts(List<Diff> diffsA, List<Diff> diffsB) {
        
        List<String> conflicts = new ArrayList<>();
        System.out.println("\n--- Running Conflict Detector ---");

        // Bの差分を高速に検索できるように、elementIdをキーとするMapに変換
        Map<String, List<Diff>> diffsBMap = new HashMap<>();
        for (Diff diffB : diffsB) {
            diffsBMap.computeIfAbsent(diffB.elementId, k -> new ArrayList<>()).add(diffB);
        }

        // Aの各差分について、Bに競合する変更がないかチェック
        for (Diff diffA : diffsA) {
            // Aで変更された要素と同じ要素がBでも変更されているか？
            if (diffsBMap.containsKey(diffA.elementId)) {
                
                for (Diff diffB : diffsBMap.get(diffA.elementId)) {
                    // 同じ要素に対する変更でも、内容が同じなら競合ではない
                    // (例: AでもBでも同じ属性を追加した場合はOK)
                    // ここでは簡単化のため、「同じ要素への変更は全て競合」と見なす
                    // ただし、全く同じDiffオブジェクトなら無視する
                    if (isConflicting(diffA, diffB)) {
                        String conflictMessage = String.format(
                            "[CONFLICT] Element (%s): Change in A ('%s') conflicts with change in B ('%s')",
                            diffA.elementId, diffA.description, diffB.description
                        );
                        conflicts.add(conflictMessage);
                        System.out.println("Conflict found: " + conflictMessage);
                    }
                }
            }
        }
        
        if (conflicts.isEmpty()) {
            System.out.println("No conflicts found.");
        }
        
        return conflicts;
    }

    /**
     * 2つの差分が競合しているかを判定するヘルパーメソッド。
     * ここでは単純に、descriptionが異なる場合は競合と見なす。
     */
    private boolean isConflicting(Diff diffA, Diff diffB) {
        // 同じ要素タイプに対する変更か？ (CLASS vs CLASS, ATTRIBUTE vs ATTRIBUTE)
        if (diffA.elementType != diffB.elementType) {
            return false; // 要素タイプが違えば競合ではない (例: クラス名変更と属性追加)
        }
        // 全く同じ変更なら競合ではない
        if (Objects.equals(diffA.description, diffB.description)) {
            return false;
        }
        return true;
    }
}