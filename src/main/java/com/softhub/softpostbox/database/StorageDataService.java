package com.softhub.softpostbox.database;

import com.softhub.softframework.SoftFramework;
import com.softhub.softpostbox.PostboxContainer;
import com.softhub.softpostbox.PostboxData;
import com.softhub.softpostbox.util.SerializeUtil;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StorageDataService {

    public static void init() {
        SoftFramework.getDatabaseManager().createTable("postbox_data", "user_id VARCHAR(36), container LONGBLOB");
    }

    public static CompletableFuture<Void> set(PostboxData postboxData) {
        String table = "postbox_data";
        String uuid = postboxData.getUserId().toString();
        byte[] container = SerializeUtil.serialize(postboxData.getContainer().getItemList());

        String selected = "user_id, container";
        Object[] values = {uuid, container};

        return SoftFramework.getDatabaseManager().set(selected, values, "user_id", "=", uuid, table);
    }

    public static void setSync(PostboxData postboxData) {
        String table = "postbox_data";
        String uuid = postboxData.getUserId().toString();
        byte[] container = SerializeUtil.serialize(postboxData.getContainer().getItemList());

        String selected = "user_id, container";
        Object[] values = {uuid, container};

        SoftFramework.getDatabaseManager().setSync(selected, values, "user_id", "=", uuid, table);
    }

    public static CompletableFuture<PostboxData> get(UUID userId) {
        String table = "postbox_data";
        String column = "user_id";
        String logicGate = "=";
        String data = userId.toString();

        String selectedColumns = "container";

        return SoftFramework.getDatabaseManager()
                .getMultipleColumnsList(selectedColumns, table, column, logicGate, data, rs -> {
                    byte[] container = rs.getBytes("container");
                    PostboxContainer postboxContainer = new PostboxContainer(SerializeUtil.deserialize(container));
                    return new PostboxData(userId, postboxContainer);
                })
                .thenApply(resultList -> resultList.isEmpty() ? new PostboxData(userId, new PostboxContainer(new ArrayList<>())) : resultList.get(0));
    }

}
