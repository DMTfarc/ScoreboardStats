package com.github.games647.scoreboardstats.variables.defaults;

import com.github.games647.scoreboardstats.variables.ReplaceEvent;
import com.github.games647.scoreboardstats.variables.ReplaceManager;
import com.github.games647.scoreboardstats.variables.VariableReplaceAdapter;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelChangeEvent;
import com.gmail.nossr50.util.player.UserManager;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Replace all variables that are associated with the mcMMO plugin
 *
 * http://dev.bukkit.org/bukkit-plugins/mcmmo/
 */
public class McmmoVariables extends VariableReplaceAdapter<Plugin> implements Listener {

    private static String[] getSkillVariables() {
        final List<String> skills = Lists.newArrayList();
        for (SkillType type : SkillType.values()) {
            final String skillName = type.name().toLowerCase(Locale.ENGLISH);
            skills.add(skillName);
        }

        skills.add("powlvl");
        return skills.toArray(new String[skills.size()]);
    }

    private final ReplaceManager replaceManager;

    /**
     * Creates a new mcMMO replacer. This also validates if all variables are available
     * and can be used in the runtime.
     *
     * @param replaceManager to update the variables by event
     */
    public McmmoVariables(ReplaceManager replaceManager) {
        super(Bukkit.getPluginManager().getPlugin("mcMMO"), getSkillVariables());

        this.replaceManager = replaceManager;
    }

    @Override
    public void onReplace(Player player, String variable, ReplaceEvent replaceEvent) {
        replaceEvent.setConstant(true);
        if (!UserManager.hasPlayerDataKey(player)) {
            //check if player is loaded
            return;
        }

        if ("powlvl".equals(variable)) {
            replaceEvent.setScore(ExperienceAPI.getPowerLevel(player));
        } else {
            final String type = variable.toUpperCase(Locale.ENGLISH);
            replaceEvent.setScore(ExperienceAPI.getLevel(player, type));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLevelUp(McMMOPlayerLevelChangeEvent levelChangeEvent) {
        final Player player = levelChangeEvent.getPlayer();
        final SkillType skill = levelChangeEvent.getSkill();
        final int newSkillLevel = levelChangeEvent.getSkillLevel();

        replaceManager.updateScore(player, skill.getName(), newSkillLevel);
        replaceManager.updateScore(player, "powlvl", ExperienceAPI.getPowerLevel(player));
    }
}
