package hcomb.eu.workflow.engine;

import hcomb.eu.workflow.engine.call.ContextHandler;
import hcomb.eu.workflow.engine.call.EventHandler;
import hcomb.eu.workflow.engine.call.ExecutionErrorHandler;
import hcomb.eu.workflow.engine.call.StateHandler;
import hcomb.eu.workflow.engine.err.ExecutionError;
import hcomb.eu.workflow.engine.err.LogicViolationError;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

import static hcomb.eu.workflow.engine.HandlerCollection.EventType;

//fork from https://github.com/Beh01der/EasyFlow
public class EasyFlow<C extends StatefulContext> {
    public class DefaultErrorHandler implements ExecutionErrorHandler<StatefulContext> {

        public void call(ExecutionError error, StatefulContext context) {
            String msg = "Execution Error in StateHolder [" + error.getState() + "] ";
            if (error.getEvent() != null) {
                msg += "on EventHolder [" + error.getEvent() + "] ";
            }
            msg += "with Context [" + error.getContext() + "] ";

            Exception e = new Exception(msg, error);
            log.error("Error", e);
        }
    }

    private String startState;
    private TransitionCollection transitions;

    private Executor executor;

    private HandlerCollection handlers = new HandlerCollection();
    private boolean trace = false;
    private FlowLogger log = new FlowLoggerImpl();

    protected EasyFlow(String startState) {
        this.startState = startState;
        this.handlers.setHandler(HandlerCollection.EventType.ERROR, null, null, new DefaultErrorHandler());
    }

    protected void processAllTransitions(boolean skipValidation) {
        transitions = new TransitionCollection(Transition.consumeTransitions(), !skipValidation);
    }

    protected void setTransitions(Collection<Transition> collection, boolean skipValidation) {
        transitions = new TransitionCollection(collection, !skipValidation);
    }

    private void prepare() {
        if (executor == null) {
            executor = new AsyncExecutor();
        }
    }

    public void start(final C context) {
        start(false, context);
    }

    public void start(boolean enterInitialState, final C context) {
        prepare();
        context.setFlow(this);

        if (context.getState() == null) {
            setCurrentState(startState, false, context);
        } else if (enterInitialState) {
            setCurrentState(context.getState(), true, context);
        }
    }

    protected void setCurrentState(final String state, final boolean enterInitialState, final C context) {
        execute(new Runnable() {

            public void run() {
                if (!enterInitialState) {
                	String prevState = context.getState();
                    if (prevState != null) {
                        leave(prevState, context);
                    }
                }

                context.setState(state);
                enter(state, context);
            }
        }, context);
    }

    protected void execute(Runnable task, final C context) {
        if (!context.isTerminated()) {
            executor.execute(task);
        }
    }

    @SuppressWarnings("unchecked")
	public <C1 extends StatefulContext> EasyFlow<C1> whenEvent(String event, ContextHandler<C1> onEvent) {
        handlers.setHandler(EventType.EVENT_TRIGGER, null, event, onEvent);
        return (EasyFlow<C1>) this;
    }

    @SuppressWarnings("unchecked")
    public <C1 extends StatefulContext> EasyFlow<C1> whenEvent(EventHandler<C1> onEvent) {
        handlers.setHandler(EventType.ANY_EVENT_TRIGGER, null, null, onEvent);
        return (EasyFlow<C1>) this;
    }

    @SuppressWarnings("unchecked")
    public <C1 extends StatefulContext> EasyFlow<C1> whenEnter(String state, ContextHandler<C1> onEnter) {
        handlers.setHandler(EventType.STATE_ENTER, state, null, onEnter);
        return (EasyFlow<C1>) this;
    }

    @SuppressWarnings("unchecked")
    public <C1 extends StatefulContext> EasyFlow<C1> whenEnter(StateHandler<C1> onEnter) {
        handlers.setHandler(EventType.ANY_STATE_ENTER, null, null, onEnter);
        return (EasyFlow<C1>) this;
    }

    @SuppressWarnings("unchecked")
    public <C1 extends StatefulContext> EasyFlow<C1> whenLeave(String state, ContextHandler<C1> onEnter) {
        handlers.setHandler(EventType.STATE_LEAVE, state, null, onEnter);
        return (EasyFlow<C1>) this;
    }

    @SuppressWarnings("unchecked")
    public <C1 extends StatefulContext> EasyFlow<C1> whenLeave(StateHandler<C1> onEnter) {
        handlers.setHandler(EventType.ANY_STATE_LEAVE, null, null, onEnter);
        return (EasyFlow<C1>) this;
    }

    @SuppressWarnings("unchecked")
    public <C1 extends StatefulContext> EasyFlow<C1> whenError(ExecutionErrorHandler<C1> onError) {
        handlers.setHandler(EventType.ERROR, null, null, onError);
        return (EasyFlow<C1>) this;
    }

    @SuppressWarnings("unchecked")
    public <C1 extends StatefulContext> EasyFlow<C1> whenFinalState(StateHandler<C1> onFinalState) {
        handlers.setHandler(EventType.FINAL_STATE, null, null, onFinalState);
        return (EasyFlow<C1>) this;
    }

    public void waitForCompletion(C context) {
      context.awaitTermination();
    }

    @SuppressWarnings("unchecked")
    public <C1 extends StatefulContext> EasyFlow<C1> executor(Executor executor) {
        this.executor = executor;
        return (EasyFlow<C1>) this;
    }

    @SuppressWarnings("unchecked")
    public <C1 extends StatefulContext> EasyFlow<C1> trace() {
        trace = true;
        return (EasyFlow<C1>) this;
    }

    @SuppressWarnings("unchecked")
    public <C1 extends StatefulContext> EasyFlow<C1> logger(FlowLogger log) {
        this.log = log;
        return (EasyFlow<C1>) this;
    }

    public boolean safeTrigger(final String event, final C context) {
        try {
            return trigger(event, true, context);
        } catch (LogicViolationError logicViolationError) {
            return false;
        }
    }

    public void trigger(final String event, final C context) throws LogicViolationError {
        trigger(event, false, context);
    }

    public List<Transition> getAvailableTransitions(String stateFrom) {
        return transitions.getTransitions(stateFrom);
    }

    public boolean isEventHandledByState(final String state, final String event) {
        for (Transition transition : transitions.getTransitions(state)) {
            if (transition.getEvent() == event) return true;
        }
        return false;
    }

    private boolean trigger(final String event, final boolean safe, final C context) throws LogicViolationError {
        if (context.isTerminated()) {
            return false;
        }

        final String stateFrom = context.getState();
        final Transition transition = transitions.getTransition(stateFrom, event);

        if (transition != null) {
            execute(new Runnable() {

                public void run() {
                    try {
                    	String stateTo = transition.getStateTo();
                        if (isTrace())
                            log.info("when triggered %s in %s for %s <<<", event, stateFrom, context);

                        handlers.callOnEventTriggered(event, stateFrom, stateTo, context);
                        context.setLastEvent(event);

                        if (isTrace())
                            log.info("when triggered %s in %s for %s >>>", event, stateFrom, context);

                        setCurrentState(stateTo, false, context);
                    } catch (Exception e) {
                        doOnError(new ExecutionError(stateFrom, event, e,
                            "Execution Error in [trigger]", context));
                    }
                }
            }, context);
        } else if (!safe){
            throw new LogicViolationError("Invalid Event: " + event +
                " triggered while in State: " + context.getState() + " for " + context);
        }

        return transition != null;
    }

    private void enter(final String state, final C context) {
        if (context.isTerminated()) {
            return;
        }

        try {
            // first enter state
            if (isTrace())
                log.info("when enter %s for %s <<<", state, context);

            handlers.callOnStateEntered(state, context);

            if (isTrace())
                log.info("when enter %s for %s >>>", state, context);

            if (transitions.isFinal(state)) {
                doOnTerminate(state, context);
            }
        } catch (Exception e) {
            doOnError(new ExecutionError(state, null, e,
                "Execution Error in [whenEnter] handler", context));
        }
    }

    private void leave(String state, final C context) {
        if (context.isTerminated()) {
            return;
        }

        try {
            if (isTrace())
                log.info("when leave %s for %s <<<", state, context);

            handlers.callOnStateLeaved(state, context);

            if (isTrace())
                log.info("when leave %s for %s >>>", state, context);
        } catch (Exception e) {
            doOnError(new ExecutionError(state, null, e,
                "Execution Error in [whenLeave] handler", context));
        }
    }

    protected boolean isTrace() {
        return trace;
    }

    @SuppressWarnings("unchecked")
	protected void doOnError(final ExecutionError error) {
        handlers.callOnError(error);
        doOnTerminate(error.getState(), (C) error.getContext());
    }

    protected String getStartState() {
        return startState;
    }

    protected void doOnTerminate(String state, final C context) {
        if (!context.isTerminated()) {
            try {
                if (isTrace())
                    log.info("terminating context %s", context);

                context.setTerminated();
                handlers.callOnFinalState(state, context);
            } catch (Exception e) {
                log.error("Execution Error in [whenTerminate] handler", e);
            }
        }
    }
}
