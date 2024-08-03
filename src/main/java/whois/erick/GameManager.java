package whois.erick;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameManager {

    private final Main plugin;
    private final List<UUID> players;
    private UUID currentHolder;
    private final ScoreboardManager manager;
    private final Scoreboard board;
    private final Objective objective;
    private int countdown;
    private final String webhookUrl;
    private final boolean webhookEnabled;

    public GameManager(Main plugin, String webhookUrl, boolean webhookEnabled) {
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.webhookUrl = webhookUrl;
        this.webhookEnabled = webhookEnabled;
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        objective = board.registerNewObjective("batatinha", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.RED + "Batatinha Quente");
    }

    public void startGame(Player starter) {
        players.clear();
        players.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toList()));

        if (players.size() < 2) {
            starter.sendMessage(ChatColor.RED + "Pelo menos dois jogadores são necessários para iniciar o jogo.");
            return;
        }

        currentHolder = players.get(new Random().nextInt(players.size()));
        Player holderPlayer = Bukkit.getPlayer(currentHolder);

        if (holderPlayer != null) {
            holderPlayer.getInventory().addItem(new ItemStack(Material.TNT));
            holderPlayer.sendMessage(ChatColor.RED + "Você está com a batatinha quente!");

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(ChatColor.RED + holderPlayer.getName() + " está com a batatinha quente!");
            }

            sendDiscordMessage(holderPlayer.getName() + " está com a batatinha quente!");

            countdown = 30;  // 30 seconds countdown
            new GameTask().runTaskTimer(plugin, 0L, 20L);  // Run task every second (20 ticks)
        }
    }

    private void sendDiscordMessage(String message) {
        if (!webhookEnabled || webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(webhookUrl);
        httpPost.addHeader("Content-Type", "application/json");

        String json = "{\"content\": \"" + message + "\"}";

        try {
            httpPost.setEntity(new StringEntity(json));
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                plugin.getLogger().info("Mensagem enviada para o Discord: " + message);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao enviar mensagem para o Discord: " + e.getMessage());
        }
    }

    private class GameTask extends BukkitRunnable {
        @Override
        public void run() {
            if (countdown <= 0) {
                Player holderPlayer = Bukkit.getPlayer(currentHolder);
                if (holderPlayer != null) {
                    holderPlayer.sendMessage(ChatColor.RED + "Você explodiu com a batatinha quente!");
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(ChatColor.RED + holderPlayer.getName() + " explodiu com a batatinha quente!");
                    }
                    sendDiscordMessage(holderPlayer.getName() + " explodiu com a batatinha quente!");
                    holderPlayer.setHealth(0.0);  // Kill the player
                    holderPlayer.getInventory().remove(Material.TNT);  // Remove TNT from inventory
                }
                this.cancel();
                return;
            }

            Player holderPlayer = Bukkit.getPlayer(currentHolder);
            if (holderPlayer != null) {
                Score score = objective.getScore(ChatColor.GREEN + "Tempo restante");
                score.setScore(countdown);
                holderPlayer.setScoreboard(board);
            }

            countdown--;
        }
    }
}
