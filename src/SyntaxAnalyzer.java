import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Stack;

public class SyntaxAnalyzer {
    private final String[] unilexs = {"motcle", "ident", "ent", "ch", "virg","ptvirg","point","deuxpts","parouv","parfer",
            "inf","sup","eg","plus","moins","mult","divi","infe","supe","diff","aff"};
    private String unilex;
    private int lastVarAddress;
    private final PrintWriter writer;
    private final Stack<String> pilOp;
    private final LexicalAnalyser lexicalAnalyser;
    public final Map<String, String[]> idents = new MyMap(); //String[]  -> [type ident, type var, valeur, adresse]      type var = 0/1 <> ent/ch

    public SyntaxAnalyzer(LexicalAnalyser lexicalAnalyser) throws IOException {
        this.lexicalAnalyser = lexicalAnalyser;
        writer = new PrintWriter("source.cod", StandardCharsets.UTF_8);
        pilOp = new Stack<>();
        run();
    }

    public void run() {
        init();
        anaSynt();
        end();
    }         //refractorer le code if(!cond) error ....
    private void init() {
        lastVarAddress=0;
        lexicalAnalyser.init();
        analex();
    }
    private void anaSynt() {
        prog();
        if(is("motcle") && isCle("CONST")) declConst();
        if(is("motcle") && isCle("VAR")) {
            declVar();
            writer.println(idents.size()+" mots réservés pour les variables globales");
        }
        bloc();
    }
    private void end() {
        System.out.println("Ident Table : "+idents);
        System.out.println("Syntax and Semantic are correct in source program");
        writer.print("STOP");
        writer.close();
        lexicalAnalyser.end();
    }
    private void error(String mesError) {
        System.out.print(mesError+" at line "+lexicalAnalyser.getNumLine());
        System.exit(1);
    }
    private void prog() {
        if(is("motcle") && isCle("PROGRAMME")) {
            analex();
            if(is("ident")) {
                analex();
                if(is("ptvirg")) analex();
                else error("Syntax error : missing ';' in prog()");
            } else error("Syntax error : missing ident after PROGRAMME in prog()");
        } else error("Syntax error : missing 'PROGRAMME' in prog()");
    }
    private void declConst() {
        analex();
        if(is("ident")) {
            String key = lexicalAnalyser.getChaine();
            analex();
            if(is("eg")) {
                analex();
                if(is("ent") || is("ch")) {
                    semanticConst(key);
                    analex();
                    while (is("virg")) {
                        analex();
                        if(is("ident")) {
                            key = lexicalAnalyser.getChaine();
                            analex();
                            if(is("eg")) {
                                analex();
                                if(is("ent") || is("ch")) {
                                    semanticConst(key);
                                    analex();
                                }
                                else error("Syntax error : missing ent or ch after '=' in declConst()");
                            } else error("Syntax error : missing '=' after ident in declConst()");
                        } else error("Syntax error : missing ident after CONST in declConst()");
                    }
                    if(is("ptvirg")) analex();
                    else error("Syntax error : missing ';' in declConst()");
                } else error("Syntax error : missing ent or ch after '=' in declConst()");
            } else error("Syntax error : missing '=' after ident in declConst()");
        } else error("Syntax error : missing ident after CONST in declConst()");
    }
    private void semanticConst(String key) {
        if(!idents.containsKey(key))
            idents.put(key, new String[]{"const",is("ent") ? String.valueOf(0) : String.valueOf(1),
                    is("ent") ? String.valueOf(lexicalAnalyser.getNumber()) : lexicalAnalyser.getChaine(),""});
        else error("Semantic error : const "+key+" already declared");
    }
    private void declVar() {
        analex();
        if(is("ident")) {
            semanticVar();
            analex();
            while (is("virg")) {
                analex();
                if(is("ident")) {
                    semanticVar();
                    analex();
                }
                else error("Syntax error : missing ident after VAR in declVar()");
            }
            if(is("ptvirg")) analex();
            else error("Syntax error : missing ';' in declVar()");
        } else error("Syntax error : missing ident after VAR in declVar()");
    }
    private void semanticVar() {
        if(!idents.containsKey(lexicalAnalyser.getChaine()))
            idents.put(lexicalAnalyser.getChaine(),new String[]{"var","0","",String.valueOf(lastVarAddress)});
        else error("Semantic error : var "+lexicalAnalyser.getChaine()+" already declared");
        lastVarAddress++;
    }
    private void bloc() {
        if(is("motcle") && isCle("DEBUT")) {
            analex();
            while (!is("motcle") || !isCle("FIN")) {
                if(isEnd()) error("'FIN' in bloc()");
                switch (unilex) {
                    case "motcle" -> {
                        switch (lexicalAnalyser.getChaine()) {
                            case "ECRIRE" -> ecriture();
                            case "LIRE" -> lecture();
                            case "DEBUT" -> bloc();
                            default -> error("Syntax error : missing ECRIRE/LIRE/DEBUT in bloc()");
                        }
                    }
                    case "ident" -> affectation();
                    default -> error("Syntax error : missing ECRIRE/LIRE/DEBUT or ident in bloc()");
                }
            }
            if(!isEnd()) analex();
        } else if(!isEnd()) error("Syntax error : missing DEBUT in bloc()");
    }
    private void ecriture() {
        analex();
        if(is("parouv")) {
            analex();
            if(is("parfer")) {
                analex();
                writer.println("ECRL");
                if(is("ptvirg")) analex();
                else error("Syntax error : missing ';' after ECRIRE() in ecriture()");
            }
            else if(ecr_Exp()) {
                while (is("virg")) {
                    analex();
                    if(!ecr_Exp()) error("Syntax error : missing ch or exp after ',' in ecriture()");
                }
                if(is("parfer")) {
                    analex();
                    if(is("ptvirg")) analex();
                    else error("Syntax error : missing ';' after ECRIRE() in ecriture()");
                } else error("Syntax error : missing ')' after ECRIRE( in ecriture()");
            } else error("Syntax error : missing ch or exp after ECRIRE( in ecriture()");
        }
        else error("Syntax error : missing '(' after ECRIRE in ecriture()");
    }
    private void lecture() {
        analex();
        if(is("parouv")) {
            analex();
            if(is("ident")) {
                semanticLecture();
                while (is("virg")) {
                    analex();
                    if(is("ident")) {
                        semanticLecture();
                    } else error("Syntax error : missing ident after ',' in lecture()");
                }
                if(is("parfer")) {
                    analex();
                    if(is("ptvirg")) analex();
                    else error("Syntax error : missing ';' after LIRE() in lecture()");
                } else error("Syntax error : missing ')' after LIRE( in lecture()");
            } else error("Syntax error : missing ident after LIRE( in lecture()");
        } else error("Syntax error : missing '(' after LIRE in lecture()");
    }
    private void semanticLecture() {
        if(idents.containsKey(lexicalAnalyser.getChaine())) {
            if(idents.get(lexicalAnalyser.getChaine())[0].equals("var")) {
                genCodeLecture();
                analex();
            } else error("Semantic error : var required in semanticLecture()");
        } else error("Semantic error : ident not declared in semanticLecture()");
    }
    private void genCodeLecture() {
        writer.println("EMPI "+idents.get(lexicalAnalyser.getChaine())[3]);
        writer.println("LIRE");
    }
    private void affectation() {
        semanticAffectation();
        if(is("aff")) {
            analex();
            if(exp()) {
                writer.println("AFFE");
                if(is("ptvirg")) analex();
                else error("Syntax error : missing ';' in affectation()");
            }
            else error("Syntax error : missing exp after ident := in affectation()");
        } else error("Syntax error : missing ':=' after ident in affectation()");
    }
    private void semanticAffectation() {
        if(idents.containsKey(lexicalAnalyser.getChaine())) {
            if(idents.get(lexicalAnalyser.getChaine())[0].equals("var")) {
                writer.println("EMPI "+idents.get(lexicalAnalyser.getChaine())[3]);
                analex();
            } else error("Semantic error : var required in semanticAffectation()");
        } else error("Semantic error : ident not declared in semanticAffectation()");
    }
    private boolean ecr_Exp() {
        if(!is("ch")) {
            boolean b = exp();
            writer.println("ECRE");
            return b;
        }
        writer.println("ECRC '"+lexicalAnalyser.getChaine()+"' FINC");
        analex();
        return true;
    }
    private boolean exp() {
        if(!terme()) return false;
        analex();
        return suiteTerme();
    }
    private boolean terme() {
        if(is("ent")) {
            writer.println("EMPI "+lexicalAnalyser.getNumber());
            if(!pilOp.isEmpty()) writer.println(pilOp.pop());
            return true;
        }
        if(is("ident")) {
            if(idents.containsKey(lexicalAnalyser.getChaine()))
                if(idents.get(lexicalAnalyser.getChaine())[1].equals("0")) {
                    writer.println("EMPI "+(idents.get(lexicalAnalyser.getChaine())[0].equals("var") ?
                            idents.get(lexicalAnalyser.getChaine())[3] :  idents.get(lexicalAnalyser.getChaine())[2]));
                    if(idents.get(lexicalAnalyser.getChaine())[0].equals("var")) writer.println("CONT");
                    if(!pilOp.isEmpty()) writer.println(pilOp.pop());
                    return true;
                }
                else error("Semantic error : integer ident required in terme()");
            else error("Semantic error : ident not declared in terme()");
        }
        else if(is("moins")) {
            pilOp.add("MOIN");
            analex();
            return terme();
        }
        else if(is("parouv")) {
            analex();
            if(exp()) {
                analex();
                return is("parfer");
            }
        }
        return false;
    }
    private boolean suiteTerme() {
        if(isOp_Bin()) {
            addPilOp();
            analex();
            if(exp()) return true;
        }
        return is("ptvirg") || is("parfer") || is("virg");
    }

    private void addPilOp() {
        switch (unilex) {
            case "plus" -> pilOp.add("ADDI");
            case "moins" -> pilOp.add("MOIN");
            case "divi" -> pilOp.add("DIVI");
            case "mult" -> pilOp.add("MULT");
        }
    }
    private boolean isOp_Bin() {
        return unilex.equals("plus") || unilex.equals("mult") || unilex.equals("moins") || unilex.equals("divi");
    }
    private void analex() {
        unilex = lexicalAnalyser.analex();
    }
    private boolean is(String lex) {
        return unilex.equals(lex);
    }
    private boolean isCle(String cle) {
        return lexicalAnalyser.getChaine().equals(cle);
    }
    private boolean isEnd() {
        return lexicalAnalyser.getCarlu().equals("$");
    }
    public void setVar(int exp, int adresse) {
        for(String key : idents.keySet()) {
            if(idents.get(key)[3].equals(String.valueOf(adresse))) {
                idents.get(key)[2]= String.valueOf(exp);
                return;
            }
        }
    }
    public int getVar(int adresse) {
        for(String key : idents.keySet()) {
            if(idents.get(key)[3].equals(String.valueOf(adresse)))
                return Integer.parseInt(idents.get(key)[2]);
        }
        return -1;
    }
}