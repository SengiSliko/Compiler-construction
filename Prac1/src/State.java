/*
 * This is a declaration that states that I've shared the following code with a few other students as it
 * is the RE -> NFA part of the practical. The rest of the practical in unique to me.
 */

import java.util.*;

public class State {

    public String name;
    public boolean isAccepting;
    public List<Transition> transitions;
    public Boolean isVisited;

    public State(String name, boolean isAccepting){
        this.name = name;
        this.isAccepting = isAccepting;
        this.transitions =new ArrayList<Transition>();
        this.isVisited = false;
    }

    public void addTransition(State to, Character symbol){
        Transition transition = new Transition(this, to, symbol);
        this.transitions.add(transition);
    } 
}
