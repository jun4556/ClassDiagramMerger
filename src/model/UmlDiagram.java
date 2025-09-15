package model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * クラス図全体を表現するクラス。
 * すべてのUmlClassとUmlRelationshipを格納・管理します。
 */
public class UmlDiagram {

    // クラスIDをキーとして、UmlClassオブジェクトを格納するマップ
    // IDによる高速な検索を可能にします。
    private final Map<String, UmlClass> classes;

    // 関係IDをキーとして、UmlRelationshipオブジェクトを格納するマップ
    private final Map<String, UmlRelationship> relationships;

    /**
     * コンストラクタ
     * クラスと関係を格納するためのMapを初期化します。
     */
    public UmlDiagram() {
        this.classes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    /**
     * 図に新しいクラスを追加します。
     * @param umlClass 追加するUmlClassオブジェクト
     */
    public void addClass(UmlClass umlClass) {
        this.classes.put(umlClass.id, umlClass);
    }

    /**
     * 図に新しい関係を追加します。
     * @param relationship 追加するUmlRelationshipオブジェクト
     */
    public void addRelationship(UmlRelationship relationship) {
        this.relationships.put(relationship.id, relationship);
    }

    /**
     * 指定されたIDを持つUmlClassオブジェクトを取得します。
     * @param id 取得したいクラスのID
     * @return 見つかったUmlClassオブジェクト。見つからない場合はnull。
     */
    public UmlClass getClassById(String id) {
        return this.classes.get(id);
    }
    
    /**
     * 指定されたIDを持つUmlRelationshipオブジェクトを取得します。
     * @param id 取得したい関係のID
     * @return 見つかったUmlRelationshipオブジェクト。見つからない場合はnull。
     */
    public UmlRelationship getRelationshipById(String id) {
        return this.relationships.get(id);
    }

    /**
     * 図に含まれるすべてのUmlClassオブジェクトのコレクションを返します。
     * @return すべてのUmlClassオブジェクト
     */
    public Collection<UmlClass> getClasses() {
        return this.classes.values();
    }

    /**
     * 図に含まれるすべてのUmlRelationshipオブジェクトのコレクションを返します。
     * @return すべてのUmlRelationshipオブジェクト
     */
    public Collection<UmlRelationship> getRelationships() {
        return this.relationships.values();
    }
}