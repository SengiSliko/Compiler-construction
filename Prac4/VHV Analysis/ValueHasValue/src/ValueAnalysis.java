import java.io.*;
import java.util.*;

public class ValueAnalysis {
    Hashtable<String, String[]> symbolTable = null;
    Scoping scope = null;
    boolean hasvalue;
    String assignor;
    String assignee;
    String currentCallId;
    int assignorIndex =0;
    int errorIndex = 0;
    Boolean insideThen = false;
    Boolean insideElse = false;
    Boolean thenHasHalt = false;
    Boolean elseHasHalt = false;
    List<String> valuesToPlaceInThen =  new ArrayList<>();
    List<String> valuesToPlaceInElse =  new ArrayList<>();


    public void Analyse() {
        scope = new Scoping();
        symbolTable = scope.Scope();
        assignee = "";
        assignor = "";
        currentCallId = "";

        checkValues(scope.root);

        System.out.println("\u001B[32mSuccess\u001B[0m: Sementic Analysis Successful");
        System.out.println("Open the symboltable.html file to view the table.");


        createHTMLTable();
        
    }

    private void checkValues(Node node){
        if(node ==null){
            return;
        }

        if(node.getType().equals("Non-Terminal")){
            
            if(node.getContent().equals("ASSIGN")){
                checkAssign(node);
                assignee = "";
                assignor = "";
                hasvalue = true;
            }
            else if(node.getContent().equals("OUTPUT")){
                checkOutPut(node);
                hasvalue = true;
            }
            else if(node.getContent().equals("INPUT")){
                placeFromInput(node);
            }
            else if(node.getContent().equals("LOOP")){
                checkLoop(node);
                hasvalue = true;
                return;
            }
            else if(node.getContent().equals("BRANCH")){
                checkBranch(node);
                hasvalue = true;
                return;
                
            }
            else if(node.getContent().equals("CALL")){
                checkCall(node, 1, "main");
            }
            else if(node.getContent().equals("PROCDEFS")){
                return;
            }
            for(Node child: node.children){
                checkValues(child);
            }
        }
        else{
            if(node.getContent().equals("h")){
                createHTMLTable();
                if(!insideThen && !insideElse){
                    System.out.println("\u001B[33mHalt\u001B[0m: Sementic Analysis Halted");
                    System.out.println("Open the symboltable.html file to view the table.");

                    System.exit(0);
                }
                else if(insideThen){
                    thenHasHalt = true;
                }
                else if(insideElse){
                    elseHasHalt = true;
                }
            }
            checkValues(null);
        }
    }

    private void checkAssign(Node node){
        setAssignee(node.children.get(0));
        if(node.children.get(2).getContent().equals("NUMEXPR")){
            hasvalue = true;
            checkNumExpr(node.children.get(2));
            if(hasvalue){
                if(!insideThen && !insideElse){
                    placeValue(assignee);
                }
                else if(insideThen){
                    valuesToPlaceInThen.add(assignee);
                }
                else if(insideElse){
                    valuesToPlaceInElse.add(assignee);
                }
                
            }
            else{
                if(node.children.get(2).children.size() == 1){
                    if(!insideThen && !insideElse){
                        System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ assignee +" := "+ assignor);
                        System.exit(0);
                    }
                }
                else if(node.children.get(2).children.size() == 6){
                    if(!insideThen && !insideElse){
                        if(errorIndex == 2)
                        System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ assignee +" := "+ node.children.get(2).children.get(0).getContent() + "("+assignor+",...)");

                        else if(errorIndex == 4)
                        System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ assignee +" := "+ node.children.get(2).children.get(0).getContent() + "(...,"+assignor+")");
        
                        System.exit(0);
                    }
                }
                
            }
            
        }
        else if(node.children.get(2).getContent().equals("BOOLEXPR")){
            hasvalue = true;
            checkBoolExpr(node.children.get(2));
            if(hasvalue){
                if(!insideThen && !insideElse){
                    placeValue(assignee);
                }
                else if(insideThen){
                    valuesToPlaceInThen.add(assignee);
                }
                else if(insideElse){
                    valuesToPlaceInElse.add(assignee);
                }
            }
            else{
                if(node.children.get(2).children.get(0).getContent().equals("LOGIC")){
                    if(node.children.get(2).children.get(0).children.size() == 1){
                        if(!insideThen && !insideElse){
                            System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ assignee +" := "+ assignor);
                            System.exit(0);
                        }
                    }
                    else if(node.children.get(2).children.get(0).children.size() == 6){
                        if(!insideThen && !insideElse){
                            if(errorIndex == 2)
                            System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ assignee +" := "+ node.children.get(2).children.get(0).children.get(0).getContent() + "("+assignor+",...)");
    
                            else if(errorIndex == 4)
                            System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ assignee +" := "+ node.children.get(2).children.get(0).children.get(0).getContent() + "(...,"+assignor+")");
            
                            System.exit(0);
                        }
                    }
                    else{
                        if(!insideThen && !insideElse){
                            System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ assignee +" := "+ node.children.get(2).children.get(0).children.get(0).getContent() + "("+assignor+")");
                            System.exit(0);
                        }
                    }
                }
                else{
                    if(!insideThen && !insideElse){
                        if(errorIndex == 2)
                        System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ assignee +" := "+ node.children.get(2).children.get(0).children.get(0).getContent() + "("+assignor+",...)");

                        else if(errorIndex == 4)
                        System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ assignee +" := "+ node.children.get(2).children.get(0).children.get(0).getContent() + "(...,"+assignor+")");
        
                        System.exit(0);
                    }
                }
                
            }
        }
        else{
            hasvalue = true;
            if(!insideThen && !insideElse){
                placeValue(assignee);
            }
            else if(insideThen){
                valuesToPlaceInThen.add(assignee);
            }
            else if(insideElse){
                valuesToPlaceInElse.add(assignee);
            }
        }
    }

    private void checkNumExpr(Node node){
        if(node.children.get(0).getContent().equals("a") || node.children.get(0).getContent().equals("m") || node.children.get(0).getContent().equals("d")){
            assignorIndex =2;
            checkNumExpr(node.children.get(2));
            assignorIndex =4;
            checkNumExpr(node.children.get(4));
        }
        else if(node.children.get(0).getContent().equals("NUMVAR")){
            String name = scope.getVAR(node.children.get(0), 0, "");
            checkVar(name);
        }
        else if(node.children.get(0).getContent().equals("DECNUM")){
           return;
        }
    } 

    private void checkBoolExpr(Node node){
        if(node.children.get(0).getContent().equals("LOGIC")){
            checkLogic(node.children.get(0));
        }
        else if(node.children.get(0).getContent().equals("CMPR")){
            checkCMPR(node.children.get(0));
        }
    }

    private void checkVar(String name){
        
        if(checkIfHasValue(name)){
            return;
        }
        else{
            errorIndex = assignorIndex;
            hasvalue = false;
        }
    }

    private void checkLogic(Node node){
        if(node.children.get(0).getContent().equals("BOOLVAR")){
            String name = scope.getVAR(node.children.get(0), 0, "");
            checkVar(name);
        }
        else if(node.children.get(0).getContent().equals("T") || node.children.get(0).getContent().equals("F")){
            return;
        }
        else if(node.children.get(0).getContent().equals("^") || node.children.get(0).getContent().equals("v")){
            assignorIndex = 2;
            checkBoolExpr(node.children.get(2));
            assignorIndex = 4;
            checkBoolExpr(node.children.get(4));
        } 
        else if(node.children.get(0).getContent().equals("!")){
            checkBoolExpr(node.children.get(2));
        }
    }

    private void checkCMPR(Node node){
        assignorIndex =2;
        checkNumExpr(node.children.get(2));
        assignorIndex =4;

        checkNumExpr(node.children.get(4));
    }

    private void checkOutPut(Node node){
        if(node.children.get(0).getContent().equals("TEXT")){
            checkText(node.children.get(0));
            if(!hasvalue){
                if(!insideThen && !insideElse){
                    System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ "r "+ assignor);
                    System.exit(0);
                }

            }
        }
        else if(node.children.get(0).getContent().equals("VALUE")){
            checkValue(node.children.get(0));
            if(!hasvalue){
                if(!insideThen && !insideElse){
                    System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ "o "+ assignor);
                    System.exit(0);
                }
            }
        }
    }

    private void checkText(Node node){
        String name = scope.getVAR(node.children.get(1), 0, "");
        checkVar(name);
    }

    private void checkValue(Node node){
        String name = scope.getVAR(node.children.get(1), 0, "");
        checkVar(name);
    }

    private void placeFromInput(Node node){
        String name = scope.getVAR(node.children.get(1), 0, "");
        if(!insideThen && !insideElse){
            placeValue(name);
        }
        else if(insideThen){
            valuesToPlaceInThen.add(name);
        }
        else if(insideElse){
            valuesToPlaceInElse.add(name);
        }
    }

    private void checkLoop(Node node){
        hasvalue = true;
        checkBoolExpr(node.children.get(2));
        if(!hasvalue){
            if(!insideThen && !insideElse){
                System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ "w("+ assignor+") loop condition.");
                System.exit(0);
            }
            
        }

    }

    private void checkBranch(Node node){
        hasvalue = true;
        checkBoolExpr(node.children.get(2));
        if(!hasvalue){
            if(!insideThen && !insideElse){
                System.out.println("\u001B[31mSementic Error\u001B[0m: Variable "+ assignor + " has no value at "+ "i("+ assignor+") branch condition.");
                System.exit(0);
            }
        }

        if(node.children.size() == 9){
            insideThen = true;
            checkValues(node.children.get(6));
            insideThen = false;
            insideElse = true;
            checkValues(node.children.get(8));
            insideElse = false;

            for(String value: valuesToPlaceInThen){
                for(String value2: valuesToPlaceInElse){

                    if(value.equals(value2)){
                        placeValue(value);
                    }
                }
            }
        }

        if(thenHasHalt && elseHasHalt){
            System.out.println("\u001B[33mHalt\u001B[0m: Sementic Analysis Halted");
            System.out.println("Open the symboltable.html file to view the table.");
            System.exit(0);
        }
    }

    private Boolean checkIfHasValue(String name){
        for(String key: symbolTable.keySet()){
            String[] value = symbolTable.get(key);
            if(name.equals(value[0])){
                
                if(value[3].equals("False")){
                    assignor = value[0];
                    return false;
                }
                else{
                    return true;
                }
            }
        }
        return false;
    }

    private void placeValue(String name){
        for(String key: symbolTable.keySet()){
            String [] value = symbolTable.get(key);

            if(name.equals(value[0])){
                String[] val = {value[0],value[1], value[2], "True"};
                symbolTable.replace(key, val);
                return;
            }
        }
    }

    private void setAssignee(Node node){
        String name = scope.getVAR(node, 0, "");
        assignee = name;
    }

    private void checkCall(Node node, int currentScopeID, String currentScope){
        String callName = scope.getCallName(node);
        String callScopeID = String.valueOf(currentScopeID);
        currentCallId = "";
        if(checkSelfCall(callName, currentScope, currentScopeID) || checkChildCall(callName, callScopeID, currentScope) || checkSiblingCall(callName, callScopeID, currentScope)){
            gotToCallLocation(currentCallId, scope.root);
        }
        else{
            System.out.println("\u001B[31mSementic Error\u001B[0m: "+callName+" has no corresponding declaration in this scope!");
            System.exit(0);
        }
    }

    private Boolean checkSelfCall(String callName, String currentScope, int currentScopeID){
        if(callName.equals(currentScope)){
            currentCallId = String.valueOf(currentScopeID);
            return true;
        }
        return false;
    }

    private Boolean checkChildCall(String callName, String currentScopeID, String currentScope){
        for(String key: symbolTable.keySet()){
            String[] value = symbolTable.get(key);
            if(value[0].charAt(0) == 'p'){
                if(value[1].equals(currentScopeID) && value[0].equals(callName)){
                    currentCallId = key;
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean checkSiblingCall(String callName, String currentScopeID, String currentScope){
        String parentScopeID = "";
        for(String key: symbolTable.keySet()){
            String[] value = symbolTable.get(key);
            if(value[0].charAt(0) == 'p'){
                if(key.equals(currentScopeID)){
                    parentScopeID = value[1];
                    //parentScopeID = 1;
                    break;
                }
            }
        }

        for(String key: symbolTable.keySet()){
            String[] value = symbolTable.get(key);
            if(value[0].charAt(0) == 'p'){
                if(key.equals(currentScopeID)){
                    continue;
                }
                if(value[1].equals(parentScopeID) && value[0].equals(callName)){
                    currentCallId = key;
                    return true;
                }
            }
        }
        return false;
    }

    private void gotToCallLocation(String id, Node node){
        if(node == null){
            return;
        }

        if(node.getType().equals("Non-Terminal")){
            if(node.getContent().equals("PROC")){
                if(node.getId() == Integer.parseInt(id)){
                    checkValues(node.children.get(3));
                }
            }
            for(Node child: node.children){
                gotToCallLocation(id, child);
            }
        }
        else{
            gotToCallLocation(id, null);
        }


    }
    private void createHTMLTable(){
        // create html file
        try {
            FileWriter fileWriter = new FileWriter("symboltable.html");
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
            fileWriter.write("<th>Node-ID</th>\n");
            fileWriter.write("<th>NodeName</th>\n");
            fileWriter.write("<th>Scope-ID</th>\n");
            fileWriter.write("<th>ScopeName</th>\n");
            fileWriter.write("<th>Has-Value</th>\n");
            fileWriter.write("</tr>\n");

            for(String key: symbolTable.keySet()){
                String[] value = symbolTable.get(key);
                fileWriter.write("<tr>\n");
                fileWriter.write("<td>" + key + "</td>\n");
                fileWriter.write("<td>" + value[0] + "</td>\n");
                fileWriter.write("<td>" + value[1] + "</td>\n");
                fileWriter.write("<td>" + value[2] + "</td>\n");
                fileWriter.write("<td>" + value[3] + "</td>\n");
                fileWriter.write("</tr>\n");
            }
            fileWriter.write("</body>\n");
            fileWriter.write("</html>\n");

            fileWriter.close();



           
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}