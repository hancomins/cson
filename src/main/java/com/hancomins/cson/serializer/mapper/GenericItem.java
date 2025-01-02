package com.hancomins.cson.serializer.mapper;

public interface GenericItem {

    /**
     * 이 항목의 중첩 수준을 가져옵니다.
     */
    int getNestedLevel();

    /**
     * 부모 항목을 설정합니다.
     */
    void setParent(GenericItem parent);

    /**
     * 부모 항목을 가져옵니다.
     */
    GenericItem getParent();

    /**
     * 자식 항목을 설정합니다.
     */
    void setChild(GenericItem child);

    /**
     * 자식 항목을 가져옵니다.
     */
    GenericItem getChild();

    /**
     * 항목이 제네릭 타입인지 확인합니다.
     */
    boolean isGeneric();

    /**
     * 제네릭 타입 플래그를 설정합니다.
     */
    void setGeneric(boolean generic);

    /**
     * 제네릭 타입의 이름을 가져옵니다 (가능한 경우).
     */
    String getGenericTypeName();

    /**
     * 제네릭 타입의 이름을 설정합니다.
     */
    void setGenericTypeName(String name);


    Class<?> getValueType();


}