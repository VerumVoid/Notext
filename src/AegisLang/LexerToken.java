package AegisLang;

public class LexerToken implements Comparable<LexerToken>{
    private String content;
    private Lexer.LexerTokenType type;

    public LexerToken(String content, Lexer.LexerTokenType type) {
        this.content = content;
        this.type = type;
    }
    public LexerToken(ASTreeNode node){
        this.content = node.getValue();
        this.type = node.getType();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Lexer.LexerTokenType getType() {
        return type;
    }

    public void setType(Lexer.LexerTokenType type) {
        this.type = type;
    }

    public int getOperationPriority(){
        return switch (content){
            case "+" -> 0;
            case "-" -> 0;
            case "*" -> 1;
            case "/" -> 1;
            case "=" -> 2;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return "LexerToken{" +
                "content='" + content + '\'' +
                ", operation=" + type +
                '}';
    }

    @Override
    public int compareTo(LexerToken o) {
        return Integer.compare(o.getOperationPriority(), getOperationPriority());
    }
}