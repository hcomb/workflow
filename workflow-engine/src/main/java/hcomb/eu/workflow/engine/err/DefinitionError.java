package hcomb.eu.workflow.engine.err;

public class DefinitionError extends RuntimeException {
	private static final long serialVersionUID = 2660004392130947539L;
	
	public DefinitionError(String message) {
		super(message);
	}
}
