package de.bfs.dokpool.client.base;

import java.lang.Exception;
import java.util.Map;

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

		public Node(Map<String,String> map) throws Exception {
			try {
				jacksonNode = mapper.readTree("{}");
				for (Map.Entry<String,String> entry : map.entrySet()){
					set(entry.getKey(), entry.getValue());
				}
			} catch(Exception ex) {
				throw new Exception("JSON creation error.");
			}
		}

		private Node(JsonNode jacksonNode){
			this.jacksonNode = jacksonNode;
		}

		public Node get(String childId){
			return new Node(jacksonNode.get(childId));
		}

		public Node get(int index){
			return new Node(jacksonNode.get(index));
		}
		
		/**
		 * Set the child node with given id if the current node is an object.
		 * This creates a **deep copy** of the child.
		 * @param index Position, if negative prepends, if >= size(), the node will be appended.
		 * @param child The node to insert.
		 */
		public void set(String childId, Node child) throws Exception{
			try {
				((ObjectNode) jacksonNode).set(childId, child.jacksonNode.deepCopy());
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an object.");
			}
		}

		public void set(String childId, String str) throws Exception{
			try {
				((ObjectNode) jacksonNode).put(childId, str);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an object.");
			}
		}

		public void set(String childId, double d) throws Exception{
			try {
				((ObjectNode) jacksonNode).put(childId, d);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an object.");
			}
		}

		/**
		 * Insert child at the specified position if node is an array.
		 * This creates a **deep copy** of the child.
		 * @param index Position, if negative prepends, if >= size(), the node will be appended.
		 * @param child The node to insert.
		 */
		public void insert(int index, Node child) throws Exception{
			try {
				((ArrayNode) jacksonNode).insert(index, child.jacksonNode.deepCopy());
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
		}

	    /**
		 * Insert double at the specified position if node is an array.
		 * @param index Position, if negative prepends, if >= size(), the node will be appended.
		 * @param d The double to insert.
		 */
		public void insert(int index, double d) throws Exception{
			try {
				((ArrayNode) jacksonNode).insert(index, d);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
		}

		/**
		 * Insert a string at the specified position if node is an array.
		 * @param index Position, if negative prepends, if >= size(), the node will be appended.
		 * @param str The string to insert.
		 */
		public void insert(int index, String str) throws Exception{
			try {
				((ArrayNode) jacksonNode).insert(index, str);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
		}

		/**
		 * Appends a node if node is an array.
		 * This creates a **deep copy** of the child.
		 * @param child The node to insert.
		 */
		public void append(Node child) throws Exception {
			try {
				((ArrayNode) jacksonNode).add(child.jacksonNode.deepCopy());
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
		}

		/**
		 * Appends a double if node is an array.
		 * @param d The double to insert.
		 */
		public void append(double d) throws Exception{
			try {
				((ArrayNode) jacksonNode).add(d);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
		}

		/**
		 * Appends a string if node is an array.
		 * @param str The string to insert.
		 */
		public void append(String str) throws Exception{
			try {
				((ArrayNode) jacksonNode).add(str);
			} catch (ClassCastException cce){
				throw new Exception("JSON node is not an array.");
			}
		}

		public String type(){
			return jacksonNode.getNodeType().name().toLowerCase();
		}

		public String toString(){
			return jacksonNode.asText();
		}

		public int toInt(){
			return jacksonNode.asInt();
		}

		public int toDouble(){
			return jacksonNode.asInt();
		}

		public String toJSON() throws Exception{
			try {
				return mapper.writeValueAsString(jacksonNode);
			} catch(JsonProcessingException jpe) {
				throw new Exception("JSON processing error.");
			}
		}
	}
}

