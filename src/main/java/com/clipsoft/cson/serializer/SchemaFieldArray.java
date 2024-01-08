package com.clipsoft.cson.serializer;

import com.clipsoft.cson.CSONElement;

import java.lang.reflect.Field;
import java.util.*;

class SchemaFieldArray extends SchemaField implements ISchemaArrayValue {

    //private final Types valueType;

    private final List<CollectionItems> collectionBundles;
    protected final Types ValueType;
    private final TypeElement objectValueTypeElement;


    protected SchemaFieldArray(TypeElement typeElement, Field field, String path) {
        super(typeElement, field, path);
        String fieldPath = field.getDeclaringClass().getName() + "." + field.getName() + "<type: " + field.getType().getName() + ">";
        this.collectionBundles = ISchemaArrayValue.getGenericType(field.getGenericType(), fieldPath);

        Class<?> valueClass = this.collectionBundles.get(collectionBundles.size() - 1).valueClass;
        ValueType = Types.of(valueClass);
        if (ValueType == Types.Object) {
            objectValueTypeElement = TypeElements.getInstance().getTypeInfo(valueClass);
        } else {
            objectValueTypeElement = null;
        }

    }



    @Override
    public TypeElement getObjectValueTypeElement() {
        return objectValueTypeElement;
    }

    @Override
    public List<CollectionItems> getCollectionItems() {
        return collectionBundles;
    }


    @Override
    public Types getEndpointValueType() {
        return ValueType;
    }

    @Override
    public ISchemaNode copyNode() {
        return new SchemaFieldArray(parentsTypeElement, field, path);
    }

    @Override
    public Object newInstance() {
        return collectionBundles.get(0).newInstance();
    }


    @Override
    boolean equalsValueType(SchemaValueAbs schemaValueAbs) {
        if(!(schemaValueAbs instanceof ISchemaArrayValue)) {
            return false;
        }
        if(!ISchemaArrayValue.equalsCollectionTypes(this.getCollectionItems(), ((ISchemaArrayValue)schemaValueAbs).getCollectionItems())) {
            return false;
        }
        if(this.getEndpointValueType() != ((ISchemaArrayValue)schemaValueAbs).getEndpointValueType()) {
            return false;
        }
        return super.equalsValueType(schemaValueAbs);
    }

}
