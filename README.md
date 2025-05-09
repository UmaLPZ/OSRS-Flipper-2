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
    <img src="https://i.imgur.com/Qr9MzrW.png" max-width="600px">
</p>

Track Sells:

<p>
    <img src="https://i.imgur.com/WHe5TjL.png" max-width="600px">
</p>

Track Flips:

<p>
    <img src="https://i.imgur.com/NdT5GQz.png" max-width="600px">
</p>

View In-Progress Offers:

<p>
    <img src="https://i.imgur.com/G9PfDi9.png" max-width="600px">
</p>

## Changelog

v1.0.2 <br />
- Fixed In Progress Panel displaying the wrong offer type
- Fixed In Progress Panel showing "Spent/Spent Per" instead of "Received/Received Per" on Sell offers
- Removed unnecessary debug boolean option

v1.0.1 <br />

- Plugin added


