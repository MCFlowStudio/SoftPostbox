package com.softhub.softpostbox.command;

import com.softhub.softframework.command.Command;
import com.softhub.softframework.command.CommandExecutor;
import com.softhub.softframework.command.CommandHelp;
import com.softhub.softframework.command.CommandParameter;
import com.softhub.softpostbox.config.ConfigManager;
import com.softhub.softpostbox.inventory.PostboxInventory;
import com.softhub.softpostbox.inventory.PostboxManageInventory;
import com.softhub.softpostbox.manager.PostboxManager;
import org.bukkit.entity.Player;

@Command(name = "우편함", aliases = "postbox", description = "우편함 명령어입니다.", permission = "softpostbox.command.postbox")
public class PostboxCommand {

    @CommandHelp(consoleAvailable = false)
    public boolean onOpen(Player sender) {
        PostboxInventory postboxInventory = new PostboxInventory();
        postboxInventory.init(sender);
        return true;
    }

    @CommandExecutor(label = "선물", description = "손에 든 아이템을 선물합니다.", permission = "softpostbox.command.postbox.gift", consoleAvailable = false)
    public boolean onGift(Player sender,
                          @CommandParameter(name = "대상 플레이어", type = CommandParameter.ParamType.STRING, index = 1) String targetName) {
        PostboxManager.gift(sender, targetName);
        return true;
    }

    @CommandExecutor(label = "지급", description = "타인의 우편함에 아이템을 지급합니다.", permission = "softpostbox.command.postbox.give", consoleAvailable = false)
    public boolean onGive(Player sender,
                          @CommandParameter(name = "대상 플레이어", type = CommandParameter.ParamType.STRING, index = 1) String targetName,
                          @CommandParameter(name = "개수", type = CommandParameter.ParamType.INTEGER, index = 2) Integer amount) {
        PostboxManager.give(sender, targetName, amount);
        return true;
    }

    @CommandExecutor(label = "관리", description = "타인의 우편함을 관리합니다.", permission = "softpostbox.command.postbox.manage", consoleAvailable = false)
    public boolean onManage(Player sender,
                          @CommandParameter(name = "대상 플레이어", type = CommandParameter.ParamType.STRING, index = 1) String targetName) {
        PostboxManageInventory postboxInventory = new PostboxManageInventory(targetName);
        postboxInventory.init(sender);
        return true;
    }

    @CommandExecutor(label = "리로드", description = "플러그인 설정을 다시 불러옵니다.", permission = "softpostbox.command.postbox.reload")
    public boolean onReload(Player sender) {
        ConfigManager.init();
        sender.sendMessage("플러그인 설정을 다시 불러왔습니다.");
        return true;
    }

}
