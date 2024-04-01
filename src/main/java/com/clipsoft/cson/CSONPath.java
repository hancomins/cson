package com.clipsoft.cson;


import java.util.List;

public class CSONPath {

    private final CSONElement csonElement;

    protected CSONPath(CSONElement csonElement) {
        this.csonElement = csonElement;
    }

    public Boolean optBoolean(String path) {
        return optBoolean(path, null);
    }

    public Boolean optBoolean(String path, Boolean defaultValue) {
        Object obj = get(path);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        } else if(obj instanceof Number) {
            return ((Number)obj).intValue() == 1;
        } else if(obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        }
        return defaultValue;
    }

    public Double optDouble(String path) {
        return optDouble(path, null);
    }

    public Double optDouble(String path, Double defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).doubleValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1.0 : 0.0;
        }
        else if(obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Float optFloat(String path) {
        return optFloat(path, null);
    }

    public Float optFloat(String path, Float defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).floatValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1.0f : 0.0f;
        }
        else if(obj instanceof String) {
            try {
                return Float.parseFloat((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }


        return defaultValue;
    }

    public Long optLong(String path) {
        return optLong(path, null);
    }

    public Long optLong(String path, Long defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).longValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1L : 0L;
        }
        else if(obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Short optShort(String path) {
        return optShort(path, null);
    }



    public Short optShort(String path, Short defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).shortValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? (short)1 : (short)0;
        }
        else if(obj instanceof String) {
            try {
                return Short.parseShort((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public Byte optByte(String path) {
        return optByte(path, null);
    }

    public Byte optByte(String path, Byte defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).byteValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? (byte)1 : (byte)0;
        }
        else if(obj instanceof String) {
            try {
                return Byte.parseByte((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }


    public Integer optInteger(String path) {
        return optInteger(path, null);
    }

    public Integer optInteger(String path, Integer defaultValue) {
        Object obj = get(path);
        if (obj instanceof Number) {
            return ((Number)obj).intValue();
        }
        else if(obj instanceof Boolean) {
            return (Boolean)obj ? 1 : 0;
        }
        else if(obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String optString(String path, String defaultValue) {
        Object obj = get(path);
        if (obj instanceof String) {
            return (String) obj;
        } else if(obj instanceof Number) {
            return String.valueOf(obj);
        }
        return defaultValue;
    }

    public String optString(String path) {
        return optString(path, null);
    }

    public CSONObject optCSONObject(String path) {
        Object obj = get(path);
        if (obj instanceof CSONObject) {
            return (CSONObject) obj;
        }
        return null;
    }


    public CSONArray optCSONArray(String path) {
        Object obj = get(path);
        if (obj instanceof CSONArray) {
            return (CSONArray) obj;
        }
        return null;
    }


    private CSONElement obtainOrCreateChild(CSONElement csonElement, PathItem pathItem) {
        if(csonElement instanceof CSONObject && !pathItem.isInArray() && pathItem.isObject()) {
            CSONObject csonObject = (CSONObject)csonElement;
            String name = pathItem.getName();
            if(pathItem.isArrayValue()) {
                CSONArray childCsonArray = csonObject.optCSONArray(name);
                if(childCsonArray == null) {
                    childCsonArray = new CSONArray();
                    childCsonArray.setAllowRawValue(csonElement.isAllowRawValue())
                                    .setUnknownObjectToString(csonElement.isUnknownObjectToString());
                    csonObject.put(name, childCsonArray);
                }

                return childCsonArray;
            } else {
                CSONObject childCsonObject = csonObject.optCSONObject(name);
                if(childCsonObject == null) {
                    childCsonObject = new CSONObject();
                    childCsonObject.setAllowRawValue(csonElement.isAllowRawValue())
                            .setUnknownObjectToString(csonElement.isUnknownObjectToString());
                    csonObject.put(name, childCsonObject);
                }
                return childCsonObject;
            }
        } else if(csonElement instanceof CSONArray && pathItem.isInArray()) {
            CSONArray csonArray = (CSONArray)csonElement;
            int index = pathItem.getIndex();
            if(pathItem.isObject()) {
                CSONObject childCsonObject = csonArray.optCSONObject(index);
                if(childCsonObject == null) {
                    childCsonObject = new CSONObject();
                    childCsonObject.setAllowRawValue(csonElement.isAllowRawValue())
                            .setUnknownObjectToString(csonElement.isUnknownObjectToString());
                    csonArray.set(index, childCsonObject);
                    if(pathItem.isArrayValue()) {
                        CSONArray childCsonArray = new CSONArray();
                        childCsonArray.setAllowRawValue(csonElement.isAllowRawValue())
                                .setUnknownObjectToString(csonElement.isUnknownObjectToString());
                        childCsonObject.put(pathItem.getName(), childCsonArray);
                        return childCsonArray;
                    }
                    CSONObject childAndChildCsonObject = new CSONObject();
                    childAndChildCsonObject.setAllowRawValue(csonElement.isAllowRawValue())
                            .setUnknownObjectToString(csonElement.isUnknownObjectToString());
                    childCsonObject.put(pathItem.getName(), childAndChildCsonObject);
                    return childAndChildCsonObject;
                } else  {
                    if(pathItem.isArrayValue()) {
                        CSONArray childChildCsonArray = childCsonObject.optCSONArray(pathItem.getName());
                        if (childChildCsonArray == null) {
                            childChildCsonArray = new CSONArray();
                            childChildCsonArray.setAllowRawValue(csonElement.isAllowRawValue())
                                    .setUnknownObjectToString(csonElement.isUnknownObjectToString());
                            childCsonObject.put(pathItem.getName(), childChildCsonArray);
                        }
                        return childChildCsonArray;
                    } else {
                        CSONObject childAndChildCsonObject = childCsonObject.optCSONObject(pathItem.getName());
                        if (childAndChildCsonObject == null) {
                            childAndChildCsonObject = new CSONObject();
                            childAndChildCsonObject.setAllowRawValue(csonElement.isAllowRawValue())
                                    .setUnknownObjectToString(csonElement.isUnknownObjectToString());
                            childCsonObject.put(pathItem.getName(), childAndChildCsonObject);
                        }
                        return childAndChildCsonObject;
                    }
                }
            }
            else if(pathItem.isArrayValue()) {
                CSONArray childCsonArray = csonArray.optCSONArray(index);
                if(childCsonArray == null) {
                    childCsonArray = new CSONArray();
                    childCsonArray.setAllowRawValue(csonElement.isAllowRawValue())
                            .setUnknownObjectToString(csonElement.isUnknownObjectToString());
                    csonArray.set(index, childCsonArray);
                }
                return childCsonArray;
            }


            throw new IllegalArgumentException("Invalid path. " + pathItem);
        } else {
            throw new IllegalArgumentException("Invalid path. " + pathItem);
        }
    }



    private void putValue(CSONElement csonElement, PathItem pathItem, Object value) {
        if(pathItem.isInArray()) {
            if(pathItem.isObject()) {
                int index = pathItem.getIndex();
                CSONObject childCsonObject = ((CSONArray)csonElement).optCSONObject(index);
                if(childCsonObject == null) {
                    childCsonObject = new CSONObject();
                    csonElement.setAllowRawValue(csonElement.isAllowRawValue())
                            .setUnknownObjectToString(csonElement.isUnknownObjectToString());
                    ((CSONArray)csonElement).set(index, childCsonObject);
                }
                childCsonObject.put(pathItem.getName(), value);
            } else {
                ((CSONArray)csonElement).set(pathItem.getIndex(), value);
            }
        } else {
            ((CSONObject)csonElement).put(pathItem.getName(), value);
        }
    }



    public CSONPath put(String path, Object value) {
        List<PathItem> list = PathItem.parseMultiPath2(path);
        CSONElement lastCsonElement = this.csonElement;
        //noinspection ForLoopReplaceableByForEach
        for(int i = 0, n = list.size(); i < n; ++i) {
            PathItem pathItem = list.get(i);
            if(pathItem.isEndPoint()) {
                putValue(lastCsonElement, pathItem, value);
                break;
            }
            lastCsonElement = obtainOrCreateChild(lastCsonElement, pathItem);
        }
        return this;
    }


    public boolean remove(String path) {
        List<PathItem> pathItemList = PathItem.parseMultiPath2(path);
        Object parents = csonElement;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = pathItemList.size(); i < n; ++i) {
            PathItem pathItem = pathItemList.get(i);

            if (pathItem.isEndPoint()) {
                if (pathItem.isInArray()) {
                    if(pathItem.isObject()) {
                        CSONObject endPointObject = ((CSONArray) parents).optCSONObject(pathItem.getIndex());
                        if(endPointObject == null) return false;
                        endPointObject.remove(pathItem.getName());
                        return true;
                    }
                    else {
                        ((CSONArray)parents).remove(pathItem.getIndex());
                        return true;
                    }
                } else {
                    ((CSONObject) parents).remove(pathItem.getName());
                    return true;
                }
            }
            else if((parents instanceof CSONObject && pathItem.isInArray()) || (parents instanceof CSONArray && !pathItem.isInArray())) {
                return false;
            }
            else {
                if (pathItem.isInArray()) {
                    assert parents instanceof CSONArray;
                    parents = ((CSONArray) parents).opt(pathItem.getIndex());
                    if(pathItem.isObject() && parents instanceof CSONObject) {
                        parents = ((CSONObject) parents).opt(pathItem.getName());
                    }
                } else {
                    assert parents instanceof CSONObject;
                    parents = ((CSONObject) parents).opt(pathItem.getName());
                }
            }
        }
        return false;
    }

    public boolean has(String path) {
        return get(path) != null;
    }



    public Object get(String path) {
        List<PathItem> pathItemList = PathItem.parseMultiPath2(path);
        Object parents = csonElement;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = pathItemList.size(); i < n; ++i) {
            PathItem pathItem = pathItemList.get(i);

            if (pathItem.isEndPoint()) {
                if (pathItem.isInArray()) {
                    if(pathItem.isObject()) {
                        CSONObject endPointObject = ((CSONArray) parents).optCSONObject(pathItem.getIndex());
                        if(endPointObject == null) return null;
                        return endPointObject.opt(pathItem.getName());
                    }
                    else {
                        return ((CSONArray)parents).get(pathItem.getIndex());
                    }
                } else {
                    return ((CSONObject) parents).opt(pathItem.getName());
                }
            }
            else if((parents instanceof CSONObject && pathItem.isInArray()) || (parents instanceof CSONArray && !pathItem.isInArray())) {
                return null;
            }
            else {
                if (pathItem.isInArray()) {
                    assert parents instanceof CSONArray;
                    parents = ((CSONArray) parents).opt(pathItem.getIndex());
                    if(pathItem.isObject() && parents instanceof CSONObject) {
                        parents = ((CSONObject) parents).opt(pathItem.getName());
                    }
                } else {
                    assert parents instanceof CSONObject;
                    parents = ((CSONObject) parents).opt(pathItem.getName());
                }
            }
        }
        return null;
    }

}
