package com.hancomins.cson.serializer.mapper;


import com.hancomins.cson.PathItem;
import com.hancomins.cson.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("DuplicatedCode")
public class _NodeBuilder {

    private final _ObjectNode node;
    private int lastID = 1;

    protected _NodeBuilder(_ObjectNode Node) {
        this.node = Node;
    }

    _ObjectNode makeNode(ClassSchema targetTypeSchema) {
        return makeNode(targetTypeSchema, -1);
    }

     _ObjectNode makeNode(ClassSchema targetTypeSchema, int parentID) {
        List<SchemaValueAbs> fieldRacks = searchAllFields(targetTypeSchema, targetTypeSchema.getType());
        _ObjectNode rootNode = new _ObjectNode();
        rootNode.setNodeType(_NodeType.OBJECT);
        int id = lastID++;
        rootNode.putClassSchema(targetTypeSchema, id, parentID);
        for(SchemaValueAbs fieldRack : fieldRacks) {
            String path = fieldRack.getPath();
            makeSubTree(rootNode, path, fieldRack, id);
            //rootNode.merge(elementNode);
        }

        return rootNode;
    }




    private  List<SchemaValueAbs> searchAllFields(ClassSchema typeSchema, Class<?> clazz) {
        //Set<String> fieldPaths = new HashSet<>();
        List<SchemaValueAbs> results = new ArrayList<>();
        findSchemaByAncestors(typeSchema, results, clazz);
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> interfaceClass : interfaces) {
            findSchemaByAncestors(typeSchema, results, interfaceClass);
        }
        return results;
    }

    private  void findSchemaByAncestors(ClassSchema typeSchema, List<SchemaValueAbs> results, Class<?> currentClass) {
        List<Field> fields = ReflectionUtils.getAllInheritedFields(currentClass);
        List<Method> methods = ReflectionUtils.getAllInheritedMethods(currentClass);
        TypeUtil.filterSupportedTypes(fields).stream().map(field -> SchemaValueAbs.of(typeSchema, field))
                .filter(Objects::nonNull).forEach(results::add);

        findCsonGetterSetterMethods(typeSchema, results, methods);

    }


    private  void findCsonGetterSetterMethods(ClassSchema typeSchema, List<SchemaValueAbs> results, List<Method> methods) {
        if(methods != null) {
            for(Method method : methods) {
                SchemaMethod methodRack = (SchemaMethod) SchemaValueAbs.of(typeSchema,method);
                if(methodRack != null) {
                    results.add(methodRack);
                }
            }
        }
    }


    /*

    private  SchemaElementNode_ obtainOrCreateChild(SchemaElementNode_ Node, PathItem pathItem) {
        if(Node instanceof SchemaObjectNode_ && !pathItem.isInArray() && pathItem.isObject()) {
            SchemaObjectNode_ ObjectNode = (SchemaObjectNode_)Node;
            String name = pathItem.getName();
            if(pathItem.isArrayValue()) {
                SchemaArrayNode childArrayNode = ObjectNode.getArrayNode(name);
                if(childArrayNode == null) {
                    childArrayNode = new SchemaArrayNode();
                    ObjectNode.put(name, childArrayNode);
                }

                return childArrayNode;
            } else {
                SchemaObjectNode_ childObjectNode = ObjectNode.getObjectNode(name);
                if(childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode_();
                    ObjectNode.put(name, childObjectNode);
                }
                return childObjectNode;
            }
        } else if(Node instanceof SchemaArrayNode && pathItem.isInArray()) {
            SchemaArrayNode ArrayNode = (SchemaArrayNode)Node;
            int index = pathItem.getIndex();
            if(pathItem.isObject()) {
                SchemaObjectNode_ childObjectNode = ArrayNode.getObjectNode(index);
                if(childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode_();
                    ArrayNode.put(index, childObjectNode);
                    if(pathItem.isArrayValue()) {
                        SchemaArrayNode childArrayNode = new SchemaArrayNode();
                        childObjectNode.put(pathItem.getName(), childArrayNode);
                        return childArrayNode;
                    }
                    SchemaObjectNode_ childAndChildObjectNode = new SchemaObjectNode_();
                    childObjectNode.put(pathItem.getName(), childAndChildObjectNode);
                    return childAndChildObjectNode;
                } else  {
                    if(pathItem.isArrayValue()) {
                        SchemaArrayNode childChildArrayNode = childObjectNode.getArrayNode(pathItem.getName());
                        if (childChildArrayNode == null) {
                            childChildArrayNode = new SchemaArrayNode();
                            childObjectNode.put(pathItem.getName(), childChildArrayNode);
                        }
                        return childChildArrayNode;
                    } else {
                        SchemaObjectNode_ childAndChildObjectNode = childObjectNode.getObjectNode(pathItem.getName());
                        if (childAndChildObjectNode == null) {
                            childAndChildObjectNode = new SchemaObjectNode_();
                            childObjectNode.put(pathItem.getName(), childAndChildObjectNode);
                        }
                        return childAndChildObjectNode;
                    }
                }
            }
            else if(pathItem.isArrayValue()) {
                SchemaArrayNode childArrayNode = ArrayNode.getArrayNode(index);
                if(childArrayNode == null) {
                    childArrayNode = new SchemaArrayNode();
                    ArrayNode.put(index, childArrayNode);
                }
                return childArrayNode;
            }

            // TODO 에러를 뿜어야함..
            //throw new RuntimeException("Invalid path");
            throw new IllegalArgumentException("Invalid path");
        } else {
            //TODO 에러를 뿜어야함..
            //throw new RuntimeException("Invalid path");
            throw new IllegalArgumentException("Invalid path");
        }
    }



    private void putNode(_ObjectNode node, PathItem pathItem, ISchemaNode value) {
        if(pathItem.isInArray()) {
            if(pathItem.isObject()) {
                int index = pathItem.getIndex();
                SchemaObjectNode_ childObjectNode = ((SchemaArrayNode)node).getObjectNode(index);
                if(childObjectNode == null) {
                    childObjectNode = new SchemaObjectNode_();
                    ((SchemaArrayNode)node).put(index, childObjectNode);
                }
                childObjectNode.put(pathItem.getName(), value);
            } else {
                ((SchemaArrayNode)node).put(pathItem.getIndex(), value);
            }
        } else {
            ((SchemaObjectNode_)node).put(pathItem.getName(), value);
        }
    }

*/

    _ObjectNode makeSubTree(_ObjectNode rootNode, String path, SchemaValueAbs valueSchema, int parentID) {
        List<PathItem> list = PathItem.parseMultiPath2(path);

        _ObjectNode currentNode = rootNode;

        /*ClassSchema classSchema = fieldRack.getClassSchema();
        _ObjectNode childNode = makeNode(classSchema, id);
        childNode.setComment(fieldRack.getComment(), fieldRack.getAfterComment());*/

        //noinspection ForLoopReplaceableByForEach
        for(int i = 0, n = list.size(); i < n; ++i) {
            PathItem pathItem = list.get(i);
            String nodeName = pathItem.getName();
            _ObjectNode childNode = currentNode.getNode(nodeName);
            if(childNode == null) {
                childNode = new _ObjectNode();
                currentNode.putNode(nodeName, childNode);
                currentNode = childNode;
            } else {
                currentNode = childNode;
            }
            currentNode.setName(nodeName);
             if(pathItem.isEndPoint()) {
                // todo : Array 타입 구분.,
                //putNode(endpointNode, pathItem, value);
                 ClassSchema objectTypeSchema = valueSchema.getClassSchema();
                 if(objectTypeSchema != null) {
                     _ObjectNode node  = makeNode(objectTypeSchema, parentID);
                     currentNode.merge(node);
                 } else {
                     currentNode.putFieldSchema(valueSchema,parentID);
                     currentNode.setEndPoint();
                 }
                break;
            }

        }
        return rootNode;
    }
}
