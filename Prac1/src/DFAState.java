/*
 * This is a declaration that states that some of the following code fragments are similar 
 * to those that I shared with other students, this dfa state class has similar code fragments to that of the nfa state code that was shared. 
 * The rest of the practical in unique to me.
 */

import java.util.*;


public class DFAState{
    public String name;
    public boolean isAccepting;
    public List<Transition> transitions;
    public List<State> nfaStates;
    public boolean isVisited;

    public DFAState(String name, boolean isAccepting){
        this.name = name;
        this.isAccepting = isAccepting;
        this.transitions = new ArrayList<Transition>();
        this.nfaStates = new ArrayList<State>();
    }

    public void addTransition(DFAState to, Character symbol){
        Transition transition = new Transition(this, to, symbol);
        this.transitions.add(transition);
    } 
}
