package io.github.apace100.origins.util;

import io.github.apace100.origins.mixin.WeightedListEntryAccessor;
import net.minecraft.util.collection.WeightedList;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilterableWeightedList<U> extends WeightedList<U> {
    private Predicate<U> filter;

    public FilterableWeightedList() {
    }

    public int size() {
        return this.entries.size();
    }

    public void addFilter(Predicate<U> filter) {
        if (this.hasFilter()) {
            this.filter = this.filter.and(filter);
        } else {
            this.setFilter(filter);
        }

    }

    public void setFilter(Predicate<U> filter) {
        this.filter = filter;
    }

    public void removeFilter() {
        this.filter = null;
    }

    public boolean hasFilter() {
        return this.filter != null;
    }

    public Stream<U> stream() {
        return this.filter != null ? this.entries.stream().map(Entry::getElement).filter(this.filter) : super.stream();
    }

    public Stream<Entry<U>> entryStream() {
        return this.entries.stream().filter((entry) -> {
            return this.filter == null || this.filter.test(entry.getElement());
        });
    }

    public void addAll(FilterableWeightedList<U> other) {
        other.entryStream().forEach((entry) -> {
            this.add(entry.getElement(), ((WeightedListEntryAccessor)entry).getWeight());
        });
    }

    public FilterableWeightedList<U> copy() {
        FilterableWeightedList<U> copied = new FilterableWeightedList();
        copied.addAll(this);
        return copied;
    }
}
