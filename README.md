AtmosphericHeights - a stratified atmosphere for your world

Adds new elevation-based regions (troposphere, mesosphere, magnetosphere, and outerspace)
to your planet with different properties.

***[AtmosphericHeights 1.2](http://dev.bukkit.org/server-mods/atmosphericheights/files/3-atmospheric-heights-1-2/)*** - released 2012/05/20 for 1.2.5-R2.0

Features:

* Thinner air
* No air
* Oxygen masks
* Cosmic rays 
* Spacesuits
* Outerspace
* Flexible configuration

## Usage

Travel *very* high up. I suggest using
Boots + Power from [EnchantMore](http://dev.bukkit.org/server-mods/enchantmore/),
but you can use any plugin or flying mod capable of extreme heights, it is up to you.

Above 128 meters, the air is thinner therefore your hunger will deplete in larger amounts.
This elevation is above the clouds in Minecraft 1.2.3+, so the increased hunger adds a downside to building huge
sky fortresses.

Above 256 meters, you will start to suffocate from lack of oxygen, with a larger damage the higher you go.
Better bring your oxygen mask: a helmet with at least the respiration enchantment
will prevent you from receiving suffocation damage.

Above 512 meters, the magnetic field engulfing the planet is no longer present and you will
be exposed to dangerous cosmic rays - with a chance to set you on fire. You can wear a spacesuit
(currently just a full suit of armor) for protection and it will absorb the damage instead.

Above 1024 meters, all you'll get is a message saying you entered outerspace.


## Configuration

**verbose** (true): Log debugging information.

*Per-world configuration options*: change 'world' to your world name, copying
the section as needed for multiple worlds. If a world is omitted, the
defaults will be used for each configuration option.

**tropopause** (128): Above this elevation, hunger will be affected.
(Disclaimer: the terminology used in this plugin is not scientifically accurate. FYI.)

**hungerPerMeter**: Scale hunger by this amount for each meter above the tropopause.



**mesopause** (256): Above this elevation, suffocation has a chance to occur
every time you move.

**damageChance**: Whenever the player moves, there is a 1:n chance for
receiving suffocation damage.

**damagePerMeter**: Scale damage by this amount for each meter above the mesopause.

**damageMax**: Maximum damage (in half hearts) to deliver from suffocation.

**damageHealthMin**: Do not suffocate the player if their health is below this
amount. Set to 0 to allow suffocation to kill the player.

**oxygenMaskEnabled** (true): Prevent suffocation damage when player is wearing
Helmet + Respiration, of at least the minimum level given below.

**oxygenMaskMinLevel**  (1): Minimum enchantment level for Respiration to act
as an oxygen mask. This defaults to I, meaning the enchantment can be legitimately
acquired at an enchantment table, but you can set it to a higher value if you want
to better control access to oxygen masks (provide through crafting, shops, etc.).



**magnetopause** (512): Above this elevation, player can be spontaneously set on fire.

**fireChance**: Whenever a player moves, there is a 1:n chance to be set aflame.

**fireTicks**: Duration in ticks for the fire to last.

**spacesuitEnabled** (true): Allow player to wear a spacesuit for protection from cosmic rays.

**spacesuitDamagePerHit**: Each time the player was about to be hit by a cosmic ray,
absorb this damage in a random piece of their spacesuit.


**kalmanLine** (1024): Above this elevation, player will receive a message congratulating
them for legally entering outerspace. Note: this doesn't mean they've escaped the graviational pull!

**kalmanLineMessage**: Message to show to players who enter outerspace. Will be shown
at most once per player login.


## Permissions and Commands

None

## See Also

Want more realism but in a completely different context? Also see 
[RealisticChat](http://dev.bukkit.org/server-mods/realisticchat/).

Inspired by [this post on /r/bukkitplugins](http://www.reddit.com/r/bukkitplugins/comments/r2a0u/thought_i_would_give_the_new_subreddit_a_whirl/).

***[Fork me on GitHub](https://github.com/mushroomhostage/AtmosphericHeights)***

