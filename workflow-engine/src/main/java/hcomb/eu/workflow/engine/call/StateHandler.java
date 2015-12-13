package hcomb.eu.workflow.engine.call;

import hcomb.eu.workflow.engine.*;

public interface StateHandler<C extends StatefulContext> extends Handler {
	void call(String state, C context) throws Exception;
}
