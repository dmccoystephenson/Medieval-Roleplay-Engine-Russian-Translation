package rpsystem;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import rpsystem.Commands.BirdCommand;
import rpsystem.Commands.CardCommand;
import rpsystem.Commands.TitleCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static rpsystem.UtilityFunctions.*;

public class Main extends JavaPlugin implements Listener {

    // saved
    ArrayList<CharacterCard> cards = new ArrayList<>();

    // temporary
    public ArrayList<String> playersWithBusyBirds = new ArrayList<>();
    public ArrayList<String> playersSpeakingInLocalChat = new ArrayList<>();
    public ArrayList<String> playersOnNameChangeCooldown = new ArrayList<>();
    public ArrayList<String> playersWithRightClickCooldown = new ArrayList<>();

    @Override
    public void onEnable() {
        System.out.println("Medieval Roleplay Engine (Russian Translation) plugin enabling....");

        this.getServer().getPluginManager().registerEvents(this, this);

        loadCards();

        System.out.println("Medieval Roleplay Engine (Russian Translation) plugin enabled.");
    }

    @Override
    public void onDisable() {
        System.out.println("Medieval Roleplay Engine (Russian Translation) plugin disabling....");

        saveCardFileNames();
        saveCards();

        System.out.println("Medieval Roleplay Engine(Russian Translation)  plugin disabled.");
    }

    public void saveCardFileNames() {
        try {
            File saveFolder = new File("./plugins/medieval-roleplay-engine/");
            if (!saveFolder.exists()) {
                saveFolder.mkdir();
            }
            File saveFile = new File("./plugins/medieval-roleplay-engine/" + "card-player-names.txt");
            if (saveFile.createNewFile()) {
                System.out.println("Save file for character card filenames created.");
            } else {
                System.out.println("Save file for character card filenames already exists. Overwriting.");
            }

            FileWriter saveWriter = new FileWriter(saveFile);

            // actual saving takes place here
            for (CharacterCard card : cards) {
                saveWriter.write(card.getPlayerName() + "\n");
            }

            saveWriter.close();

        } catch (IOException e) {
            System.out.println("An error occurred while saving character card filenames.");
        }
    }

    public void saveCards() {
        for (CharacterCard card : cards) {
            card.save();
        }
    }

    public void loadCards() {
        try {
            System.out.println("Attempting to load character cards...");
            File loadFile = new File("./plugins/medieval-roleplay-engine/" + "card-player-names.txt");
            Scanner loadReader = new Scanner(loadFile);

            // actual loading
            while (loadReader.hasNextLine()) {
                String nextName = loadReader.nextLine();
                CharacterCard temp = new CharacterCard(nextName);
                temp.load(nextName + ".txt"); // provides owner field among other things

                // existence check
                boolean exists = false;
                for (int i = 0; i < cards.size(); i++) {
                    if (cards.get(i).getName().equalsIgnoreCase(temp.getName())) {
                        cards.remove(i);
                    }
                }

                cards.add(temp);

            }

            loadReader.close();
            System.out.println("Character cards successfully loaded.");
        } catch (FileNotFoundException e) {
            System.out.println("Error loading the character cards!");
            e.printStackTrace();
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // card command
        if (label.equalsIgnoreCase("card")) {
            if (args.length == 0) {
                CardCommand.showCard(sender, args, cards);
                return true;
            } else {

                if (args[0].equalsIgnoreCase("help")) {
                    CardCommand.showHelpMessage(sender);
                    return true;
                }

                if (args[0].equalsIgnoreCase("name")) {
                    CardCommand command = new CardCommand(this);
                    command.changeName(sender, args, cards);
                    return true;
                }
                if (args[0].equalsIgnoreCase("race")) {
                    CardCommand.changeRace(sender, args, cards);
                    return true;
                }
                if (args[0].equalsIgnoreCase("subculture")) {
                    CardCommand.changeSubculture(sender, args, cards);
                    return true;
                }
                if (args[0].equalsIgnoreCase("religion")) {
                    CardCommand.changeReligion(sender, args, cards);
                    return true;
                }
                if (args[0].equalsIgnoreCase("age")) {
                    CardCommand.changeAge(sender, args, cards);
                    return true;
                }
                if (args[0].equalsIgnoreCase("gender")) {
                    CardCommand.changeGender(sender, args, cards);
                    return true;
                }

                if (args[0].equalsIgnoreCase("forcesave")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;

                        if (player.hasPermission("rp.card.forcesave") || player.hasPermission("rp.admin")) {
                            saveCardFileNames();
                            saveCards();
                            return true;
                        }
                        else {
                            player.sendMessage(ChatColor.RED + "Sorry! In order to use this command, you need the following permission: 'rp.card.forcesave'");
                            return false;
                        }

                    }
                }

                if (args[0].equalsIgnoreCase("forceload")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;

                        if (player.hasPermission("rp.card.forceload") || player.hasPermission("rp.admin")) {
                            loadCards();
                            return true;
                        }
                        else {
                            player.sendMessage(ChatColor.RED + "Sorry! In order to use this command, you need the following permission: 'rp.card.forceload'");
                            return false;
                        }
                    }

                }

                CardCommand.showPlayerInfo(sender, args, cards);
                return true;
            }
        }

        if (label.equalsIgnoreCase("bird")) {
            BirdCommand command = new BirdCommand(this);
            command.sendBird(sender, args);
            return true;
        }

        if (label.equalsIgnoreCase("local") || label.equalsIgnoreCase("rp")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("rp.local") || player.hasPermission("rp.rp") || player.hasPermission("rp.default")) {
                    if (!playersSpeakingInLocalChat.contains(player.getName())) {
                        playersSpeakingInLocalChat.add(player.getName());
                        player.sendMessage(ChatColor.GREEN + "Ты говоришь в локальный чат!");
                        return true;
                    }
                    else {
                        player.sendMessage(ChatColor.RED + "Ты уже говоришь в локальный чат!");
                        return false;
                    }
                }
                else {
                    player.sendMessage(ChatColor.RED + "Sorry! In order to use this command, you need one the following permissions: 'rp.local', 'rp.rp'");
                    return false;
                }

            }
        }

        if (label.equalsIgnoreCase("global") || label.equalsIgnoreCase("ooc")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("rp.global") || player.hasPermission("rp.ooc") || player.hasPermission("rp.default")) {
                    if (playersSpeakingInLocalChat.contains(player.getName())) {
                        playersSpeakingInLocalChat.remove(player.getName());
                        player.sendMessage(ChatColor.GREEN + "Ты говоришь в глобальный чат!");
                    }
                    else {
                        player.sendMessage(ChatColor.RED + "Ты уже в глобальном чате!");
                    }
                }
                else {
                    player.sendMessage(ChatColor.RED + "Sorry! In order to use this command, you need one the following permissions: 'rp.global', 'rp.ooc'");
                }

            }
        }

        if (label.equalsIgnoreCase("emote") || label.equalsIgnoreCase("me")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("rp.emote") || player.hasPermission("rp.me") || player.hasPermission("rp.default")) {
                    if (args.length > 0) {
                        String message = createStringFromFirstArgOnwards(args, 0);
                        String characterName = getCard(player.getName()).getName();

                        sendMessageToPlayersWithinDistance(player,ChatColor.GRAY + "" + ChatColor.ITALIC + characterName + " " + message, 25);
                    }
                }
                else {
                    player.sendMessage(ChatColor.RED + "Sorry! In order to use this command, you need one the following permissions: 'rp.emote', 'rp.me'");
                }

            }
        }

        if (label.equalsIgnoreCase("roll") || label.equalsIgnoreCase("dice")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("rp.roll") || player.hasPermission("rp.dice") || player.hasPermission("rp.default")) {
                    if (args.length > 0) {
                        try {
                            int max = Integer.parseInt(args[0]);
                            sendMessageToPlayersWithinDistance(player,ChatColor.AQUA + "" + ChatColor.ITALIC + player.getName() + " кинул кость .Ему выпало " + rollDice(max) + " из " + max + ".", 25);
                        }
                        catch(Exception ignored) {

                        }
                    }
                }
                else {
                    player.sendMessage(ChatColor.RED + "Sorry! In order to use this command, you need one the following permissions: 'rp.roll', 'rp.dice'");
                }

            }
        }

        if (label.equalsIgnoreCase("title")) {
            TitleCommand command = new TitleCommand(this);
            command.titleBook(sender, args);
        }

        return false;
    }

    @EventHandler()
    public void onJoin(PlayerJoinEvent event) {
        if (!hasCard(event.getPlayer().getName())) {
            CharacterCard newCard = new CharacterCard(event.getPlayer().getName());
            cards.add(newCard);
        }
    }

    public boolean hasCard(String playerName) {
        for (CharacterCard card : cards) {
            if (card.getPlayerName().equalsIgnoreCase(playerName)) {
                return true;
            }
        }
        return false;
    }

    public CharacterCard getCard(String playerName) {
        for (CharacterCard card : cards) {
            if (card.getPlayerName().equalsIgnoreCase(playerName)) {
                return card;
            }
        }
        return null;
    }

    @EventHandler()
    public void onChat(AsyncPlayerChatEvent event) {
        if (playersSpeakingInLocalChat.contains(event.getPlayer().getName())) {
            sendMessageToPlayersWithinDistance(event.getPlayer(), ChatColor.GRAY + "" + String.format("%s: \"%s\"", getCard(event.getPlayer().getName()).getName(), event.getMessage()), 25);
            event.setCancelled(true);
        }
    }

    @EventHandler()
    public void onRightClick(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {

            Player target = (Player) event.getRightClicked();
            CharacterCard card = getCard(target.getName());

            Player player = event.getPlayer();

            if (!playersWithRightClickCooldown.contains(player.getName())) {
                playersWithRightClickCooldown.add(player.getName());

                if (player.hasPermission("rp.card.show.others") || player.hasPermission("rp.card.*") || player.hasPermission("rp.default")) {
                    player.sendMessage(ChatColor.BOLD + "" + ChatColor.AQUA + "\n == " + "Анкета " + card.getPlayerName() + " == ");
                    player.sendMessage(ChatColor.AQUA + "Имя: " + card.getName());
                    player.sendMessage(ChatColor.AQUA + "Раса: " + card.getRace());
                    player.sendMessage(ChatColor.AQUA + "Субкультура: " + card.getSubculture());
                    player.sendMessage(ChatColor.AQUA + "Возраст: " + card.getAge());
                    player.sendMessage(ChatColor.AQUA + "Пол: " + card.getGender());
                    player.sendMessage(ChatColor.AQUA + "Религия: " + card.getReligion());

                    int seconds = 2;
                    getServer().getScheduler().runTaskLater(this, new Runnable() {
                        @Override
                        public void run() {
                            playersWithRightClickCooldown.remove(player.getName());

                        }
                    }, seconds * 20);
                }

            }

        }
    }

}
