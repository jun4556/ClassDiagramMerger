package model;

/**
 * クラス間の関係（関連、継承など）を表現するクラス。
 */
public class UmlRelationship {

    public final String id;
    public final String sourceClassId;
    public final String targetClassId;
    public final String type;
    // 必要であれば多重度などの情報もフィールドとして追加できます
    // public String sourceCardinality;
    // public String targetCardinality;

    /**
     * コンストラクタ
     * @param id 関係の一意なID (例: "11")
     * @param sourceClassId 関係の始点となるクラスのID (例: "7")
     * @param targetClassId 関係の終点となるクラスのID (例: "6")
     * @param type 関係の種類 (例: "SimpleRelation")
     */
    public UmlRelationship(String id, String sourceClassId, String targetClassId, String type) {
        this.id = id;
        this.sourceClassId = sourceClassId;
        this.targetClassId = targetClassId;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Relationship " + id + " [" + sourceClassId + " -> " + targetClassId + ", Type: " + type + "]";
    }
}