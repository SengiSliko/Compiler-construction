import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.print("Enter a regular expression: ");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        scanner.close();

        input = input.replaceAll("\\s+","");

        if(isRegexCorrect(input)){
            System.out.println("\u001B[32mRegex is correct\u001B[0m");
            ToNFA nfa = new ToNFA();

            NFA convertedNFA = nfa.ConvertToNFA(input);
             // nfa.printNFA(convertedNFA);
     
             // System.out.println("=================================================================");
             // System.out.println();
     
             ToDFA dfa = new ToDFA();
             DFA convertedDFA = dfa.convertToDFA(convertedNFA);
             // dfa.printDFA(convertedDFA);
     
             // System.out.println("=================================================================");
             // System.out.println();
     
             ToMinDFA minDFA = new ToMinDFA();
             MinDFA minDFA1 = minDFA.convertToMinDFA(convertedDFA);
             minDFA.printMinDFA(minDFA1);
         }
        else{
            System.out.println("\u001B[31mRegex is incorrect\u001B[0m");
        }
    }
    
    public static boolean isRegexCorrect(String input){
        int numoOpenBrackets = 0;
        int numClosedBrackets = 0;
        for(int i = 0;i<input.length();i++){
            if(numoOpenBrackets < numClosedBrackets){
                return false;
            }
            if(input.charAt(i) == '('){
                numoOpenBrackets++;
            }
            else if(input.charAt(i) == ')'){
                numClosedBrackets++;
            }
        }
        if(numoOpenBrackets != numClosedBrackets){
            return false;
        }

        if(input.charAt(0) == '*' || input.charAt(0) == '|' || input.charAt(0) == '?' || input.charAt(0) == '+'){
            return false;
        }

        for(int i = 0;i<input.length();i++){
            if(input.charAt(i) != '(' && input.charAt(i) != ')' && input.charAt(i) != '*' && input.charAt(i) != '|' && input.charAt(i) != '?' && input.charAt(i) != '+' && !Character.isLetterOrDigit(input.charAt(i)) ){
                return false;
            }

            if(input.charAt(i) == '('){
                if(i != input.length()-1){
                    if(input.charAt(i+1) == '*' || input.charAt(i+1) == '|' || input.charAt(i+1) == '?' || input.charAt(i+1) == '+'){
                        return false;
                    }
                    if(input.charAt(i+1) == ')'){
                        return false;
                    }
                }
                else{
                    return false;
                }
                
            }
            if(input.charAt(i) == '|'){
                if(i == 0 || i == input.length()-1){
                    return false;
                }
                if(input.charAt(i-1) == '(' || input.charAt(i+1) == ')' || input.charAt(i-1) == '|' || input.charAt(i+1) == '*' || input.charAt(i+1) == '|' || input.charAt(i+1) == '?' || input.charAt(i+1) == '+'){
                    return false;
                }
            }

        }
        return true;
      
    }
}
