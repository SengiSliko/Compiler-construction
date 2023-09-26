import java.util.*;
import java.io.*;

class Parser {
    private List<Token> tokens;
    private int index = 0;
    private int id = 1;
    private Node root = null;

    public Parser(String filename) {
        Lexer lex = new Lexer();
        tokens = lex.tokenise(filename);
    }

    public Node parse() {
        if (parsePROGR(null)) {
            System.out.println("\u001B[32mSuccess\u001B[0m: parser passed");
            writeXML();
            return root;
        }

        return null;
    }

    private Boolean parsePROGR(Node parent) {
        Node startNode = new Node(id++, "Non-Terminal", "PROGR");
        if (parent != null) {
            parent.children.add(startNode);
        } else {
            root = startNode;
        }
        Boolean algo = parseALGO(startNode);
        if (algo) {
            if (index >= tokens.size()) {
                return true;
            }

            Boolean pd = parsePROCDEFS(startNode);
            if (pd) {
                if (parent == null) {
                    if (index < tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid symbol at line "
                                + tokens.get(index).getRow() + " col " + tokens.get(index).getCol() + ", token: "
                                + tokens.get(index).getContent());
                        System.exit(0);

                        return false;
                    }
                }
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            return false;
            // parser failed
        }
    }

    private Boolean parseALGO(Node parent) {
        Node algoNode = new Node(id++, "Non-Terminal", "ALGO");
        parent.children.add(algoNode);

        Boolean instr = parseINSTR(algoNode);
        if (instr) {
            if (index >= tokens.size()) {
                return true;
            }
            Boolean comm = parseCOMMENT(algoNode);
            if (comm) {
                if (index >= tokens.size()) {
                    return true;
                }
                Boolean seq = parseSEQ(algoNode);
                if (seq) {
                    if (index >= tokens.size()) {
                        return true;
                    }
                    // parser passed
                    return true;
                } else {
                    // parser failed
                    return false;
                }
            } else {
                // parser failed
                return false;
            }
        } else {
            return false;
            // parser failed
        }
    }

    private Boolean parsePROCDEFS(Node parent) {

        if (tokens.get(index).getContent() == ",") {

            Node procdefsNode = new Node(id++, "Non-Terminal", "PROCDEFS");
            parent.children.add(procdefsNode);
            Node comma = new Node(id++, "Terminal", ",");
            procdefsNode.children.add(comma);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid end of program at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol()
                        + ", expected proc definition after token: " + tokens.get(index - 1).getContent());
                System.exit(0);
                return false;
            }
            Boolean proc = parsePROC(procdefsNode);
            if (proc) {
                index++;
                if (index >= tokens.size()) {
                    return true;
                }
                Boolean procdefs = parsePROCDEFS(procdefsNode);
                if (procdefs) {
                    // index++;

                    if (index >= tokens.size()) {
                        return true;
                    }
                    // parser passed
                    return true;
                } else {
                    // parser failed
                    return false;
                }
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.size() == index || tokens.get(index).getContent() == "}") {
            // parser passed
            return true;
        } else {
            // parser failed
            return false;
        }
    }

    private Boolean parsePROC(Node parent) {
        Node procNode = new Node(id++, "Non-Terminal", "PROC");
        parent.children.add(procNode);

        if (tokens.get(index).getContent() == "p") {
            Node pNode = new Node(id++, "Terminal", "p");
            procNode.children.add(pNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid end of program at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol()
                        + ", invalid proc definition after token: " + tokens.get(index - 1).getContent());
                System.exit(0);
                return false;
            }
            Boolean digits = parseDIGITS(procNode);
            if (digits) {
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid end of program at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol()
                            + ", invalid proc definition after token: " + tokens.get(index - 1).getContent());
                    System.exit(0);
                    return false;
                }
                if (tokens.get(index).getContent() == "{") {
                    Node openBrace = new Node(id++, "Terminal", "{");
                    procNode.children.add(openBrace);
                    index++;
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid end of program at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol()
                                + ", invalid proc definition after token: " + tokens.get(index - 1).getContent());
                        System.exit(0);
                        return false;
                    }
                    Boolean progr = parsePROGR(procNode);
                    if (progr) {
                        if (index >= tokens.size()) {
                            System.out.println("\u001B[31mError\u001B[0m: invalid end of program at line "
                                    + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol()
                                    + ", expected '}' after token: " + tokens.get(index - 1).getContent());
                            System.exit(0);
                            return false;
                        }
                        if (tokens.get(index).getContent() == "}") {
                            Node closeBrace = new Node(id++, "Terminal", "}");
                            procNode.children.add(closeBrace);
                            // parser passed
                            return true;
                        } else {
                            System.out.println("\u001B[31mError\u001B[0m: invalid token at line "
                                    + tokens.get(index).getRow() + " col " + tokens.get(index).getCol()
                                    + ", expected \'" + "}" + "\' token");
                            System.exit(0);
                            // parser failed
                            return false;
                        }
                    } else {
                        // parser failed
                        return false;
                    }
                } else {
                    System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                            + " col " + tokens.get(index).getCol() + ", expected \'" + "{" + "\' token");
                    System.exit(0);
                    // parser failed
                    return false;
                }
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "p" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseINSTR(Node parent) {
        Node instrNode = new Node(id++, "Non-Terminal", "INSTR");
        parent.children.add(instrNode);
        if (tokens.get(index).getContent() == "g") {
            Boolean input = parseINPUT(instrNode);
            if (input) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "o") {
            Boolean output = parseOUTPUT(instrNode);
            if (output) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "r") {
            Boolean output = parseOUTPUT(instrNode);
            if (output) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "n") {
            Boolean assign = parseASSIGN(instrNode);
            if (assign) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "b") {
            Boolean assign = parseASSIGN(instrNode);
            if (assign) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "s") {
            Boolean assign = parseASSIGN(instrNode);
            if (assign) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "c") {
            Boolean call = parseCALL(instrNode);
            if (call) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "w") {
            Boolean loop = parseLOOP(instrNode);
            if (loop) {

                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "i") {
            Boolean branch = parseBRANCH(instrNode);
            if (branch) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "h") {
            Node hNode = new Node(id++, "Terminal", "h");
            instrNode.children.add(hNode);
            index++;
            return true;
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line " + tokens.get(index).getRow()
                    + " col " + tokens.get(index).getCol());
            System.exit(0);
            // parser failed
            return false;

        }
    }

    private Boolean parseINPUT(Node parent) {
        Node inputNode = new Node(id++, "Non-Terminal", "INPUT");
        parent.children.add(inputNode);
        if (tokens.get(index).getContent() == "g") {
            Node gNode = new Node(id++, "Terminal", "g");
            inputNode.children.add(gNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid input symbol at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol() + ", after \'"
                        + tokens.get(index - 1).getContent() + "\' token");
                System.exit(0);
                return false;
            }
            Boolean numvar = parseNUMVAR(inputNode);
            if (numvar) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "g" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }

    }

    private Boolean parseOUTPUT(Node parent) {
        Node outputNode = new Node(id++, "Non-Terminal", "OUTPUT");
        parent.children.add(outputNode);
        if (tokens.get(index).getContent() == "r") {
            Boolean text = parseTEXT(outputNode);
            if (text) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "o") {
            Boolean value = parseVALUE(outputNode);
            if (value) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol());
            System.exit(0);
            // parser failed
            return false;
        }

    }

    private Boolean parseASSIGN(Node parent) {
        Node assignNode = new Node(id++, "Non-Terminal", "ASSIGN");
        parent.children.add(assignNode);
        if (tokens.get(index).getContent() == "n") {
            Boolean numvar = parseNUMVAR(assignNode);
            if (numvar) {
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                if (tokens.get(index).getContent() == ":=") {
                    Node equalNode = new Node(id++, "Terminal", ":=");
                    assignNode.children.add(equalNode);
                    index++;
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid assignment at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                        System.exit(0);
                        return false;
                    }
                    Boolean numexpr = parseNUMEXPR(assignNode);
                    if (numexpr) {
                        // parser passed
                        return true;
                    } else {
                        // parser failed
                        return false;
                    }
                } else {
                    System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                            + " col " + tokens.get(index).getCol() + ", expected \'" + ":=" + "\' token");
                    System.exit(0);
                    // parser failed
                    return false;
                }
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "b") {
            Boolean boolvar = parseBOOLVAR(assignNode);
            if (boolvar) {
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                if (tokens.get(index).getContent() == ":=") {
                    Node equalNode = new Node(id++, "Terminal", ":=");
                    assignNode.children.add(equalNode);
                    index++;
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid assignment at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                        System.exit(0);
                        return false;
                    }
                    Boolean boolexpr = parseBOOLEXPR(assignNode);
                    if (boolexpr) {
                        // parser passed
                        return true;
                    } else {
                        // parser failed
                        return false;
                    }
                } else {
                    System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                            + " col " + tokens.get(index).getCol() + ", expected \'" + ":=" + "\' token");
                    System.exit(0);
                    // parser failed
                    return false;
                }
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "s") {
            Boolean strvar = parseSTRINGV(assignNode);
            if (strvar) {
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                if (tokens.get(index).getContent() == ":=") {
                    Node equalNode = new Node(id++, "Terminal", ":=");
                    assignNode.children.add(equalNode);
                    index++;
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid assignment at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                        System.exit(0);
                        return false;
                    }
                    Boolean strval = parseSTRI(assignNode);
                    if (strval) {
                        index++;
                        // parser passed
                        return true;
                    } else {
                        // parser failed
                        return false;
                    }
                } else {
                    System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                            + " col " + tokens.get(index).getCol() + ", expected \'" + ":=" + "\' token");
                    System.exit(0);
                    // parser failed
                    return false;
                }
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol());
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseCALL(Node parent) {
        Node callNode = new Node(id++, "Non-Terminal", "CALL");
        parent.children.add(callNode);
        if (tokens.get(index).getContent() == "c") {
            Node cNode = new Node(id++, "Terminal", "c");
            callNode.children.add(cNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            if (tokens.get(index).getContent() == "p") {
                Node pNode = new Node(id++, "Terminal", "p");
                callNode.children.add(pNode);
                index++;
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                Boolean digits = parseDIGITS(callNode);
                if (digits) {
                    // parser passed
                    return true;
                } else {
                    // parser failed
                    return false;
                }
            } else {
                System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                        + " col " + tokens.get(index).getCol() + ", expected \'" + "p" + "\' token");
                System.exit(0);
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "c" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseLOOP(Node parent) {
        Node loopNode = new Node(id++, "Non-Terminal", "LOOP");
        parent.children.add(loopNode);
        if (tokens.get(index).getContent() == "w") {
            Node wNode = new Node(id++, "Terminal", "w");
            loopNode.children.add(wNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            if (tokens.get(index).getContent() == "(") {
                Node openNode = new Node(id++, "Terminal", "(");
                loopNode.children.add(openNode);
                index++;
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                Boolean boolexpr = parseBOOLEXPR(loopNode);
                if (boolexpr) {
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                        System.exit(0);
                        return false;
                    }
                    if (tokens.get(index).getContent() == ")") {

                        Node closeNode = new Node(id++, "Terminal", ")");
                        loopNode.children.add(closeNode);
                        index++;
                        if (index >= tokens.size()) {
                            System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                    + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                            System.exit(0);
                            return false;
                        }
                        if (tokens.get(index).getContent() == "{") {
                            Node openCurlNode = new Node(id++, "Terminal", "{");
                            loopNode.children.add(openCurlNode);
                            index++;
                            if (index >= tokens.size()) {
                                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                                System.exit(0);
                                return false;
                            }
                            Boolean algo = parseALGO(loopNode);
                            if (algo) {
                                if (index >= tokens.size()) {
                                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                            + tokens.get(index - 1).getRow() + " col "
                                            + tokens.get(index - 1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                                if (tokens.get(index).getContent() == "}") {
                                    Node closeCurlNode = new Node(id++, "Terminal", "}");
                                    loopNode.children.add(closeCurlNode);
                                    index++;
                                    // parser passed
                                    return true;
                                } else {
                                    // parser failed
                                    return false;
                                }
                            } else {
                                // parser failed
                                return false;
                            }
                        } else {
                            // parser failed
                            return false;
                        }

                    } else {
                        // parser failed
                        return false;
                    }
                } else {
                    // parser failed
                    return false;
                }
            } else {
                // parser failed
                return false;
            }
        } else {
            // parser failed
            return false;
        }
    }

    private Boolean parseBRANCH(Node parent) {
        Node branchNode = new Node(id++, "Non-Terminal", "BRANCH");
        parent.children.add(branchNode);

        if (tokens.get(index).getContent() == "i") {
            Node iNode = new Node(id++, "Terminal", "i");
            branchNode.children.add(iNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            if (tokens.get(index).getContent() == "(") {
                Node openNode = new Node(id++, "Terminal", "(");
                branchNode.children.add(openNode);
                index++;
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                Boolean boolexpr = parseBOOLEXPR(branchNode);
                if (boolexpr) {
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                        System.exit(0);
                        return false;
                    }
                    if (tokens.get(index).getContent() == ")") {
                        Node closeNode = new Node(id++, "Terminal", ")");
                        branchNode.children.add(closeNode);
                        index++;
                        if (index >= tokens.size()) {
                            System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                    + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                            System.exit(0);
                            return false;
                        }
                        if (tokens.get(index).getContent() == "t") {
                            Node tNode = new Node(id++, "Terminal", "t");
                            branchNode.children.add(tNode);
                            index++;
                            if (index >= tokens.size()) {
                                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                                System.exit(0);
                                return false;
                            }
                            if (tokens.get(index).getContent() == "{") {
                                Node openCurlNode = new Node(id++, "Terminal", "t");
                                branchNode.children.add(openCurlNode);
                                index++;
                                if (index >= tokens.size()) {
                                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                            + tokens.get(index - 1).getRow() + " col "
                                            + tokens.get(index - 1).getCol());
                                    System.exit(0);
                                    return false;
                                }
                                Boolean algo = parseALGO(branchNode);
                                if (algo) {
                                    if (tokens.get(index).getContent() == "}") {
                                        Node closeCurlNode = new Node(id++, "Terminal", "}");
                                        branchNode.children.add(closeCurlNode);
                                        index++;
                                        if (index >= tokens.size()) {
                                            return true;
                                        }
                                        Boolean BElse = parseELSE(branchNode);
                                        if (BElse) {
                                            // parser passed
                                            return true;
                                        } else {
                                            // parser failed
                                            return false;
                                        }
                                    } else {
                                        System.out.println("\u001B[31mError\u001B[0m: invalid token at line "
                                                + tokens.get(index).getRow() + " col " + tokens.get(index).getCol()
                                                + ", expected \'" + "}" + "\' token");
                                        System.exit(0);
                                        // parser failed
                                        return false;
                                    }
                                } else {

                                    // parser failed
                                    return false;
                                }
                            } else {
                                System.out.println("\u001B[31mError\u001B[0m: invalid token at line "
                                        + tokens.get(index).getRow() + " col " + tokens.get(index).getCol()
                                        + ", expected \'" + "{" + "\' token");
                                System.exit(0);
                                // parser failed
                                return false;
                            }

                        } else {
                            System.out.println("\u001B[31mError\u001B[0m: invalid token at line "
                                    + tokens.get(index).getRow() + " col " + tokens.get(index).getCol()
                                    + ", expected \'" + "t" + "\' token");
                            System.exit(0);
                            // parser failed
                            return false;
                        }
                    } else {
                        System.out
                                .println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                                        + " col " + tokens.get(index).getCol() + ", expected \'" + ")" + "\' token");
                        System.exit(0);
                        // parser failed
                        return false;
                    }
                } else {
                    // parser failed
                    return false;
                }
            } else {
                System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                        + " col " + tokens.get(index).getCol() + ", expected \'" + "(" + "\' token");
                System.exit(0);
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "i" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseCOMMENT(Node parent) {

        if (tokens.get(index).getType() == "Comment") {
            Node commentNode = new Node(id++, "Non-Terminal", "COMMENT");
            parent.children.add(commentNode);
            Node cNOde = new Node(id++, "Terminal", "*" + tokens.get(index).getContent() + "*");
            commentNode.children.add(cNOde);
            index++;
            return true;
        } else if (tokens.get(index).getContent() == ";" || tokens.get(index).getContent() == "}"
                || tokens.get(index).getContent() == ",") {
            return true;
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line " + tokens.get(index - 1).getRow()
                    + " col " + tokens.get(index - 1).getCol());
            System.exit(0);
            return false;
        }
    }

    private Boolean parseSTRI(Node parent) {
        Node striNode = new Node(id, "Non-Terminal", "STRI");
        parent.children.add(striNode);
        if (tokens.get(index).getType() == "String") {
            Node sNOde = new Node(id++, "Terminal", "\"" + tokens.get(index).getContent() + "\"");
            striNode.children.add(sNOde);
            return true;
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line " + tokens.get(index - 1).getRow()
                    + " col " + tokens.get(index - 1).getCol() + ", expected \'" + "String" + "\' token");
            System.exit(0);
            return false;
        }
    }

    private Boolean parseSEQ(Node parent) {
        if (tokens.get(index).getContent() == ";") {
            Node seqNode = new Node(id++, "Non-Terminal", "SEQ");
            parent.children.add(seqNode);
            Node semiNode = new Node(id++, "Terminal", ";");
            seqNode.children.add(semiNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println(
                        "\u001B[31mError\u001B[0m: invalid instruction at line " + tokens.get(index - 1).getRow()
                                + " col " + tokens.get(index - 1).getCol() + ", unexpected \';\' token");
                System.exit(0);
                return false;
            }
            Boolean algo = parseALGO(seqNode);
            if (algo) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "}" || tokens.get(index).getContent() == ",") {
            // parser passed
            return true;
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line " + tokens.get(index - 1).getRow()
                    + " col " + tokens.get(index - 1).getCol());
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseNUMVAR(Node parent) {
        Node numvarNode = new Node(id++, "Non-Terminal", "NUMVAR");
        parent.children.add(numvarNode);

        if (tokens.get(index).getContent() == "n") {
            Node nNode = new Node(id++, "Terminal", "n");
            numvarNode.children.add(nNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            Boolean digits = parseDIGITS(numvarNode);
            if (digits) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "n" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseBOOLVAR(Node parent) {
        Node boolvarNode = new Node(id++, "Non-Terminal", "BOOLVAR");
        parent.children.add(boolvarNode);

        if (tokens.get(index).getContent() == "b") {
            Node bNode = new Node(id++, "Terminal", "b");
            boolvarNode.children.add(bNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            Boolean digits = parseDIGITS(boolvarNode);
            if (digits) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "b" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseSTRINGV(Node parent) {
        Node stringvNode = new Node(id++, "Non-Terminal", "STRINGV");
        parent.children.add(stringvNode);

        if (tokens.get(index).getContent() == "s") {
            Node sNode = new Node(id++, "Terminal", "s");
            stringvNode.children.add(sNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            Boolean digits = parseDIGITS(stringvNode);
            if (digits) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "s" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseTEXT(Node parent) {
        Node textNode = new Node(id++, "Non-Terminal", "TEXT");
        parent.children.add(textNode);

        if (tokens.get(index).getContent() == "r") {
            Node rNode = new Node(id++, "Terminal", "r");
            textNode.children.add(rNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            Boolean stringv = parseSTRINGV(textNode);
            if (stringv) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "s" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseVALUE(Node parent) {
        Node valueNode = new Node(id++, "Non-Terminal", "VALUE");
        parent.children.add(valueNode);

        if (tokens.get(index).getContent() == "o") {
            Node oNode = new Node(id++, "Terminal", "o");
            valueNode.children.add(oNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            Boolean numvar = parseNUMVAR(valueNode);
            if (numvar) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "s" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseNUMEXPR(Node parent) {
        Node numexprNode = new Node(id++, "Non-Terminal", "NUMEXPR");
        parent.children.add(numexprNode);

        if (tokens.get(index).getContent() == "a" || tokens.get(index).getContent() == "m"
                || tokens.get(index).getContent() == "d") {
            Node node = new Node(id++, "Terminal", tokens.get(index).getContent());
            numexprNode.children.add(node);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            if (tokens.get(index).getContent() == "(") {
                Node openNode = new Node(id++, "Terminal", "(");
                numexprNode.children.add(openNode);
                index++;
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                Boolean numexpr = parseNUMEXPR(numexprNode);
                if (numexpr) {
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                        System.exit(0);
                        return false;
                    }
                    if (tokens.get(index).getContent() == ",") {
                        Node commaNode = new Node(id++, "Terminal", ",");
                        numexprNode.children.add(commaNode);
                        index++;
                        if (index >= tokens.size()) {
                            System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                    + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                            System.exit(0);
                            return false;
                        }
                        Boolean numexpr2 = parseNUMEXPR(numexprNode);
                        if (numexpr2) {
                            if (index >= tokens.size()) {
                                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                                System.exit(0);
                                return false;
                            }
                            if (tokens.get(index).getContent() == ")") {
                                Node closeNode = new Node(id++, "Terminal", ")");
                                numexprNode.children.add(closeNode);
                                index++;
                                // parser passed
                                return true;
                            } else {
                                System.out.println("\u001B[31mError\u001B[0m: invalid token at line "
                                        + tokens.get(index).getRow() + " col " + tokens.get(index).getCol()
                                        + ", expected \'" + ")" + "\' token");
                                System.exit(0);
                                // parser failed
                                return false;
                            }
                        } else {
                            // parser failed
                            return false;
                        }
                    } else {
                        System.out
                                .println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                                        + " col " + tokens.get(index).getCol() + ", expected \'" + "," + "\' token");
                        System.exit(0);
                        // parser failed
                        return false;
                    }
                } else {
                    // parser failed
                    return false;
                }
            } else {
                System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                        + " col " + tokens.get(index).getCol() + ", expected \'" + "(" + "\' token");
                System.exit(0);
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "n") {
            Boolean numvar = parseNUMVAR(numexprNode);
            if (numvar) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "0.00" || tokens.get(index).getContent() == "-"
                || (isDigit(tokens.get(index).getContent()) && tokens.get(index).getContent() != "0")) {
            Boolean decnum = parseDECNUM(numexprNode);
            if (decnum) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "valid" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseBOOLEXPR(Node parent) {
        Node boolexprNode = new Node(id++, "Non-Terminal", "BOOLEXPR");
        parent.children.add(boolexprNode);

        if (tokens.get(index).getContent() == "b" || tokens.get(index).getContent() == "T"
                || tokens.get(index).getContent() == "F" || tokens.get(index).getContent() == "^"
                || tokens.get(index).getContent() == "v" || tokens.get(index).getContent() == "!") {
            Boolean logic = parseLOGIC(boolexprNode);
            if (logic) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "E" || tokens.get(index).getContent() == "<"
                || tokens.get(index).getContent() == ">") {
            Boolean cmpr = parseCMPR(boolexprNode);
            if (cmpr) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "a valid" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseDIGITS(Node parent) {
        Node digitsNode = new Node(id++, "Non-Terminal", "DIGITS");
        parent.children.add(digitsNode);

        if (isDigit(tokens.get(index).getContent())) {
            Boolean d = parseD(digitsNode);
            if (d) {
                index++;
                if (index >= tokens.size()) {
                    return true;
                }
                if (isDigit(tokens.get(index).getContent())) {
                    Boolean more = parseMORE(digitsNode);
                    if (more) {
                        // parser passed
                        return true;
                    } else {
                        // parser failed
                        return false;
                    }
                } else if (tokens.get(index).getContent() == "." || tokens.get(index).getContent() == ";"
                        || tokens.get(index).getContent() == ")" || tokens.get(index).getContent() == ","
                        || tokens.get(index).getType() == "Comment" || tokens.get(index).getContent() == "}"
                        || tokens.get(index).getContent() == "{" || tokens.get(index).getContent() == ":=") {
                    // parser passed
                    return true;
                } else {
                    System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                            + " col " + tokens.get(index).getCol() + ", expected \'" + "a digit or a valid symbol"
                            + "\' token");
                    System.exit(0);
                    // parser failed
                    return false;
                }
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "a digit" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseELSE(Node parent) {

        if (tokens.get(index).getContent() == "e") {
            Node elseNode = new Node(id++, "Non-Terminal", "ELSE");
            parent.children.add(elseNode);
            Node eNode = new Node(id++, "Terminal", "e");
            elseNode.children.add(eNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid else instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            if (tokens.get(index).getContent() == "{") {
                Node openNode = new Node(id++, "Terminal", "{");
                elseNode.children.add(openNode);
                index++;
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid else instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                Boolean algo = parseALGO(elseNode);
                if (algo) {
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid else instruction at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol()
                                + "missing '}'' token");
                        System.exit(0);
                        return false;
                    }
                    if (tokens.get(index).getContent() == "}") {
                        Node closeNode = new Node(id++, "Terminal", "}");
                        elseNode.children.add(closeNode);
                        index++;
                        // parser passed
                        return true;
                    } else {
                        System.out
                                .println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                                        + " col " + tokens.get(index).getCol() + ", expected \'" + '}' + "\' token");
                        System.exit(0);
                        // parser failed
                        return false;
                    }
                } else {
                    // parser failed
                    return false;
                }
            } else {
                System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                        + " col " + tokens.get(index).getCol() + ", expected \'" + '{' + "\' token");
                System.exit(0);
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "*" || tokens.get(index).getContent() == "}"
                || tokens.get(index).getContent() == "," || tokens.get(index).getContent() == ";") {
            // parser passed
            return true;
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "a valid" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseDECNUM(Node parent) {
        Node decnumNode = new Node(id++, "Non-Terminal", "DECNUM");
        parent.children.add(decnumNode);

        if (tokens.get(index).getContent() == "0.00") {
            Node zeroNode = new Node(id++, "Terminal", "0.00");
            decnumNode.children.add(zeroNode);
            index++;
            // parser passed
            return true;
        } else if (tokens.get(index).getContent() == "-") {
            Boolean neg = parseNEG(decnumNode);

            if (neg) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (isDigit(tokens.get(index).getContent()) && tokens.get(index).getContent() != "0") {
            Boolean pos = parsePOS(decnumNode);
            if (pos) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "a valid token" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseLOGIC(Node parent) {
        Node logicNode = new Node(id++, "Non-Terminal", "LOGIC");
        parent.children.add(logicNode);

        if (tokens.get(index).getContent() == "b") {
            Boolean boolvar = parseBOOLVAR(logicNode);
            if (boolvar) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "T" || tokens.get(index).getContent() == "F") {
            Node boolNode = new Node(id++, "Terminal", tokens.get(index).getContent());
            logicNode.children.add(boolNode);
            index++;
            if (index >= tokens.size()) {
                return true;
            }
            // parser passed
            return true;
        } else if (tokens.get(index).getContent() == "^" || tokens.get(index).getContent() == "v") {
            Node node = new Node(id++, "Terminal", tokens.get(index).getContent());
            logicNode.children.add(node);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            if (tokens.get(index).getContent() == "(") {
                Node openNode = new Node(id++, "Terminal", "(");
                logicNode.children.add(openNode);
                index++;
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                Boolean numexpr = parseBOOLEXPR(logicNode);
                if (numexpr) {
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                        System.exit(0);
                        return false;
                    }
                    if (tokens.get(index).getContent() == ",") {
                        Node commaNode = new Node(id++, "Terminal", ",");
                        logicNode.children.add(commaNode);
                        index++;
                        Boolean numexpr2 = parseBOOLEXPR(logicNode);
                        if (numexpr2) {
                            if (index >= tokens.size()) {
                                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                                System.exit(0);
                                return false;
                            }
                            if (tokens.get(index).getContent() == ")") {
                                Node closeNode = new Node(id++, "Terminal", ")");
                                logicNode.children.add(closeNode);
                                index++;
                                // parser passed
                                return true;
                            } else {
                                System.out.println("\u001B[31mError\u001B[0m: invalid token at line "
                                        + tokens.get(index).getRow() + " col " + tokens.get(index).getCol()
                                        + ", expected \'" + ")" + "\' token");
                                System.exit(0);
                                // parser failed
                                return false;
                            }
                        } else {
                            // parser failed
                            return false;
                        }
                    } else {
                        System.out
                                .println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                                        + " col " + tokens.get(index).getCol() + ", expected \'" + "," + "\' token");
                        System.exit(0);
                        // parser failed
                        return false;
                    }
                } else {
                    // parser failed
                    return false;
                }
            } else {
                System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                        + " col " + tokens.get(index).getCol() + ", expected \'" + "(" + "\' token");
                System.exit(0);
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "!") {
            Node exNode = new Node(id++, "Terminal", "!");
            logicNode.children.add(exNode);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            if (tokens.get(index).getContent() == "(") {
                Node openNode = new Node(id++, "Terminal", "(");
                logicNode.children.add(openNode);
                index++;
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                Boolean numexpr = parseBOOLEXPR(logicNode);
                if (numexpr) {
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                        System.exit(0);
                        return false;
                    }
                    if (tokens.get(index).getContent() == ")") {
                        Node closeNode = new Node(id++, "Terminal", ")");
                        logicNode.children.add(closeNode);
                        index++;
                        // parser passed
                        return true;
                    } else {
                        System.out
                                .println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                                        + " col " + tokens.get(index).getCol() + ", expected \'" + ")" + "\' token");
                        System.exit(0);
                        // parser failed
                        return false;
                    }
                } else {
                    // parser failed
                    return false;
                }
            } else {
                System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                        + " col " + tokens.get(index).getCol() + ", expected \'" + "(" + "\' token");
                System.exit(0);
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "!" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }

    }

    private Boolean parseCMPR(Node parent) {
        Node cmprNode = new Node(id++, "Non-Terminal", "CMPR");
        parent.children.add(cmprNode);

        if (tokens.get(index).getContent() == "E" || tokens.get(index).getContent() == "<"
                || tokens.get(index).getContent() == ">") {
            Node node = new Node(id++, "Terminal", tokens.get(index).getContent());
            cmprNode.children.add(node);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            if (tokens.get(index).getContent() == "(") {
                Node openNode = new Node(id++, "Terminal", "(");
                cmprNode.children.add(openNode);
                index++;
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                Boolean numexpr = parseNUMEXPR(cmprNode);
                if (numexpr) {
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                        System.exit(0);
                        return false;
                    }
                    if (tokens.get(index).getContent() == ",") {
                        Node commaNode = new Node(id++, "Terminal", ",");
                        cmprNode.children.add(commaNode);
                        index++;
                        if (index >= tokens.size()) {
                            System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                    + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                            System.exit(0);
                            return false;
                        }
                        Boolean numexpr2 = parseNUMEXPR(cmprNode);
                        if (numexpr2) {
                            if (index >= tokens.size()) {
                                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                                System.exit(0);
                                return false;
                            }
                            if (tokens.get(index).getContent() == ")") {
                                Node closeNode = new Node(id++, "Terminal", ")");
                                cmprNode.children.add(closeNode);
                                index++;
                                // parser passed
                                return true;
                            } else {
                                System.out.println("\u001B[31mError\u001B[0m: invalid token at line "
                                        + tokens.get(index).getRow() + " col " + tokens.get(index).getCol()
                                        + ", expected \'" + ")" + "\' token");
                                System.exit(0);
                                // parser failed
                                return false;
                            }
                        } else {
                            // parser failed
                            return false;
                        }
                    } else {
                        System.out
                                .println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                                        + " col " + tokens.get(index).getCol() + ", expected \'" + "," + "\' token");
                        System.exit(0);
                        // parser failed
                        return false;
                    }
                } else {
                    // parser failed
                    return false;
                }
            } else {
                System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                        + " col " + tokens.get(index).getCol() + ", expected \'" + "(" + "\' token");
                System.exit(0);
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "a valid" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseD(Node parent) {
        Node dNode = new Node(id++, "Non-Terminal", "D");
        parent.children.add(dNode);

        if (isDigit(tokens.get(index).getContent())) {
            Node node = new Node(id++, "Terminal", tokens.get(index).getContent());
            dNode.children.add(node);
            // parser passed
            return true;
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "a digit" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseMORE(Node parent) {

        if (isDigit(tokens.get(index).getContent())) {
            Node moreNode = new Node(id++, "Non-Terminal", "MORE");
            parent.children.add(moreNode);
            Boolean digits = parseDIGITS(moreNode);
            if (digits) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else if (tokens.get(index).getContent() == "." || tokens.get(index).getContent() == ")"
                || tokens.get(index).getContent() == "{" || tokens.get(index).getContent() == ","
                || tokens.get(index).getContent() == "*" || tokens.get(index).getContent() == "}") {
            // parser passed
            return true;
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "a valid" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseNEG(Node parent) {
        Node negNode = new Node(id++, "Non-Terminal", "NEG");
        parent.children.add(negNode);

        if (tokens.get(index).getContent() == "-") {
            Node node = new Node(id++, "Terminal", "-");
            negNode.children.add(node);
            index++;
            if (index >= tokens.size()) {
                System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                        + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                System.exit(0);
                return false;
            }
            Boolean pos = parsePOS(negNode);
            if (pos) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "-" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parsePOS(Node parent) {
        Node posNode = new Node(id++, "Non-Terminal", "POS");
        parent.children.add(posNode);

        if (isDigit(tokens.get(index).getContent()) && tokens.get(index).getContent() != "0") {
            Boolean BInt = parseINT(posNode);
            if (BInt) {
                if (index >= tokens.size()) {
                    System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                            + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                    System.exit(0);
                    return false;
                }
                if (tokens.get(index).getContent() == ".") {
                    Node dotNode = new Node(id++, "Terminal", ".");
                    posNode.children.add(dotNode);
                    index++;
                    if (index >= tokens.size()) {
                        System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                        System.exit(0);
                        return false;
                    }
                    Boolean d = parseD(posNode);
                    if (d) {
                        index++;
                        if (index >= tokens.size()) {
                            System.out.println("\u001B[31mError\u001B[0m: invalid instruction at line "
                                    + tokens.get(index - 1).getRow() + " col " + tokens.get(index - 1).getCol());
                            System.exit(0);
                            return false;
                        }

                        Boolean d2 = parseD(posNode);
                        if (d2) {
                            index++;
                            // parser passed
                            return true;
                        } else {
                            // parser failed
                            return false;
                        }
                    } else {
                        // parser failed
                        return false;
                    }
                } else {
                    System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow()
                            + " col " + tokens.get(index).getCol() + ", expected \'" + "." + "\' token");
                    System.exit(0);
                    // parser failed
                    return false;
                }
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "a digit" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean parseINT(Node parent) {
        Node intNode = new Node(id++, "Non-Terminal", "INT");
        parent.children.add(intNode);

        if (isDigit(tokens.get(index).getContent()) && tokens.get(index).getContent() != "0") {
            Node node = new Node(id++, "Terminal", tokens.get(index).getContent());
            intNode.children.add(node);
            index++;
            if (index >= tokens.size()) {
                return true;
            }
            Boolean more = parseMORE(intNode);
            if (more) {
                // parser passed
                return true;
            } else {
                // parser failed
                return false;
            }
        } else {
            System.out.println("\u001B[31mError\u001B[0m: invalid token at line " + tokens.get(index).getRow() + " col "
                    + tokens.get(index).getCol() + ", expected \'" + "a digit" + "\' token");
            System.exit(0);
            // parser failed
            return false;
        }
    }

    private Boolean isDigit(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) < '0' || str.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }

    private void writeXML() {
        try {
            FileWriter writer = new FileWriter("AST.xml");
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            printTree(root, writer, 0);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printTree(Node node, FileWriter writer, int level) {

        if (node == null) {
            return;
        }

        try {
            for (int i = 0; i < level; i++) {
                writer.write("\t");
            }
            if (node.getType() == "Non-Terminal") {
                writer.write("<" + node.getContent() + " id=\"" + node.getId() + "\" children=\"");
                for (int i = 0; i < node.children.size(); i++) {
                    writer.write(node.children.get(i).getId() + " ");

                }
                writer.write("\">\n");

                for (int i = 0; i < node.children.size(); i++) {
                    printTree(node.children.get(i), writer, level + 1);
                }

                for (int i = 0; i < level; i++) {
                    writer.write("\t");
                }
                writer.write("</" + node.getContent() + ">\n");
            } else {
                writer.write(node.getContent() + '\n');
                printTree(null, writer, level);
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

    }
}
