import java.util.*;

class ToDFA{
    public int stateCount;

    public ToDFA(){
        this.stateCount = 0;
    }

    public DFA convertToDFA(NFA nfa){
        List<Character> symbols = new ArrayList<Character>();
        for(State state : nfa.states){
            for(Transition transition : state.transitions){
                if(!symbols.contains(transition.symbol) && transition.symbol != '#'){
                    symbols.add(transition.symbol);
                }
            }
        }


        DFA dfa = new DFA();

        State firstState = nfa.startState;

        DFAState dfaStartState = new DFAState("s" + stateCount++, false);

        dfa.startState = dfaStartState;

        List<State> closure = new ArrayList<State>();

        dfaStartState.nfaStates = findEpsilonClosure(firstState, closure, nfa);
        for(State state: nfa.states){
            state.isVisited = false;
        }
        
        dfa.states.add(dfaStartState);

        boolean isDone = false;

        DFAState currentState = dfaStartState;

        dfa = createTheDFA(currentState, isDone, dfa, nfa, closure, symbols);

        addAccepting(dfa);

        return dfa;
    }

    public DFA createTheDFA(DFAState currentState, Boolean isDone, DFA dfa, NFA nfa, List<State> closure, List<Character> symbols){
        if(!isDone && !currentState.isVisited){
            for(Character symbol : symbols){
                List<State> states = new ArrayList<State>();
                for(State state : nfa.states){
                    for(Transition transition : state.transitions){
                        if(transition.symbol == symbol && isThere(currentState, transition.from)  && !states.contains(transition.to)){
                            states.add(transition.to);

                        }
                    }
                }
                if(!states.isEmpty()){
                   
                    closure = new ArrayList<State>();
                    List<State> tempList = new ArrayList<State>();
                    for(State state : states){
                        tempList = findEpsilonClosure(state, tempList, nfa);

                        for(State nfastate: nfa.states){
                            nfastate.isVisited = false;
                        }
                        for(State tempState : tempList){
                            if(!closure.contains(tempState)){
                                closure.add(tempState);
                            }
                        }

                    }


                    boolean isNew = true;
                    for(DFAState dfaStates : dfa.states){
                       if(isTheSame(dfaStates.nfaStates, closure)){
                            currentState.addTransition(dfaStates, symbol);
                            isNew = false;
                            break;
                       }
                    }


                    if(isNew){
                        DFAState newState = new DFAState("s" + stateCount++, false);
                        newState.nfaStates = closure;
                        currentState.addTransition(newState, symbol);
                        dfa.states.add(newState);
                    }


                }
                   
            }
            currentState.isVisited = true;
            

            for(Transition transition : currentState.transitions){
                createTheDFA(transition.dfaTo, isDone,dfa , nfa,closure, symbols);
            }

            
        }
        return dfa;
    }

    public boolean isThere(DFAState dfaState, State nfaState){
        for(State state : dfaState.nfaStates){
            if(state.name.equals(nfaState.name)){
                return true;
            }
        }
        return false;
    }

    public List<State> findEpsilonClosure(State state, List<State> closure, NFA nfa){
        if(state.isVisited == false){
            state.isVisited = true;
            closure.add(state);
            for(Transition transition : state.transitions){
                if(transition.symbol == '#'){
                    findEpsilonClosure(transition.to, closure, nfa);
                }
            }
        }
        return closure;
        
    }

    public boolean isTheSame(List<State> list1, List<State> list2){
        if(list1.size() != list2.size()){
            return false;
        }
        for(State state : list1){
            if(!list2.contains(state)){
                return false;
            }
        }
        return true;
    }

    public void addAccepting(DFA dfa){
        for(DFAState state: dfa.states){
            for(State nfastate : state.nfaStates){
                if(nfastate.isAccepting){
                    state.isAccepting = true;
                    break;
                }
            }
        }
    }

    public void printDFA(DFA dfa){
        System.out.println("Printing DFA");
        for(DFAState state : dfa.states){
            System.out.print("State: " + state.name);
            if(state.isAccepting){
                System.out.print(" Accepting");
            }
            System.out.println();
            for(Transition transition : state.transitions){
                System.out.println("Transition: " + transition.symbol + " to " + transition.dfaTo.name);
            }
        }
    }
}

class DFA{
    public List<DFAState> states;
    public DFAState startState;
    public DFAState endState;

    public DFA(){
        this.states = new ArrayList<DFAState>();
    }
}