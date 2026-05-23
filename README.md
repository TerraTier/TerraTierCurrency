# TerraTierCurrency

TerraTierCurrency is a lightweight, configurable currency plugin for Paper (Minecraft). It supports multiple currency types, per-player balances, in-game commands to view and modify balances, WorldGuard total-coin gates, and YAML-based persistence.

## Features
- Multiple currency types (configured in `config.yml`)
- Built-in `coins` and `total-coins` balances
- Commands: `/terratiercurrency` and `/ttc` (aliases: `/balance`, `/coins`)
- Subcommands: `add`, `subtract`, `set`, `view` with tab-completion
- WorldGuard flag: `required-total-coins`
- Balances persisted to `plugins/TerraTierCurrency/balances.yml`

## Usage
- `/terratiercurrency` or `/ttc` - show your balances (lists all configured currencies)
- `/terratiercurrency view <player>` - view another player's balances
- `/terratiercurrency add <player> [currency] <amount>` - add currency (permission: `terratier.currency.admin`)
- `/terratiercurrency subtract <player> [currency] <amount>` - subtract currency (permission: `terratier.currency.admin`)
- `/terratiercurrency set <player> [currency] <amount>` - set currency to an exact amount (permission: `terratier.currency.admin`)

Adding `coins` also increases `total-coins`. Subtracting `coins` does not decrease `total-coins`.

Tab completion suggestions:
- First argument: `add`, `subtract`, `set`, `view`
- Second argument (for those commands): online player names
- Third argument (for add/subtract/set): configured currency ids (e.g., `coins`, `total-coins`)

## WorldGuard
Require a player to have at least 50 lifetime coins before entering a region:

```text
/rg flag <region> required-total-coins 50
```

## Developer API
Other Paper plugins can access TerraTierCurrency through the server services manager.

Compile against the TerraTierCurrency jar:

```gradle
dependencies {
    compileOnly files("libs/TerraTierCurrency-1.0.1.jar")
}
```

Add TerraTierCurrency to the consuming plugin's `plugin.yml`:

```yaml
softdepend: [TerraTierCurrency]
```

Use `depend` instead of `softdepend` if the plugin cannot run without currency support.

Load the API from another plugin:

```java
import org.bukkit.plugin.RegisteredServiceProvider;

import com.terratier.currency.api.TerraTierCurrencyApi;

private TerraTierCurrencyApi currencyApi;

@Override
public void onEnable() {
    RegisteredServiceProvider<TerraTierCurrencyApi> provider =
            getServer().getServicesManager().getRegistration(TerraTierCurrencyApi.class);

    if (provider != null) {
        currencyApi = provider.getProvider();
    }
}
```

Reward coins from another plugin:

```java
if (currencyApi != null) {
    currencyApi.addBalance(player.getUniqueId(), TerraTierCurrencyApi.COINS, 5.0);
}
```

Adding `coins` through the API also increases `total-coins` automatically.

Check lifetime coins:

```java
boolean canEnter = currencyApi.hasBalance(
        player.getUniqueId(),
        TerraTierCurrencyApi.TOTAL_COINS,
        50.0
);
```

Available API methods:

```java
double getBalance(UUID playerId, String currencyId);
void setBalance(UUID playerId, String currencyId, double amount);
void addBalance(UUID playerId, String currencyId, double amount);
boolean subtractBalance(UUID playerId, String currencyId, double amount);
boolean hasBalance(UUID playerId, String currencyId, double amount);
boolean hasCurrency(String currencyId);
Currency getCurrency(String currencyId);
Set<String> getCurrencyIds();
Map<String, Currency> getCurrencies();
```

## Configuration
- See `plugins/TerraTierCurrency/config.yml`. Default entries:

```yaml
currencies:
  coins:
    display-name: Coins
    icon: "\uD83D\uDCB0"
  total-coins:
    display-name: Total Coins
    icon: "\uD83D\uDCB0"
```

Add additional currencies by adding entries under `currencies`.

## Persistence
- Player balances are stored in YAML at `plugins/TerraTierCurrency/balances.yml` and are loaded on plugin start and saved on shutdown.

## Development
- Paper API target: `1.21.11-R0.1-SNAPSHOT` (set in `build.gradle`)
- To run a build locally:

```powershell
.\gradlew.bat clean build
```

## Support
Open an issue in the repository or ask on the TerraTier Discord for help.
