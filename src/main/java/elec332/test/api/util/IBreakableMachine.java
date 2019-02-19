package elec332.test.api.util;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 7-11-2017.
 */
public interface IBreakableMachine {

    public void breakMachine(@Nonnull BreakReason reason);

}
