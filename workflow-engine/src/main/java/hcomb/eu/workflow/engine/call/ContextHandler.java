package hcomb.eu.workflow.engine.call;

import hcomb.eu.workflow.engine.StatefulContext;

/**
 * User: andrey
 * Date: 12/03/13
 * Time: 7:29 PM
 */
public interface ContextHandler<C extends StatefulContext> extends Handler {
    void call(C context) throws Exception;
}
