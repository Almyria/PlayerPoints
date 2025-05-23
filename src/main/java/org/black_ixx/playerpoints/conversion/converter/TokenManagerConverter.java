package org.black_ixx.playerpoints.conversion.converter;

import dev.rosewood.rosegarden.RosePlugin;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.data.DataManager;
import me.realized.tokenmanager.data.database.Database;
import org.black_ixx.playerpoints.conversion.CurrencyConverter;
import org.black_ixx.playerpoints.models.SortedPlayer;
import org.bukkit.Bukkit;

public class TokenManagerConverter extends CurrencyConverter {

    public TokenManagerConverter(RosePlugin rosePlugin) {
        super(rosePlugin, "TokenManager");
    }

    @Override
    public void convert(String currencyId) {
        TokenManagerPlugin tokenManager = (TokenManagerPlugin) this.plugin;
        DataManager dataManager = tokenManager.getDataManager();
        try {
            Field field_database = DataManager.class.getDeclaredField("database");
            field_database.setAccessible(true);
            Database database = (Database) field_database.get(dataManager);
            this.rosePlugin.getLogger().warning("Converting data from TokenManager, this may take a while if you have a lot of data...");
            database.ordered(Integer.MAX_VALUE, data -> {
                if (data.isEmpty())
                    return;

                boolean isUUID;
                try {
                    UUID.fromString(data.get(0).getKey());
                    isUUID = true;
                } catch (Exception e) {
                    isUUID = false;
                }

                int count = 0;
                Map<UUID, Integer> pointsData = new HashMap<>();
                Map<UUID, String> usernameMap = new HashMap<>();
                for (Database.TopElement entry : data) {
                    try {
                        UUID uuid;
                        String name;
                        if (isUUID) {
                            uuid = UUID.fromString(entry.getKey());
                            name = "Unknown";
                        } else {
                            uuid = Bukkit.getOfflinePlayer(entry.getKey()).getUniqueId();
                            name = entry.getKey();
                        }

                        int amount = Math.toIntExact(entry.getTokens());
                        if (amount > 0) {
                            pointsData.put(uuid, amount);
                            usernameMap.put(uuid, name);
                        }

                        if (++count % 500 == 0)
                            this.rosePlugin.getLogger().warning(String.format("Converted %d entries...", count));
                    } catch (Exception e) {
                        this.rosePlugin.getLogger().warning(String.format("Data entry [%s:%d] skipped due to invalid data", entry.getKey(), entry.getTokens()));
                    }
                }

                this.rosePlugin.getManager(org.black_ixx.playerpoints.manager.DataManager.class).importData(pointsData, usernameMap);
                this.rosePlugin.getLogger().warning(String.format("Successfully converted %d entries!", count));
            });
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

}
