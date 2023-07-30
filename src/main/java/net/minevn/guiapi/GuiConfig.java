package net.minevn.guiapi;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class GuiConfig {

	private final ConfigurationSection dataMap;
	private final LoadingCache<String, ItemStack> iconCache = CacheBuilder.newBuilder()
			.weakValues()
			.build(new CacheLoader<>() {
				@Override
				public ItemStack load(String s) {
					return ItemConfigAdapter.adapt(dataMap.getConfigurationSection(s.toLowerCase()));
				}
			});
	private final LoadingCache<String, String> langCache = CacheBuilder.newBuilder()
			.weakValues()
			.build(new CacheLoader<>() {
				@Override
				public String load(String s) {
					String lang = dataMap.getString(s.toLowerCase());
					if (lang == null || lang.isEmpty()) {
						lang = String.join(", ", dataMap.getKeys(true));
					}
					return ChatColor.translateAlternateColorCodes('&', lang);
				}
			});

	public GuiConfig(ConfigurationSection dataMap) {
		this.dataMap = dataMap;
	}

	public ItemStack[] getBackground() {
		String type = getRawString("background.type");

		switch (type) {
			case "CHARACTER_MAP":
				return getCharacterMapBackground();
			default:
				return new ItemStack[0];
		}
	}

	public ConfigurationSection getRaw() {
		return dataMap;
	}

	private ItemStack[] getCharacterMapBackground() {
		Map<Character, ItemStack> itemMap = Maps.newHashMap();

		ConfigurationSection itemDataMap = dataMap.getConfigurationSection("background.items");
		itemDataMap.getKeys(false).forEach(key -> itemMap.put(key.charAt(0), getItem("background.items." + key)));

		List<String> contentStr = dataMap.getStringList("background.map");
		ItemStack[] result = new ItemStack[contentStr.stream().mapToInt(String::length).sum()];

		int index = 0;
		for (String s : contentStr) {
			for (char c : s.toCharArray()) {
				result[index++] = itemMap.get(c);
			}
		}

		return result;
	}

	public ItemStack getItem(String id) {
		if (!dataMap.contains(id)) return null;
		return iconCache.getUnchecked(id);
	}

	public int getIconSlot(String id) {
		return dataMap.getInt(id + ".slot", 0);
	}

	public int getInt(String id) {
		return dataMap.getInt(id);
	}

	public String getRawString(String id) {
		return dataMap.getString(id);
	}

	public String getString(String id) {
		return langCache.getUnchecked(id);
	}
}

