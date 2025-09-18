import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import diff.ConflictDetector;
import diff.Diff;
import diff.DiffEngine;
import matching.Matcher;
import model.UmlClass;
import model.UmlDiagram;
import parser.DiagramParser;

/**
 * プログラムの実行を開始するメインクラス。
 */
public class Main {

    public static void main(String[] args) {
        // --- 1. 準備 ---
        DiagramParser parser = new DiagramParser();
        Matcher matcher = new Matcher();
        DiffEngine diffEngine = new DiffEngine();
        ConflictDetector conflictDetector = new ConflictDetector();

        try {
            // --- 2. ファイルの読み込み ---
            System.out.println("--- Parsing Files ---");
            Path basePath = Paths.get("diagrams/base.txt");
            Path versionAPath = Paths.get("diagrams/versionA.txt");
            Path versionBPath = Paths.get("diagrams/versionB.txt");

            UmlDiagram baseDiagram = parser.parse(basePath);
            UmlDiagram versionADiagram = parser.parse(versionAPath);
            UmlDiagram versionBDiagram = parser.parse(versionBPath);

            System.out.println("Base Diagram Parsed Successfully.");
            baseDiagram.getClasses().forEach(System.out::println);
            System.out.println("\nVersion A Diagram Parsed Successfully.");
            versionADiagram.getClasses().forEach(System.out::println);
            System.out.println("\nVersion B Diagram Parsed Successfully.");
            versionBDiagram.getClasses().forEach(System.out::println);


            // --- ★★★ ここからが改善ロジック ★★★ ---
            List<String> conflicts;

            // baseダイアグラムが空かどうかで処理を分岐
            if (baseDiagram.getClasses().isEmpty()) {
                // 【baseが空の場合】 versionA と versionB を直接比較して競合を検出
                conflicts = conflictDetector.detectConflictsInTwoWayMerge(versionADiagram, versionBDiagram, matcher);

            } else {
                // 【baseが存在する場合】 従来の3者間マージの競合検出を実行
                System.out.println("\n--- Running Diff (Base vs Version A) ---");
                Map<UmlClass, UmlClass> matchesA = matcher.match(baseDiagram, versionADiagram);
                List<Diff> diffsA = diffEngine.diff(baseDiagram, versionADiagram, matchesA);
                System.out.println("\n--- Difference Results (Base vs A) ---");
                diffsA.forEach(System.out::println);

                System.out.println("\n--- Running Diff (Base vs Version B) ---");
                Map<UmlClass, UmlClass> matchesB = matcher.match(baseDiagram, versionBDiagram);
                List<Diff> diffsB = diffEngine.diff(baseDiagram, versionBDiagram, matchesB);
                System.out.println("\n--- Difference Results (Base vs B) ---");
                diffsB.forEach(System.out::println);

                conflicts = conflictDetector.detectConflicts(diffsA, diffsB);
            }
            
            // --- 競合結果の表示 ---
            System.out.println("\n--- Conflict Results ---");
            if (conflicts.isEmpty()) {
                System.out.println("No conflicts were detected.");
            } else {
                System.out.println("The following conflicts were detected:");
                conflicts.forEach(System.out::println);
            }

        } catch (Exception e) {
            System.err.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
}