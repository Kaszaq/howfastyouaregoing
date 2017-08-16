package pl.kaszaq.cfd;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import pl.kaszaq.agile.IssueStatusTransition;

public class CfdDataCollector implements Collector<IssueStatusTransition, CfdData, CfdData> {

    @Override
    public Set<Characteristics> characteristics() {
        return ImmutableSet.of(Characteristics.IDENTITY_FINISH);
    }

    @Override
    public Supplier<CfdData> supplier() {
        return () -> new CfdData();
    }

    @Override
    public BiConsumer<CfdData, IssueStatusTransition> accumulator() {
        return (CfdData t, IssueStatusTransition u) -> t.addTransition(u);
    }

    @Override
    public BinaryOperator<CfdData> combiner() {
        return (CfdData a, CfdData b) -> {
            throw new UnsupportedOperationException("Not supported yet.");
        };
    }

    @Override
    public Function<CfdData, CfdData> finisher() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
