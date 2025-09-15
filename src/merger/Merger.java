package merger;

import java.util.List;

import diff.Diff;
import diff.Diff.ChangeType;
import diff.Diff.ElementType;
import model.UmlClass;
import model.UmlDiagram;

/**
 * 2つのUmlDiagramを、検出された差分リストに基づいてマージ（統合）するクラス。
 */
public class Merger {

    /**
     * baseDiagramに差分リストを適用し、マージされた新しいUmlDiagramを返します。
     * @param baseDiagram 元となるダイアグラム
     * @param versionADiagram 追加するクラスの情報を持つダイアグラム
     * @param diffs 適用する差分のリスト
     * @return マージ後の新しいUmlDiagramオブジェクト
     */
    public UmlDiagram merge(UmlDiagram baseDiagram, UmlDiagram versionADiagram, List<Diff> diffs) {
        
        System.out.println("\n--- Running Merger ---");

        UmlDiagram mergedDiagram = createDeepCopy(baseDiagram);

        for (Diff diff : diffs) {
            // 対象となるクラスをマージ後のダイアグラムから取得
            UmlClass targetClass = mergedDiagram.getClassById(diff.elementId);
            
            // --- クラスに関する差分処理 ---
            if (diff.elementType == ElementType.CLASS) {
                if (diff.changeType == ChangeType.ADD) {
                    UmlClass classToAdd = versionADiagram.getClassById(diff.elementId);
                    if (classToAdd != null) {
                        mergedDiagram.addClass(createClassCopy(classToAdd));
                        System.out.println("Applied ADD: Added class '" + classToAdd.name + "'");
                    }
                } else if (diff.changeType == ChangeType.CHANGE) {
                    // ★★★ ここからが新しいロジック ★★★
                    if (targetClass != null) {
                        // descriptionから新しいクラス名を取得
                        String newName = diff.description.split("'")[3];
                        System.out.println("Applied CHANGE: Renamed class '" + targetClass.name + "' to '" + newName + "'");
                        targetClass.name = newName;
                    }
                }
            } 
            // --- 属性に関する差分処理 ---
            else if (diff.elementType == ElementType.ATTRIBUTE) {
                if (targetClass != null) {
                    // descriptionから属性名を取得
                    String attributeName = diff.description.split("'")[1];

                    if (diff.changeType == ChangeType.ADD) {
                        targetClass.attributes.add(attributeName);
                        System.out.println("Applied ADD_ATTR: Added attribute '" + attributeName + "' to class '" + targetClass.name + "'");
                    } else if (diff.changeType == ChangeType.DELETE) {
                        targetClass.attributes.remove(attributeName);
                        System.out.println("Applied DELETE_ATTR: Deleted attribute '" + attributeName + "' from class '" + targetClass.name + "'");
                    }
                }
            }
        }
        
        return mergedDiagram;
    }

    // クラスのディープコピーを作成するヘルパーメソッド
    private UmlClass createClassCopy(UmlClass original) {
        UmlClass copy = new UmlClass(original.id, original.name);
        for (String attr : original.attributes) {
            copy.attributes.add(attr);
        }
        return copy;
    }

    private UmlDiagram createDeepCopy(UmlDiagram original) {
        UmlDiagram copy = new UmlDiagram();
        for (UmlClass originalClass : original.getClasses()) {
            copy.addClass(createClassCopy(originalClass));
        }
        return copy;
    }
}