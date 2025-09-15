import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import diff.ConflictDetector; // ★インポート追加
import diff.Diff;
import diff.DiffEngine;
import matching.Matcher;
// import merger.Merger;
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
        ConflictDetector conflictDetector = new ConflictDetector(); // ★ConflictDetectorをインスタンス化
        // Merger merger = new Merger();

        try {
            // --- 2. ファイルの読み込み (3ファイル) ---
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

            // --- 3. 差分検出 (base vs A) ---
            System.out.println("\n--- Running Diff (Base vs Version A) ---");
            Map<UmlClass, UmlClass> matchesA = matcher.match(baseDiagram, versionADiagram);
            List<Diff> diffsA = diffEngine.diff(baseDiagram, versionADiagram, matchesA);

            System.out.println("\n--- Difference Results (Base vs A) ---");
            diffsA.forEach(System.out::println);

            // --- 4. 差分検出 (base vs B) ---
            System.out.println("\n--- Running Diff (Base vs Version B) ---");
            Map<UmlClass, UmlClass> matchesB = matcher.match(baseDiagram, versionBDiagram);
            List<Diff> diffsB = diffEngine.diff(baseDiagram, versionBDiagram, matchesB);

            System.out.println("\n--- Difference Results (Base vs B) ---");
            diffsB.forEach(System.out::println);

            // --- 5. 競合検出 (★ここからが追加部分) ---
            List<String> conflicts = conflictDetector.detectConflicts(diffsA, diffsB);
            
            // --- 6. 競合結果の表示 ---
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