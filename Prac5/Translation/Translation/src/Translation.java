import java.io.*;
import java.util.*;

class Translation{

    private String code="";
    int lineNumber = 10;
    Scoping scope = null;
    Hashtable<Integer, Integer> procTable = new Hashtable<Integer, Integer>();
    Hashtable<Integer, Integer> AlgoTable = new Hashtable<Integer, Integer>();
    int currentProcId;
    boolean inProc = false;
    boolean inBranch = false;
    int branchCount = 0;
    int loopCount = 0;
    
    public void Translate(){
        currentProcId = 0;
        scope = new  Scoping();
        scope.Scope();

        preOrder(scope.root, 1, "main");

        createBASICFile();
    }

    private void preOrder(Node node, int currentScope, String currentProc){
        if(node == null){
            return;
        }


        if(node.getType().equals("Non-Terminal")){
           if(node.getContent().equals("PROGR")){
                if(!inProc){
                    if(node.children.size() ==2){
                        preOrder(node.children.get(0), currentScope, currentProc);
                        code+= String.valueOf(lineNumber) + " END\n";
                        lineNumber+=10;
                        preOrder(node.children.get(1), currentScope, currentProc);
                    }
                    else{
                        preOrder(node.children.get(0), currentScope, currentProc);
                        code += String.valueOf(lineNumber) + " END\n";
                        lineNumber+=10;
    
                    }
                    return;
                }
                else{
                    if(node.children.size() ==2){
                        preOrder(node.children.get(0), currentScope, currentProc);
                        code+= String.valueOf(lineNumber) + " RETURN\n";
                        lineNumber+=10;
                        preOrder(node.children.get(1), currentScope, currentProc);
                    }
                    else{
                        preOrder(node.children.get(0), currentScope, currentProc);
                        code += String.valueOf(lineNumber) + " RETURN\n";
                        lineNumber+=10;
    
                    }
                    return;
                }
               
           }
           else if(node.getContent().equals("INPUT")){
                TransInput(node);
           }
           else if(node.getContent().equals("OUTPUT")){
                TransOutput(node);
           }
           else if(node.getContent().equals("ASSIGN")){
                TransAssign(node);
           }
           else if(node.getContent().equals("CALL")){
            String callName = scope.getCallName(node);
            String callScopeID = String.valueOf(currentScope);
            if(checkSelfCall(callName, currentProc, currentScope) || checkChildCall(callName, callScopeID , currentProc) || checkSiblingCall(callName, callScopeID, currentProc)){
                TransCall(node);
            }
           }
           else if(node.getContent().equals("BRANCH")){
                branchCount++;
                TransBranch(node, currentScope, currentProc);
                
                return;
           }
           else if(node.getContent().equals("LOOP")){
                loopCount++;
                TransLoop(node, currentScope, currentProc);
                return;
           }
           else if(node.getContent().equals("PROC")){
                TransProc(node, node.getId());
                return;
           }
            for(Node child: node.children){
                preOrder(child, currentScope, currentProc);
            }

        }else{
            if(node.getContent().equals("h"))
                code+= String.valueOf(lineNumber) + " STOP\n";

            preOrder(null, currentScope, currentProc);
        }
    }

    private void TransInput(Node node){
        String varName = scope.getVAR(node.children.get(1), lineNumber, code);
        code += String.valueOf(lineNumber) + " INPUT \"\";" + varName + "\n";
        lineNumber += 10;
    }

    private void TransOutput(Node node){
        if(node.children.get(0).getContent().equals("TEXT")){
            TransText(node.children.get(0));
        }
        else if(node.children.get(0).getContent().equals("VALUE")){
            TransValue(node.children.get(0));
        }
    }

    private void TransText(Node node){
        String varName = scope.getVAR(node.children.get(1), lineNumber, code);
        code += String.valueOf(lineNumber) + " PRINT; " + varName + "$\n";
        lineNumber += 10;
}

    private void TransValue(Node node){
        String varName = scope.getVAR(node.children.get(1), lineNumber, code);
        code += String.valueOf(lineNumber) + " PRINT; " + varName + "\n";
        lineNumber += 10;
    }

    private void TransAssign(Node node){
        String assignee = scope.getVAR(node.children.get(0), lineNumber, code);
        String assignor = "";
        if(node.children.get(2).getContent().equals("NUMEXPR")){
            assignor = TransNumExpr(node.children.get(2));
        }
        else if(node.children.get(2).getContent().equals("BOOLEXPR")){
            assignor = TransBoolExpr(node.children.get(2));
        }
        else{
            assignor = TransStri(node.children.get(2));
            assignee += "$";
        }

        code += String.valueOf(lineNumber) + " LET " + assignee + " = " + assignor + "\n";
        lineNumber += 10;
    }

    private void TransCall(Node node){
        code+= String.valueOf(lineNumber) + " GOSUB " + currentProcId + "\n";
        lineNumber += 10;
    }

    private void TransLoop(Node node, int currentScope, String currentProc){
        String boolexpr = TransBoolExpr(node.children.get(2));
        int thisLoopCount = loopCount;
        int entryLineNumber = lineNumber;

        code += String.valueOf(lineNumber) + " " + "IF " + boolexpr + " Then GOTO other"+ thisLoopCount  + "\n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " " + "GOTO exit" +thisLoopCount  + "\n";
        lineNumber += 10;
        int otherLineNumber = lineNumber;
        preOrder(node.children.get(5), currentScope, currentProc);
        code+= String.valueOf(lineNumber) + " " + "GOTO " + entryLineNumber + "\n";
        lineNumber += 10;
        int exitLineNumber = lineNumber;

        code = code.replace("other"+ thisLoopCount, String.valueOf(otherLineNumber));
        code = code.replace("exit" + thisLoopCount, String.valueOf(exitLineNumber));

    }

    private void TransBranch(Node node, int currentScope, String currentProc){
        if(node.children.size() == 9){
            String boolexpr = TransBoolExpr(node.children.get(2));
            int thisBranchCount = branchCount;

            code+= String.valueOf(lineNumber) + " " + "IF " + boolexpr + " Then GOTO other"+ thisBranchCount  + "\n";
            lineNumber += 10;
            preOrder(node.children.get(8), currentScope, currentProc);
            code+= String.valueOf(lineNumber) + " " + "GOTO exit" + thisBranchCount + "\n";
            lineNumber += 10;
            int otherLineNumber = lineNumber;
            preOrder(node.children.get(6), currentScope, currentProc);
            int exitLineNumber = lineNumber;

            code = code.replace("other" + thisBranchCount, String.valueOf(otherLineNumber));
            code = code.replace("exit" + thisBranchCount, String.valueOf(exitLineNumber));

        }else{
            String boolexpr = TransBoolExpr(node.children.get(2));
            int thisBranchCount = branchCount;

            code+= String.valueOf(lineNumber) + " " + "IF " + boolexpr + " Then GOTO other"+ thisBranchCount  + "\n";
            lineNumber += 10;
            code+= String.valueOf(lineNumber) + " " + "GOTO exit" + thisBranchCount   + "\n";
            lineNumber += 10;
            int otherLineNumber = lineNumber;
            preOrder(node.children.get(6), currentScope, currentProc);
            int exitLineNumber = lineNumber;

            code = code.replace("other" + thisBranchCount, String.valueOf(otherLineNumber));
            code = code.replace("exit" + thisBranchCount, String.valueOf(exitLineNumber));
        }
        

    }

    // private String TransCond(String cond, int line1, int line2){
    //     if(cond.equals("1")){
    //         return "GOTO " + line1 ;
    //     }
    //     else if(cond.equals("0")){
    //         return "GOTO " + line2 ;
    //     }
    //     else if(cond.contains("=")){
    //         return "IF " + cond + " GOTO " + line1 + "\n";
    //     }
    //     else{
    //         return "IF " + cond + " <> 0 GOTO " + line1 + "\n";
    //     }
    // }

    private String TransStri(Node node){
        return node.children.get(0).getContent();
    }

    private String TransNumExpr(Node node){
        if(node.children.get(0).getContent().equals("a")){
            return TransNumExpr(node.children.get(2)) + " + " + TransNumExpr(node.children.get(4));
        }
        else if(node.children.get(0).getContent().equals("m")){
            return TransNumExpr(node.children.get(2)) + " * " + TransNumExpr(node.children.get(4));
        }
        else if(node.children.get(0).getContent().equals("d")){
            return TransNumExpr(node.children.get(2)) + " / " + TransNumExpr(node.children.get(4));

        }
        else if(node.children.get(0).getContent().equals("NUMVAR")){
            return scope.getVAR(node.children.get(0), lineNumber, code);
        }
        else if(node.children.get(0).getContent().equals("DECNUM")){
            return TransDecnum(node.children.get(0));
        }
        else{
            return "";
        }
    }

    private String TransDecnum(Node node){
        if(node.children.get(0).getContent().equals("0.00")){
            return node.children.get(0).getContent();
        }
        else if(node.children.get(0).getContent().equals("NEG")){
            return TransNeg(node.children.get(0));
        }
        else if(node.children.get(0).getContent().equals("POS")){
            return TransPos(node.children.get(0));
        }
        else{
            return "";
        }
    }

    private String TransNeg(Node node){
        return "-" + TransPos(node.children.get(1));
    }

    private String TransPos(Node node){
        return TransInt(node.children.get(0)) + "." + TransD(node.children.get(2)) + TransD(node.children.get(3));
    }

    private String TransInt(Node node){
        if(node.children.size() == 2)
            return node.children.get(0).getContent() + TransMore(node.children.get(1));
        else
            return node.children.get(0).getContent();
    }

    private String TransD(Node node){
        return node.children.get(0).getContent();
    }

    private String TransMore(Node node){
        return TransDigits(node.children.get(0));
    }

    private String TransDigits(Node node){
        if(node.children.size() == 2)
            return TransD(node.children.get(0)) + TransMore(node.children.get(1));
        else
            return TransD(node.children.get(0));
    }

    private String TransBoolExpr(Node node){
        if(node.children.get(0).getContent().equals("LOGIC")){
            return TransLogic(node.children.get(0));
        }
        else{

            return TransCmpr(node.children.get(0));
        }
    }

    private String TransLogic(Node node){
        if(node.children.get(0).getContent().equals("BOOLVAR")){
            return scope.getVAR(node.children.get(0), currentProcId, code);
        }
        else if(node.children.get(0).getContent().equals("T")){
            return "1";
        }
        else if(node.children.get(0).getContent().equals("F")){
            return "0";
        }
        else if(node.children.get(0).getContent().equals("^")){
            TransAnd(node);
            return "P";
        }
        else if(node.children.get(0).getContent().equals("v")){
            TransOr(node);
            return "P";

        }
        else if(node.children.get(0).getContent().equals("!")){
            TransNot(node);
            return "P";

        }
        else{
            return "";
        }
    }

    private void TransAnd(Node node){
        String boolexpr1 = TransBoolExpr(node.children.get(2));
        String boolexpr2 = TransBoolExpr(node.children.get(4));

        code += String.valueOf(lineNumber) + " IF " + boolexpr1 + " THEN GOTO otherCond \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " GOTO failed \n";
        lineNumber += 10;
        int otherCondLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " IF " + boolexpr2 + " THEN GOTO success \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " GOTO failed \n";
        lineNumber += 10;
        int successLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " LET P = 1 \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " GOTO exit \n";
        lineNumber += 10;
        int failedLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " LET P = 0 \n";
        lineNumber += 10;
        int exitLineNumber = lineNumber;

        code = code.replace("otherCond", String.valueOf(otherCondLineNumber));
        code = code.replace("success", String.valueOf(successLineNumber));
        code = code.replace("failed", String.valueOf(failedLineNumber));
        code = code.replace("exit", String.valueOf(exitLineNumber));


    }

    private void TransOr(Node node){
        String boolexpr1 = TransBoolExpr(node.children.get(2));
        String boolexpr2 = TransBoolExpr(node.children.get(4));

        code += String.valueOf(lineNumber) + " IF " + boolexpr1 + " THEN GOTO success \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " IF " + boolexpr2 + " THEN GOTO success \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " GOTO failed \n";
        lineNumber += 10;
        int successLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " LET P = 1 \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " GOTO exit \n";
        lineNumber += 10;
        int failedLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " LET P = 0 \n";
        lineNumber += 10;
        int exitLineNumber = lineNumber;

        code = code.replace("success", String.valueOf(successLineNumber));
        code = code.replace("failed", String.valueOf(failedLineNumber));
        code = code.replace("exit", String.valueOf(exitLineNumber));


    }

    private void TransNot(Node node){
        String boolexpr1 = TransBoolExpr(node.children.get(2));

        code += String.valueOf(lineNumber) + " IF " + boolexpr1 + " THEN GOTO failed \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " LET P = 1 \n";
        lineNumber += 10;
        code += String.valueOf(lineNumber) + " GOTO exit \n";
        lineNumber += 10;
        int failedLineNumber = lineNumber;
        code += String.valueOf(lineNumber) + " LET P = 0 \n";
        lineNumber += 10;
        int exitLineNumber = lineNumber;

        code = code.replace("failed", String.valueOf(failedLineNumber));
        code = code.replace("exit", String.valueOf(exitLineNumber));
    }

    private String TransCmpr(Node node){
        if(node.children.get(0).getContent().equals("E")){
            return TransNumExpr(node.children.get(2)) + " = " + TransNumExpr(node.children.get(4));
        }
        else if(node.children.get(0).getContent().equals("<")){
            return TransNumExpr(node.children.get(2)) + " < " + TransNumExpr(node.children.get(4));
        }
        else if(node.children.get(0).getContent().equals(">")){
            return TransNumExpr(node.children.get(2)) + " > " + TransNumExpr(node.children.get(4));
        }
        else{
            return "";
        }

    }

    private void TransProc(Node node, int currentScopeID){

        inProc = true;
        for(Integer key: procTable.keySet()){
            if(key == currentScopeID){
                procTable.replace(key, lineNumber);
            }
        }

        preOrder(node.children.get(3), currentScopeID, code);
        inProc = false;
    }

    private Boolean checkSelfCall(String callName, String currentScope, int currentScopeID){
        if(callName.equals(currentScope)){
            procTable.put(currentScopeID, 0);
            currentProcId = currentScopeID;
            return true;
        }
        return false;
    }

    private Boolean checkChildCall(String callName, String currentScopeID, String currentScope){
        for(String key: scope.scopeTable.keySet()){
            String[] value = scope.scopeTable.get(key);
            if(value[0].charAt(0) == 'p'){
                if(value[1].equals(currentScopeID) && value[0].equals(callName)){
                    procTable.put(Integer.parseInt(key), 0);
                    currentProcId = Integer.parseInt(key);
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean checkSiblingCall(String callName, String currentScopeID, String currentScope){
        String parentScopeID = "";
        for(String key: scope.scopeTable.keySet()){
            String[] value = scope.scopeTable.get(key);
            if(value[0].charAt(0) == 'p'){
                if(key.equals(currentScopeID)){
                    parentScopeID = value[1];
                    //parentScopeID = 1;
                    break;
                }
            }
        }

        for(String key: scope.scopeTable.keySet()){
            String[] value = scope.scopeTable.get(key);
            if(value[0].charAt(0) == 'p'){
                if(key.equals(currentScopeID)){
                    continue;
                }
                if(value[1].equals(parentScopeID) && value[0].equals(callName)){
                    procTable.put(Integer.parseInt(key), 0);
                    currentProcId = Integer.parseInt(key);
                    return true;
                }
            }
        }
        return false;
    }

    // private String getVarName(Node node){
    //     String text = node.getTextContent().trim().replaceAll("\t", "");
    //     String text1 = text.trim().replaceAll("\n", "");
    //     String text2 = text1.trim().replaceAll("\s", "");
    //     return text2;
    // }

    private void createBASICFile(){
        String Gosub = "";
        String basicCode= "";
        for(int i = 0;i<code.length();i++){
            if(code.charAt(i) == 'G'){
                Gosub = "";
            }

            if(Gosub.equals("GOSUB")){
                String procId = "";
                for(int j = i+1;j<code.length();j++){
                    if(Character.isDigit(code.charAt(j))){
                        procId+=code.charAt(j);
                    }
                    else if(code.charAt(j) == ' '){
                        continue;
                    }
                    else{
                        break;
                    }
                }
                basicCode += " "+ procTable.get(Integer.parseInt(procId));

                for(i = i;i<code.length();i++){
                    if(code.charAt(i) == '\n'){
                        break;
                    }
                }
            }
            Gosub += code.charAt(i);
            basicCode += code.charAt(i);

        }
        try{
            FileWriter fileWriter = new FileWriter("BASIC.txt");
            fileWriter.write(basicCode);
            fileWriter.close();
        }
        catch(Exception e){
            System.out.println(e);
        }

    }

}