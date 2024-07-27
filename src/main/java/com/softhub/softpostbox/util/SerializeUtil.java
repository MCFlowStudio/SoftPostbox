package com.softhub.softpostbox.util;

import com.softhub.softpostbox.ItemContainer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SerializeUtil {

    public static byte[] serialize(List<ItemContainer> itemList) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objStream = new ObjectOutputStream(byteStream)) {
            objStream.writeObject(itemList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteStream.toByteArray();
    }

    public static List<ItemContainer> deserialize(byte[] bytes) {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        try (ObjectInputStream objStream = new ObjectInputStream(byteStream)) {
            return (List<ItemContainer>) objStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
