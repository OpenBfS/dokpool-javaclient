/* Copyright (C) 2015-2025 by Bundesamt fuer Strahlenschutz
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 * See LICENSE for details.
 */

package de.bfs.dokpool.client.content;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bfs.dokpool.client.base.DokpoolBaseService;
import de.bfs.dokpool.client.base.DokpoolRuntimeException;
import de.bfs.dokpool.client.base.JSON;

/**
 * Wraps the ELANDocument / DPDocument type
 *
 */
public class Document extends Folder {
    @Deprecated
    public Document(DokpoolBaseService service, String path, Object[] alldata) {
        super(service, path, alldata);
    }

    public Document(DokpoolBaseService service, String path, Map<String,Object> data) {
        super(service, path, data);
    }

    /**
     * Uploads a file into the document.
     * This method never replaces an exising file.
     * If the id is not null and a file exists, upload is rejected.
     * If the id is null, a new file is created in any case.
     * @param id: short name for the file
     * @param title
     * @param description
     * @param data: binary data of the file
     * @param filename (used for display and download)
     * @param mimeType
     * @return the File object representing the file on the server
     */
    public File uploadFile(String id, String title, String description, byte[] data, String filename, String mimeType) {
        try {
            JSON.Node createJS = new JSON.Node("{}")
                .set("@type","File")
                .set("id", id)
                .set("title", title)
                .set("description", description)
                .set("file", new JSON.Node("{}")
                    .set("encoding", "base64")
                    .setNonNull("content-type", mimeType)
                    .set("data", new String(Base64.getEncoder().encode(data)))
                    .set("filename", filename)
                )
            ;
            invalidateContentsNode();
            JSON.Node rspNode = privateService.postRequestWithNode(pathAfterPlonesite, createJS);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return null;
            }
            String newpath = service.pathWithoutPrefix(rspNode);
            return new File(service, newpath, (Map<String,Object>) null);
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    /**
     * Uploads a file into the document.
     * This method never replaces an exising file.
     * If the id is not null and a file exists, upload is rejected.
     * If the id is null, a new file is created in any case.
     * @param id: short name for the file
     * @param title
     * @param description
     * @param data: binary data of the file
     * @param filename (used for display and download)
     * @return the File object representing the file on the server
     */
    public File uploadFile(String id, String title, String description, byte[] data, String filename) {
        return uploadFile(id, title, description, data, filename, null);
    }

    /**
     * Uploads a file into the document or replaces a file with the same id.
     * If the id is not null and a file exists, the file is replaced.
     * If the id is null, a new file is created in any case.
     * @param id: short name for the file
     * @param title
     * @param description
     * @param data: binary data of the file
     * @param filename (used for display and download)
     * @param mimeType
     * @return the File object representing the file on the server
     */
    public File uploadOrReplaceFile(String id, String title, String description, byte[] data, String filename, String mimeType) {
        //same behavior as uploadFile without an id
        if (id == null) {
            return uploadFile(id, title, description, data, filename, mimeType);
        }
        File oldFile = (File) getContentItem(id);
        if (oldFile == null) {
            return uploadFile(id, title, description, data, filename, mimeType);
        } else {
            oldFile.replace(title, description, data, filename, mimeType);
            return oldFile;
        }
    }

    protected static String mimeTypeFromFilename(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".")+1).toLowerCase();
        switch (ext) {
            case "bmp":
                return "image/bmp";
            case "eps":
                return "image/x-eps";
            case "gif":
                return "image/gif";
            case "jpeg":
            case "jpg":
                return "image/jpeg";
            case "pdf":
                return "application/pdf";
            case "png":
                return "image/png";
            case "svg":
                return "image/svg+xml";
            case "svgz":
                return "image/svg+xml-compressed";
            case "tif":
            case "tiff":
                return "image/tiff";
            default:
                return "text/plain";
        }
    }

    protected static String ensureImageMimeType(String mimeType) {
        if (mimeType.startsWith("image/")) {
            return mimeType;
        }
        return "image/" + mimeType.replaceAll("/", "_");
    }

    /**
     * Uploads an image into the document.
     * This method never replaces an exising image.
     * If the id is not null and an image already exists, upload is rejected.
     * If the id is null, a new image is created in any case.
     * @param id: short name for the image
     * @param title
     * @param description
     * @param data: binary data of the image
     * @param filename (used for display and download)
     * @param mimeType MIME-Type of the Image (e.g. image/png)
     * @return the Image object representing the image on the server
     */
    public Image uploadImage(final String id, final String title, final String description, final byte[] data, final String filename, String mimeType) {
        try {
            mimeType = mimeType != null ? Document.ensureImageMimeType(mimeType) : Document.mimeTypeFromFilename(filename);
            JSON.Node createJS = new JSON.Node("{}")
                .set("@type","Image")
                .set("id", id)
                .set("title", title)
                .set("description", description)
                .set("image", new JSON.Node("{}")
                    .set("encoding", "base64")
                    .set("content-type", ensureImageMimeType(mimeType))
                    .set("data", new String(Base64.getEncoder().encode(data)))
                    .set("filename", filename)
                )
            ;
            invalidateContentsNode();
            JSON.Node rspNode = privateService.postRequestWithNode(pathAfterPlonesite, createJS);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return null;
            }
            String newpath = service.pathWithoutPrefix(rspNode);
            return new Image(service, newpath, rspNode.toMap());
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    /**
     * Uploads an image into the document
     * This method never replaces an exising image.
     * If the id is not null and an image already exists, upload is rejected.
     * If the id is null, a new image is created in any case.
     * @param id: short name for the image
     * @param title
     * @param description
     * @param data: binary data of the image
     * @param filename The image type must be deducible from the file name extension (.jpeg/.jpg/.png)
     * @return the Image object representing the image on the server
     */
    public Image uploadImage(final String id, final String title, final String description, final byte[] data, final String filename) {
        return uploadImage(id, title, description, data, filename, mimeTypeFromFilename(filename));
    }

    /**
     * Uploads an image into the document or replaces an image with the same id.
     * If the id is not null and a image exists, the image is replaced.
     * If the id is null, a new image is created in any case.
     * @param id: short name for the file
     * @param title
     * @param description
     * @param data: binary data of the image
     * @param filename (used for display and download)
     * @param mimeType MIME-Type of the Image (e.g. image/png)
     * @return the Image object representing the image on the server
     */
    public Image uploadOrReplaceImage(String id, String title, String description, byte[] data, String filename, String mimeType) {
        //same behavior as uploadImage without an id
        if (id == null) {
            return uploadImage(id, title, description, data, filename, mimeType);
        }
        Image oldImg = (Image) getContentItem(id);
        if (oldImg == null) {
            return uploadImage(id, title, description, data, filename, mimeType);
        } else {
            oldImg.replace(title, description, data, filename, mimeType);
            return oldImg;
        }
    }

    /**
     * Assigns all events from the list to the document that are currently active.
     * @param evIdsUids: list of event ids and/or uids (can be mixed)
     * @return the list of those entries evIdsUids which correspond to active events,
     */
    public List<String> assignEventIdsUids(List<String> evIdsUids) {
        List<String> eventsUpdate = new ArrayList<String>();
        List<String> iuSet = new ArrayList<String>();
        DocumentPool dp = new DocumentPool(service, "/" + docPoolId(), (Map<String,Object>) null);
        Map<String,Event> activeEvMap = dp.getActiveEventsMap();
        for (String iu: evIdsUids) {
            if (activeEvMap.containsKey(iu)) {
                eventsUpdate.add(activeEvMap.get(iu).getUid());
                iuSet.add(iu);
            }
        }

        update(new HashMap<String,Object>() {{ put("scenarios", eventsUpdate); }});
        return iuSet;
    }

    /**
     * Assigns all events to the document that are currently active.
     * @return a list of ids of active events,
     */
    public List<String> assignAllActiveEvents() {
        List<String> eventsUpdate = new ArrayList<String>();
        List<String> iuSet = new ArrayList<String>();
        DocumentPool dp = new DocumentPool(service, "/" + docPoolId(), (Map<String,Object>) null);
        List<Event> activeEv = dp.getActiveEvents();
        for (Event ev: activeEv) {
            eventsUpdate.add(ev.getUid());
            iuSet.add(ev.getId());
        }

        update(new HashMap<String,Object>() {{ put("scenarios", eventsUpdate); }}, false);
        return iuSet;
    }

    @Override
    protected void checkAttrNodeUpdate(JSON.Node attrNode) throws DokpoolRuntimeException {
        JSON.Node newLocalBehaviors = attrNode.get("local_behaviors");
        if (newLocalBehaviors == null) {
            newLocalBehaviors = new JSON.Node("[]");
        }
        JSON.Node oldLocalBehaviors = new JSON.Node(getAllAttributes()).get("local_behaviors");
        if (oldLocalBehaviors == null) {
            oldLocalBehaviors = new JSON.Node("[]");
        } else {
            oldLocalBehaviors = oldLocalBehaviors.flattenArray("token");
        }

        if (newLocalBehaviors.arrayHasValue("doksys") || oldLocalBehaviors.arrayHasValue("doksys")) {
            boolean bahaviorAdded = newLocalBehaviors.arrayHasValue("doksys") &&
                !oldLocalBehaviors.arrayHasValue("doksys");
            doksysCheck(attrNode, bahaviorAdded);
        }

        if (newLocalBehaviors.arrayHasValue("rodos") || oldLocalBehaviors.arrayHasValue("rodos")) {
            boolean bahaviorAdded = newLocalBehaviors.arrayHasValue("rodos") &&
                !oldLocalBehaviors.arrayHasValue("rodos");
            rodosCheck(attrNode, bahaviorAdded);
        }

        if (newLocalBehaviors.arrayHasValue("rei") || oldLocalBehaviors.arrayHasValue("rei")) {
            boolean bahaviorAdded = newLocalBehaviors.arrayHasValue("rei") &&
                !oldLocalBehaviors.arrayHasValue("rei");
            reiCheck(attrNode, bahaviorAdded);
        }
    }


    protected static class Attribute {
        public final String name;
        public final boolean mandatory;
        public final Object defaultValue;

        Attribute(String name, boolean mandatory, Object defaultValue) {
            this.name = name;
            this.mandatory = mandatory;
            this.defaultValue = defaultValue;
        }
    }

    protected static class ChoiceAttribute extends Attribute {
        // public final String name;
        public final boolean isArray;
        // public final boolean mandatory;
        public final Set<String> values;
        public final Map<String,String> synonyms;

        ChoiceAttribute(String name, boolean isArray, boolean mandatory, Object defaultValue, Set<String> values) {
            this(name, isArray, mandatory, defaultValue, values, null);
        }

        ChoiceAttribute(String name, boolean isArray, boolean mandatory, Object defaultValue, Set<String> values, Map<String,String> synonyms) {
            super(name, mandatory, defaultValue);
            this.isArray = isArray;
            this.values = values;
            this.synonyms = synonyms;
        }
    }

    private static JSON.Node nodeTypeFittingToChoice(JSON.Node attrNode, ChoiceAttribute attr) throws DokpoolRuntimeException {
        JSON.Node in = attrNode.get(attr.name);
        if (in == null) {
            return null;
        }
        if (in.type().equals("array")) {
            if (attr.isArray) {
                return in;
            } else {
                log.log(INFO, "attribute \"" + attr.name +
                    "\" switched from array to single value.");
                attrNode.set(attr.name,in.get(0));
                return in.get(0);
            }
        } else {
            if (attr.isArray) {
                log.log(INFO, "attribute \"" + attr.name +
                    "\" switched from single value to array.");
                attrNode.set(attr.name,(new JSON.Node("[]")).append(in));
                return attrNode.get(attr.name);
            } else {
                return in;
            }
        }
    }


    private static void choiceCheck(ChoiceAttribute[] choiceAttrs, JSON.Node attrNode, boolean addMandatory) throws DokpoolRuntimeException {
        for (ChoiceAttribute attr : choiceAttrs) {
            // log.log(INFO, "attr" + attr.name + attrNode.get(attr.name).toJSON());
            JSON.Node childNode = nodeTypeFittingToChoice(attrNode, attr);
            Object isVal = childNode != null ? childNode.toString() : null;
            if (isVal != null && !attr.isArray && !attr.values.contains(isVal)) {
                if (attr.synonyms != null && attr.synonyms.containsKey(isVal)) {
                    String synVal = attr.synonyms.get(isVal);
                    log.log(WARNING, "attribute \"" + attr.name +
                        "\": value \"" + isVal.toString() + "\" replaced by \"" + synVal + "\"");
                    attrNode.set(attr.name, synVal);
                } else {
                    log.log(WARNING, "attribute \"" + attr.name +
                        "\": removed invalid value \"" + isVal.toString() + "\"");
                    isVal = null;
                    attrNode.remove(attr.name);
                }
            } else if (isVal != null && attr.isArray) {
                boolean rewrite = false;
                // int valid = 0;
                JSON.Node repl = new JSON.Node("[]");
                for (JSON.Node entryNode: attrNode.get(attr.name)) {
                    String entry = entryNode.toString();
                    if (!attr.values.contains(entry)) {
                        rewrite = true;
                        if (attr.synonyms != null && attr.synonyms.containsKey(entry)) {
                            String synEntry = attr.synonyms.get(entry);
                            log.log(WARNING, "attribute \"" + attr.name +
                                "\": array entry \"" + entry + "\" replaced by \"" + synEntry + "\"");
                            repl.append(synEntry);
                            // valid++;
                        } else {
                            log.log(WARNING, "attribute \"" + attr.name +
                                "\": removed invalid array entry \"" + entry + "\"");
                        }
                    } else {
                        repl.append(entry);
                        // valid++;
                    }
                }
                if (rewrite /*&& valid > 0*/) {
                    attrNode.set(attr.name, repl);
                }
                // empty list may be allowed or not.
                // do not simply remove it.
                // if (valid == 0) {
                //     isVal = null;
                //     attrNode.remove(attr.name);
                // }
            }
            if (addMandatory && attr.mandatory && attr.defaultValue != null && isVal == null) {
                log.log(WARNING, "missing attribute \"" + attr.name +
                    "\" set to default value \"" + attr.defaultValue + "\"");
                if (attr.defaultValue instanceof JSON.Node) {
                    attrNode.set(attr.name, (JSON.Node) attr.defaultValue);
                } else {
                    attrNode.set(attr.name, attr.defaultValue.toString());
                }
            } else if (addMandatory && attr.mandatory && attr.defaultValue == null && isVal == null) {
                log.log(ERROR, "missing attribute \"" + attr.name + "\" with no sensible default (likely rejected bey Dokpool).");
            }
        }
    }

    private static void dateCheck(Attribute[] dateAttrs, JSON.Node attrNode, boolean addMandatory) throws DokpoolRuntimeException {
        for (Attribute attr : dateAttrs) {
            // log.log(INFO, "attr" + attr.name + attrNode.get(attr.name).toJSON());
            JSON.Node childNode = attrNode.get(attr.name);
            String isVal = childNode != null ? childNode.toString() : null;
            if (isVal != null && JSON.stringToDate(isVal) == null) {
                log.log(WARNING, "attribute \"" + attr.name +
                    "\": removed invalid date value \"" + isVal.toString() + "\"");
                isVal = null;
                attrNode.remove(attr.name);
            }
            if (addMandatory && attr.mandatory && attr.defaultValue != null && isVal == null) {
                if (attr.defaultValue.toString().equals("now")) {
                    attrNode.set(attr.name, JSON.dateToString(Date.from(Instant.now())));
                } else {
                    attrNode.set(attr.name, attr.defaultValue.toString());
                }
                log.log(WARNING, "missing attribute \"" + attr.name +
                    "\" set to default value \"" + attrNode.get(attr.name).toString() + "\"");
            } else if (addMandatory && attr.mandatory && attr.defaultValue == null && isVal == null) {
                log.log(ERROR, "missing attribute \"" + attr.name + "\" with no sensible default (likely rejected bey Dokpool).");
            }
        }
    }

    protected static void doksysCheck(JSON.Node attrNode, boolean addMandatory) throws DokpoolRuntimeException {
        choiceCheck(AttributeSpec.doksysChoices, attrNode, addMandatory);
        dateCheck(AttributeSpec.doksysDates, attrNode, addMandatory);
    }

    protected static void rodosCheck(JSON.Node attrNode, boolean addMandatory) throws DokpoolRuntimeException {
        choiceCheck(AttributeSpec.rodosChoices, attrNode, addMandatory);
    }

    protected static void reiCheck(JSON.Node attrNode, boolean addMandatory) throws DokpoolRuntimeException {
        choiceCheck(AttributeSpec.reiChoices, attrNode, addMandatory);
    }

    /*
     * This is the current python implememtation:
        def autocreateSubdocuments(self):
       """
       TODO: specifically for XMLRPC usage
       """
       # * Von den allowed Types alle autocreatable Types durchgehen und ihre Muster "ausprobieren"
       # * Wenn Files oder Images gefunden zu einem Muster: entsprechendes DPDocument erzeugen und Files/Images verschieben
       return "ok"
     */
    /**
     * NOT IMPLEMENTED for REST-API (and was not for XMLRPC either).
     * @deprecated
     * @return null
     */
    @Deprecated public String autocreateSubdocuments() {
        return null;
    }

    /**
      * NOT IMPLEMENTED for REST-API
      * @deprecated If you need this functionality, fetch the file "properties.txt" within the document and set each property using setAttribute(name, value) yourself.
      * @return null
      */
    @Deprecated public String readPropertiesFromFile() {
        return null;
    }

    /**
     * Sets property name to the given value.
     * This Method can no longer create a new property with the REST-API,
     * wich was the only use of the argument `type`.
     * @deprecated Use BaseObject.setAttribute(name, value) instead
     * @param name
     * @param value
     * @param type ignored
     * @return
     */
    @Deprecated public String setProperty(final String name, final String value, final String type) {
        return setAttribute(name, value) ? "ok" : "error";
    }

    /**
     * Unsets property `name` (does not delete it).
     * This Method can no longer delete a property with the REST-API
     * @deprecated Use BaseObject.setAttribute(name, null) instead
     * @param name
     * @return
     */
    @Deprecated public String deleteProperty(final String name) {
        return setAttribute(name, null) ? "ok" : "error";
    }

    /**
     * Returns property `name`.
     * @deprecated Use BaseObject.getStringAttribute(name) instead
     * @param name
     * @return
     */
    @Deprecated public String getProperty(final String name) {
        return getStringAttribute(name);
    }

    /**
     * Returns all propties as a map.
     * @deprecated Use BaseObject.getAllAttributes() instead
     * @return map of all attributes (values are casted to (String))
     */
    @Deprecated public Map<String,String> getProperties() {
        Map<String,Object> soMap = getAllAttributes();
        Map<String,String> sMap = new HashMap<String,String>();
        for (Map.Entry<String,Object> entry: soMap.entrySet()) {
            sMap.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString(): null);
        }
        return sMap;
    }
}
