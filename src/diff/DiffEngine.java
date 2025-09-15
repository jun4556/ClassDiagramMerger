package diff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.UmlClass;
import model.UmlDiagram;

/**
 * 2つのUmlDiagramを比較し、差分を検出するエンジン。
 */
public class DiffEngine {

    /**
     * 2つのダイアグラムとマッチング結果を元に、差分のリストを生成します。
     * @param baseDiagram 比較元（基準）となるダイアグラム
     * @param versionDiagram 比較先となるダイアグラム
     * @param matches マッチング結果
     * @return 検出された差分のリスト
     */
    public List<Diff> diff(UmlDiagram baseDiagram, UmlDiagram versionDiagram, Map<UmlClass, UmlClass> matches) {
        List<Diff> diffs = new ArrayList<>();

        // --- 1. クラスの変更を検出し、削除は無視する ---
        for (UmlClass baseClass : baseDiagram.getClasses()) {
            UmlClass versionClass = matches.get(baseClass);

            if (versionClass == null) {
                // ★★★ 変更点 ★★★
                // マッチするクラスがなくても削除とは見なさず、何もしない（クラスを保持する）
                continue; 
            } else {
                // マッチするクラスがあれば、内容の変更をチェック
                // 名前の変更
                if (!baseClass.name.equals(versionClass.name)) {
                    diffs.add(new Diff(Diff.ChangeType.CHANGE, Diff.ElementType.CLASS, baseClass.id, 
                        "Name changed from '" + baseClass.name + "' to '" + versionClass.name + "'."));
                }
                // 属性の変更 (追加・削除)
                diffAttributes(baseClass, versionClass, diffs);
            }
        }
        
        // --- 2. クラスの追加を検出 ---
        Set<UmlClass> matchedVersionClasses = new HashSet<>(matches.values());
        for (UmlClass versionClass : versionDiagram.getClasses()) {
            if (!matchedVersionClasses.contains(versionClass)) {
                diffs.add(new Diff(Diff.ChangeType.ADD, Diff.ElementType.CLASS, versionClass.id, 
                    "Class '" + versionClass.name + "' added."));
            }
        }
        
        // TODO: 関係(Relationship)の差分検出も同様に追加可能
        
        return diffs;
    }
    
    /**
     * 2つのクラス間で属性の差分を検出し、リストに追加するヘルパーメソッド。
     */
    private void diffAttributes(UmlClass baseClass, UmlClass versionClass, List<Diff> diffs) {
        // 追加された属性を検出
        Set<String> addedAttrs = new HashSet<>(versionClass.attributes);
        addedAttrs.removeAll(baseClass.attributes);
        for (String attr : addedAttrs) {
            diffs.add(new Diff(Diff.ChangeType.ADD, Diff.ElementType.ATTRIBUTE, versionClass.id, 
                "Attribute '" + attr + "' added to class '" + versionClass.name + "'."));
        }
        
        // 削除された属性を検出
        Set<String> deletedAttrs = new HashSet<>(baseClass.attributes);
        deletedAttrs.removeAll(versionClass.attributes);
        for (String attr : deletedAttrs) {
            diffs.add(new Diff(Diff.ChangeType.DELETE, Diff.ElementType.ATTRIBUTE, baseClass.id,
                "Attribute '" + attr + "' deleted from class '" + baseClass.name + "'."));
        }
    }
}