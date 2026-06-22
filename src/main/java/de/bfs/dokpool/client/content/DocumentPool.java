/* Copyright (C) 2015-2025 by Bundesamt fuer Strahlenschutz
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 * See LICENSE for details.
 */

package de.bfs.dokpool.client.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.bfs.dokpool.client.base.DokpoolBaseService;
import de.bfs.dokpool.client.base.DokpoolRuntimeException;
import de.bfs.dokpool.client.base.JSON;

/**
 * Wraps the ELANESD type
 *
 */
public class DocumentPool extends Folder {

    @Deprecated
    public DocumentPool(DokpoolBaseService service, String path, Object[] data) {
        super(service, path, data);
    }

    public DocumentPool(DokpoolBaseService service, String path, Map<String,Object> data) {
        super(service, path, data);
    }

    /**
     * @return all DocTypes within this Pool
     */
    @SuppressWarnings("unused")
    public List<DocType> getTypes() {
        JSON.Node typeListNode = null;
        try {
            typeListNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/@types");
            if (typeListNode.errorInfo != null) {
                log.log(INFO, typeListNode.errorInfo.toString());
                return null;
            }
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
        if (typeListNode != null) {
            ArrayList<DocType> res = new ArrayList<DocType>();
            for (JSON.Node typeNode : typeListNode) {
                res.add(new DocType(service, service.pathWithoutPrefix(typeNode), dataFromNode(typeNode)));
            }
            return res;
        } else {
            return null;
        }
    }

    /**
     * @return all supported Apps as lowercase strings.
     */
    public List<String> getSupportedApps() {
        List<String> apps = new ArrayList<String>();

        Object appObj = getAttribute("supportedApps");
        if (appObj == null || !(appObj instanceof List)) {
            log.log(ERROR, "supportedApps should be an array.");
            return null;
        }
        @SuppressWarnings("unchecked")
        List<Map<String,String>> appMapList = (List<Map<String,String>>) appObj;
        for (Map<String,String> app : appMapList){
            apps.add(app.get("token"));
        }

        return apps;
    }

    /**
     * @return all Events within this document pool
     */
    public List<Event> getEvents() {
        JSON.Node itemsNode = null;
        try {
            JSON.Node rspNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/contentconfig/scen/@search", "portal_type=DPEvent&metadata_fields=id");
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return null;
            }
            itemsNode = rspNode.get("items");
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
        if (itemsNode != null) {
            ArrayList<Event> res = new ArrayList<Event>();
            for (JSON.Node eventNode : itemsNode) {
                res.add(new Event(service, service.pathWithoutPrefix(eventNode), dataFromNode(eventNode)));
            }
            return res;
        } else {
            return null;
        }
    }

    /**
     * @return all active Events within this document pool
     */
    public List<Event> getActiveEvents() {
        JSON.Node itemsNode = null;
        try {
            JSON.Node rspNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/contentconfig/scen/@search", "portal_type=DPEvent&dp_type=active&metadata_fields=id&metadata_fields=UID");
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return null;
            }
            itemsNode = rspNode.get("items");
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
        if (itemsNode != null) {
            ArrayList<Event> res = new ArrayList<Event>();
            for (JSON.Node eventNode : itemsNode) {
                res.add(new Event(service, service.pathWithoutPrefix(eventNode), dataFromNode(eventNode)));
            }
            return res;
        } else {
            return null;
        }
    }

    /**
     * @return all ids of active events within this document pool
     */
    protected Map<String,Event> getActiveEventsMap() {
        List<Event> ativeEvList = getActiveEvents();
        Map<String,Event> res = new HashMap<String,Event>();
        for (Event ev: ativeEvList) {
            res.put(ev.getId(), ev);
            res.put(ev.getUid(), ev);
        }
        return res;
    }

    /**
     * @return all active Events within this document pool
     */
    public List<String> filterInactiveEventIds(List<String> evIdList) {
        List<String> evIdFiltred = new ArrayList<String>();
        Map<String,Event> activeEvMap = getActiveEventsMap();
        for (String iu : evIdList) {
            if (activeEvMap.containsKey(iu)) {
                evIdFiltred.add(iu);
            }
        }
        return evIdFiltred;
    }

    /**
     * @return the event with given eventId or null if no such event exists
     */
    public Event getEventById(String eventId) {
        String evPathAfterPloneSite = pathAfterPlonesite + "/contentconfig/scen/" + eventId;
        try {
            JSON.Node rspNode = privateService.nodeFromGetRequest(evPathAfterPloneSite);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return null;
            }
            return new Event(service, evPathAfterPloneSite, rspNode.toMap());
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    /**
     * @return the event with given eventUid or null if no such event exists
     */
    public Event getEventByUid(String eventUid) {
        String evPathAfterPloneSite = privateService.uidToPathAfterPlonesite(eventUid);
        if (evPathAfterPloneSite == null) {
            log.log(INFO, "UID " + eventUid + " refers to no object.");
            return null;
        }
        try {
            JSON.Node rspNode = privateService.nodeFromGetRequest(evPathAfterPloneSite);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return null;
            }
            if (NTS(rspNode.get("@type")).toString().equals("DPEvent")) {
                return new Event(service, evPathAfterPloneSite, rspNode.toMap());
            } else {
                log.log(INFO, "UID " + eventUid + "refers to no object that is not an event.");
                return null;
            }
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    /**
     * @return the user folder of the current user (via @get_user_folder)
     */
    public Folder getUserFolder() {
        try {
            //the Dokpool is an argument to the endpoint, so we append ist
            JSON.Node folderNode = privateService.nodeFromGetRequest("/@get_user_folder" + pathAfterPlonesite);
            if (folderNode.errorInfo != null) {
                log.log(INFO, folderNode.errorInfo.toString());
                return null;
            }
            return new Folder(service, service.pathWithoutPrefix(folderNode), folderNode.toMap());
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    /**
     * Get user folders for any user. This does **not** use a special endpoint, but
     * as the endpoint get_user_folder constructs its path with the constant
     * "{esdpath}/content/Members/{username}", we can do the same.
     * @return the user folder under /DocumentPool.fullpath()/content/Members/&lt;user&gt;
     */
    public Folder getUserFolder(String user) {
        if (user.contains("-") && !user.contains("--")) {
            user = user.replaceAll("-","--");
        }
        try {
            JSON.Node folderNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/content/Members/" + user);
            if (folderNode.errorInfo != null) {
                log.log(INFO, folderNode.errorInfo.toString());
                return null;
            }
            return new Folder(service, service.pathWithoutPrefix(folderNode), folderNode.toMap());
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    /**
     * Get the current user's group folders.
     * The corresponding API endpoint get_group_folders gets all groups for the current user
     * and searches for folder with theier ids in /DocumentPool.fullpath()/content/Groups.
     * @return all group folders for the current user
     */
    public List<Folder> getGroupFolders() {
        try {
            //the Dokpool is an argument to the endpoint, so we append ist
            JSON.Node gfListNode = privateService.nodeFromGetRequest("/@get_group_folders" + pathAfterPlonesite, "metadata_fields=id");
            if (gfListNode.errorInfo != null) {
                log.log(INFO, gfListNode.errorInfo.toString());
                return null;
            }
            ArrayList<Folder> res = new ArrayList<Folder>();
            for (JSON.Node gfNode : gfListNode.get("items")) {
                res.add(new Folder(service, service.pathWithoutPrefix(gfNode), dataFromNode(gfNode)));
            }
            return res;
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    /**
     * This method returns all group folders, irrespective of the current users membership
     * within any of the returned groups (via. DocumentPool.fullpath()//content/Groups/).
     * @return all group folders of the current dokpool.
     */
    public List<Folder> getAllGroupFolders() {
        try {
            JSON.Node gfListNode = privateService.nodeFromGetRequest(pathAfterPlonesite + "/content/Groups/", "metadata_fields=id");
            if (gfListNode.errorInfo != null) {
                log.log(INFO, gfListNode.errorInfo.toString());
                return null;
            }
            ArrayList<Folder> res = new ArrayList<Folder>();
            for (JSON.Node gfNode : gfListNode.get("items")) {
                res.add(new Folder(service, service.pathWithoutPrefix(gfNode), dataFromNode(gfNode)));
            }
            return res;
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    /**
     * @return all transfer folders for this Dokpool.
     */
    public List<Folder> getTransferFolders() {
        try {
            //the Dokpool is an argument to the endpoint, so we append ist
            JSON.Node tfListNode = privateService.nodeFromGetRequest("/@get_transfer_folders" + pathAfterPlonesite, "metadata_fields=id");
            if (tfListNode.errorInfo != null) {
                log.log(INFO, tfListNode.errorInfo.toString());
                return null;
            }
            ArrayList<Folder> res = new ArrayList<Folder>();
            for (JSON.Node tfNode : tfListNode.get("items")) {
                res.add(new Folder(service, service.pathWithoutPrefix(tfNode), dataFromNode(tfNode)));
            }
            return res;
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    /**
     * Creates a new user.
     * @param dp the primary dokpool for the new user (e.g. "hessen")
     * @return the created User.
     */
    public User createUser(String userId, String password, String fullname, String dp, String email) {
        try {
            String dpUid = null;
            if (dp != null && !dp.equals("")) {
                dp = dp.startsWith("/") ? dp : "/"+dp;
                DocumentPool docPool = new DocumentPool(service, dp, (Object[]) null);
                dpUid = docPool.getStringAttribute("UID");
            }
            JSON.Node createJS = new JSON.Node("{}")
                .set("username", userId)
                .set("email", email)
                .set("password", password)
                .set("fullname", fullname)
                .set("dp", dpUid)
            ;
            JSON.Node rspNode = privateService.postRequestWithNode("/@users", createJS);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return null;
            }
            return new User(service, service.pathWithoutPrefix(rspNode), userId, password, fullname, dp);
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    /**
     * Creates a new user.
     *
     * This version sets the email address to "ihotline@bfs.de".
     * @param dp the primary dokpool for the new user (e.g. "hessen").
     * @deprecated Please also provide an actual email adress if you can.
     * @return the created User.
     */
    @Deprecated
    public User createUser(String userId, String password, String fullname, String dp) {
        return createUser(userId, password, fullname, dp, "ihotline@bfs.de");
    }

    /**
     * Deletes the given user.
     * DO NOT USE. Currently causes an error on the server side that crashes
     * @return true if deletion succeds, false otherwise
     */
    public boolean deleteUser(String userId) {
        try {
            JSON.Node rspNode = privateService.deleteRequest("/@users/"+userId);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return false;
            }
            return true;
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return false;
        }
    }

    /**
     * Creates a new user. Currently the dokpool dp cannot be set and is silently ignored.
     * @return the created User.
     */
    public Group createGroup(String groupId, String title, String description, String dp) {
        try {
            @SuppressWarnings("unused")
            String dpUid = null;
            if (dp != null && !dp.equals("")) {
                dp = dp.startsWith("/") ? dp : "/"+dp;
                DocumentPool docPool = new DocumentPool(service, dp, (Object[]) null);
                dpUid = docPool.getStringAttribute("UID");
            }
            JSON.Node createJS = new JSON.Node("{}")
                .set("groupname", groupId)
                .set("title", title)
                .set("description", description)
                //TODO: can wie set dp from REST? Is it used?
                //the following line has to visible effect
                // .set("dp", dpUid)
            ;
            JSON.Node rspNode = privateService.postRequestWithNode("/@groups", createJS);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return null;
            }
            return new Group(service, service.pathWithoutPrefix(rspNode), groupId, title, description, dp);
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    /**
     * Deletes the given group.
     * This methods actually works (but see deleteUser).
     * @return true if deletion succeds, false otherwise
     */
    public boolean deleteGroup(String groupId) {
        try {
            JSON.Node rspNode = privateService.deleteRequest("/@groups/"+groupId);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                return false;
            }
            return true;
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return false;
        }
    }

    public Optional<Folder> getGroupFolder(String name) {
        List<Folder> groupFolders = getGroupFolders();
        return groupFolders.stream().filter(folder -> folder.getId().equals(name)).findFirst();
    }
}
