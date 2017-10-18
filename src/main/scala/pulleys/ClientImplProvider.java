package pulleys;

public interface ClientImplProvider {
    Class getTriggerClass(String name);

    Class getActionClass(String name);
}
