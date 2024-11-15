package de.bfs.dokpool.client.base;

import java.lang.Exception;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * This class will be a wrapper around Jackson's API as we also use org.json in the IRIX stack.
 * Method names are similar, but we only have a single instantiated nested class Node.
 */
public class JSON {

	private static final Log log = LogFactory.getLog(JSON.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	public static class Node {
		private JsonNode jacksonNode;

		public Node(String json) throws Exception {
			try {
				jacksonNode = mapper.readTree(json);
			} catch(JsonProcessingException jpe) {
				throw new Exception("JSON processing error.");
			}
		}

		public Node(Map<String,Object> map) throws Exception {
			try {
				jacksonNode = mapper.readTree("{}");
				for (Map.Entry<String,Object> entry : map.entrySet()){
					Object val =  entry.getValue();
					if (val instanceof Map){
						@SuppressWarnings("unchecked")
						Map<String,Object> valMap = (Map<String,Object>) val;
						set(entry.getKey(),new Node(valMap));
					} else if (val instanceof List){
						@SuppressWarnings("unchecked")
						List<Object> valVec = (List<Object>) val;
						set(entry.getKey(),new Node(valVec));
					} else if (val instanceof String){
						String valStr = (String) val;
						set(entry.getKey(),valStr);
					} else if (val instanceof Number){
						Number valNum = (Number) val;
						if (valNum.doubleValue() == valNum.longValue()){
							set(entry.getKey(),valNum.longValue());
						} else {
							set(entry.getKey(),valNum.doubleValue());
						}
					} else if (val instanceof Boolean){
						Boolean valBool = (Boolean) val;
						set(entry.getKey(),valBool.booleanValue());
					}
					//we ignore null values and everything else for now
				}
			} catch(Exception ex) {
				throw new Exception("JSON creation error.");
			}
		}

		public Node(List<Object> vec) throws Exception {
			try {
				jacksonNode = mapper.readTree("[]");
				for (Object val : vec){
					if (val instanceof Map){
						@SuppressWarnings("unchecked")
						Map<String,Object> valMap = (Map<String,Object>) val;
						append(new Node(valMap));
					} else if (val instanceof List){
						@SuppressWarnings("unchecked")
						List<Object> valVec = (List<Object>) val;
						append(new Node(valVec));
					} else if (val instanceof String){
						String valStr = (String) val;
						append(valStr);
					} else if (val instanceof Number){
						Number valNum = (Number) val;
						if (valNum.doubleValue() == valNum.longValue()){
							append(valNum.longValue());
						} else {
							append(valNum.doubleValue());
						}
					} else if (val instanceof Boolean){
						Boolean valBool = (Boolean) val;
						append(valBool.booleanValue());
					}
					//we ignore null values and everything else for now
				}
			} catch(Exception ex) {
				throw new Exception("JSON creation error.");
			}
		}

		private Node(JsonNode jacksonNode){
			this.jacksonNode = jacksonNode;
		}

		public Node get(String childId){
			return jacksonNode != null ? new Node(jacksonNode.get(childId)) : null;
		}

		public Node get(int index){
			return jacksonNode != null ?  new Node(jacksonNode.get(index)) : null;
		}
		
		/**
		 * Set the child node with given id if the current node is an object.
		 * This creates a **deep copy** of the child.
		 * @param index Position, if negative prepends, if >= size(), the node will be appended.
		 * @param child The node to insert.
		 */
		public Node set(String childId, Node child) throws Exception {
			try {
				((ObjectNode) jacksonNode).set(childId, child.jacksonNode.deepCopy());
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an object.");
			}
			return this;
		}

		public Node set(String childId, String str) throws Exception {
			try {
				((ObjectNode) jacksonNode).put(childId, str);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an object.");
			}
			return this;
		}

		public Node set(String childId, double d) throws Exception {
			try {
				((ObjectNode) jacksonNode).put(childId, d);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an object.");
			}
			return this;
		}

		public Node set(String childId, long i) throws Exception {
			try {
				((ObjectNode) jacksonNode).put(childId, i);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an object.");
			}
			return this;
		}

		public Node set(String childId, boolean b) throws Exception {
			try {
				((ObjectNode) jacksonNode).put(childId, b);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an object.");
			}
			return this;
		}

		/**
		 * Insert child at the specified position if node is an array.
		 * This creates a **deep copy** of the child.
		 * @param index Position, if negative prepends, if >= size(), the node will be appended.
		 * @param child The node to insert.
		 */
		public Node insert(int index, Node child) throws Exception {
			try {
				((ArrayNode) jacksonNode).insert(index, child.jacksonNode.deepCopy());
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
			return this;
		}

	    /**
		 * Insert double at the specified position if node is an array.
		 * @param index Position, if negative prepends, if >= size(), the node will be appended.
		 * @param d The double to insert.
		 */
		public Node insert(int index, double d) throws Exception {
			try {
				((ArrayNode) jacksonNode).insert(index, d);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
			return this;
		}

	    /**
		 * Insert double at the specified position if node is an array.
		 * @param index Position, if negative prepends, if >= size(), the node will be appended.
		 * @param d The double to insert.
		 */
		public Node insert(int index, long i) throws Exception {
			try {
				((ArrayNode) jacksonNode).insert(index, i);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
			return this;
		}

		/**
		 * Insert a string at the specified position if node is an array.
		 * @param index Position, if negative prepends, if >= size(), the node will be appended.
		 * @param str The string to insert.
		 */
		public Node insert(int index, String str) throws Exception {
			try {
				((ArrayNode) jacksonNode).insert(index, str);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
			return this;
		}

		/**
		 * Appends a node if node is an array.
		 * This creates a **deep copy** of the child.
		 * @param child The node to insert.
		 */
		public Node append(Node child) throws Exception {
			try {
				((ArrayNode) jacksonNode).add(child.jacksonNode.deepCopy());
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
			return this;
		}

		/**
		 * Appends a double if node is an array.
		 * @param d The double to insert.
		 */
		public Node append(double d) throws Exception {
			try {
				((ArrayNode) jacksonNode).add(d);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
			return this;
		}

		/**
		 * Appends a double if node is an array.
		 * @param d The double to insert.
		 */
		public Node append(long i) throws Exception {
			try {
				((ArrayNode) jacksonNode).add(i);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
			return this;
		}

		/**
		 * Appends a string if node is an array.
		 * @param str The string to insert.
		 */
		public Node append(String str) throws Exception {
			try {
				((ArrayNode) jacksonNode).add(str);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
			return this;
		}

		/**
		 * Appends a boolean if node is an array.
		 * @param str The string to insert.
		 */
		public Node append(boolean b) throws Exception {
			try {
				((ArrayNode) jacksonNode).add(b);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
			return this;
		}

		public String type(){
			return jacksonNode.getNodeType().name().toLowerCase();
		}

		public String toString(){
			return jacksonNode.asText();
		}

		public long toLong(){
			return jacksonNode.asLong();
		}

		public double toDouble(){
			return jacksonNode.asDouble();
		}

		public boolean toBoolean(){
			return jacksonNode.asBoolean();
		}

		
		public Object toObject() throws Exception {
			switch(type()){
				case "object":
					return toMap();
				case "array":
					return toList();
				case "number":
					if (toDouble() == toLong()){
						log.info("equal:" + toDouble());
						return new Long(toLong());
					} else {
						log.info("not equal:" + toDouble());
						return new Double(toDouble());
					}
				case "string":
					return toString();
				case "boolean":
					return new Boolean(toBoolean());
				case "null":
					return null;
				default:
					return null;
			}
		}

		public ArrayList<Object> toList() throws Exception {
			ArrayList<Object> al = new ArrayList<>();
			try {
				ArrayNode arrayNode = ((ArrayNode) jacksonNode);
				for(JsonNode node : arrayNode){
					al.add((new Node(node)).toObject());
				}
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
			return al;
		}

		public Map<String,Object> toMap() throws Exception {
			HashMap<String,Object> hm = new HashMap<>();
			try {
				ObjectNode objectNode = ((ObjectNode) jacksonNode);
				for(Iterator<Map.Entry<String,JsonNode>> it = objectNode.fields(); it.hasNext();) {
					Map.Entry<String,JsonNode> entry = it.next();
					hm.put(entry.getKey(),new Node(entry.getValue()).toObject());
				 }
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an object.");
			}
			return hm;
		}

		public String toJSON() throws Exception {
			try {
				return mapper.writeValueAsString(jacksonNode);
			} catch(JsonProcessingException jpe) {
				throw new Exception("JSON processing error.");
			}
		}
	}
}

