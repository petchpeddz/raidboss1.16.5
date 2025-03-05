package ca.landonjw.remoraids.implementation.ui.creators;

import java.util.Collection;

import javax.annotation.Nonnull;

import ca.landonjw.remoraids.RemoRaids;
import ca.landonjw.remoraids.api.rewards.contents.IRewardContent;
import ca.landonjw.remoraids.api.ui.IBossUI;
import ca.landonjw.remoraids.api.ui.ICreatorUI;
import ca.landonjw.remoraids.implementation.rewards.contents.CurrencyContent;
import ca.landonjw.remoraids.internal.api.config.Config;
import ca.landonjw.remoraids.internal.config.MessageConfig;
import ca.landonjw.remoraids.internal.inventory.api.InventoryAPI;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * The {@link ICreatorUI} used for creating a new {@link CurrencyContent}.
 *
 * @author landonjw
 * @since 1.0.0
 */
public class CurrencyContentCreator implements ICreatorUI<IRewardContent> {

	/** {@inheritDoc} */
	@Override
	public void open(@Nonnull IBossUI source, @Nonnull EntityPlayerMP player, @Nonnull Collection<IRewardContent> toAddTo) {
		new Creator(source, player, toAddTo);
	}

	/** {@inheritDoc} */
	@Override
	public ItemStack getCreatorIcon() {
		return new ItemStack(Items.GOLD_NUGGET);
	}

	/** {@inheritDoc} */
	@Override
	public String getCreatorTitle() {
		Config config = RemoRaids.getMessageConfig();
		return config.get(MessageConfig.UI_ADD_REWARD_CONTENT_CURRENCY);
	}

	class Creator {

		/** The source that opened this creator. */
		private IBossUI source;
		/** The player that the user interface is intended for. */
		private EntityPlayerMP player;
		/** The collection of reward contents to append creation to. */
		private Collection<IRewardContent> toAddTo;

		/**
		 * Constructor for the creator user interface.
		 *
		 * @param source  source that opened this creator
		 * @param player  player that user interface is intended for
		 * @param toAddTo collection of reward contents to append creation to
		 */
		public Creator(IBossUI source, EntityPlayerMP player, Collection<IRewardContent> toAddTo) {
			this.source = source;
			this.player = player;
			this.toAddTo = toAddTo;

			InventoryAPI.getInstance().closePlayerInventory(player);
			player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Enter an amount (ie. '100') or 'cancel' to cancel!"));
			MinecraftForge.EVENT_BUS.register(this);
		}

		/**
		 * Used to receive message from player to determine the amount of money for the reward content.
		 *
		 * @param event called when a player sends message on server
		 */
		@SubscribeEvent
		public void onMessage(ServerChatEvent event) {
			if (event.getPlayer().equals(player)) {
				event.setCanceled(true);
				String message = event.getMessage();
				if (!message.equalsIgnoreCase("cancel")) {
					try {
						int amount = Integer.parseInt(message);

						if (amount > 0) {
							CurrencyContent content = new CurrencyContent(amount);
							toAddTo.add(content);
							source.getSource().get().open();
							MinecraftForge.EVENT_BUS.unregister(this);
						} else {
							player.sendMessage(new TextComponentString(TextFormatting.RED + "Incorrect amount. Try again."));
						}
					} catch (NumberFormatException e) {
						player.sendMessage(new TextComponentString(TextFormatting.RED + "Incorrect amount. Try again."));
					}
				} else {
					source.getSource().get().open();
					MinecraftForge.EVENT_BUS.unregister(this);
				}
			}
		}

	}

}
