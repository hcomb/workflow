package hcomb.eu.workflow.engine.call;

import hcomb.eu.workflow.engine.*;

public interface EventHandler<C extends StatefulContext> extends Handler {
	void call(String event, String from, String to, C context) throws Exception;
}
