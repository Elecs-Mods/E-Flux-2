package elec332.eflux2.proxies;

import elec332.core.client.RenderHelper;
import elec332.eflux2.client.TerminalColor;
import elec332.eflux2.register.EFlux2BlockRegister;
import elec332.eflux2.register.EFlux2ItemRegister;

/**
 * Created by Elec332 on 1-1-2020
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void registerColors() {
        TerminalColor tc = new TerminalColor();
        RenderHelper.getItemColors().register(tc, EFlux2ItemRegister.terminal);
        RenderHelper.getBlockColors().register(tc, EFlux2BlockRegister.wire);
    }

}
