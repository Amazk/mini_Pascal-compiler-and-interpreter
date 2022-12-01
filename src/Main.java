import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;

public class Main {
    public static void main(String[] args) throws IOException {
        SyntaxAnalyzer analyzer = new SyntaxAnalyzer(new LexicalAnalyser(new File("D:\\bureau\\Coding\\Java\\Compilator\\src\\source.txt")));
        interpreter(analyzer);
        System.out.println("Ident Table : "+analyzer.idents);
    }
    private static void interpreter(SyntaxAnalyzer analyzer) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("D:\\bureau\\Coding\\Java\\Compilator\\source.cod"));
        Stack<Integer> pilex = new Stack<>();
        scanner.nextLine();
        String pCode = scanner.nextLine();
        while (!pCode.equals("STOP")) {
            switch (pCode.substring(0, 4)) {
                case "ADDI" -> {
                    pilex.add(pilex.pop() + pilex.pop());
                    pCode = scanner.nextLine();
                }
                case "SOUS" -> {
                    pilex.add(pilex.pop() - pilex.pop());
                    pCode = scanner.nextLine();
                }
                case "MULT" -> {
                    pilex.add(pilex.pop() * pilex.pop());
                    pCode = scanner.nextLine();
                }
                case "DIVI" -> {
                    pilex.add(pilex.pop() / pilex.pop());
                    pCode = scanner.nextLine();
                }
                case "MOIN" -> {
                    pilex.add(-pilex.pop());
                    pCode = scanner.nextLine();
                }
                case "AFFE" -> {
                    analyzer.setVar(pilex.pop(), pilex.pop());
                    pCode = scanner.nextLine();
                }
                case "LIRE" -> {
                    Scanner scanner1 = new Scanner(System.in);
                    System.out.print("Entrer un entier : ");
                    analyzer.setVar(Integer.parseInt(scanner1.nextLine()), pilex.pop());
                    scanner1.close();
                    pCode = scanner.nextLine();
                }
                case "ECRL" -> {
                    System.out.println();
                    pCode = scanner.nextLine();
                }
                case "ECRE" -> {
                    System.out.println(pilex.pop());
                    pCode = scanner.nextLine();
                }
                case "ECRC" -> {
                    System.out.println(pCode.substring(5, pCode.length() - 5));
                    pCode = scanner.nextLine();
                }
                case "EMPI" -> {
                    pilex.add(Integer.parseInt(pCode.substring(5)));
                    pCode = scanner.nextLine();
                }
                case "CONT" -> {
                    pilex.add(analyzer.getVar(pilex.pop()));
                    pCode = scanner.nextLine();
                }
            }
        }
        scanner.close();
    }
}
