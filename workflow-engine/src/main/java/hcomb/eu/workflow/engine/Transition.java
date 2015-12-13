package hcomb.eu.workflow.engine;

import java.util.*;

/**
 * User: andrey
 * Date: 6/12/2013
 * Time: 2:21 PM
 */
public final class Transition {
    private static ThreadLocal<List<Transition>> transitions = new ThreadLocal<List<Transition>>();
    private String event;
    private String stateFrom;
    private String stateTo;
    private boolean isFinal;

    public Transition(String event, String stateFrom, String stateTo) {
        this.event = event;
        this.stateFrom = stateFrom;
        this.stateTo = stateTo;
        this.isFinal = false;
    }

    public Transition(String event, String stateFrom, String stateTo, boolean isFinal) {
        this.event = event;
        this.stateFrom = stateFrom;
        this.stateTo = stateTo;
        this.isFinal = isFinal;
    }

    protected Transition(String event, String stateTo, boolean isFinal) {
        this.event = event;
        this.stateTo = stateTo;
        this.isFinal = isFinal;
        register(this);
    }

    private static void register(Transition transition) {
        List<Transition> list = transitions.get();
        if (list == null) {
            list = new ArrayList<Transition>();
            transitions.set(list);
        }
        list.add(transition);
    }

    public String getEvent() {
        return event;
    }

    protected void setStateFrom(String stateFrom) {
        this.stateFrom = stateFrom;
    }

    public String getStateFrom() {
        return stateFrom;
    }

    public String getStateTo() {
        return stateTo;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public Transition transit(Transition... transitions) {
        for (Transition transition : transitions) {
            transition.setStateFrom(stateTo);
        }

        return this;
    }

    @Override
    public String toString() {
        return "Transition{" +
            "event=" + event +
            ", stateFrom=" + stateFrom +
            ", stateTo=" + stateTo +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transition that = (Transition) o;

        if (!event.equals(that.event)) return false;
        if (!stateFrom.equals(that.stateFrom)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = event.hashCode();
        result = 31 * result + stateFrom.hashCode();
        return result;
    }

    protected static List<Transition> consumeTransitions() {
        List<Transition> ts = transitions.get();
        transitions.remove();
        return ts;
    }
}