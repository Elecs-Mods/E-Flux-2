package elec332.eflux2.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.util.Direction;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Elec332 on 21-2-2018
 */
public class ListedEnumMap<T extends ListedEnumMap.ISidedObject> implements Iterable<T> {

    public static <T extends ISidedObject> ListedEnumMap<T> create() {
        return new ListedEnumMap<>();
    }

    private ListedEnumMap() {
        this.objects = Lists.newArrayList();
        this.facedMap = new EnumMap<>(Direction.class);
        this.objects_ = Collections.unmodifiableList(objects);
    }

    private final List<T> objects, objects_;
    private final EnumMap<Direction, Map<Integer, T>> facedMap;

    public boolean remove(T obj) {
        if (obj == null) {
            return false;
        }
        T t = getObject(obj.getSide(), obj.getSlot());
        if (t != obj) {
            return false;
        }
        get(obj.getSide()).remove(obj.getSlot());
        objects.remove(t);
        return true;
    }

    public boolean remove(Direction f, int slot) {
        T t = getObject(f, slot);
        if (t == null) {
            return false;
        }
        get(f).remove(slot);
        objects.remove(t);
        return true;
    }

    public boolean add(T obj) {
        if (containsObject(obj.getSide(), obj.getSlot())) {
            return false;
        }
        get(obj.getSide()).put(obj.getSlot(), obj);
        objects.add(obj);
        return true;
    }

    public boolean containsObject(Direction f, int slot) {
        return getObject(f, slot) != null;
    }

    public T getObject(Pair<Direction, Integer> data) {
        if (data == null) {
            return null;
        }
        return getObject(data.getLeft(), data.getRight());
    }

    public T getObject(Direction f, int slot) {
        return get(f).get(slot);
    }

    public void clear() {
        facedMap.clear();
        objects.clear();
    }

    public List<T> getObjects() {
        return objects_;
    }

    private Map<Integer, T> get(Direction f) {
        return facedMap.computeIfAbsent(f, Direction -> Maps.newHashMap());
    }

    @Override
    @Nonnull
    public Iterator<T> iterator() {
        return objects_.iterator();
    }

    public Stream<T> streamValues() {
        return objects_.stream();
    }

    public interface ISidedObject {

        public Direction getSide();

        public int getSlot();

    }

}
