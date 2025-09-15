package parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.UmlClass;
import model.UmlDiagram;
import model.UmlRelationship; // UmlRelationshipをインポート

/**
 * クラス図のテキストファイルを解釈して、UmlDiagramオブジェクトを生成するクラス。
 */
public class DiagramParser {

    // クラス定義行を解釈するための正規表現パターン
    private static final Pattern CLASS_PATTERN = 
        Pattern.compile("<(\\d+)>.*?Class\\$.*?!([^!]+)!!(.*);");
    
    // 関係定義行を解釈するための正規表現パターン (★追加)
    private static final Pattern RELATION_PATTERN = 
        Pattern.compile("<(\\d+)>.*?ClassRelationLink\\$<(\\d+)>!<(\\d+)>!([^!]+)!!");

    /**
     * 指定されたファイルパスからクラス図データを読み込み、UmlDiagramオブジェクトとして返します。
     * @param filePath 読み込むクラス図ファイルのパス
     * @return パースされたUmlDiagramオブジェクト
     * @throws IOException ファイルの読み込みに失敗した場合
     */
    public UmlDiagram parse(Path filePath) throws IOException {
        UmlDiagram diagram = new UmlDiagram();
        
        // ファイルを1行ずつ読み込んで処理
        for (String line : Files.readAllLines(filePath)) {
            
            // まずクラス行のマッチングを試みる
            Matcher classMatcher = CLASS_PATTERN.matcher(line);
            if (classMatcher.find()) {
                String id = classMatcher.group(1);
                String name = classMatcher.group(2);
                String attrsStr = classMatcher.group(3);
                
                UmlClass newClass = new UmlClass(id, name);
                
                // 属性文字列があればパースして追加
                if (!attrsStr.isEmpty()) {
                    String[] attributes = attrsStr.replace("-", "").split("%");
                    for (String attr : attributes) {
                        if (!attr.trim().isEmpty()) {
                            newClass.attributes.add(attr.trim());
                        }
                    }
                }
                diagram.addClass(newClass);
                continue; // クラス行だったので次の行へ
            }
            
            // ★ここからが追加部分★
            // クラス行でなければ、関係行のマッチングを試みる
            Matcher relationMatcher = RELATION_PATTERN.matcher(line);
            if (relationMatcher.find()) {
                String id = relationMatcher.group(1);
                String sourceId = relationMatcher.group(2);
                String targetId = relationMatcher.group(3);
                String type = relationMatcher.group(4);
                
                UmlRelationship newRelationship = new UmlRelationship(id, sourceId, targetId, type);
                diagram.addRelationship(newRelationship);
            }
        }
        return diagram;
    }
}