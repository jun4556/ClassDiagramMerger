package diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import matching.Matcher; // ★インポート追加
import model.UmlClass; // ★インポート追加
import model.UmlDiagram; // ★インポート追加

/**
 * 2つの差分リスト（例：base vs A と base vs B）を比較し、
 * 変更の競合（コンフリクト）を検出するクラス。
 */
public class ConflictDetector {

    /**
     * 【3者間マージ用】2つの差分リストを受け取り、競合している差分のリストを返します。
     * elementId の一致を基準に競合を判断します。
     * @param diffsA 比較元Aの差分リスト
     * @param diffsB 比較元Bの差分リスト
     * @return 競合内容を説明する文字列のリスト
     */
    public List<String> detectConflicts(List<Diff> diffsA, List<Diff> diffsB) {
        
        List<String> conflicts = new ArrayList<>();
        System.out.println("\n--- Running 3-Way Conflict Detector ---");

        Map<String, List<Diff>> diffsBMap = new HashMap<>();
        for (Diff diffB : diffsB) {
            diffsBMap.computeIfAbsent(diffB.elementId, k -> new ArrayList<>()).add(diffB);
        }

        for (Diff diffA : diffsA) {
            if (diffsBMap.containsKey(diffA.elementId)) {
                
                for (Diff diffB : diffsBMap.get(diffA.elementId)) {
                    if (isConflicting(diffA, diffB)) {
                        String conflictMessage = String.format(
                            "[CONFLICT] Element ID (%s): Change in A ('%s') conflicts with change in B ('%s')",
                            diffA.elementId, diffA.description, diffB.description
                        );
                        conflicts.add(conflictMessage);
                        System.out.println("Conflict found: " + conflictMessage);
                    }
                }
            }
        }
        
        if (conflicts.isEmpty()) {
            System.out.println("No conflicts found based on Element ID.");
        }
        
        return conflicts;
    }

    /**
     * ★★★ ここから下を新規追加 ★★★
     * 【2者間マージ用】baseが空の場合に、2つのダイアグラム間で競合する追加を検出します。
     * 類似性マッチングにより、意味的に同じ要素が両方で追加された場合を競合と見なします。
     * @param diagramA バージョンAのダイアグラム
     * @param diagramB バージョンBのダイアグラム
     * @param matcher 類似性マッチングを行うためのMatcherオブジェクト
     * @return 競合内容を説明する文字列のリスト
     */
    public List<String> detectConflictsInTwoWayMerge(UmlDiagram diagramA, UmlDiagram diagramB, Matcher matcher) {
        List<String> conflicts = new ArrayList<>();
        System.out.println("\n--- Running 2-Way Conflict Detector (for empty base) ---");

        // Matcherを使って、AとBの間で意味的に類似するクラスのペアを見つける
        Map<UmlClass, UmlClass> matchedClasses = matcher.match(diagramA, diagramB);

        for (Map.Entry<UmlClass, UmlClass> entry : matchedClasses.entrySet()) {
            UmlClass classA = entry.getKey();
            UmlClass classB = entry.getValue();

            String conflictMessage = String.format(
                "[CONFLICT] Semantic match found: Class '%s' in A and class '%s' in B are conflicting additions.",
                classA.name, classB.name
            );
            conflicts.add(conflictMessage);
            System.out.println("Conflict found: " + conflictMessage);
        }
        
        if (conflicts.isEmpty()) {
            System.out.println("No semantically conflicting additions found.");
        }

        return conflicts;
    }


    private boolean isConflicting(Diff diffA, Diff diffB) {
        if (diffA.elementType != diffB.elementType) {
            return false;
        }
        if (Objects.equals(diffA.description, diffB.description)) {
            return false;
        }
        return true;
    }
}