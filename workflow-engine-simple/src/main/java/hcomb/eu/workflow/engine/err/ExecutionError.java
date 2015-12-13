package hcomb.eu.workflow.engine.err;

import hcomb.eu.workflow.engine.*;

public class ExecutionError extends Exception {
	private static final long serialVersionUID = 4362053831847081229L;
	private String state;
	private String event;
	private StatefulContext context;
	
	public ExecutionError(String state, String event, Exception error, String message, StatefulContext context) {
		super(message, error);
		
		this.state = state;
		this.event = event;
		this.context = context;
	}

	public String getState() {
		return state;
	}

	public String getEvent() {
		return event;
	}

	@SuppressWarnings("unchecked")
	public <C extends StatefulContext> C getContext() {
		return (C) context;
	}
}
