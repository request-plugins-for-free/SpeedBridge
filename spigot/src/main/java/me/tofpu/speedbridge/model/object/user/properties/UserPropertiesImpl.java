package me.tofpu.speedbridge.model.object.user.properties;

import me.tofpu.speedbridge.api.model.object.user.UserProperties;
import me.tofpu.speedbridge.api.model.object.user.timer.Timer;

public class UserPropertiesImpl implements UserProperties {
    private Integer islandSlot;
    private Timer timer;

    public UserPropertiesImpl() {}

    @Override
    public Integer islandSlot() {
        return islandSlot;
    }

    @Override
    public void islandSlot(final Integer islandSlot) {
        this.islandSlot = islandSlot;
    }

    @Override
    public Timer timer() {
        return timer;
    }

    @Override
    public void timer(final Timer timer) {
        this.timer = timer;
    }
}
