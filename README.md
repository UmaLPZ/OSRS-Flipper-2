# Flipper 2 plugin for RuneLite

Plugin for RuneLite to track buys, sells, flips, and in-progress offers forked from the original [Flipper Plugin by OkayestDev](https://github.com/OkayestDev/OSRS-Flipper). I also included a tax feature (with some modifications) initially created by [StrawberrySandwich](https://github.com/StrawberrySandwich/OSRS-Flipper/tree/feature/add-tax) that was never added to the original Flipper plugin but seemed too helpful not to include. This plugin also includes some code from the Grand Exchange plugin for the In-Progress tab.

I removed the connection to the Flipper website since it has also gone down and this plugin will not pull any item data from the wiki. Because there is no data from the wiki I also removed the high-alch function.
All data is pulled from the RuneLite api and everything is done and stored locally in some JSON files.

Unfortunetly the original Flipper JSON files are not compatable with this plugin. This version relabled some values as well as added a couple new ones. Trying to use old JSON files will likely lead to the the plugin breaking.

## Support
There will be a lot of bugs because I am not that great at programming. It should work but there might be some issues due to my limited testing.
<br />

If you notice any bugs or have any suggestions please let me know by making an [issue](https://github.com/UmaLPZ/OSRS-Flipper-2/issues). Please include a detailed description of the issue and a screenshot if possible.

## Features

Track Buys:

<p>
    <img src="https://i.imgur.com/aGH7ugM.png" max-width="600px">
</p>

Track Sells:

<p>
    <img src="https://i.imgur.com/eFbO9Qa.png" max-width="600px">
</p>

Track Flips:

<p>
    <img src="https://i.imgur.com/RQb9OQb.png" max-width="600px">
</p>

View In-Progress Offers:

<p>
    <img src="https://i.imgur.com/TXW79Hl.png" max-width="600px">
</p>

## Changelog

v1.0.6 <br />
- Changed Flip panel layout to 3 columns
- Fixed Sell panel horizontal overflow
- Fixed Sell panel showing incorrect value after tax

v1.0.5 <br />
- Switched to 3 separate columns instead of two for buy/sell/in progress tabs
- Changed formatting for a lot of labels and values
- Changed progress bars
- Renamed some things
- Added bottom line border

v1.0.4 <br />
- truncated item names if they are longer than 20 characters
- used different colors for the progress bar
- changed coding format

v1.0.3 <br />
- Fixed In Progress panel overflowing causing horizontal scroll
- Shortened "Received" to "Rcvd" to prevent overflow
- Changed text to "Spent/Per" and "Rcvd/Per"
- Changed "Total Value" to "Offer Value"

v1.0.2 <br />
- Fixed In Progress Panel displaying the wrong offer type
- Fixed In Progress Panel showing "Spent/Spent Per" instead of "Received/Received Per" on Sell offers
- Removed unnecessary debug boolean option

v1.0.1 <br />

- Plugin added


