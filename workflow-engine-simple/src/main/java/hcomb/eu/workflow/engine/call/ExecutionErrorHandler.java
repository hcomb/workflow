package hcomb.eu.workflow.engine.call;

import hcomb.eu.workflow.engine.*;
import hcomb.eu.workflow.engine.err.ExecutionError;

public interface ExecutionErrorHandler<C extends StatefulContext> extends Handler {
	void call(ExecutionError error, C context);
}
