package org.kristie.methods;

import org.kristie.core.context.Accessor;
import org.kristie.core.context.Context;
import org.kristie.util.Timer;
import org.powerbot.script.Condition;
import org.powerbot.script.Random;
import org.powerbot.script.Tile;
import org.powerbot.script.rt6.Component;
import org.powerbot.script.rt6.TileMatrix;
import org.powerbot.script.rt6.Widgets;

import java.awt.*;
import java.util.concurrent.Callable;

public class Lodestone extends Accessor {

    private final Widgets widgets = ctx.widgets;
    private final Component TELEPORT_INTERFACE = widgets.component(1092, 0);
    private final Component OPEN_TELEPORT_INTERFACE = widgets.component(1465, 10);
    private final Timer t = new Timer(Random.nextInt(1800, 2200));
    private boolean wrongDest = false;

    public Lodestone(Context context) {
        super(context);
    }

    private boolean componentPresent(Component component) {
        return component.valid() && component.visible();
    }

    private boolean teleporting() {
        if (ctx.players.local().animation() != -1) {
            t.reset(); // To account for small lag when the map changes and animation becomes -1
            return true;
        }
        return t.isRunning();
    }


    /**
     * @param lodestones: lodestone that we want to teleport to
     * @return returns true if we can use the lodestone
     */

    public boolean canUse(Lodestone.Location lodestones) {
        switch (lodestones) {
            case BANDIT_CAMP:
                return ctx.varpbits.varpbit(2151, 0x7fff) == 15;
            case LUNAR_ISLE:
                return ctx.varpbits.varpbit(2253, 0xfffff) == 190;
            case ASHDALE:
                return ctx.varpbits.varpbit(4390, 0, 0x7f) == 100;
        }
        return ctx.varpbits.varpbit(3, lodestones.shift(), 1) == 1;
    }


    /**
     * @param lodestone: lodestone that we want to teleport to
     * @return returns true if player is within 6 tiles of lodestone and animation is -1
     */

    public boolean teleport(Lodestone.Location lodestone) {
        return teleport(lodestone,false);
    }
    /**
     * @param lodestone: lodestone that we want to teleport to
     * @param key: use keyboard key instead of clicking with mouse
     * @return returns true if player is within 6 tiles of lodestone and animation is -1
     */

    public boolean teleport(Lodestone.Location lodestone,boolean key) {
        final TileMatrix lodestoneMatrix = lodestone.location().matrix(ctx);
        if (lodestoneMatrix.onMap() && lodestoneMatrix.reachable()) {
            ctx.movement.step(lodestone.location());
        } else if (!teleporting() || wrongDest) {
            if (componentPresent(TELEPORT_INTERFACE)) {
                final Component dest = widgets.component(1092, lodestone.widgetIndex());
                if (key && ctx.input.send(lodestone.key()) || ctx.input.click(dest.nextPoint(), true) && clickedCorrect(dest)) {
                    wrongDest = false;
                    Condition.wait(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return teleporting();
                        }
                    }, 100, 25);
                } else {
                    wrongDest = true;
                }
            } else {
                if (ctx.input.click(OPEN_TELEPORT_INTERFACE.nextPoint(), true)) {
                    Condition.wait(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return componentPresent(TELEPORT_INTERFACE);
                        }
                    }, 100, 23);
                }
            }
        }
        return ctx.players.local().tile().distanceTo(lodestone.location()) <= 6 && !teleporting();
    }


    private boolean clickedCorrect(Component correct) {
        final Point clickPoint = ctx.input.getPressLocation();
        return correct.boundingRect().contains(clickPoint);
    }

    public static enum Location {
        LUNAR_ISLE(new Tile(2085, 3914, 0), -1, "{VK_ALT down}{VK_L}{VK_ALT up}"),
        AL_KHARID(new Tile(3297, 3184, 0), 0,"{VK_A}"),
        ARDOUGNE(new Tile(2634, 3348, 0), 1,"{VK_ALT down}{VK_A}{VK_ALT up}"),
        BURTHORPE(new Tile(2899, 3544, 0), 2,"{VK_B}"),
        CATHERBY(new Tile(2831, 3451, 0), 3,"{VK_C}"),
        DRAYNOR(new Tile(3105, 3298, 0), 4,"{VK_D}"),
        EDGEVILLE(new Tile(3067, 3505, 0), 5,"{VK_E}"),
        FALADOR(new Tile(2967, 3403, 0), 6,"{VK_F}"),
        LUMBRIDGE(new Tile(3233, 3221, 0), 7,"{VK_L}"),
        PORT_SARIM(new Tile(3011, 3215, 0), 8,"{VK_P}"),
        SEERS_VILLAGE(new Tile(2689, 3482, 0), 9,"{VK_S}"),
        TAVERLEY(new Tile(2689, 3482, 0), 10,"{VK_T}"),
        VARROCK(new Tile(3214, 3376, 0), 11,"{VK_V}"),
        YANILlE(new Tile(2529, 3094, 0), 12,"{VK_Y}"),
        CANIFIS(new Tile(3518, 3517, 0), 15,"{VK_ALT down}{VK_C}{VK_ALT up}"),
        EAGLES_PEEK(new Tile(2366, 3479, 0), 16,"{VK_ALT down}{VK_E}{VK_ALT up}"),
        FREMENIK_PROVINCE(new Tile(2711, 3678, 0), 17,"{VK_ALT down}{VK_F}{VK_ALT up}"),
        KARAMJA(new Tile(2761, 3148, 0), 18,"{VK_K}"),
        OOGLOG(new Tile(2533, 2871, 0), 19,"{VK_O}"),
        TIRANNWN(new Tile(2254, 3150, 0), 20,"{VK_ALT down}{VK_T}{VK_ALT up}"),
        WILDERNESS_VOLCANO(new Tile(3142, 3636, 0), 21,"{VK_W}"),
        ASHDALE(new Tile(2460, 2686, 0), -1,"{VK_SHIFT down}{VK_A}{VK_SHIFT up}"),
        BANDIT_CAMP(new Tile(3214, 2954, 0), -12,"{VK_ALT down}{VK_B}{VK_ALT up}");


        private final int widgetIndex;
        private final int shift;
        private final Tile location;
        private final String key;

        private Location(final Tile location,final int shift, final String key) {
            this.widgetIndex = 15 + ordinal();
            this.shift = shift;
            this.location = location;
            this.key = key;
        }

        public int widgetIndex() {
            if (this.equals(BANDIT_CAMP)) return 4;
            return widgetIndex;
        }

        public int shift() {
            return shift;
        }

        public Tile location() {
            return location;
        }

        public String key(){
            return key;
        }
    }
}
