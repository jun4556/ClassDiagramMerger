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
            UmlClass targetClass = mergedDiagram.getClassById(diff.elementId);
            
            if (diff.elementType == ElementType.CLASS) {
                if (diff.changeType == ChangeType.ADD) {
                    UmlClass classToAdd = versionADiagram.getClassById(diff.elementId);
                    if (classToAdd != null) {
                        mergedDiagram.addClass(createClassCopy(classToAdd));
                        System.out.println("Applied ADD: Added class '" + classToAdd.name + "'");
                    }
                } else if (diff.changeType == ChangeType.CHANGE) {
                    if (targetClass != null) {
                        String newName = diff.description.split("'")[3];
                        System.out.println("Applied CHANGE: Renamed class '" + targetClass.name + "' to '" + newName + "'");
                        targetClass.name = newName;
                    }
                }
            } 
            else if (diff.elementType == ElementType.ATTRIBUTE) {
                if (targetClass != null) {
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
        // ★★★ ここを修正 ★★★
        // 元のクラスの座標(x, y)も引き継いで新しいUmlClassオブジェクトを生成する
        UmlClass copy = new UmlClass(original.id, original.name, original.x, original.y);
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