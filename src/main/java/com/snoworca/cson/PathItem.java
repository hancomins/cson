package com.snoworca.cson;

import java.util.ArrayList;
import java.util.List;

public class PathItem {

    private final static int READ_MODE_KEY = 0;
    private final static int READ_MODE_INDEX = 1;
    private final static int READ_MODE_UNDEFINED = -1;

    private final String name;
    private final int index;


    private boolean isInArray;
    private boolean isArrayItem;

    private final boolean isObject;
    private boolean isEndPoint;


    private PathItem(String name, int index) {
        this.name = name;
        this.index = index;
        isObject = !this.name.isEmpty();
        if(index > -1) {
            isInArray = true;
        }
    }



    private PathItem(int index) {
        this.name = "";
        this.index = index;
        this.isObject = false;
        if(index > -1) {
            isInArray = true;
        }
    }

    public boolean isEndPoint() {
        return isEndPoint;
    }

    public boolean isObject() {
        return isObject;
    }


    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public boolean isArrayValue() {
        return isArrayItem;
    }

    public boolean isInArray() {
        return isInArray;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static List<PathItem> parseMultiPath2(String path) {
        ArrayList<PathItem> itemList = new ArrayList<>();
        char[] chars = path.toCharArray();
        StringBuilder builder = new StringBuilder(path.length());
        PathItem lastItem;
        int readMode = READ_MODE_UNDEFINED;
        int lastIndex = -1;
        for(int i = 0, n = chars.length; i < n; i++) {
            char c = chars[i];
            if(c == '.') {
                String key = builder.toString();
                key = key.trim();
                builder.setLength(0);
                if(!key.isEmpty()) {
                    PathItem item = new PathItem(key, lastIndex);
                    lastIndex = -1;
                    itemList.add(item);
                }
                readMode = READ_MODE_KEY;
            } else if(c == '[') {

                String key = builder.toString();
                key = key.trim();
                builder.setLength(0);
                if(!key.isEmpty() || lastIndex > -1) {
                    PathItem item = new PathItem(key, lastIndex);
                    lastIndex = -1;
                    item.isArrayItem = true;
                    itemList.add(item);
                }
                readMode = READ_MODE_INDEX;
            } else if(c == ']' && readMode == READ_MODE_INDEX) {
                String indexString = builder.toString().trim();
                builder.setLength(0);
                lastIndex = Integer.parseInt(indexString);
            } else {
                builder.append(c);
            }
        }
        String key = builder.toString();
        key = key.trim();
        if(!key.isEmpty()) {
            PathItem item = new PathItem(key, lastIndex);
            itemList.add(item);
        } else if(lastIndex > -1) {
            PathItem item = new PathItem(lastIndex);
            itemList.add(item);
        }
        if(!itemList.isEmpty()) {
            lastItem = itemList.get(itemList.size() - 1);
            lastItem.isEndPoint = true;
        }

        return itemList;
    }

}



