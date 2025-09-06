This is very basic documentation for now, will be updated in the future



 --- Importing ---

Download the most recent release and put it into your local maven repository
 - Your local maven repo is usually in C:\Users\(user)\.m2\repository\

Once in your local maven repo you can use it in all of your projects easily by just putting the maven dependency into your pom.xml



 --- Maven Dependency ---
```
<dependency>
      <groupId>me.ericdavis</groupId>
      <artifactId>LazyGUI</artifactId>
      <version>2.4.0</version> (or whatever version you're using)
</dependency>
```


 --- Initilization ---

In onEnable(), create a "new LazyGui(JavaPlugin plugin)"
 - this will register the GuiManager as an event listener

For every gui page you create you will need to initialize it by just creating it 
 - example: new MainPage()




 --- Opening a page ---

GuiManager.getInstance().openPage(String pageId, Player player)



 --- Creating a page ---

1. Create a new class
2. Extend AbstractGuiPage
3. Implement all methods and constructor
4. Create a public static String pageId to something unique to this page -- no two pages can have the same id
5. fill out overridden abstract methods (getDisplayName, getRows, getPageIdentifier)

Constructor Parameters
 - JavaPlugin plugin: your main plugin class
 - boolean fillBorder: fills border with dark gray glass panes
 - boolean buttonsFollowListPages: buttons added in assignItems() will be on every page of a listed page -- false means the buttons will only be on the first page
 - String parentPageId: allows usage of openParentPage just to make order of pages easier
 - boolean autoGenBackButton: will auto generate the "previous page" button at the bottom middle of the page, only shows up if there is a parent page to go to

 --- Adding Buttons/Items ---

You can assign buttons in the assignItems by using the method assignItem
 - Leave empty if you don't want normal buttons (only using listed)
```
@Override
    protected void assignItems(UUID playerId) {
        assignItem(playerId, 20, new GuiItem(Material.GREEN_CONCRETE, e -> {
            e.getWhoClicked().sendMessage(ChatColor.GREEN + "Starting the Game...");
        }).setName(ChatColor.GREEN + "Start"));

        assignItem(playerId, 24, new GuiItem(Material.RED_CONCRETE, e -> {
            e.getWhoClicked().sendMessage(ChatColor.GREEN + "Stopping the Game...");
        }).setName(ChatColor.RED + "Stop"));
    }
```
You can assign buttons in getListedButtons by adding new GuiItems to a List<GuiItem> and returning the list
 - Return null if you don't want listed buttons
```
@Override
    protected List<GuiItem> getListedButtons() {
        List<GuiItem> listedButtons = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {

            listedButtons.add(new GuiItem(Material.PLAYER_HEAD, e -> {
                e.getWhoClicked().teleport(p.getLocation());
            }).setName(p.getName()).setSkullOwner(p));

        }

        return listedButtons;
    }
```

Example Main Plugin Class:
 - this is all you need for making the pages
```
public final class ExamplePlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        new LazyGui(this);
        
        new MainPage();

        for (Player player : Bukkit.getOnlinePlayers()) {
            GuiManager.getInstance().openPage(MainPage.pageId, player);
        }
    }
    
}
```

Example Class 1:
 - this class doesn't use the listed feature but it does use the other stuff

```
public class GameControlsPage extends AbstractGuiPage {

    public static String pageId = "main.game-controls";

    public GameControlsPage() {
        super(MCOHexRoyale.getInstance(), true, false, MainPage.pageId, true);
    }

    @Override
    public String getDisplayName() {
        return ChatColor.BLUE + "Game Controls";
    }

    @Override
    protected int getRows() {
        return 6;
    }

    @Override
    protected void assignItems(UUID playerId) {
        assignItem(playerId, 20, new GuiItem(Material.GREEN_CONCRETE, e -> {
            e.getWhoClicked().sendMessage(ChatColor.GREEN + "Starting the Game...");
        }).setName(ChatColor.GREEN + "Start"));

        assignItem(playerId, 24, new GuiItem(Material.RED_CONCRETE, e -> {
            e.getWhoClicked().sendMessage(ChatColor.GREEN + "Stopping the Game...");
        }).setName(ChatColor.RED + "Stop"));
    }

    @Override
    public String getPageIdentifier() {
        return pageId;
    }

    @Override
    protected List<GuiItem> getListedButtons() {
        return null;
    }
}
```

Example Class 2:
 - uses the listed feature to show a teleport menu to teleport to any online player
```
public class SingleTeamPage extends AbstractGuiPage {
    public static String pageId = "main.teams.single-team";
    public static HexTeam.TeamColor teamToOpen = HexTeam.TeamColor.RED;

    public SingleTeamPage() {
        super(MCOHexRoyale.getInstance(), true, true, TeamsPage.pageId, true);
    }

    @Override
    protected String getDisplayName() {
        return teamToOpen.getColor() + teamToOpen.getName();
    }

    @Override
    protected int getRows() {
        return 6;
    }

    @Override
    protected void assignItems(UUID playerId) {

    }

    @Override
    public String getPageIdentifier() {
        return pageId;
    }

    @Override
    protected List<GuiItem> getListedButtons() {
        List<GuiItem> listedButtons = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {

            listedButtons.add(new GuiItem(Material.PLAYER_HEAD, e -> {
                e.getWhoClicked().teleport(p.getLocation());
            }).setName(p.getName()).setSkullOwner(p));

        }

        return listedButtons;
    }
}
```
