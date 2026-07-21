package com.flipper2.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import com.flipper2.helpers.GrandExchange;
import com.flipper2.helpers.Log;
import com.flipper2.helpers.Persistor;
import com.flipper2.helpers.UiUtilities;
import com.flipper2.models.Flip;
import com.flipper2.models.Transaction;
import com.flipper2.views.components.Pagination;
import com.flipper2.views.flips.FlipPage;
import com.flipper2.views.flips.FlipPanel;
import com.flipper2.FlipperConfig;

import lombok.Getter;
import lombok.Setter;

import java.awt.BorderLayout;

import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

public class FlipsController
{
	@Getter
	@Setter
	private List<Flip> flips = new ArrayList<Flip>();
	private List<Flip> filteredFlips = new ArrayList<Flip>();
	private FlipPage flipPage;
	private Consumer<UUID> removeFlipConsumer;
	private Runnable refreshFlipsRunnable;
	private String totalProfit = "0";
	private Pagination pagination;
	private String searchText;
	private ItemManager itemManager;
	private Consumer<String> onSearchTextChangedCallback;
	private boolean isTrackingFlips = true;
	private ClientThread cThread;


	private Consumer<Flip> onFlipRemovedCallback;

	public FlipsController(ItemManager itemManager, FlipperConfig config, ClientThread cThread) throws IOException
	{
		this.itemManager = itemManager;
		this.cThread = cThread;
		this.removeFlipConsumer = id -> this.removeFlip(id);
		this.refreshFlipsRunnable = () -> this.loadFlips();
		this.onSearchTextChangedCallback = (searchText) -> this.onSearchTextChanged(searchText);

		this.flipPage = new FlipPage(
			this.onSearchTextChangedCallback,
			this::toggleIsTrackingFlips,
			this.isTrackingFlips
		);

		Consumer<Object> renderItemCallback = (Object flip) -> {
			FlipPanel flipPanel = new FlipPanel(
				(Flip) flip,
				itemManager,
				this.removeFlipConsumer,
				config.isPromptDeleteFlip()
			);
			this.flipPage.addFlipPanel(flipPanel);
		};

		Runnable buildViewCallback = () -> this.buildView();

		this.pagination = new Pagination(renderItemCallback, UiUtilities.ITEMS_PER_PAGE, buildViewCallback);
		this.loadFlips();
	}

	public void setOnFlipRemovedCallback(Consumer<Flip> callback)
	{
		this.onFlipRemovedCallback = callback;
	}

	private void toggleIsTrackingFlips()
	{
		this.isTrackingFlips = !this.isTrackingFlips;
	}

	public void onSearchTextChanged(String searchText)
	{
		this.searchText = searchText;
		this.pagination.resetPage();
		this.buildView();
	}

	public void addFlip(Flip flip)
	{
		if (this.isTrackingFlips)
		{
			this.flips.add(0, flip);
			this.totalProfit = calculateTotalProfit(flips);
			Persistor.saveFlips(this.flips);
			getFlipNamesAndBuild();
		}
	}

	public void removeFlip(UUID flipId)
	{
		Iterator<Flip> flipsIter = this.flips.iterator();
		while (flipsIter.hasNext())
		{
			Flip flip = flipsIter.next();
			if (flip.getFlipId().equals(flipId))
			{
				flipsIter.remove();
				this.totalProfit = calculateTotalProfit(flips);
				Persistor.saveFlips(this.flips);


				if (this.onFlipRemovedCallback != null)
				{
					this.onFlipRemovedCallback.accept(flip);
				}

				this.buildView();
				return;
			}
		}
	}

	public FlipPage getPage()
	{
		return this.flipPage;
	}

	public void loadFlips()
	{
		try
		{
			this.flips = Persistor.loadFlips();
			this.totalProfit = calculateTotalProfit(flips);
			this.filteredFlips = new ArrayList<>(this.flips);
			getFlipNamesAndBuild();
		}
		catch (IOException e)
		{
			Log.info("Failed to load flips from file: " + e.getMessage());
			this.flips = new ArrayList<>();
			this.filteredFlips = new ArrayList<>();
		}
	}

	public void getFlipNamesAndBuild()
	{
		cThread.invoke(() -> {
			for (Flip flip : flips)
			{
				if (flip.getItemName() == null)
				{
					flip.setItemName(itemManager.getItemComposition(flip.getItemId()).getName());
				}
			}
			this.buildView();
		});
	}

	private String calculateTotalProfit(List<Flip> flipList)
	{
		long total = 0;
		for (Flip flip : flipList)
		{
			total += flip.getTotalProfit();
		}
		return String.valueOf(total);
	}

	private boolean isRender(Flip flip)
	{
		if (flip.getItemName() == null)
		{
			ItemComposition itemComp = itemManager.getItemComposition(flip.getItemId());
			flip.setItemName(itemComp.getName());
		}
		String itemName = flip.getItemName();

		if (this.searchText != null &&
			itemName.toLowerCase().contains(this.searchText.toLowerCase()))
		{
			return true;
		}
		else if (this.searchText != null && !this.searchText.isEmpty())
		{
			return false;
		}

		return true;
	}

	private void removeFlipsBySellId(UUID sellId, List<Transaction> buys)
	{
		Iterator<Flip> it = flips.iterator();
		while (it.hasNext())
		{
			Flip flip = it.next();
			if (flip.getSellId().equals(sellId))
			{
				for (Transaction buy : buys)
				{
					if (buy.getId().equals(flip.getBuyId()))
					{
						buy.setFlippedQuantity(Math.max(0, buy.getFlippedQuantity() - flip.getQuantity()));
						buy.setIsFlipped(false);
					}
				}
				it.remove();
			}
		}
	}

	public void upsertFlip(Transaction sell, List<Transaction> buys)
	{
		if (sell.isBuy()) return;

		if (sell.isFlipped())
		{
			removeFlipsBySellId(sell.getId(), buys);
			sell.setIsFlipped(false);
		}

		if (this.isTrackingFlips)
		{
			int remainingToFlip = sell.getFinQuantity();
			ListIterator<Transaction> buysIterator = buys.listIterator();

			while (buysIterator.hasNext() && remainingToFlip > 0)
			{
				Transaction buy = buysIterator.next();

				if (GrandExchange.checkIsSellAFlipOfBuy(sell, buy))
				{
					int availableInBuy = buy.getFinQuantity() - buy.getFlippedQuantity();
					int amountToTake = Math.min(remainingToFlip, availableInBuy);

					if (amountToTake > 0)
					{
						Flip flip = new Flip(buy, sell, amountToTake);
						this.addFlip(flip);

						buy.setFlippedQuantity(buy.getFlippedQuantity() + amountToTake);
						if (buy.getFlippedQuantity() >= buy.getFinQuantity())
						{
							buy.setIsFlipped(true);
						}
						remainingToFlip -= amountToTake;
					}
				}
			}

			if (remainingToFlip < sell.getFinQuantity())
			{
				sell.setIsFlipped(true);
			}
		}
	}

	public void filterList()
	{
		if (this.searchText == null || this.searchText.isEmpty())
		{
			this.filteredFlips = new ArrayList<>(this.flips);
		}
		else
		{
			this.filteredFlips = new ArrayList<>();
			for (Flip flip : this.flips)
			{
				if (isRender(flip))
				{
					filteredFlips.add(flip);
				}
			}
		}
	}

	public void buildView()
	{
		SwingUtilities.invokeLater(() -> {
			this.filterList();
			this.flipPage.resetContainer(isTrackingFlips);
			this.flipPage.add(
				this.pagination.getComponent(this.filteredFlips),
				BorderLayout.SOUTH
			);
			this.pagination.renderFromBeginning(this.filteredFlips);
			this.flipPage.setTotalProfit(totalProfit);
			this.flipPage.revalidate();
			this.flipPage.repaint();
		});
	}

	public void setRefreshFlipsRunnable(Runnable runnable)
	{
		this.refreshFlipsRunnable = runnable;
		this.flipPage.setRefreshFlipsRunnable(runnable);
	}

	public void repairFlips(List<Transaction> allBuys, List<Transaction> allSells)
	{

		Map<String, Flip> oldFlipsMap = new HashMap<>();
		for (Flip f : this.flips)
		{
			String uniqueKey = f.getBuyId().toString() + "_" + f.getSellId().toString();
			oldFlipsMap.put(uniqueKey, f);
		}

		this.flips.clear();
		this.filteredFlips.clear();

		for (Transaction b : allBuys)
		{
			b.setIsFlipped(false);
			b.setFlippedQuantity(0);
		}
		for (Transaction s : allSells)
		{
			s.setIsFlipped(false);
			s.setFlippedQuantity(0);
		}

		List<Transaction> sortedSellsForMatching = new ArrayList<>(allSells);
		sortedSellsForMatching.sort((a, b) -> a.getCreatedTime().compareTo(b.getCreatedTime()));

		for (Transaction sell : sortedSellsForMatching)
		{
			if (sell.isBuy()) continue;

			int remainingToFlip = sell.getFinQuantity();
			for (int i = 0; i < allBuys.size() && remainingToFlip > 0; i++)
			{
				Transaction buy = allBuys.get(i);
				String uniqueKey = buy.getId().toString() + "_" + sell.getId().toString();
				boolean isHistoricalFlip = oldFlipsMap.containsKey(uniqueKey);


				if (isHistoricalFlip || GrandExchange.checkIsSellAFlipOfBuy(sell, buy))
				{
					int availableInBuy = buy.getFinQuantity() - buy.getFlippedQuantity();
					int amountToTake = Math.min(remainingToFlip, availableInBuy);

					if (amountToTake > 0)
					{
						Flip flip = new Flip(buy, sell, amountToTake);


						if (isHistoricalFlip)
						{
							Flip old = oldFlipsMap.get(uniqueKey);
							flip.setFlipId(old.getFlipId());


							long timeDifferenceSeconds = Math.abs(old.getCreatedAt().getEpochSecond() - sell.getCreatedTime().getEpochSecond());
							if (timeDifferenceSeconds <= 86400)
							{
								flip.setCreatedAt(old.getCreatedAt());
								flip.setUpdatedAt(old.getUpdatedAt());
							}
							else
							{
								flip.setCreatedAt(sell.getCreatedTime());
								flip.setUpdatedAt(sell.getCreatedTime());
							}
						}

						this.flips.add(0, flip);

						buy.setFlippedQuantity(buy.getFlippedQuantity() + amountToTake);
						if (buy.getFlippedQuantity() >= buy.getFinQuantity())
						{
							buy.setIsFlipped(true);
						}
						remainingToFlip -= amountToTake;
					}
				}
			}
			if (remainingToFlip < sell.getFinQuantity())
			{
				sell.setIsFlipped(true);
			}
		}

		this.totalProfit = calculateTotalProfit(this.flips);
		this.filteredFlips = new ArrayList<>(this.flips);


		Persistor.saveFlips(this.flips);
		Persistor.saveBuys(allBuys);
		Persistor.saveSells(allSells);

		getFlipNamesAndBuild();
	}

	public void saveTransactions()
	{
		Persistor.saveFlips(this.flips);
	}
}