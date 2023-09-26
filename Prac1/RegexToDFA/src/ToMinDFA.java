import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class ToMinDFA {

    private char stateCount = 'A';

    public MinDFA convertToMinDFA(DFA dfa){
        List<DFAState> isNotAcceptList = new ArrayList<DFAState>();
        List<DFAState> isAcceptList = new ArrayList<DFAState>();

        MinDFA minDFA = new MinDFA();

        List<Character> symbols = new ArrayList<Character>();
        for(DFAState state: dfa.states){
            for(Transition transition: state.transitions){
                if(!symbols.contains(transition.symbol)){
                    symbols.add(transition.symbol);
                }
            }
        }

        for(DFAState state: dfa.states){
            if(state.isAccepting){
                isAcceptList.add(state);
            }
            else{
                isNotAcceptList.add(state);
            }
        }
            MinDFAState minDFAState1= null;
            MinDFAState minDFAState2 =null;
            if(!isNotAcceptList.isEmpty()){
                minDFAState1 = new MinDFAState(stateCount++, false);
                minDFAState1.dfaStates = new ArrayList<DFAState>();
                for(DFAState state: isNotAcceptList){
                    minDFAState1.dfaStates.add(state);
                }
                minDFA.states.add(minDFAState1);
            }
            if(!isAcceptList.isEmpty()){
                minDFAState2 = new MinDFAState(stateCount++, true);
                for(DFAState state: isAcceptList){
                    minDFAState2.dfaStates.add(state);
                }
                minDFA.states.add(minDFAState2);
            }

            minDFA = breakDown(minDFA, dfa, minDFAState1, minDFAState2, symbols);
        
        return minDFA;
    }

    public MinDFA breakDown(MinDFA minDFA,DFA dfa, MinDFAState acceptingState, MinDFAState nonAcceptingState, List<Character> symbols){
        int prevNumStates = 0;
        int currNumStates = minDFA.states.size();
        while(prevNumStates!=currNumStates){
            prevNumStates=currNumStates;
            for(int j =0;j<minDFA.states.size(); j++){
                if(minDFA.states.get(j).isAccepting){
                    for(int i =1; i< minDFA.states.get(j).dfaStates.size(); i++){

                        boolean isEquivalent = checkStates(minDFA,dfa.states, minDFA.states.get(j).dfaStates.get(0),minDFA.states.get(j).dfaStates.get(i), symbols);
                        if(!isEquivalent){
                            boolean isEquivalent2 = false;
                            for(int k =0; k<minDFA.states.size(); k++){
                                if(minDFA.states.get(k).dfaStates.size()>0){
                                    isEquivalent2 = checkStates(minDFA,dfa.states, minDFA.states.get(k).dfaStates.get(0),minDFA.states.get(j).dfaStates.get(i), symbols);
                                    
                                    if(isEquivalent2 && minDFA.states.get(k).isAccepting){
                                        minDFA.states.get(k).dfaStates.add(minDFA.states.get(j).dfaStates.get(i));
                                        minDFA.states.get(j).dfaStates.remove(i);
                                        --i;
                                        break;
                                    }
                                }
                             }
                            if(!isEquivalent2){
                                MinDFAState newMinDFAState = new MinDFAState(stateCount++, true);
                                currNumStates++;
                                newMinDFAState.dfaStates.add(minDFA.states.get(j).dfaStates.get(i));
                                minDFA.states.add(newMinDFAState);
                                minDFA.states.get(j).dfaStates.remove(i);
                                --i;
                            }
                        }
                    }   
                }
                else{
                    for(int i =1; i< minDFA.states.get(j).dfaStates.size(); i++){
                        boolean isEquivalent = checkStates(minDFA,dfa.states, minDFA.states.get(j).dfaStates.get(0),minDFA.states.get(j).dfaStates.get(i), symbols);
                        if(!isEquivalent){
                            boolean isEquivalent2 = false;
                            for(int k =0; k<minDFA.states.size(); k++){
                                isEquivalent2 = checkStates(minDFA,dfa.states, minDFA.states.get(k).dfaStates.get(0),minDFA.states.get(j).dfaStates.get(i), symbols);
                                if(isEquivalent2 && !minDFA.states.get(k).isAccepting){
                                    minDFA.states.get(k).dfaStates.add(minDFA.states.get(j).dfaStates.get(i));

                                    minDFA.states.get(j).dfaStates.remove(i);
                                    --i;
                                    break;
                                }
                            }
                            if(!isEquivalent2){
                                MinDFAState newMinDFAState = new MinDFAState(stateCount++, false);
                                currNumStates++;
                                newMinDFAState.dfaStates.add(minDFA.states.get(j).dfaStates.get(i));

                                minDFA.states.add(newMinDFAState);

                                minDFA.states.get(j).dfaStates.remove(i);
                                --i;
                            }
                        }
                    }  
                }
            }
        }

        for(MinDFAState minDFAState: minDFA.states){
            for(DFAState dfaState: minDFAState.dfaStates){
                if(dfaState == dfa.startState){
                    minDFA.startState = minDFAState;
                    break;
                }
            }
        }

        for(MinDFAState minDFAState: minDFA.states){

            if(minDFAState.dfaStates.size()>0){
                for(Transition transition: minDFAState.dfaStates.get(0).transitions){
                    for(MinDFAState minDFAState2: minDFA.states){
                        if(minDFAState2.dfaStates.contains(transition.dfaTo)){
                            minDFAState.addTransition(minDFAState2, transition.symbol);
                            break;
                        }
                    }
                    
                }
            }
            
            
        }

        return minDFA;

    }

    public boolean checkStates(MinDFA minDFA,List<DFAState> dfaStates,DFAState state1, DFAState state2, List<Character> symbols){
        boolean isEquivalent = false;

        List<Boolean> bothEquList = new ArrayList<Boolean>();
        for(Character symbol: symbols){
            List<DFAState> desStates = new ArrayList<DFAState>();
            for(Transition transition: state1.transitions){
                if(transition.symbol==symbol){
                    desStates.add(transition.dfaTo);
                }
            }
            for(Transition transition: state2.transitions){
                if(transition.symbol==symbol){
                    desStates.add(transition.dfaTo);
                }
            }
            if(desStates.size()>1){
                isEquivalent = false;
                for(MinDFAState minDFAState: minDFA.states){
                    if(minDFAState.dfaStates.contains(desStates.get(0)) && minDFAState.dfaStates.contains(desStates.get(1))){
                        bothEquList.add(true);
                        break;
                    }
                }
            }
            else if(desStates.size()==1){
                isEquivalent = false;
            }
            else if(desStates.size()==0){
                bothEquList.add(true);
            }

        }
        if(state2.transitions.size()==state1.transitions.size()){
            if(bothEquList.size()==symbols.size()){
                isEquivalent = true;
            }
        }
        else{
            isEquivalent = false;
        }
        return isEquivalent;
    }


    public void printMinDFA(MinDFA minDFA){
        System.out.println("<states>");
        for(MinDFAState minDFAState: minDFA.states){
            System.out.print("<"+minDFAState.name+"");
            if(minDFAState.isAccepting){
                System.out.print(" accepting=\"true\"");
            }
            if(minDFAState==minDFA.startState){
                System.out.print(" start=\"true\"");
            }
            System.out.println("/>");
        }
        System.out.println("</states>");
        System.out.println("<transitions>");
        for(MinDFAState minDFAState: minDFA.states){
            for(Transition transition: minDFAState.transitions){
                System.out.println("\t<"+transition.minDfaFrom.name+">");
                System.out.println("\t\s\s\s<"+transition.minDfaTo.name+">"+transition.symbol+"</"+transition.minDfaTo.name+">");
                System.out.println("\t</"+transition.minDfaFrom.name+">");

            }
        }
        System.out.println("</transitions>");
    
        try {
            FileWriter writer = new FileWriter("minDFA.xml");

            writer.write("<MinDFA>\n");
            writer.write("<states>\n");
        for(MinDFAState minDFAState: minDFA.states){
            writer.write("<"+minDFAState.name+"");
            if(minDFAState.isAccepting){
                writer.write(" accepting=\"true\"");
            }
            if(minDFAState==minDFA.startState){
                writer.write(" start=\"true\"");
            }
            writer.write("/>\n");
        }
            writer.write("</states>\n");
            writer.write("<transitions>\n");
            for(MinDFAState minDFAState: minDFA.states){
                for(Transition transition: minDFAState.transitions){
                    writer.write("\t<"+transition.minDfaFrom.name+">\n");
                    writer.write("\t\s\s\s<"+transition.minDfaTo.name+">"+transition.symbol+"</"+transition.minDfaTo.name+">\n");
                    writer.write("\t</"+transition.minDfaFrom.name+">\n");

                }
            }
            writer.write("</transitions>\n");
            writer.write("</MinDFA>\n");

            writer.close();




        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
}

class MinDFA{
    public List<MinDFAState> states;
    public MinDFAState startState;
    public MinDFAState endState;

    public MinDFA(){
        this.states = new ArrayList<MinDFAState>();
    }
}