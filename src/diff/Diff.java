package diff;

/**
 * 1つの変更（差分）を表すクラス。
 */
public class Diff {

    public enum ChangeType {
        ADD,     // 追加
        DELETE,  // 削除
        CHANGE   // 変更
    }
    
    public enum ElementType {
        CLASS,
        ATTRIBUTE,
        RELATIONSHIP
    }

    public final ChangeType changeType;
    public final ElementType elementType;
    public final String elementId; // 変更があった要素のID
    public final String description; // 変更内容を説明する文字列

    public Diff(ChangeType changeType, ElementType elementType, String elementId, String description) {
        this.changeType = changeType;
        this.elementType = elementType;
        this.elementId = elementId;
        this.description = description;
    }

    @Override
    public String toString() {
        // 例: [CHANGE] CLASS (6): Name changed from 'User' to 'Customer'
        return "[" + changeType + "] " + elementType + " (" + elementId + "): " + description;
    }
}