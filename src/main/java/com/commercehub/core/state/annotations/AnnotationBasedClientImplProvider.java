package com.commercehub.core.state.annotations;

import com.commercehub.core.state.StateAction;
import com.commercehub.core.state.Trigger;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Set;

public class AnnotationBasedClientImplProvider implements com.commercehub.core.state.ClientImplProvider {
    private final HashMap<String, Class> triggers;
    private final HashMap<String, Class> actions;

    public AnnotationBasedClientImplProvider(){
        Reflections ref = new Reflections("com");
        Set<Class<?>> types = ref.getTypesAnnotatedWith(RefName.class);

        triggers = new HashMap<String, Class>();
        actions = new HashMap<String, Class>();

        scanForRefNames(types);
    }

    private void scanForRefNames(Set<Class<?>> types) {
        for (Class c: types){
            for (Annotation a: c.getAnnotations()) {
                if (a instanceof RefName) {
                    RefName r = (RefName) a;
                    if (StateAction.class.isAssignableFrom(c)) {
                        actions.put(r.value(), c);
                    }
                    if (Trigger.class.isAssignableFrom(c)) {
                        triggers.put(r.value(), c);
                    }
                }
            }
        }
    }

    @Override
    public Class getTriggerClass(String name){
        return triggers.get(name);
    }

    @Override
    public Class getActionClass(String name){
        return actions.get(name);
    }
}
