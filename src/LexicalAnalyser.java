import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LexicalAnalyser {

    private static final int LongMaxIdent = 20;
    private static final int LongMaxChaine = 50;
    private static final int NbMotsReserves = 7;
    private final Scanner source;
    private String carlu;
    private int number;
    private String chaine;
    private int numLine;
    private final String[] motsReserves = {"PROGRAMME","DEBUT","FIN","CONST","VAR","ECRIRE","LIRE"};
    private final String[] symbs = {",",";",".",":","(",")","<",">","=","+","-","*","/","<=",">=",":=","<>"};

    public LexicalAnalyser(File file) {
        try {
            source = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        source.useDelimiter("");
    }

    public int getNumber() {
        return number;
    }

    public String getCarlu() {
        return carlu;
    }

    public void run() {
        init();
        while (!carlu.equals("$")) {
            analex();
        }
        end();
    }

    public void init() {
        numLine = 0;
        lireCar();
    }

    public void end() {
        source.close();
        error(1);
    }

    public String analex() {
        String uniLex;
        sauterSeparateur();
        if(carlu.matches("\\d")) uniLex = recoEntier();
        else if(carlu.equals("'")) uniLex = recoChaine();
        else if(carlu.matches("[a-zA-Z]")) uniLex = reco_Ident_Motreserve();
        else if(isSymb()) uniLex = recoSymb();
        else {
            source.nextLine();
            numLine++;
            uniLex = analex();
        }
        return uniLex;
    }

    private void error(int numError) {
        String mesError="";
        switch (numError) {
            case 1 -> mesError = "End of file";
            case 2 -> mesError = "Entier trop grand";
            case 3 -> mesError = "Chaine trop grande";
            default -> {
            }
        }
        System.out.println(mesError+" at line "+numLine);
        System.exit(1);
    }

    private void lireCar() {
        if(!source.hasNext()) {           // End of file
            carlu = "$";
            return;
        }
        carlu = source.next();
    }

    private void sauterSeparateur() {
        while (carlu.equals(" ")) lireCar();
        if (carlu.equals("{")) {
            int count = 1;
            while (count != 0) {
                lireCar();
                if(carlu.equals("{")) count++;
                if(carlu.equals("}")) count--;
            }
        }
    }

    private String recoEntier() {
        StringBuilder temp = new StringBuilder(carlu);
        lireCar();
        while (carlu.matches("\\d")) {
            temp.append(carlu);
            lireCar();
        }
        number = Integer.parseInt(temp.toString());
        if(number>10e+8) error(2);             // number > MaxInt
        return "ent";
    }

    private String recoChaine() {
        StringBuilder temp = new StringBuilder();
        lireCar();
        while (!carlu.equals("'")) {
            temp.append(carlu);
            lireCar();
            if(carlu.equals("'")) {
                lireCar();
                if(!carlu.equals("'")) break;
                else {
                    lireCar();
                    temp.append("'");
                }
            }
        }
        chaine = temp.toString();
        if(chaine.length()>LongMaxChaine) error(3);
        return "ch";
    }

    private String reco_Ident_Motreserve() {
        StringBuilder temp = new StringBuilder(carlu);
        lireCar();
        while (carlu.equals("_") || carlu.matches("\\d") || carlu.matches("[a-zA-Z]")) {
            temp.append(carlu);
            lireCar();
        }
        chaine = temp.toString().toUpperCase();
        if(chaine.length() > LongMaxIdent) chaine = chaine.substring(0,LongMaxIdent);
        return isReservedWord() ? "motcle" : "ident";
    }

    private boolean isReservedWord() {
        for(String mot : motsReserves)
            if(chaine.equals(mot))
                return true;
        return false;
    }

    private String recoSymb() {
        String unilex="";
        switch (carlu) {
            case "," -> unilex = "virg";
            case ";" -> unilex = "ptvirg";
            case "." -> unilex = "point";
            case ":" -> {
                lireCar();
                return carlu.equals("=") ? "aff" : "deuxpts";
            }
            case "(" -> unilex = "parouv";
            case ")" -> unilex = "parfer";
            case "<" -> {
                lireCar();
                return switch (carlu) {
                    case "=" -> "infe";
                    case ">" -> "diff";
                    default -> "inf";
                };
            }
            case ">" -> {
                lireCar();
                return carlu.equals("=") ? "supe" : "sup";
            }
            case "=" -> unilex = "eg";
            case "+" -> unilex = "plus";
            case "-" -> unilex = "moins";
            case "*" -> unilex = "mult";
            case "/" -> unilex = "divi";
        }
        lireCar();
        return unilex;
    }

    private boolean isSymb() {
        for(String symb : symbs)
            if(carlu.equals(symb))
                return true;
        return false;
    }
}
