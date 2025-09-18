package model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List; // ★インポート追加
import java.util.Map;
import java.util.stream.Collectors; // ★インポート追加

/**
 * クラス図全体を表現するクラス。
 * すべてのUmlClassとUmlRelationshipを格納・管理します。
 */
public class UmlDiagram {

    private final Map<String, UmlClass> classes;
    private final Map<String, UmlRelationship> relationships;

    public UmlDiagram() {
        this.classes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    public void addClass(UmlClass umlClass) {
        this.classes.put(umlClass.id, umlClass);
    }

    public void addRelationship(UmlRelationship relationship) {
        this.relationships.put(relationship.id, relationship);
    }

    public UmlClass getClassById(String id) {
        return this.classes.get(id);
    }
    
    public UmlRelationship getRelationshipById(String id) {
        return this.relationships.get(id);
    }

    public Collection<UmlClass> getClasses() {
        return this.classes.values();
    }

    public Collection<UmlRelationship> getRelationships() {
        return this.relationships.values();
    }

    // --- ★ここから下を新規追加 ---

    /**
     * 指定されたクラスIDを終点とする（指されている）関係のリストを返します。
     * @param classId 終点となるクラスのID
     * @return 関係(UmlRelationship)のリスト
     */
    public List<UmlRelationship> getIncomingRelationships(String classId) {
        return relationships.values().stream()
                .filter(rel -> rel.targetClassId.equals(classId))
                .collect(Collectors.toList());
    }

    /**
     * 指定されたクラスIDを始点とする（指している）関係のリストを返します。
     * @param classId 始点となるクラスのID
     * @return 関係(UmlRelationship)のリスト
     */
    public List<UmlRelationship> getOutgoingRelationships(String classId) {
        return relationships.values().stream()
                .filter(rel -> rel.sourceClassId.equals(classId))
                .collect(Collectors.toList());
    }
}