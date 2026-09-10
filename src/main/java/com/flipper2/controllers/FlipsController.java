package com.flipper2.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import com.flipper2.helpers.DataMigrator;
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

import java.awt.BorderLayout;

import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

public class FlipsController
{
	@Getter
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

	private Supplier<List<Transaction>> buysSupplier;
	private Runnable buysSaveCallback;
	private Supplier<List<Transaction>> sellsSupplier;
	private Runnable sellsSaveCallback;

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

	public void setTransactionAccess(
		Supplier<List<Transaction>> buysSupplier,
		Runnable buysSaveCallback,
		Supplier<List<Transaction>> sellsSupplier,
		Runnable sellsSaveCallback)
	{
		this.buysSupplier = buysSupplier;
		this.buysSaveCallback = buysSaveCallback;
		this.sellsSupplier = sellsSupplier;
		this.sellsSaveCallback = sellsSaveCallback;
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

				decrementLinkedBuy(flip);
				decrementLinkedSell(flip);

				this.totalProfit = calculateTotalProfit(flips);
				Persistor.saveFlips(this.flips);

				if (this.buysSaveCallback != null)
				{
					this.buysSaveCallback.run();
				}
				if (this.sellsSaveCallback != null)
				{
					this.sellsSaveCallback.run();
				}

				this.buildView();
				return;
			}
		}
	}

	private void decrementLinkedBuy(Flip flip)
	{
		if (this.buysSupplier == null)
		{
			return;
		}

		for (Transaction buy : this.buysSupplier.get())
		{
			if (buy.getId().equals(flip.getBuyId()))
			{
				int newFlippedQuantity = Math.max(0, buy.getFlippedQuantity() - flip.getQuantity());
				buy.setFlippedQuantity(newFlippedQuantity);
				break;
			}
		}
	}

	private void decrementLinkedSell(Flip flip)
	{
		if (this.sellsSupplier == null)
		{
			return;
		}

		for (Transaction sell : this.sellsSupplier.get())
		{
			if (sell.getId().equals(flip.getSellId()))
			{
				int newFlippedQuantity = Math.max(0, sell.getFlippedQuantity() - flip.getQuantity());
				sell.setFlippedQuantity(newFlippedQuantity);
				break;
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

		if (this.searchText == null || this.searchText.isEmpty())
		{
			return true;
		}

		return flip.getItemName().toLowerCase().contains(this.searchText.toLowerCase());
	}

	private boolean removeFlipsBySellId(UUID sellId, List<Transaction> buys)
	{
		boolean removedAny = false;
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
					}
				}
				it.remove();
				removedAny = true;
			}
		}
		return removedAny;
	}

	public void upsertFlip(Transaction sell, List<Transaction> buys)
	{
		if (sell.isBuy())
		{
			return;
		}

		boolean removedExistingFlips = false;
		if (sell.getFlippedQuantity() > 0)
		{
			removedExistingFlips = removeFlipsBySellId(sell.getId(), buys);
			sell.setFlippedQuantity(0);
		}

		boolean addedNewFlip = false;

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
						addedNewFlip = true;

						buy.setFlippedQuantity(buy.getFlippedQuantity() + amountToTake);
						sell.setFlippedQuantity(sell.getFlippedQuantity() + amountToTake);

						remainingToFlip -= amountToTake;
					}
				}
			}
		}

		if (removedExistingFlips && !addedNewFlip)
		{
			this.totalProfit = calculateTotalProfit(flips);
			Persistor.saveFlips(this.flips);
			this.buildView();
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

		this.flips = DataMigrator.repairFlips(allBuys, allSells, this.flips);

		this.totalProfit = calculateTotalProfit(this.flips);
		this.filteredFlips = new ArrayList<>(this.flips);


		Persistor.saveFlips(this.flips);

		getFlipNamesAndBuild();
	}

	public void saveTransactions()
	{
		Persistor.saveFlips(this.flips);
	}
}