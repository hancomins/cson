package com.clipsoft.cson;

import java.util.ArrayDeque;
import java.util.Set;

public class XMLWriter {

    private ArrayDeque<ElementItem> elementStack = new ArrayDeque<>();

    private StringBuilder stringBuilder = new StringBuilder();

    private String rootElementName = "root";

    public void write(CSONObject csonObject) {
        ElementItem elementItem = new ElementItem(rootElementName);
        elementStack.push(elementItem);
        CSONObject root = csonObject;

        Set<String> keySet = csonObject.keySet();
        for (String key : keySet) {
            Object value = csonObject.get(key);
            if (value instanceof CSONObject) {

            } else if (value instanceof CSONArray) {

            } else {
                stringBuilder.append("\"").append(key).append("\"").append("=").append("\"").append(value).append("\"");
            }
        }















    }


    private class ElementItem {
        public ElementItem(String elementName) {
            this.elementName = elementName;
        }
        String elementName;
    }


}
