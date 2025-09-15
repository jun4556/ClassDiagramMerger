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

    public List<Diff> diff(UmlDiagram baseDiagram, UmlDiagram versionDiagram, Map<UmlClass, UmlClass> matches) {
        List<Diff> diffs = new ArrayList<>();

        for (UmlClass baseClass : baseDiagram.getClasses()) {
            UmlClass versionClass = matches.get(baseClass);

            if (versionClass == null) {
                continue; 
            } else {
                if (!baseClass.name.equals(versionClass.name)) {
                    diffs.add(new Diff(Diff.ChangeType.CHANGE, Diff.ElementType.CLASS, baseClass.id, 
                        "Name changed from '" + baseClass.name + "' to '" + versionClass.name + "'."));
                }
                diffAttributes(baseClass, versionClass, diffs);
            }
        }
        
        Set<UmlClass> matchedVersionClasses = new HashSet<>(matches.values());
        for (UmlClass versionClass : versionDiagram.getClasses()) {
            if (!matchedVersionClasses.contains(versionClass)) {
                diffs.add(new Diff(Diff.ChangeType.ADD, Diff.ElementType.CLASS, versionClass.id, 
                    "Class '" + versionClass.name + "' added."));
            }
        }
        
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
            // ★★★ 変更点 ★★★
            // versionClass.id ではなく baseClass.id を使うように統一する
            diffs.add(new Diff(Diff.ChangeType.ADD, Diff.ElementType.ATTRIBUTE, baseClass.id, 
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