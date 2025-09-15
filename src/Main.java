import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List; // ★インポート追加
import java.util.Map;

import diff.Diff; // ★インポート追加
import diff.DiffEngine; // ★インポート追加
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
        DiffEngine diffEngine = new DiffEngine(); // ★DiffEngineをインスタンス化

        try {
            // --- 2. ファイルの読み込み (パース) ---
            System.out.println("--- Parsing Files ---");
            Path basePath = Paths.get("diagrams/base.txt");
            Path versionAPath = Paths.get("diagrams/versionA.txt");

            UmlDiagram baseDiagram = parser.parse(basePath);
            UmlDiagram versionADiagram = parser.parse(versionAPath);

            System.out.println("Base Diagram Parsed Successfully.");
            baseDiagram.getClasses().forEach(System.out::println);
            System.out.println("\nVersion A Diagram Parsed Successfully.");
            versionADiagram.getClasses().forEach(System.out::println);

            // --- 3. マッチングの実行 ---
            System.out.println("\n--- Running Matcher (Base vs Version A) ---");
            Map<UmlClass, UmlClass> matches = matcher.match(baseDiagram, versionADiagram);

            System.out.println("\n--- Matching Results ---");
            for (Map.Entry<UmlClass, UmlClass> entry : matches.entrySet()) {
                System.out.println("Matched: [Base] " + entry.getKey().name + " <--> " +
                                   "[Ver. A] " + entry.getValue().name);
            }
            
            // --- 4. 差分検出の実行 (★ここからが追加部分) ---
            System.out.println("\n--- Running Diff Engine ---");
            List<Diff> diffs = diffEngine.diff(baseDiagram, versionADiagram, matches);

            // --- 5. 差分結果の表示 ---
            System.out.println("\n--- Difference Results ---");
            if (diffs.isEmpty()) {
                System.out.println("No differences found.");
            } else {
                for (Diff diff : diffs) {
                    System.out.println(diff);
                }
            }

        } catch (Exception e) {
            System.err.println("エラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
}