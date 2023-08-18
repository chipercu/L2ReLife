package scripts.services.PartyMaker;

import com.sun.tools.javac.util.List;
import fuzzy.Html_Constructor.tags.Button;
import fuzzy.Html_Constructor.tags.Combobox;
import fuzzy.Html_Constructor.tags.Edit;
import fuzzy.Html_Constructor.tags.Table;
import fuzzy.Html_Constructor.tags.parameters.Parameters;
import fuzzy.Html_Constructor.tags.parameters.Position;
import l2open.extensions.scripts.Functions;
import l2open.extensions.scripts.ScriptFile;
import l2open.gameserver.communitybbs.CommunityBoard;
import l2open.gameserver.listener.PlayerListenerList;
import l2open.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2open.gameserver.model.L2Object;
import l2open.gameserver.model.L2ObjectsStorage;
import l2open.gameserver.model.L2Player;
import l2open.gameserver.network.L2GameClient;
import l2open.gameserver.serverpackets.NpcHtmlMessage;
import l2open.util.Strings;
import services.PartyMaker.PartyMakerGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static fuzzy.Html_Constructor.tags.parameters.Position.*;

public class PartyMaker extends Functions implements ScriptFile, Parameters {

    private static final Map<Integer, PartyMakerGroup> partyMakerGroupMap = new HashMap<>();
    private static final String bypass = "bypass -h party_maker:";


    private static PartyMaker _instance;
    private static int MONEY_ID = 4357;

    public static PartyMaker getInstance() {
        if (_instance == null)
            _instance = new PartyMaker();
        return _instance;
    }

    public void handleCommands(L2GameClient client, String command) {

        L2Player player = client.getActiveChar();
        if (player == null) {
            return;
        }

        if (command.startsWith("showCreateDialog")) {
            showCreateDialog(player);
        }else if (command.startsWith("showGroups")){
            showGroups(player);
        } else if (command.startsWith("createGroup")) {
            final String[] params = command.split(" ");
            createGroup(player, Strings.parseInt(params[1]), Strings.parseInt(params[2]), params[3], params[4]);
            showGroups(player);
        } else if (command.startsWith("myGroup")) {
            myGroup(player);
        }
    }


    public void createGroup(L2Player player, int minLevel, int maxLevel, String instance, String description) {
        final PartyMakerGroup partyMakerGroup = new PartyMakerGroup(minLevel, maxLevel, player.getObjectId(), description, instance);
        partyMakerGroupMap.put(player.getObjectId(), partyMakerGroup);
        showGroups(player);
    }
    public void myGroup(L2Player player){
        final PartyMakerGroup partyMakerGroup = partyMakerGroupMap.get(player.getObjectId());







    }
    
    

    public void showGroups(L2Player player) {

        StringBuilder HTML = new StringBuilder("<title>Группы</title>");
        Table mainTable = new Table(partyMakerGroupMap.size() + 3, 1)
                .setParams(border(0), width(280), background("l2ui_ct1.Windows_DF_TooltipBG"), cellpadding(2), cellspacing(2));

        mainTable.row(0).col(0).setParams(height(20), width(280)).insert("");
        Table buttonsTable = new Table(1, 6);
        buttonsTable.row(0).col(0).setParams(width(30), height(32)).insert("");
        buttonsTable.row(0).col(1).setParams(width(60), height(32)).insert(new Button("Отмена", action(""), 60, 32).build());
        buttonsTable.row(0).col(2).setParams(width(30), height(32)).insert("");
        buttonsTable.row(0).col(3).setParams(width(60), height(32)).insert(new Button("Создать", action(bypass + "showCreateDialog"), 60, 32).build());
        buttonsTable.row(0).col(4).setParams(width(30), height(32)).insert("");
        buttonsTable.row(0).col(5).setParams(width(60), height(32)).insert(new Button("Моя группа", action(bypass + "myGroup"), 60, 32).build());


        mainTable.row(1).col(0).setParams(height(32), width(280)).insert(buttonsTable.build());

        final Table header = new Table(1, 5).setParams(width(280), background("L2UI_CT1.CharacterPassword_DF_KeyBack"));
        header.row(0).col(0).setParams(height(20), width(62)).insert("Тип");
        header.row(0).col(1).setParams(height(20), width(120)).insert("Описание");
        header.row(0).col(2).setParams(height(20), width(66)).insert("Лидер");
        header.row(0).col(3).setParams(height(20), width(32)).insert("");

        mainTable.row(2).col(0).setParams(height(32), width(280)).insert(header.build());

        int count = 3;
        for (Map.Entry<Integer, PartyMakerGroup> group : partyMakerGroupMap.entrySet()) {
            final Table table = new Table(1, 4).setParams(background("L2UI_CT1.CharacterPassword_DF_KeyBack"));
            table.row(0).col(0).setParams(width(32), height(32)).insert(group.getValue().getInstance());
            table.row(0).col(1).setParams(width(120), height(32)).insert(group.getValue().getDescription());
            final L2Player creator = L2ObjectsStorage.getPlayer(group.getValue().getCreatorId());
            table.row(0).col(2).setParams(width(66), height(32)).insert(creator.getName());
            final Button requestButton = new Button("+", action(""), 32, 32);
            table.row(0).col(3).setParams(width(32), height(32)).insert(requestButton.build());
            mainTable.row(count).col(0).setParams(height(32), width(280)).insert(buttonsTable.build());
            count++;
        }
        sendDialog(player, HTML.append(mainTable.build()).toString());

    }





    public void showCreateDialog(L2Player player) {
        String descriptionText = "Description Description Description Description Description Description Description Description Description ";
        StringBuilder HTML = new StringBuilder("<title>Создание группы</title>");
        Table mainTable = new Table(8, 1)
                .setParams(border(0), width(280), background("l2ui_ct1.Windows_DF_TooltipBG"), cellpadding(2), cellspacing(2));
        Table levelTable = new Table(2, 5).setParams(border(0), width(280));
        levelTable.row(0).col(0).setParams(height(20), width(60)).insert("<center>Мин.Ур.</center>");
        levelTable.row(0).col(1).setParams(height(20), width(10)).insert("");
        levelTable.row(0).col(2).setParams(height(20), width(60)).insert("<center>Макс.Ур.</center>");
        levelTable.row(1).col(0).setParams(height(32), width(60)).insert("<center>" + new Edit("minLevel").build() + "</center>");
        levelTable.row(1).col(1).setParams(height(20), width(10)).insert("");
        levelTable.row(1).col(2).setParams(height(32), width(60)).insert("<center>" + new Edit("maxLevel").build() + "</center>");
        levelTable.row(0).col(3).setParams(height(20), width(10)).insert("");
        levelTable.row(1).col(3).setParams(height(32), width(10)).insert("");
        levelTable.row(0).col(4).setParams(height(20), width(60)).insert("<center>Выбор Инсты</center>");
        final List<String> instances = List.of("Freya", "Zaken", "Frinteza", "7RB");
        levelTable.row(1).col(4).setParams(height(32), width(60)).insert(new Combobox("instance", instances).setParams(width(60)).build());

        mainTable.row(0).col(0).setParams(height(20)).insert("");
        mainTable.row(1).col(0).setParams(height(60), align(CENTER), valign(TOP)).insert("<center>" + descriptionText + "</center>");
        mainTable.row(2).col(0).setParams(height(20)).insert("");
        mainTable.row(3).col(0).setParams(height(20)).insert(levelTable.build());
        mainTable.row(4).col(0).setParams(height(20)).insert("");
        mainTable.row(5).col(0).setParams(height(20)).insert(new Edit("description").setParams(width(280), height(50)).build());
        mainTable.row(4).col(0).setParams(height(20)).insert("");
        Table buttonsTable = new Table(1, 5);
        buttonsTable.row(0).col(0).setParams(width(30), height(32)).insert("");
        buttonsTable.row(0).col(1).setParams(width(60), height(32))
                .insert(new Button("Отмена", action(bypass + "showGroups"), 80, 32).build());
        buttonsTable.row(0).col(2).setParams(width(30), height(32)).insert("");
        buttonsTable.row(0).col(3).setParams(width(60), height(32))
                .insert(new Button("Создать", action(bypass + "createGroup $minLevel:$maxLevel:$instance:$description"), 80, 32).build());
        buttonsTable.row(0).col(3).setParams(width(30), height(32)).insert("");

        mainTable.row(7).col(0).setParams(height(20)).insert(buttonsTable.build());

        sendDialog(player, HTML.append(mainTable.build()).toString());

    }

    public void sendDialog(L2Player player, String html) {
        final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(player, null);
        player.sendPacket(npcHtmlMessage.setHtml(html));

    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onReload() {

    }

    @Override
    public void onShutdown() {

    }


}
