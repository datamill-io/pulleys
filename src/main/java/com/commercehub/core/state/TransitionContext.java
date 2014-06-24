
package com.commercehub.core.state;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A TransitionContext is attached to a TransitionRecord and provides
 * information about the state of the hub prior to a successful transition. This
 * information can be used along with the TransitionRecord to discover exactly
 * why a specific transition was fired. TransitionContexts are doled out by
 * thread.
 *
 * @author mmiller
 */
public class TransitionContext {
    private static Map contextsByThread = new WeakHashMap();

    private HashMap map;

    /**
     * Initializes a new TransitionContext object.
     */
    private TransitionContext() {
        map = new HashMap();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static TransitionContext getInstance() {
        Thread thisThread = Thread.currentThread();
        TransitionContext context =
            (TransitionContext) contextsByThread.get(thisThread);
        if (context == null) {
            context = new TransitionContext();
            contextsByThread.put(thisThread, context);
        }
        return context;
    }

    /**
     * Sets a context key/value pair. If the key already exists within this
     * context, its value is replaced with this new one.
     *
     * @param key DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    public void setContext(String key, String value) {
        map.put(key, value);
    }

    /**
     * A call to clear removes all contextual information from this Transition
     * Context object.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Returns a map of all keys and values currently in this context.
     *
     * @return a Map
     */
    public Map getContextMap() {
        return new HashMap(map);
    }
}
