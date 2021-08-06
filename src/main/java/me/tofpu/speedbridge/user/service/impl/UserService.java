package me.tofpu.speedbridge.user.service.impl;

import com.google.gson.Gson;
import me.tofpu.speedbridge.user.IUser;
import me.tofpu.speedbridge.user.impl.User;
import me.tofpu.speedbridge.user.service.IUserService;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserService implements IUserService {
    private final List<IUser> users = new ArrayList<>();

    @Override
    public IUser createUser(final UUID uuid) {
        final IUser user = new User(uuid);
        this.users.add(user);

        return user;
    }

    @Override
    public void removeUser(final IUser user) {
        this.users.remove(user);
    }

    @Override
    public IUser getOrDefault(final UUID uuid) {
        IUser user = searchForUUID(uuid);
        if (user == null) user = createUser(uuid);
        return user;
    }

    @Override
    public IUser searchForUUID(final UUID uuid) {
        for (final IUser user : this.users) {
            if (user.getUuid().equals(uuid)) return user;
        }
        return null;
    }

    @Override
    public void saveAll(final Gson gson, final File directory) {
        if (!directory.exists()) directory.mkdirs();
        for (final IUser user : this.users) {
            final File file = new File(directory, user.getUuid().toString() + ".json");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                try (final FileWriter writer = new FileWriter(file)) {
                    writer.write(gson.toJson(user, IUser.class));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IUser load(final Gson gson, final UUID uuid, final File directory) {
        final File file = new File(directory, uuid.toString() + ".json");
        if (!file.exists()) return null;

        final IUser user;
        try {
            user = gson.fromJson(new FileReader(file), IUser.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        this.users.add(user);
        return user;
    }
}
