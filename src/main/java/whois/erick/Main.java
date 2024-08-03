package whois.erick;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {

    private GameManager gameManager;
    private String webhookUrl;
    private boolean webhookEnabled;

    @Override
    public void onEnable() {
        getLogger().info("Batatinha Quente ativado!");
        createConfig();
        loadConfig();
        gameManager = new GameManager(this, webhookUrl, webhookEnabled);
    }

    @Override
    public void onDisable() {
        getLogger().info("Batatinha Quente desativado!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("startbatatinha")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Este comando só pode ser usado por jogadores.");
                return true;
            }
            Player player = (Player) sender;
            gameManager.startGame(player);
            return true;
        }
        return false;
    }

    private void createConfig() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        webhookUrl = config.getString("webhook.url");
        webhookEnabled = config.getBoolean("webhook.enabled");
    }
}
