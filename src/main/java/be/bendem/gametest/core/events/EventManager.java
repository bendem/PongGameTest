package be.bendem.gametest.core.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * @author bendem
 */
public class EventManager<E> {

    private final Map<Class<? extends E>, List<InternalCallback<? extends E>>> events;

    public EventManager() {
        events = new ConcurrentHashMap<>();
    }

    public <T extends E> PredicateProvider<T> register(Callback<T> callback, Class<T> eventType) {
        List<InternalCallback<? extends E>> callbackList = events
            .computeIfAbsent(eventType, e -> new CopyOnWriteArrayList<>());
        PredicateProvider<T> predicateProvider = new PredicateProvider<>();
        InternalCallback<T> internalCallback = new InternalCallback<>(callback, predicateProvider);
        callbackList.add(internalCallback);
        return predicateProvider;
    }

    @SuppressWarnings("unchecked")
    public <T extends E> void spawnEvent(T event) {
        events.entrySet().stream()
            .filter(entry -> entry.getKey().isAssignableFrom(event.getClass()))
            .forEach(entry -> entry.getValue()
                .forEach(internalCallback -> ((InternalCallback<T>) internalCallback)
                    .call(event)));
    }

    public class PredicateProvider<T extends E> {
        private final List<Predicate<T>> predicates;

        public PredicateProvider() {
            predicates = new ArrayList<>();
        }

        public PredicateProvider<T> filter(Predicate<T> predicate) {
            predicates.add(predicate);
            return this;
        }

        private List<Predicate<T>> getPredicates() {
            return predicates;
        }
    }

    private class InternalCallback<T extends E> {
        private final Callback<T> callback;
        private final PredicateProvider<T> predicateProvider;

        private InternalCallback(Callback<T> callback, PredicateProvider<T> predicateProvider) {
            this.callback = callback;
            this.predicateProvider = predicateProvider;
        }

        public void call(T event) {
            if(shouldCall(event)) {
                callback.call(event);
            }
        }

        private boolean shouldCall(T event) {
            return predicateProvider.getPredicates().size() == 0
                || !predicateProvider.getPredicates().stream()
                .map(predicate -> predicate.test(event))
                .anyMatch(value -> !value);
        }
    }

}
