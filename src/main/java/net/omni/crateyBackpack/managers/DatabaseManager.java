package net.omni.crateyBackpack.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.omni.crateyBackpack.CrateyBackpack;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private final CrateyBackpack plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(CrateyBackpack plugin) {
        this.plugin = plugin;
    }

    public void init() {
        File dbFile = new File(plugin.getDataFolder(), plugin.getConfigUtil().getDatabaseFile());
        File parent = dbFile.getParentFile();
        if (parent != null && !parent.exists())
            parent.mkdirs();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");
        config.setMaximumPoolSize(1);
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");
        config.addDataSourceProperty("foreign_keys", "true");

        this.dataSource = new HikariDataSource(config);
        createTable();
    }

    private void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS player_keys (
                    uuid VARCHAR(36) NOT NULL,
                    key_id VARCHAR(64) NOT NULL,
                    amount INT NOT NULL DEFAULT 0,
                    PRIMARY KEY (uuid, key_id)
                )
                """;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not create database table: " + e.getMessage());
        }
    }

    public void addKey(UUID uuid, String keyId, int amount) {
        if (amount <= 0) return;
        String sql = "INSERT INTO player_keys (uuid, key_id, amount) VALUES (?, ?, ?) " +
                "ON CONFLICT(uuid, key_id) DO UPDATE SET amount = amount + ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, keyId);
            stmt.setInt(3, amount);
            stmt.setInt(4, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not add key for " + uuid + ": " + e.getMessage());
        }
    }

    public CompletableFuture<Map<String, Integer>> loadPlayerKeysAsync(UUID uuid) {
        CompletableFuture<Map<String, Integer>> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                future.complete(loadPlayerKeys(uuid)));
        return future;
    }

    public Map<String, Integer> loadPlayerKeys(UUID uuid) {
        Map<String, Integer> keys = new HashMap<>();
        String sql = "SELECT key_id, amount FROM player_keys WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    keys.put(rs.getString("key_id"), rs.getInt("amount"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not load keys for " + uuid + ": " + e.getMessage());
        }
        return keys;
    }

    public CompletableFuture<Void> savePlayerKeysAsync(UUID uuid, Map<String, Integer> keys) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            savePlayerKeys(uuid, keys);
            future.complete(null);
        });
        return future;
    }

    public void savePlayerKeys(UUID uuid, Map<String, Integer> keys) {
        String delete = "DELETE FROM player_keys WHERE uuid = ?";
        String insert = "INSERT OR REPLACE INTO player_keys (uuid, key_id, amount) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement delStmt = conn.prepareStatement(delete);
                 PreparedStatement insStmt = conn.prepareStatement(insert)) {
                delStmt.setString(1, uuid.toString());
                delStmt.executeUpdate();

                for (Map.Entry<String, Integer> entry : keys.entrySet()) {
                    if (entry.getValue() <= 0)
                        continue;
                    insStmt.setString(1, uuid.toString());
                    insStmt.setString(2, entry.getKey());
                    insStmt.setInt(3, entry.getValue());
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not save keys for " + uuid + ": " + e.getMessage());
        }
    }

    public CompletableFuture<Void> setKeyAsync(UUID uuid, String keyId, int amount) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            setKey(uuid, keyId, amount);
            future.complete(null);
        });
        return future;
    }

    public void setKey(UUID uuid, String keyId, int amount) {
        if (amount <= 0) {
            removeKey(uuid, keyId);
            return;
        }
        String sql = "INSERT OR REPLACE INTO player_keys (uuid, key_id, amount) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, keyId);
            stmt.setInt(3, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not set key for " + uuid + ": " + e.getMessage());
        }
    }

    public void removeKey(UUID uuid, String keyId) {
        String sql = "DELETE FROM player_keys WHERE uuid = ? AND key_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, keyId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not remove key for " + uuid + ": " + e.getMessage());
        }
    }

    public CompletableFuture<Void> removeKeyAsync(UUID uuid, String keyId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            removeKey(uuid, keyId);
            future.complete(null);
        });
        return future;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed())
            dataSource.close();
    }
}
