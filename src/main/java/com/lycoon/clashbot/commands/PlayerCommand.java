package com.lycoon.clashbot.commands;

import com.lycoon.clashapi.cocmodels.player.Player;
import com.lycoon.clashapi.cocmodels.player.Troop;
import com.lycoon.clashapi.core.exception.ClashAPIException;
import com.lycoon.clashbot.core.CacheComponents;
import com.lycoon.clashbot.core.ClashBotMain;
import com.lycoon.clashbot.lang.LangUtils;
import com.lycoon.clashbot.utils.*;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static com.lycoon.clashbot.utils.GameUtils.*;

public class PlayerCommand {
    private final static int WIDTH = 932;
    private final static int HEIGHT = 559;
    private final static float FONT_SIZE = 12f;

    private final static int COLUMNS = 4;
    private final static int ARMY_TOPLINE = 238;
    private final static int ARMY_BOTLINE = 436;

    private final static String[] TROOPS = {"Barbarian", "Archer", "Giant", "Goblin", "Wall Breaker", "Balloon",
            "Wizard", "Healer", "Dragon", "P.E.K.K.A", "Baby Dragon", "Miner", "Electro Dragon", "Yeti", "Minion",
            "Hog Rider", "Valkyrie", "Golem", "Witch", "Lava Hound", "Bowler", "Ice Golem", "Headhunter"};
    private final static String[] SPELLS = {"Lightning Spell", "Healing Spell", "Rage Spell", "Jump Spell",
            "Freeze Spell", "Clone Spell", "Invisibility Spell", "Poison Spell", "Earthquake Spell", "Haste Spell",
            "Skeleton Spell", "Bat Spell"};
    private final static String[] BUILDER_TROOPS = {"Raged Barbarian", "Sneaky Archer", "Boxer Giant", "Beta Minion",
            "Bomber", "Baby Dragon", "Cannon Cart", "Night Witch", "Drop Ship", "Super P.E.K.K.A", "Hog Glider"};
    private final static String[] SUPER_TROOPS = {"Super Barbarian", "Super Archer", "Super Giant", "Sneaky Goblin",
            "Super Wall Breaker", "Super Wizard", "Inferno Dragon", "Super Minion", "Super Valkyrie", "Super Witch",
            "Ice Hound"};
    private final static String[] HEROES = {"Barbarian King", "Archer Queen", "Grand Warden", "Royal Champion",
            "Battle Machine"};
    private final static String[] MACHINES = {"Wall Wrecker", "Battle Blimp", "Stone Slammer", "Siege Barracks",
            "Log Launcher"};
    private final static String[] PETS = {"L.A.S.S.I", "Electro Owl", "Mighty Yak", "Unicorn"};

    public static void dispatch(MessageReceivedEvent event, String... args) {
        CompletableFuture.runAsync(() -> {
            if (args.length > 1)
                PlayerCommand.execute(event, args[1]);
            else
                PlayerCommand.execute(event);
        });
    }

    public static void drawSuperTroop(Graphics2D g2d, Troop troop, String troopName, int x, int y) {
        if (troop == null || troop.isActiveSuperTroop() == null)
            g2d.drawImage(FileUtils.getImageFromFile("troops/locked/" + troopName + ".png"), x, y, 44, 44, null);
        else
            g2d.drawImage(FileUtils.getImageFromFile("troops/" + troop.getName() + ".png"), x, y, 44, 44, null);
    }

    public static void drawTroop(Graphics2D g2d, Font font, Troop troop, String troopName, int x, int y) {
        // If the player has not unlocked the troop yet
        if (troop == null)
            g2d.drawImage(FileUtils.getImageFromFile("troops/locked/" + troopName + ".png"), x, y, 44, 44, null);
        else {
            g2d.drawImage(FileUtils.getImageFromFile("troops/" + troop.getName() + ".png"), x, y, 44, 44, null);
            if (troop.getLevel().intValue() == troop.getMaxLevel().intValue())
                g2d.drawImage(FileUtils.getImageFromFile("icons/level-label-max.png"), x + 1, y + 22, 20, 20, null);
            else if (troop.getLevel() != 1)
                g2d.drawImage(FileUtils.getImageFromFile("icons/level-label.png"), x + 1, y + 22, 20, 20, null);

            if (troop.getLevel() != 1) {
                Rectangle levelRect = new Rectangle(x + 1, y + 22, 20, 20);
                DrawUtils.drawCenteredString(g2d, levelRect, font.deriveFont(font.getSize() - 2f), troop.getLevel().toString());
            }
        }
    }

    public static void drawTroops(Graphics2D g2d, Font font, List<Troop> troops, int y) {
        for (int i = 0, j = 0; i < TROOPS.length; i++, j += i % COLUMNS == 0 ? 1 : 0)
            drawTroop(g2d, font, getHomeTroopByName(troops, TROOPS[i]), TROOPS[i], (i % COLUMNS) * 50 + 20, y + j * 50);
    }

    public static void drawSpells(Graphics2D g2d, Font font, List<Troop> spells, int y) {
        for (int i = 0, j = 0; i < SPELLS.length; i++, j += i % COLUMNS == 0 ? 1 : 0)
            drawTroop(g2d, font, getTroopByName(spells, SPELLS[i]), SPELLS[i], (i % COLUMNS) * 50 + 250, y + j * 50);
    }

    public static void drawBuilderTroops(Graphics2D g2d, Font font, List<Troop> builderTroops, int y) {
        for (int i = 0, j = 0; i < BUILDER_TROOPS.length; i++, j += i % COLUMNS == 0 ? 1 : 0)
            drawTroop(g2d, font, getBuilderTroopByName(builderTroops, BUILDER_TROOPS[i]), BUILDER_TROOPS[i], (i % COLUMNS) * 50 + 480, y + j * 50);
    }

    public static void drawSuperTroops(Graphics2D g2d, Font font, List<Troop> superTroops, int y) {
        for (int i = 0, j = 0; i < SUPER_TROOPS.length; i++, j += i % COLUMNS == 0 ? 1 : 0)
            drawSuperTroop(g2d, getTroopByName(superTroops, SUPER_TROOPS[i]), SUPER_TROOPS[i], (i % COLUMNS) * 50 + 710, y + j * 50);
    }

    public static void drawHeroes(Graphics2D g2d, Font font, List<Troop> heroes, int y) {
        for (int i = 0, j = 0; i < HEROES.length; i++, j += i % COLUMNS == 0 ? 1 : 0)
            drawTroop(g2d, font, getTroopByName(heroes, HEROES[i]), HEROES[i], (i % COLUMNS) * 50 + 250, y + j * 50);
    }

    public static void drawMachines(Graphics2D g2d, Font font, List<Troop> machines, int y) {
        for (int i = 0, j = 0; i < MACHINES.length; i++, j += i % COLUMNS == 0 ? 1 : 0)
            drawTroop(g2d, font, getTroopByName(machines, MACHINES[i]), MACHINES[i], (i % COLUMNS) * 50 + 480, y + j * 50);
    }

    public static void drawPets(Graphics2D g2d, Font font, List<Troop> pets, int y) {
        for (int i = 0, j = 0; i < PETS.length; i++, j += i % COLUMNS == 0 ? 1 : 0)
            drawTroop(g2d, font, getTroopByName(pets, PETS[i]), PETS[i], (i % COLUMNS) * 50 + 710, y + j * 50);
    }

    public static void execute(MessageReceivedEvent event, String... args) {
        MessageChannel channel = event.getChannel();

        Locale lang = LangUtils.getLanguage(event.getAuthor().getIdLong());
        ResourceBundle i18n = LangUtils.getTranslations(lang);
        NumberFormat nf = NumberFormat.getInstance(lang);

        // Checking rate limitation
        if (!CoreUtils.checkThrottle(event, lang))
            return;

        Player player = null;
        String tag = args.length > 0 ? args[0] : DatabaseUtils.getPlayerTag(event.getAuthor().getIdLong());

        if (tag == null) {
            ErrorUtils.sendError(channel, i18n.getString("set.player.error"), i18n.getString("set.player.help"));
            return;
        }

        try {
            player = ClashBotMain.clashAPI.getPlayer(tag);
        } catch (ClashAPIException | IOException e) {
            ErrorUtils.sendExceptionError(event, i18n, e, tag, "player");
            return;
        }

        // Initializing image
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = DrawUtils.initGraphics(WIDTH, HEIGHT, image);

        Font font = DrawUtils.getFont("Supercell.ttf").deriveFont(FONT_SIZE);
        g2d.setFont(font);

        g2d.drawImage(FileUtils.getImageFromFile("backgrounds/player-profile.png"), 0, 0, null);

        // Experience level
        g2d.drawImage(FileUtils.getImageFromFile("icons/exp-star.png"), 20, 18, 45, 45, null);
        Rectangle level = new Rectangle(23, 30, 40, 20);
        DrawUtils.drawCenteredString(g2d, level, font.deriveFont(FONT_SIZE + 5f), Objects.requireNonNull(player).getExpLevel().toString());

        // Nickname
        DrawUtils.drawShadowedString(g2d, player.getName(), 75, 36, FONT_SIZE + 8f);

        // Player tag
        DrawUtils.drawShadowedString(g2d, player.getTag(), 75, 55, FONT_SIZE - 1f);

        // Townhall
        g2d.drawImage(CacheComponents.getTownHallImage(player.getTownHallLevel()), 80, 80, 100, 100, null);
        DrawUtils.drawShadowedString(g2d, i18n.getString("townhall"), 25, 125, FONT_SIZE - 2f);
        DrawUtils.drawShadowedString(g2d, i18n.getString("level") + " " + player.getTownHallLevel(), 25, 150, FONT_SIZE + 8f);

        // Builder hall
        if (player.getBuilderHallLevel() != null) {
            g2d.drawImage(CacheComponents.getBuilderHallImage(player.getBuilderHallLevel()), 265, 85, 95, 95, null);
            DrawUtils.drawShadowedString(g2d, i18n.getString("level") + " " + player.getBuilderHallLevel(), 200, 150, FONT_SIZE + 8f);
        } else {
            // In case the player has not built the builder hall yet
            DrawUtils.drawShadowedString(g2d, i18n.getString("no.builderhall"), 200, 150, FONT_SIZE + 8f);
        }
        DrawUtils.drawShadowedString(g2d, i18n.getString("builderhall"), 200, 125, FONT_SIZE - 2f);

        // League
        if (player.getLeague() != null)
            g2d.drawImage(FileUtils.getImageFromUrl(player.getLeague().getIconUrls().getMedium()), 383, 30, 90, 90, null);
        else {
            g2d.drawImage(FileUtils.getImageFromFile("icons/noleague.png"), 383, 30, 90, 90, null);
            Rectangle noLeagueRect = new Rectangle(375, 60, 105, 20);
            DrawUtils.drawCenteredString(g2d, noLeagueRect, font.deriveFont(FONT_SIZE - 4f), i18n.getString("no.league"));
        }

        // Clan
        if (player.getClan() != null) {
            g2d.drawImage(FileUtils.getImageFromUrl(player.getClan().getBadgeUrls().getLarge()), 800, 30, 105, 105, null);

            Rectangle clanNameRect = new Rectangle(775, 130, 148, 30);
            Rectangle clanRoleRect = new Rectangle(775, 151, 148, 30);
            DrawUtils.drawCenteredString(g2d, clanNameRect, font.deriveFont(FONT_SIZE + 2f), player.getClan().getName());
            DrawUtils.drawCenteredString(g2d, clanRoleRect, font.deriveFont(FONT_SIZE - 2f), i18n.getString(player.getRole()));
        } else {
            Rectangle noClanRect = new Rectangle(775, 130, 148, 30);
            DrawUtils.drawCenteredString(g2d, noClanRect, font.deriveFont(FONT_SIZE + 2f), i18n.getString("no.clan"));
            g2d.drawImage(FileUtils.getImageFromFile("icons/noclan.png"), 812, 40, 75, 75, null);
        }

        // Trophies
        Rectangle trophiesRect = new Rectangle(375, 148, 75, 24);
        DrawUtils.drawCenteredString(g2d, trophiesRect, font.deriveFont(FONT_SIZE + 4f), nf.format(player.getTrophies()));

        // Statistics
        DrawUtils.drawShadowedString(g2d, i18n.getString("season") + " " + GameUtils.getCurrentSeason(lang), 486, 45, FONT_SIZE + 3f);
        DrawUtils.drawShadowedString(g2d, i18n.getString("attacks.won"), 486, 77, FONT_SIZE - 1.5f);
        DrawUtils.drawShadowedString(g2d, i18n.getString("defenses.won"), 486, 107, FONT_SIZE - 1.5f);
        DrawUtils.drawShadowedString(g2d, i18n.getString("donations"), 486, 143, FONT_SIZE - 1.5f);
        DrawUtils.drawShadowedString(g2d, i18n.getString("donations.received"), 486, 173, FONT_SIZE - 1.5f);

        DrawUtils.drawSimpleString(g2d, nf.format(player.getAttackWins()), 693, 79, FONT_SIZE, new Color(0x444545));
        DrawUtils.drawSimpleString(g2d, nf.format(player.getDefenseWins()), 693, 109, FONT_SIZE, new Color(0x444545));
        DrawUtils.drawSimpleString(g2d, nf.format(player.getDonations()), 693, 144, FONT_SIZE, new Color(0x444545));
        DrawUtils.drawSimpleString(g2d, nf.format(player.getDonationsReceived()), 693, 174, FONT_SIZE, new Color(0x444545));

        // Army
        DrawUtils.drawShadowedString(g2d, i18n.getString("troops"), 21, ARMY_TOPLINE - 12, FONT_SIZE + 2f);
        DrawUtils.drawShadowedString(g2d, i18n.getString("spells"), 251, ARMY_TOPLINE - 12, FONT_SIZE + 2f);
        DrawUtils.drawShadowedString(g2d, i18n.getString("label.builderbase"), 481, ARMY_TOPLINE - 12, FONT_SIZE + 2f);
        DrawUtils.drawShadowedString(g2d, i18n.getString("super.troops"), 711, ARMY_TOPLINE - 12, FONT_SIZE + 2f);
        DrawUtils.drawShadowedString(g2d, i18n.getString("heroes"), 251, ARMY_BOTLINE - 12, FONT_SIZE + 2f);
        DrawUtils.drawShadowedString(g2d, i18n.getString("machines"), 481, ARMY_BOTLINE - 12, FONT_SIZE + 2f);
        DrawUtils.drawShadowedString(g2d, i18n.getString("pets"), 711, ARMY_BOTLINE - 12, FONT_SIZE + 2f);

        // Troops
        List<Troop> troops = player.getTroops();
        List<Troop> heroes = player.getHeroes();
        List<Troop> spells = player.getSpells();

        drawTroops(g2d, font, troops, ARMY_TOPLINE);
        drawSpells(g2d, font, spells, ARMY_TOPLINE);
        drawBuilderTroops(g2d, font, troops, ARMY_TOPLINE);
        drawSuperTroops(g2d, font, troops, ARMY_TOPLINE);

        drawHeroes(g2d, font, heroes, ARMY_BOTLINE);
        drawMachines(g2d, font, troops, ARMY_BOTLINE);
        drawPets(g2d, font, troops, ARMY_BOTLINE);

        FileUtils.sendImage(event, image, "player", "png");

        g2d.dispose();
    }
}
