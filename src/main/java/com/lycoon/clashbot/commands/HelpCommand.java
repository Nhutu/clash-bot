package com.lycoon.clashbot.commands;

import com.lycoon.clashbot.lang.LangUtils;
import com.lycoon.clashbot.utils.CoreUtils;
import com.lycoon.clashbot.utils.DatabaseUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ResourceBundle;

public class HelpCommand
{
    private static String prefix;
    private static ResourceBundle i18n;
    private static EmbedBuilder builder;

    public static void dispatch(MessageReceivedEvent event, String... args)
    {
        execute(event);
    }

    public static void drawCategory(CommandCategory category, Command[] commands)
    {
        StringBuilder categoryField = new StringBuilder();
        for (Command cmd : commands)
            if (cmd.getCategory().equals(category)) {
                categoryField.append("▫ `").append(cmd.formatFullCommand(prefix)).append("` ");
                categoryField.append(i18n.getString(cmd.getDescription())).append("\n");
            }
        builder.addField(i18n.getString(category.toString()), categoryField.toString(), false);
    }

    public static void execute(MessageReceivedEvent event)
    {
        i18n = LangUtils.getTranslations(event.getAuthor().getIdLong());
        prefix = DatabaseUtils.getServerPrefix(event.getGuild().getIdLong());
        builder = new EmbedBuilder();

        builder.setColor(Color.GRAY);
        builder.setTitle(i18n.getString("cmd.help.panel"));
        builder.appendDescription(i18n.getString("tip.help"));

        CommandCategory[] categories = CommandCategory.values();
        for (CommandCategory category : categories)
            drawCategory(category, Command.values());

        CoreUtils.sendMessage(event, i18n, builder);
    }
}
