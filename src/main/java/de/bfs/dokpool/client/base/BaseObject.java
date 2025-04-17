/* Copyright (C) 2015-2025 by Bundesamt fuer Strahlenschutz
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 * See LICENSE for details.
 */

package de.bfs.dokpool.client.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for all API objects. Contains helper methods for all types.
 *
 */
public class BaseObject {
    protected static final java.lang.System.Logger log = System.getLogger(BaseObject.class.getName());
    protected static final java.lang.System.Logger.Level ERROR = java.lang.System.Logger.Level.ERROR;
    protected static final java.lang.System.Logger.Level INFO = java.lang.System.Logger.Level.INFO;
    protected static final java.lang.System.Logger.Level WARNING = java.lang.System.Logger.Level.WARNING;

    protected Object NTS(Object mayBeNull) {
        return mayBeNull != null ? mayBeNull : "null";
    }

    protected DokpoolBaseService service = null;
    protected DokpoolBaseService.PrivateDokpoolBaseService privateService;
    //
    private String pathWithPlonesite = null;
    protected String pathAfterPlonesite = null;
    protected Map<String,Object> data = null;
    //we may initilize an Object with partial data
    protected boolean dataComplete = false;
    protected Map<String,Object> metadata = null;

    @Deprecated
    @SuppressWarnings("unchecked")
    public BaseObject(DokpoolBaseService service, String path, Object[] alldata) {
        this.service = service;
        this.privateService = service.privateService;
        this.pathAfterPlonesite = path;
        if (alldata != null) {
            data = (Map<String,Object>) alldata[0];
        }
    }

    protected static final Map<String,Object> noData = null;

    public BaseObject(DokpoolBaseService service, String path, Map<String,Object> data) {
        this.service = service;
        this.privateService = service.privateService;
        this.pathAfterPlonesite = path;
        this.data = data;
    }

    protected String fullpath() {
        if (pathAfterPlonesite != null) {
            return "/" + service.plonesite + pathAfterPlonesite;
        } else {
            return pathWithPlonesite;
        }
    }

    /**
     * e.g. if pathAfterPlonesite = /bund/... -&gt; bund
     * @return the part of the path that specifies the Dokpool.
     */
    protected String docPoolId() {
        return pathAfterPlonesite.substring(1,pathAfterPlonesite.indexOf('/', 1));
    }

    /**
     * Get the path within the dokpool plone instance.
     * @return the path after the plonesite including the first /,
     * e.g. http://dokpool.example.com:8080/dokpool/bund/content -&gt; /bund/content
     */
    public String getPathAfterPlonesite() {
        return pathAfterPlonesite;
    }

    /**
     * Get path on the server including the name of the ploneite (e.g. /dokpool).
     * DO NOT USE this function to construct a path for the cosntructors of
     * BaseObject and its descendant.
     * @return the path after domain (and port) including the first /,
     * e.g. http://dokpool.example.com:8080/dokpool/bund/content -&gt; to /dokpool/bund/content
     */
    public String getPathWithPlonesite() {
        return fullpath();
    }

    /**
     * Gets just the attributes for the object via a REST-Request.
     * @return object attributes as map
     */
    private Map<String,Object> getData() {
        if (data == null || !dataComplete || !service.allowCaching) {
            try {
                JSON.Node rspNode = privateService.nodeFromGetRequest(pathAfterPlonesite);
                if (rspNode.errorInfo != null) {
                    log.log(INFO, rspNode.errorInfo.toString());
                    data = new HashMap<String,Object>();
                } else {
                    data = rspNode.toMap();
                }
            } catch (Exception ex) {
                log.log(ERROR, exceptionToString(ex));
            }
            dataComplete = true;
        }
        return data;
    }

    protected void clearData() {
        data = null;
        dataComplete = false;
    }

    /**
     * Helper to get value of an attribute.
     * @param name: the name of the attribute
     * @return the value
     */
    public Object getAttribute(String name) {
        //we fetch data if and only if we have no data or
        //(incomplete data with the requesting string missing)
        if (data == null || (!dataComplete && data.get(name) == null) || !service.allowCaching) {
            getData();
        }
        //data or data.get(name) may still be null
        if (data != null) {
            return data.get(name);
        } else {
            return null;
        }
    }

    /**
     * Helper to get all attributes.
     * @return a map of all attributes
     */
    public Map<String,Object> getAllAttributes() {
        //we fetch data if and only if we have no data or
        //(incomplete data with the requesting string missing)
        if (data == null || !dataComplete || !service.allowCaching) {
            getData();
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    protected Object detokenize(Object obj) {
        if (obj instanceof Map && ((Map<String,Object>) obj).containsKey("token")) {
            return ((Map<String,Object>) obj).get("token");
        }
        return obj;
    }

    /**
     * Helper to get value of a string valued attribute.
     * @param name: the name of the attribute
     * @return the String value
     */
    public String getStringAttribute(String name) {
        return (String) detokenize(getAttribute(name));
    }

    //TODO: Dates will be likely be Strings, so we can simplify this in a REST-only world.
    public Date getDateAttribute(String name) {
        if (getAttribute(name) != null) {
            Object dateObject = getAttribute(name);
            if (dateObject instanceof Date) {
                return (Date) dateObject;
            } else {
                return JSON.stringToDate((String) dateObject);
            }
        } else {
            return null;
        }
    }

    public List<String> getStringsAttribute(String name) {
        if (getAttribute(name) != null) {
            List<String> values = new ArrayList<>();
            Object resultsObj = getAttribute(name);
            if (resultsObj instanceof Object[]) {
                Object[] results = (Object[]) resultsObj;
                for (Object result: results) {
                    result = detokenize(result);
                    if (result instanceof String) {
                        values.add((String) result);
                    } else {
                        //add a String representation as a last resort
                        values.add(result.toString());
                    }
                }
            } else if (resultsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> results = (List<Object>) resultsObj;
                for (Object result: results) {
                    result = detokenize(result);
                    if (result instanceof String) {
                        values.add((String) result);
                    } else {
                        //add a String representation as a last resort
                        values.add(result.toString());
                    }
                }
            }
            return values;
        } else {
            return new ArrayList<>();
        }
    }

    public String getId() {
        return getStringAttribute("id");
    }

    public String getUid() {
        return getStringAttribute("UID");
    }

    public String getTitle() {
        return getStringAttribute("title");
    }

    public String getDescription() {
        return getStringAttribute("description");
    }

    /**
     * @return The workflow status of the object (i.e. 'published', 'private', ...)
     */
    public String getWorkflowStatus() {
        try {
            JSON.Node rspNode = privateService.nodeFromGetRequest(pathAfterPlonesite+"/@workflow");
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return null;
            }
            return rspNode.get("state").get("id").toString();
        } catch (Exception ex) {
            log.log(ERROR, exceptionToString(ex));
            return null;
        }
    }

    /**
     * Attempts to execute a transition to set a new workflow status.
     * @param transition: the name of the transition
     */
    public void setWorkflowStatus(String transition) {
        try {
            String endpoint = "/@workflow";
            JSON.Node transNode = new JSON.Node("{}");
            switch (transition) {
                case "publish":
                    transNode
                        .set("action","publish")
                        .set("review_state", "published")
                    ;
                    endpoint = endpoint + "/publish";
                    break;
                case "retract":
                default:
                    transNode
                        .set("action","retract")
                        .set("review_state", "private")
                    ;
                    endpoint = endpoint + "/retract";
                    break;
            }
            JSON.Node rspNode = privateService.postRequestWithNode(pathAfterPlonesite+endpoint, transNode);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
            }
        } catch (Exception ex) {
            log.log(ERROR, exceptionToString(ex));
        }

    }

    protected List<Object> ensureObjectIsList(Object o) {
        if (o instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) o;
            return list;
        } else if (o.getClass().isArray()) {
            List<Object> list = Arrays.<Object>asList((Object[]) o);
            return list;
        } else {
            String clazz = o != null ? o.getClass().toString() : "null";
            log.log(ERROR, "Cannot convert to list, object has class: " + clazz);
            return null;
        }

    }

    //TODO: merge with checkAttrNode?
    protected void attributeCompatibilityAdjustment(Map<String,Object> attributes) {
        if (attributes == null) {
            return;
        }
        for (Map.Entry<String,Object> entry: attributes.entrySet()) {
            switch (entry.getKey()) {
                case "events":
                case "scenarios":
                    attributes.put(entry.getKey(),eventIdsToUids(ensureObjectIsList(entry.getValue())));
                    break;
                default:
            }
        }
    }

    protected List<Object> eventIdsToUids(List<Object> eventIds) {
        List<Object> ret = new ArrayList<Object>();
        //we assume the event ids refer to events of the current BasePbject's Dokpool
        String dpId = docPoolId();
        for (Object evIdObj : eventIds) {
            String evId = (String) evIdObj;
            String uid = (new de.bfs.dokpool.client.content.Event(service, "/"+dpId+"/contentconfig/scen/"+evId, noData)).getStringAttribute("UID");
            if (uid == null) {
                //Maybe the evId already was a uid? Then we will get a non-null path fot it:
                if (privateService.uidToPathAfterPlonesite(evId) != null) {
                    uid = evId;
                    ret.add(uid);
                } else {
                    log.log(ERROR, "Could not get event uid for event with id: " + evId);
                }
            } else {
                ret.add(uid);
            }
        }
        return ret;
    }

    protected void checkAttrNodeUpdate(JSON.Node attrNode) throws Exception {}

    /**
     * Update the object's attributes with the given map.
     * Any attribute that is not explicitly set will keep its value;
     * @param attributes
     * @return true, if the update succeeded; false otherwise
     */
    public boolean update(Map<String, Object> attributes) {
        return update(attributes, true);
    }

    /**
     * Update the object's attributes with the given map (internal method).
     * Any attribute that is not explicitly set will keep its value;
     * @param attributes
     * @param doChecks: if false, no checks are performed prior to http request;
     *                  Only do this, if you checked the Dokpool REST spec.
     * @return true, if the update succeeded; false otherwise
     */
    protected boolean update(Map<String, Object> attributes, boolean doChecks) {
        /* we reset the data, as setting some attribute to a new value
         * might trigger changes in other attributes
         */
        data = null;
        dataComplete = false;
        if (doChecks) {
            //Some attributes (e.g. scenarios) need to be handled differently in Plone6
            attributeCompatibilityAdjustment(attributes);
        }
        try {
            JSON.Node attrNode = new JSON.Node(attributes);
            log.log(INFO, "update: " + attrNode.toJSON());
            if (doChecks) {
                checkAttrNodeUpdate(attrNode);
            }
            JSON.Node rspNode = privateService.patchRequestWithNode(pathAfterPlonesite, attrNode);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.log(ERROR, exceptionToString(ex));
            return false;
        }
    }

    /**
     * Update a single attribute with a given value.
     * To unset a value set, set it to null.
     * @param name the name of the attribute
     * @param value its designated value
     */
    public boolean setAttribute(String name, Object value) {
        Map<String,Object> attribute = new HashMap<String,Object>();
        attribute.put(name,value);
        return update(attribute);
    }

    /**
     * Deletes this BaseObject.
     * @return true if deletion succeeds, false otherwise
     */
    public boolean delete() {
        try {
            JSON.Node rspNode = privateService.deleteRequest(pathAfterPlonesite);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return false;
            }
            return true;
        } catch (Exception ex) {
            log.log(ERROR, exceptionToString(ex));
            return false;
        }
    }

    protected static String exceptionToString(Exception ex) {
        return DokpoolBaseService.exceptionToString(ex);
    }

    protected Map<String,Object> dataFromNode(JSON.Node node) {
        try {
            return node.toMap();
        } catch (Exception ex) {
            log.log(ERROR, exceptionToString(ex));
            return null;
        }
    }


}
