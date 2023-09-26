import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Scoping {

    private Node root = null;

    private Hashtable<String, String[]> scopeTable = null;

    private List<String> calledList;

    public Scoping() {
        scopeTable = new Hashtable<String, String[]>();
        calledList = new ArrayList<String>();
    }

    public void Scope() {

        System.out.print("Enter file name: ");
        try (Scanner scanner = new Scanner(System.in)) {
            String fileName = scanner.nextLine();
            Parser parser = new Parser(fileName);
            root = parser.parse();
            System.out.println("---------------------------------------------------------------");
            System.out.println("Sementic Analysis:");

            createTable(root,"main",1);

            // printTable();

            checkNaming();

            checkCalls(root, "main", 1);

            
            if(checkIfAllCalled()){
                System.out.println("\u001B[32mSuccess\u001B[0m: Sementic Analysis Successful");
            }
            System.out.println();
            System.out.println("View the SymbolTable.html file for the symbol table");

            createHTMLTable();

        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        
    }

    private void createTable(Node node, String currentScope, int currentScopeID) {
        if(node == null) {
            return;
        }


        if(node.getType().equals("Non-Terminal")){

            if(node.getContent().equals("NUMVAR") || node.getContent().equals("BOOLVAR") || node.getContent().equals("STRINGV")){
                String var = getVAR(node, node.getId() , "");

                String[] value = {var, "0", "global"};
                Boolean isDuplicate = false;

                for(String key: scopeTable.keySet()){
                    String[] tableValue = scopeTable.get(key);
                    if(isDuplicate(value, tableValue)){
                        isDuplicate = true;
                        break;
                    }
                }
                if(!isDuplicate){
                    scopeTable.put(String.valueOf(node.getId()), value);
                }

                for(Node child : node.children) {
                    createTable(child, currentScope, currentScopeID);
                }
            }
            else if(node.getContent().equals("PROC")){
                String procName = getProcName(node, node.getId(), "");

                String[] value = {procName, String.valueOf(currentScopeID), currentScope};

                scopeTable.put(String.valueOf(node.getId()), value);

                // procName = p1
                //
                for(Node child : node.children) {
                    createTable(child, procName, node.getId());
                }
            }
            else{
                for(Node child : node.children) {
                    createTable(child, currentScope, currentScopeID);
                }
            }
            
        }
        else{
            createTable(null, currentScope, currentScopeID);
        }

    }

    private String getVAR(Node node, int id,  String var){
        if(node == null) {
            return "";
        }

        if(node.getType().equals("Non-Terminal")){
           
            if(node.children.size()==2){
                return getVAR(node.children.get(0), id, var) + getVAR(node.children.get(1), id, var);
            }
            else{
                return getVAR(node.children.get(0), id, var);
            }
        }
        else{
            return node.getContent();
        }
        
    }

    private String getProcName(Node node, int id,  String procName){
        if(node == null) {
            return "";
        }

        if(node.getType().equals("Non-Terminal")){

            if(node.getContent().equals("PROC")){
                return getProcName(node.children.get(0), id, procName) + getProcName(node.children.get(1), id, procName);
            }
            else{
                if(node.children.size()==2){
                    return getProcName(node.children.get(0), id, procName) + getProcName(node.children.get(1), id, procName);
                }
                else{
                    return getProcName(node.children.get(0), id, procName);
                }
            }
           
            
        }
        else{
            return node.getContent();
        }
        
    }

    private Boolean isDuplicate(String [] str1, String [] str2){
        if(str1[0].equals(str2[0])){
            return true;
        }

        return false;
    }

    private void checkNaming(){
        for(String key: scopeTable.keySet()){
            String[] value = scopeTable.get(key);
            if(value[0].charAt(0) == 'p'){
                if(checkSiblings(key,value) && checkParent(key, value) && checkUncle(key, value)){
                    continue;
                }
                else{
                    System.out.println("\u001B[31mSementic Error\u001B[0m: invalid procedure declaration in "+ value[2]);
                    System.exit(0);
                }
            } 
        }
    }

    private Boolean checkSiblings(String key, String [] value){
        String scopeID =  value[1];
        String nodeName = value[0];
        for(String Tablekey: scopeTable.keySet()){
            String[] TableValue = scopeTable.get(Tablekey);
            if(TableValue[0].charAt(0) == 'p'){
                if(Tablekey.equals(key)){
                    continue;
                }
                else if(scopeID.equals(TableValue[1]) && nodeName.equals(TableValue[0]) ){
                    return false;
                }
            }
            
        }

        return true;

    }

    private Boolean checkParent(String key, String [] value){
        if(value[0].equals(value[2])){
            return false;
        }

        return true;
    }

    private Boolean checkUncle(String key, String [] value){
        String parentId = value[1];
        String parentScopeID = "";
        for(String Tablekey: scopeTable.keySet()){
            String[] TableValue = scopeTable.get(Tablekey);
            if(TableValue[0].charAt(0) == 'p'){
                if(Tablekey.equals(parentId)){
                    parentScopeID = TableValue[1];
                }
            }
        }

        for(String Tablekey: scopeTable.keySet()){
            String[] TableValue = scopeTable.get(Tablekey);
            if(TableValue[0].charAt(0) == 'p'){
                if(Tablekey.equals(key)){
                    continue;
                }
                else if(parentScopeID.equals(TableValue[1]) && value[0].equals(TableValue[0]) ){
                    return false;
                }
            }
        }
        return true;

    }

    private void checkCalls(Node node, String currentScope, int currentScopeID) {
        if(node == null) {
            return;
        }

        if(node.getType().equals("Non-Terminal")){

            if(node.getContent().equals("PROC")){
                String procName = getProcName(node, node.getId(), "");

                for(Node child : node.children) {
                    checkCalls(child, procName, node.getId());
                }
            }
            else if(node.getContent().equals("CALL")){
                String callName = getCallName(node);
                String callScopeID = String.valueOf(currentScopeID);
                if(checkSelfCall(callName, currentScope, currentScopeID) || checkChildCall(callName, callScopeID, currentScope) || checkSiblingCall(callName, callScopeID, currentScope)){
                    for(Node child : node.children) {
                        checkCalls(child, currentScope, currentScopeID);
                    }
                }
                else{
                    System.out.println("\u001B[31mSementic Error\u001B[0m: "+callName+" has no corresponding declaration in this scope!");
                    System.exit(0);
                }
            }
            else{
                for(Node child : node.children) {
                    checkCalls(child, currentScope, currentScopeID);
                }
            }
            
        }
        else{
            checkCalls(null, currentScope, currentScopeID);
        }

    }

    private String getCallName(Node node){
        if(node == null) {
            return "";
        }

        if(node.getType().equals("Non-Terminal")){
           
            if(node.getContent().equals("CALL")){
                return getCallName(node.children.get(1)) + getCallName(node.children.get(2));
            }else{
                if(node.children.size()==2){
                    return getCallName(node.children.get(0)) + getCallName(node.children.get(1));
                }
                else{
                    return getCallName(node.children.get(0));
                }
            }
        }
        else{
            return node.getContent();
        }
        
    }

    private Boolean checkSelfCall(String callName, String currentScope, int currentScopeID){
        if(callName.equals(currentScope)){
            calledList.add(String.valueOf(currentScopeID));
            return true;
        }
        return false;
    }

    private Boolean checkChildCall(String callName, String currentScopeID, String currentScope){
        for(String key: scopeTable.keySet()){
            String[] value = scopeTable.get(key);
            if(value[0].charAt(0) == 'p'){
                if(value[1].equals(currentScopeID) && value[0].equals(callName)){
                    calledList.add(key);
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean checkSiblingCall(String callName, String currentScopeID, String currentScope){
        String parentScopeID = "";
        for(String key: scopeTable.keySet()){
            String[] value = scopeTable.get(key);
            if(value[0].charAt(0) == 'p'){
                if(key.equals(currentScopeID)){
                    parentScopeID = value[1];
                    //parentScopeID = 1;
                    break;
                }
            }
        }

        for(String key: scopeTable.keySet()){
            String[] value = scopeTable.get(key);
            if(value[0].charAt(0) == 'p'){
                if(key.equals(currentScopeID)){
                    continue;
                }
                if(value[1].equals(parentScopeID) && value[0].equals(callName)){
                    calledList.add(key);
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean checkIfAllCalled(){
        Boolean var = true;
        for(String key: scopeTable.keySet()){
            String[] value = scopeTable.get(key);
            if(value[0].charAt(0) == 'p'){
                if(!calledList.contains(key)){
                    System.out.println("\u001B[34mWarning\u001B[0m: "+ value[0] +" is not called from anywhere within the scope to which it belongs!");
                    var = false;
                }
            }
        }

        return var;
    }

    private void createHTMLTable(){
        // create html file
        try {
            FileWriter fileWriter = new FileWriter("SymbolTable.html");

            fileWriter.write("<!DOCTYPE html>\n");
            fileWriter.write("<html lang=\"en\">\n");
            fileWriter.write("<head>\n");
            fileWriter.write("<meta charset=\"UTF-8\">\n");
            fileWriter.write("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            fileWriter.write("<title>Symbol Table</title>\n");
            fileWriter.write("</head>\n");

            fileWriter.write("<style>\n");
            fileWriter.write("table, th, td {\n");
            fileWriter.write("border: 1px solid black;\n");
            fileWriter.write("}\n");
            fileWriter.write("</style>\n");
            
            fileWriter.write("<body>\n");
            fileWriter.write("<table>\n");
            fileWriter.write("<tr>\n");
            fileWriter.write("<th>NodeID</th>\n");
            fileWriter.write("<th>NodeName</th>\n");
            fileWriter.write("<th>ScopeID</th>\n");
            fileWriter.write("<th>ScopeName</th>\n");
            fileWriter.write("</tr>\n");

            for(String key: scopeTable.keySet()){
                String[] value = scopeTable.get(key);
                fileWriter.write("<tr>\n");
                fileWriter.write("<td>" + key + "</td>\n");
                fileWriter.write("<td>" + value[0] + "</td>\n");
                fileWriter.write("<td>" + value[1] + "</td>\n");
                fileWriter.write("<td>" + value[2] + "</td>\n");
                fileWriter.write("</tr>\n");
            }
            
            fileWriter.write("</table>\n");
            fileWriter.write("</body>\n");
            fileWriter.write("</html>\n");

            fileWriter.close();



           
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }


    // private void printTable(){
    //     for(String key: scopeTable.keySet()){
    //         String[] value = scopeTable.get(key);
    //         System.out.println("NodeID: " + key + " | " + "NodeName: " + value[0] + " | " + "ScopeID: " + value[1] + " | " + "ScopeName: " + value[2]);
    //     }
    // }
}
