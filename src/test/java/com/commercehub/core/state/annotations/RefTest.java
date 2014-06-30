package com.commercehub.core.state.annotations;

import com.commercehub.core.state.Condition;
import com.commercehub.core.state.HairTrigger;
import com.commercehub.core.state.Stateful;
import com.commercehub.core.state.Trigger;
import com.commercehub.core.state.annotations.RefName;
import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import static java.lang.System.out;
import static org.junit.Assert.*;

public class RefTest {
    @Test
    public void testRef(){
        for (Annotation a: Tongs.class.getAnnotations()){
            if (a instanceof RefName){
                if (((RefName)a).value().equals("beezl")){
                    return;
                }
            }
        }
        fail("Couldn't find the annotation!");
    }

    @Test
    public void testLookup(){
        Reflections ref = new Reflections("com");
        Set<Class<?>> annot = ref.getTypesAnnotatedWith(RefName.class);

        assertTrue("Could not find via reflection", annot.contains(Tongs.class));
        assertTrue("Could not find via reflection", annot.contains(HairTrigger.class));
    }

    @Test
    public void testLookupUsingProvider(){
        AnnotationBasedClientImplProvider annot = new AnnotationBasedClientImplProvider();

        Class c = annot.getTriggerClass("beezl");
        assertNotNull("Could not find via reflection", c);
        assertTrue("Could not find via reflection", Tongs.class.isAssignableFrom(c));
        c = annot.getTriggerClass("hair");
        assertNotNull("Could not find via reflection", c);
        assertTrue("Could not find via reflection", HairTrigger.class.isAssignableFrom(c));
    }


    @RefName("beezl")
    class Tongs implements Trigger {
        @Override
        public boolean eval(Stateful stateful, Object param, Condition cond) {
            return false;
        }
    }
}
