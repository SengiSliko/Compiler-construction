/*
 * This is a declaration that states that some of the following code fragments are similar 
 * to those that I shared with other students, this minDFA state class has similar code fragments to that of the nfa state code that was shared. 
 * The rest of the practical in unique to me.
 */

import java.util.*;

public class MinDFAState {
    public char name;
    public boolean isAccepting;
    public List<DFAState> dfaStates;
    public List<Transition> transitions;
    public Boolean isVisited;

    public MinDFAState(char name, boolean isAccepting){
        this.name = name;
        this.isAccepting = isAccepting;
        this.transitions =new ArrayList<Transition>();
        this.dfaStates = new ArrayList<DFAState>();
        this.isVisited = false;
    }

    public void addTransition(MinDFAState to, Character symbol){
        Transition transition = new Transition(this, to, symbol);
        this.transitions.add(transition);
    } 
}
