package objectexplorer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import objectexplorer.ObjectExplorer.Feature;

/**
 * A tool that can qualitatively measure the footprint ({@literal e.g.}, number of objects,
 * references, primitives) of a graph structure.
 */
public class ObjectGraphMeasurer {
  /**
   * The footprint of an object graph.
   */
  public static class Footprint {
    private final int objects;
    private final int references;
    private final Map<Class<?>, AtomicInteger> primitives;

    private static final Set<Class<?>> primitiveTypes;
    static {
      Collection<Class<?>> primitives = Arrays.asList(boolean.class, byte.class, char.class,
          short.class, int.class, float.class, long.class, double.class);
      primitiveTypes = Collections.unmodifiableSet(new HashSet<>(primitives));
    }

    /**
     * Constructs a Footprint, by specifying the number of objects, references, and primitives
     * (represented as a {@link Map}).
     *
     * @param objects the number of objects
     * @param references the number of references
     * @param primitives the number of primitives (represented by the respective primitive classes,
     *        e.g. {@code int.class} etc)
     */
    public Footprint(int objects, int references, Map<Class<?>, AtomicInteger> primitives) {
      if (objects < 0) {
        throw new IllegalStateException("Negative number of objects");
      }
      if (references < 0) {
        throw new IllegalStateException("Negative number of references");
      }
      if (!primitiveTypes.containsAll(primitives.keySet())) {
        throw new IllegalStateException("Unexpected primitive type");
      }
      this.objects = objects;
      this.references = references;
      this.primitives = Collections.unmodifiableMap(primitives);
    }

    /**
     * Returns the number of objects of this footprint.
     */
    public int getObjects() {
      return objects;
    }

    /**
     * Returns the number of references of this footprint.
     */
    public int getReferences() {
      return references;
    }

    /**
     * Returns the number of primitives of this footprint (represented by the respective primitive
     * classes, {@literal e.g.} {@code int.class} etc).
     */
    public Set<Map.Entry<Class<?>, AtomicInteger>> getPrimitives() {
      return Collections.unmodifiableSet(primitives.entrySet());
    }

    @Override
    public String toString() {
      return "Footprint [objects=" + objects + ", references=" + references + ", primitives="
          + primitives + "]";
    }

  }

  /**
   * Measures the footprint of the specified object graph. The object graph is defined by a root
   * object and whatever object can be reached through that, excluding static fields, {@code Class}
   * objects, and fields defined in {@code enum}s (all these are considered shared values, which
   * should not contribute to the cost of any single object graph).
   *
   * <p>
   * Equivalent to {@code measure(rootObject, Predicates.alwaysTrue())}.
   *
   * @param rootObject the root object of the object graph
   * @return the footprint of the object graph
   */
  public static Footprint measure(Object rootObject) {
    return measure(rootObject, x -> true);
  }

  /**
   * Measures the footprint of the specified object graph. The object graph is defined by a root
   * object and whatever object can be reached through that, excluding static fields, {@code Class}
   * objects, and fields defined in {@code enum}s (all these are considered shared values, which
   * should not contribute to the cost of any single object graph), and any object for which the
   * user-provided predicate returns {@code false}.
   *
   * @param rootObject the root object of the object graph
   * @param objectAcceptor a predicate that returns {@code true} for objects to be explored (and
   *        treated as part of the footprint), or {@code false} to forbid the traversal to traverse
   *        the given object
   * @return the footprint of the object graph
   */
  public static Footprint measure(Object rootObject, Predicate<Object> objectAcceptor) {
    Objects.requireNonNull(objectAcceptor, "predicate");

    Predicate<Chain> completePredicate = ObjectExplorer.notEnumFieldsOrClasses
        .and(chain -> objectAcceptor.test(objectAcceptor.test(ObjectExplorer.chainToObject)))
        .and(new ObjectExplorer.AtMostOncePredicate());

    return ObjectExplorer.exploreObject(rootObject, new ObjectGraphVisitor(completePredicate),
        EnumSet.of(Feature.VISIT_PRIMITIVES, Feature.VISIT_NULL));
  }

  private static class ObjectGraphVisitor implements ObjectVisitor<Footprint> {
    private int objects;
    // -1 to account for the root, which has no reference leading to it
    private int references = -1;
    private final Map<Class<?>, AtomicInteger> primitives = new HashMap<>();
    private final Predicate<Chain> predicate;

    ObjectGraphVisitor(Predicate<Chain> predicate) {
      this.predicate = predicate;
    }

    public Traversal visit(Chain chain) {
      if (chain.isPrimitive()) {
        Class<?> element = chain.getValueType();
        AtomicInteger frequency = primitives.get(element);
        int occurrences = 1;
        if (frequency == null) {
          primitives.put(element, new AtomicInteger(occurrences));
        } else {
          long newCount = (long) frequency.get() + (long) occurrences;
          if (newCount > Integer.MAX_VALUE) {
              throw new IllegalStateException(String.format("too many occurrences: %s", newCount));
          }
          frequency.getAndAdd(occurrences);
        }
        return Traversal.SKIP;
      } else {
        references++;
      }
      if (predicate.test(chain) && chain.getValue() != null) {
        objects++;
        return Traversal.EXPLORE;
      }
      return Traversal.SKIP;
    }

    public Footprint result() {
      return new Footprint(objects, references, Collections.unmodifiableMap(primitives));
    }
  }
}
