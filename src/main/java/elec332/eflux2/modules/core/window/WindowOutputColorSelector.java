package elec332.eflux2.modules.core.window;

import com.google.common.collect.Sets;
import elec332.core.inventory.widget.WidgetEnumChange;
import elec332.core.inventory.widget.WidgetText;
import elec332.core.inventory.window.Window;
import elec332.eflux2.api.util.ConnectionPoint;
import elec332.eflux2.util.ConnectionPointHandler;
import net.minecraft.item.DyeColor;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Created by Elec332 on 20-2-2019
 */
public class WindowOutputColorSelector extends Window {

    public WindowOutputColorSelector(@Nonnull ConnectionPointHandler cph, Object... points) {
        this.cph = cph;
        if (points == null || points.length == 0) {
            throw new IllegalArgumentException();
        }
        this.points = Sets.newHashSet(points);
        this.points.forEach(cph::getStrict); //Check validity
    }

    private final ConnectionPointHandler cph;
    private final Set<Object> points;

    @Override
    protected void initWindow() {
        int height = 5;
        for (Object o : points) {
            ConnectionPoint cp = cph.getStrict(o);
            addWidget(new WidgetText(5, height, false, cph.getInfoFor(o)));
            WidgetEnumChange<DyeColor> w;
            addWidget(w = new WidgetEnumChange<>(80, height, 30, 10, DyeColor.class));
            w.setEnum(DyeColor.values()[cp.getSideNumber()]);
            w.onValueChanged(e -> cph.updateConnection(o, cp.getSide(), e.ordinal(), cp.getEdge()));
            height += 15;
        }
    }

}
