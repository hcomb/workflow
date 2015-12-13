package hcomb.eu.workflow.engine;

import hcomb.eu.workflow.engine.err.DefinitionError;

import java.util.*;

/**
 * User: andrey
 * Date: 6/12/2013
 * Time: 2:08 PM
 */
final class TransitionCollection {
    private Map<String, Map<String, Transition>> transitionFromState =
        new HashMap<String, Map<String, Transition>>();
    private Set<String> finalStates = new HashSet<String>();

    protected TransitionCollection(Collection<Transition> transitions, boolean validate) {
        if (transitions != null) {
            for (Transition transition : transitions) {
                Map<String, Transition> map = transitionFromState.get(transition.getStateFrom());
                if (map == null) {
                    map = new HashMap<String, Transition>();
                    transitionFromState.put(transition.getStateFrom(), map);
                }
                map.put(transition.getEvent(), transition);
                if (transition.isFinal()) {
                    finalStates.add(transition.getStateTo());
                }
            }
        }

        if (validate) {
            if (transitions == null || transitions.isEmpty()) {
                throw new DefinitionError("No transitions defined");
            }

            Set<Transition> processedTransitions = new HashSet<Transition>();
            for (Transition transition : transitions) {
            	String stateFrom = transition.getStateFrom();
                if (finalStates.contains(stateFrom)) {
                    throw new DefinitionError("Some events defined for final State: " + stateFrom);
                }

                if (processedTransitions.contains(transition)) {
                    throw new DefinitionError("Ambiguous transitions: " + transition);
                }

                String stateTo = transition.getStateTo();
                if (!finalStates.contains(stateTo) &&
                        !transitionFromState.containsKey(stateTo)) {
                    throw new DefinitionError("No events defined for non-final State: " + stateTo);
                }

                if (stateFrom.equals(stateTo)) {
                    throw new DefinitionError("Circular transition: " + transition);
                }

                processedTransitions.add(transition);
            }
        }
    }

    public Transition getTransition(String stateFrom, String event) {
        Map<String, Transition> transitionMap = transitionFromState.get(stateFrom);
        return transitionMap == null ? null : transitionMap.get(event);
    }

    public List<Transition> getTransitions(String stateFrom) {
        Map<String, Transition> transitionMap = transitionFromState.get(stateFrom);
        return transitionMap == null ? Collections.<Transition>emptyList() : new ArrayList<Transition>(transitionMap.values());
    }

    protected boolean isFinal(String state) {
        return finalStates.contains(state);
    }
}
