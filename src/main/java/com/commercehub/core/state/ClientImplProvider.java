package com.commercehub.core.state;

public interface ClientImplProvider {
    Class getTriggerClass(String name);

    Class getActionClass(String name);
}
