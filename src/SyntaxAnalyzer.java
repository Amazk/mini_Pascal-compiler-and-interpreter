public class SyntaxAnalyzer {

    private final String[] unilexs = {"motcle", "ident", "ent", "ch", "virg","ptvirg","point","deuxpts","parouv","parfer",
            "inf","sup","eg","plus","moins","mult","divi","infe","supe","diff","aff"};

    private String unilex;

    private final LexicalAnalyser lexicalAnalyser;


    public SyntaxAnalyzer(LexicalAnalyser lexicalAnalyser) {
        this.lexicalAnalyser = lexicalAnalyser;
    }


    public void run() {
        init();
        while (!lexicalAnalyser.getCarlu().equals("$")) {
            anaSynt();
        }
        end();
    }

    private void init() {
        lexicalAnalyser.init();
    }

    private void anaSynt() {
        unilex = lexicalAnalyser.analex();
    }

    private void end() {
        lexicalAnalyser.end();
    }

    private boolean instruction() {
        return affectation() || lecture() || ecriture() || bloc();
    }

    private boolean ecriture() {
        return false;
    }

    private boolean lecture() {
        return false;
    }

    private boolean affectation() {
        if(unilex.equals("ident")) {
            unilex = lexicalAnalyser.analex();
            if(unilex.equals("aff")) {
                unilex = lexicalAnalyser.analex();
                return true;
            }
        }
        return false;
    }

    private boolean bloc() {
        return false;
    }
}
