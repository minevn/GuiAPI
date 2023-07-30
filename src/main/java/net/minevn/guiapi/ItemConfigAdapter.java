package net.minevn.guiapi;

import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemConfigAdapter {

	private static final boolean SAVE_RAW = false;

	public static ItemStack adapt(ConfigurationSection cf) {
		Validate.notNull(cf);
		if (cf.isItemStack("item")) {
			return cf.getItemStack("item");
		}
		Validate.isTrue(cf.contains("material"));
		Material material = cf.isInt("material") ? XMaterial.matchXMaterial(cf.getInt("material"), (byte) 0).orElse(XMaterial.AIR).parseMaterial()
				: XMaterial.matchXMaterial(cf.getString("material")).orElse(XMaterial.AIR).parseMaterial();
		Validate.isTrue(material != null && material != Material.AIR, "Cannot match material " + cf.getString("material"));
		int data = cf.isInt("data")
			? cf.getInt("data")
			: 0;
		int amount = cf.isInt("amount")
			? cf.getInt("amount")
			: 1;
		ItemStack item = new ItemStack(material, amount, (short) data);
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setUnbreakable(cf.getBoolean("unbreaking", false));
			if (cf.isString("name")) {
				meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', cf.getString("name")));
			}
			if (cf.isList("lore")) {
				List<String> lore = new ArrayList<>();
				for (String s : cf.getStringList("lore")) {
					lore.add(ChatColor.translateAlternateColorCodes('&', s));
				}
				meta.setLore(lore);
			}
			if (cf.isConfigurationSection("enchant")) {
				ConfigurationSection enchant = cf.getConfigurationSection("enchant");
				Set<String> keys;
				if (enchant != null) {
					keys = enchant.getKeys(false);
					for (String k : keys) {
						Enchantment enchantment = Enchantment.getByName(k.toUpperCase());
						if (enchantment != null && enchant.isInt(k)) {
							meta.addEnchant(enchantment, enchant.getInt(k), false);
						}
					}
				}
			}
			if (cf.getBoolean("glow", false)) {
				if (!meta.hasEnchants() && !meta.hasEnchant(Enchantment.DURABILITY)) {
					meta.addEnchant(Enchantment.DURABILITY, 1, true);
				}
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
		}
		item.setItemMeta(meta);
		return item;
	}

	public static ConfigurationSection toConfig(ItemStack item) {
		Validate.notNull(item);
		ConfigurationSection cf = new YamlConfiguration();
		if (SAVE_RAW) {
			cf.set("item", item);
			return cf;
		}
		cf.set("material", item.getType().name().toLowerCase());
		int data = item.getDurability();
		if (data != 0) {
			cf.set("data", data);
		}
		cf.set("amount", item.getAmount());
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			if (meta.hasLore()) cf.set("lore", meta.getLore().toArray(new String[0]));
			if (meta.hasDisplayName()) cf.set("name", meta.getDisplayName());
			if (meta.isUnbreakable()) {
				cf.set("unbreaking", true);
			}
		}
		item.getEnchantments().forEach((k, v) -> cf.set("enchant." + k.getName(), v));
		return cf;
	}

}
