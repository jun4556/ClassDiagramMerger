package parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.UmlClass;
import model.UmlDiagram;
import model.UmlRelationship;

/**
 * クラス図のテキストファイルを解釈して、UmlDiagramオブジェクトを生成するクラス。
 */
public class DiagramParser {

    // ★座標 (x,y) をキャプチャできるように正規表現を修正
    private static final Pattern CLASS_PATTERN = 
        Pattern.compile("<(\\d+)>.*?Class\\$\\((\\d+),(\\d+)\\)!([^!]+)!!(.*);");
    
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
        
        for (String line : Files.readAllLines(filePath)) {
            
            Matcher classMatcher = CLASS_PATTERN.matcher(line);
            if (classMatcher.find()) {
                // ★正規表現のグループ番号がずれるため、インデックスを修正
                String id = classMatcher.group(1);
                int x = Integer.parseInt(classMatcher.group(2)); // ★X座標を抽出
                int y = Integer.parseInt(classMatcher.group(3)); // ★Y座標を抽出
                String name = classMatcher.group(4);
                String attrsStr = classMatcher.group(5);
                
                // ★修正したコンストラクタを呼び出す
                UmlClass newClass = new UmlClass(id, name, x, y); 
                
                if (attrsStr != null && !attrsStr.isEmpty()) {
                    String cleanedAttrs = attrsStr.replace("-", "").replace("!", "");
                    String[] attributes = cleanedAttrs.split("%");
                    for (String attr : attributes) {
                        String trimmedAttr = attr.trim();
                        if (!trimmedAttr.isEmpty()) {
                            newClass.attributes.add(trimmedAttr);
                        }
                    }
                }
                diagram.addClass(newClass);
                continue; 
            }
            
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