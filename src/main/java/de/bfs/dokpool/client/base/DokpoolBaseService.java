/* Copyright (C) 2015-2025 by Bundesamt fuer Strahlenschutz
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 * See LICENSE for details.
 */

package de.bfs.dokpool.client.base;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bfs.dokpool.client.content.DocumentPool;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;


/**
 * The root class to access REST-API services for DOKPOOL.
 *
 */
public class DokpoolBaseService {
    private final java.lang.System.Logger log = System.getLogger(DokpoolBaseService.class.getName());

    protected Object NTS(Object mayBeNull) {
        return mayBeNull != null ? mayBeNull : "null";
    }

    private String proto;
    private String host;
    private String port;
    /*package-private*/ String plonesite;
    private String username;
    private String password;
    private final String urlPrefix;
    private final int urlPrefixLength;

    public final boolean allowCaching;
    public static final boolean NOCACHING = false;

    public final Set<String> exceptionPolicy;
    /** Policy: Do not throw DokpoolRuntimeExceptions. */
    public static final Set<String> NOEXCEP = Set.of();
    /** Policy: Throw DokpoolRuntimeExceptions if object creation fails. */
    public static final String OBJCREXCEP = "OBJCREXCEP";
    /** Policy: Throw DokpoolRuntimeExceptions if document pool(s) cannot be optained. */
    public static final String DOCPEXCEP = "DOCPEXCEP";
    /** Policy: Throw all currently available DokpoolRuntimeExceptions. */
    public static final Set<String> ALLEXCEP = Set.of(OBJCREXCEP, DOCPEXCEP);

    /*package-private*/ PrivateDokpoolBaseService privateService;

    /**
     * Get a service object.
     *
     * @param url:
     *            the address of the ELAN instance root
     * @param username
     * @param password
     * @param caching (default: true) whether or not to cache metadata and folder contents
     */
    public DokpoolBaseService(String url, String username, String password) {
        this(url, username, password, true);
    }

    /**
     * Get a service object from a config map.
     *
     * The arguments for the otheer constructors can be used as keys.
     * exceptionPolicy is an additional key that controls which runtime
     * exceptions are thrown. You can either use url or proto+host+port+plonesite.
     * If "url" is present, the other values are ignored.
     *
     * @param config a map containing the config arguments.
     */
    public DokpoolBaseService(Map<String,Object> config) {
        //new REST-Code:
        this.privateService = new PrivateDokpoolBaseService(this);

        String url = (String) config.get("url");
        if (url != null) {
            try {
                URL urlObject = new URL(url);
                this.proto = urlObject.getProtocol();
                this.host = urlObject.getHost();
                this.port = urlObject.getPort() == -1 ? "" : Integer.toString(urlObject.getPort());
                this.plonesite = urlObject.getPath();
                this.plonesite = this.plonesite.startsWith("/") ? this.plonesite.substring(1) : this.plonesite;
            } catch (MalformedURLException mue) {
                log.log(/*fatal*/ERROR, "Incorrect URL provided!", mue);
            }
        } else {
            this.proto = (String) config.get("proto");
            this.host = (String) config.get("host");
            this.port = config.get("port") != null? (String) config.get("port") : "";
            String ps = (String) config.get("plonesite");
            this.plonesite = ps.startsWith("/") ? ps.substring(1) : ps;
        }

        this.urlPrefix = HttpClient.composeUrl(this.proto,this.host,this.port,"/"+ this.plonesite);
        this.urlPrefixLength = urlPrefix.length();

        this.username = (String) config.get("username");
        this.password = (String) config.get("password");
        this.allowCaching = config.get("caching") != null ? (Boolean) config.get("caching") : true;
        Object exPol = config.get("exceptionPolicy");
        if (exPol instanceof String) {
            this.exceptionPolicy = Set.of((String) exPol);
        } else if (exPol instanceof Set) {
            @SuppressWarnings("unchecked")
            Set<String> s = (Set<String>) exPol;
            this.exceptionPolicy = s;
        } else {
            this.exceptionPolicy = NOEXCEP;
        }
    }

    /**
     * Get a service object.
     *
     * @param url:
     *            the address of the ELAN instance root
     * @param username
     * @param password
     * @param caching (default: true) whether or not to cache metadata and folder contents
     */
    public DokpoolBaseService(String url, String username, String password, boolean caching) {
        //new REST-Code:
        this.privateService = new PrivateDokpoolBaseService(this);
        try {
            URL urlObject = new URL(url);
            this.proto = urlObject.getProtocol();
            this.host = urlObject.getHost();
            this.port = urlObject.getPort() == -1 ? "" : Integer.toString(urlObject.getPort());
            this.plonesite = urlObject.getPath();
            this.plonesite = this.plonesite.startsWith("/") ? this.plonesite.substring(1) : this.plonesite;
            this.username = username;
            this.password = password;
        } catch (MalformedURLException mue) {
            log.log(/*fatal*/ERROR, "Incorrect URL provided!", mue);
        }
        this.urlPrefix = HttpClient.composeUrl(this.proto,this.host,this.port,"/"+ this.plonesite);
        this.urlPrefixLength = urlPrefix.length();
        this.allowCaching = caching;
        this.exceptionPolicy = NOEXCEP;
    }

    /**
     * Get a service object.
     *
     * @param proto http/https
     * @param host name or ip of the host
     * @param port can be null or "" if standard for http or https, respectively
     * @param plonesite usually "dokpool"
     * @param username
     * @param password
     */
    public DokpoolBaseService(String proto, String host, String port, String plonesite, String username, String password) {
        this(proto, host, port, plonesite, username, password, true);
    }

    /**
     * Get a service object.
     *
     * @param proto http/https
     * @param host name or ip of the host
     * @param port can be null or "" if standard for http or https, respectively
     * @param plonesite usually "dokpool"
     * @param username
     * @param password
     * @param caching (default: true) whether or not to cache metadata and folder contents
     */
    public DokpoolBaseService(String proto, String host, String port, String plonesite, String username, String password, boolean caching) {
        this.privateService = new PrivateDokpoolBaseService(this);
        this.urlPrefix = HttpClient.composeUrl(proto,host,port,"/"+ plonesite);
        this.urlPrefixLength = urlPrefix.length();
        this.proto = proto;
        this.host = host;
        this.port = port != null? port : "";
        this.plonesite = plonesite.startsWith("/") ? plonesite.substring(1) : plonesite;
        this.username = username;
        this.password = password;
        this.allowCaching = true;
        this.exceptionPolicy = NOEXCEP;
    }

    public String pathWithoutPrefix(String path) {
        return path.substring(urlPrefixLength);
    }

    private String uidToPathAfterPlonesite(String uid) {
        try {
            HttpClient.Response rsp = HttpClient.doGetRequest(proto,host,port,urlPrefix+"/resolveuid/"+uid,defaultHeaders());
            if (rsp.status == 404 || rsp.headers.get("Location") == null) {
                return null;
            }
            return rsp.headers.get("Location").substring(urlPrefixLength);
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            return null;
        }
    }

    public String pathWithoutPrefix(JSON.Node node) {
        String atid = node.get("@id").toString();
        //If the `@id` is a dview, we need to fetch the actual path differently.
        //In this case, the path is always realtive like: "esd/..." for ELAN,
        //so it will not start with the urlPrefix
        if (!atid.startsWith(urlPrefix)) {
            String uid = node.get("UID") != null ? node.get("UID").toString() : null;
            if (uid == null) { //The node has no attribute `UID`, so we will get it from the dview String
                int uidStart = atid.indexOf("@@dview?d=")+10;
                int uidEnd = atid.indexOf("&", uidStart);
                uid = atid.substring(uidStart, uidEnd);
                log.log(INFO, "uid from dview: "+ uid);
            }
            String path = uidToPathAfterPlonesite(uid);
            if (path == null) {
                log.log(ERROR, "UID " + uid + " cannot be resolved");
            } else {
                log.log(INFO, "path from UID: " + path);
            }
            return path;
        }
        return node.get("@id").toString().substring(urlPrefixLength);
    }

    public String getUsername() {
        return username;
    }

    //TODO: we also log the execptions in mosy cases, so this might be no longer neeededs
    /*package-private*/ static String exceptionToString(Exception ex) {
        Writer stBuffer = new StringWriter();
        PrintWriter stPrintWriter = new PrintWriter(stBuffer);
        ex.printStackTrace(stPrintWriter);
        return ex.toString() + ": " + ex.getLocalizedMessage() + "\n" + stBuffer.toString();
    }

    private JSON.Node addErrorInfo(JSON.Node baseNode, HttpClient.Response rsp) throws DokpoolRuntimeException {
        if (baseNode == null || baseNode.type().equals("null")) {
            baseNode = new JSON.Node("null");
            baseNode.errorInfo = "REST response error: No content or null content";
            return baseNode;
        }
        if (baseNode.get("error") != null) {
            baseNode.errorInfo = baseNode.get("error").get("message");
            baseNode.errorInfo = "REST response error: " + (baseNode.errorInfo != null ? baseNode.errorInfo : "error");
            return baseNode;
        }
        final List<Integer> errorCodes = Arrays.asList(400, 401, 403, 404, 405, 409, 500);
        if (errorCodes.contains(rsp.status)) {
            String message = baseNode.get("message") != null ? "; " + baseNode.get("message").toString() : "";
            baseNode.errorInfo = "REST response error: HTTP " + rsp.status + message;
            return baseNode;
        }
        if (baseNode.get("type") != null &&  baseNode.get("message") != null) {
            baseNode.errorInfo = "REST response error: " + baseNode.get("message");
            return baseNode;
        }
        return baseNode;
    }

    /**
     *
     * @return A Map with authentication and accept (JSON) headers.
     */
    private Map<String,String> defaultHeaders() {
        Map<String,String> headers = new HashMap<String,String>();
        headers.put(HttpClient.Headers.ACCEPT,HttpClient.MimeTypes.JSON);
        HttpClient.addBasicAuthToHeaders(headers,username,password);
        return headers;
    }

    private JSON.Node nodeFromGetRequest(String endpoint, String queryString) throws DokpoolRuntimeException {
        queryString = (queryString == null || queryString.equals("")) ? "" : ("?"+queryString);
        String path = urlPrefix + endpoint;
        HttpClient.Response    rsp;
        rsp = HttpClient.doGetRequest(proto,host,port,path+queryString,defaultHeaders());
        log.log(INFO, "response content length: " + rsp.content.length());
        JSON.Node node = new JSON.Node(rsp.content);
        if (node != null && node.get("batching") != null) {
            long itemsTotal = node.get("items_total").toLong();
            if (queryString.equals("")) {
                queryString = "?b_size=" + itemsTotal;
            } else {
                queryString += "&b_size=" + itemsTotal;
            }
            rsp = HttpClient.doGetRequest(proto,host,port,path+queryString,defaultHeaders());
            log.log(INFO, "response content length: " + rsp.content.length());
            node = new JSON.Node(rsp.content);
        }
        return addErrorInfo(node,rsp);
    }

    private JSON.Node nodeFromGetRequest(String endpoint) throws DokpoolRuntimeException {
        return nodeFromGetRequest(endpoint, null);
    }

    private JSON.Node patchRequestWithNode(String endpoint, JSON.Node patchNode) throws DokpoolRuntimeException {
        String patchUrl = urlPrefix + endpoint;
        byte[] patchData = patchNode.toJSON().getBytes();
        HttpClient.Response rsp = HttpClient.doPatchRequest(proto,host,port,patchUrl,defaultHeaders(),HttpClient.MimeTypes.JSON,patchData);
        return addErrorInfo(new JSON.Node(rsp.content), rsp);
    }

    private JSON.Node postRequestWithNode(String endpoint, JSON.Node patchNode) throws DokpoolRuntimeException {
        String patchUrl = urlPrefix + endpoint;
        byte[] patchData = patchNode.toJSON().getBytes();
        HttpClient.Response rsp = HttpClient.doPostRequest(proto,host,port,patchUrl,defaultHeaders(),null,HttpClient.MimeTypes.JSON,patchData);
        return addErrorInfo(new JSON.Node(rsp.content), rsp);
    }

    private JSON.Node deleteRequest(String endpoint) throws DokpoolRuntimeException {
        String path = urlPrefix + endpoint;
        HttpClient.Response rsp = HttpClient.doDeleteRequest(proto,host,port,path,defaultHeaders());
        return addErrorInfo(new JSON.Node(rsp.content), rsp);
    }

    private void throwCreateDRE(String message, Throwable cause, String policy) {
        if (exceptionPolicy.contains(policy)) {
            throw new DokpoolRuntimeException(message, cause);
        }
    }

    private void throwCreateDRE(DokpoolRuntimeException ex, String policy) {
        if (exceptionPolicy.contains(policy)) {
            throw ex;
        }
    }

    /**
     * Get all ESDs available to the current user.
     * Calls the endpoint /@get_documentpools.
     *
     * @return a list of Dokpools
     */
    public List<DocumentPool> getDocumentPools() {
        return getDocumentPools("");
    }

    /**
     * Get all ESDs available to a user.
     * Calls the endpoint /@get_documentpools.
     *
     * @param user The user for whom we want to get the Dokpools.
     * @return a list of Dokpools
     */
    public List<DocumentPool> getDocumentPools(String user) {
        user = (user == null || user == "") ? "" : ("/"+user);
        String ep = "/@get_documentpools" + user;
        JSON.Node rspNode = null;
        try {
            rspNode = nodeFromGetRequest(ep);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                privateService.throwCreateDRE(rspNode.errorInfo.toString(), null, DOCPEXCEP);
                return null;
            }
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            privateService.throwCreateDRE(dre, DOCPEXCEP);
            return null;
        }
        List<DocumentPool> dpList = new ArrayList<>();
        for (JSON.Node child : rspNode) {
            dpList.add(new DocumentPool(this, "/"+child.toString(), Map.of("id", child.toString())));
        }
        return dpList;
    }

    /**
     *
     * Calls the endpoint /@get_primary_documentpool.
     * @return the ESD, which the user is a member of - or the first available ESD
     *         for global users.
     *
     */
    public DocumentPool getPrimaryDocumentPool() {
        return getPrimaryDocumentPool("");
    }

    /**
     * Get the primary Dokpool for user `user`.
     * Calls the endpoint /@get_primary_documentpool.
     * @param user The user for which we want to get the dokpool,
     *             if empty or null, this will return the primary Dokpool for the login user.
     * @return the ESD, which the user is a member of - or the first available ESD
     *         for global users.
     *
     */
    public DocumentPool getPrimaryDocumentPool(String user) {
        user = (user == null || user == "") ? "" : ("/"+user);
        String ep = "/@get_primary_documentpool" + user;
        JSON.Node rspNode = null;
        try {
            rspNode = nodeFromGetRequest(ep);
            if (rspNode.errorInfo != null) {
                log.log(INFO, rspNode.errorInfo.toString());
                privateService.throwCreateDRE(rspNode.errorInfo.toString(), null, DOCPEXCEP);
                return null;
            }
            return new DocumentPool(this, pathWithoutPrefix(rspNode), rspNode.toMap());
        } catch (DokpoolRuntimeException dre) {
            log.log(ERROR, exceptionToString(dre), dre);
            privateService.throwCreateDRE(dre, DOCPEXCEP);
            return null;
        }
    }

    /**
     * This class only exists as a visibility hack across (sub)packages.
     */
    public class PrivateDokpoolBaseService {
        DokpoolBaseService service;
        PrivateDokpoolBaseService(DokpoolBaseService service) {
            this.service = service;
        }

        public String uidToPathAfterPlonesite(String uid) {
            return service.uidToPathAfterPlonesite(uid);
        };
        public JSON.Node nodeFromGetRequest(String endpoint, String queryString) throws DokpoolRuntimeException {
            return service.nodeFromGetRequest(endpoint,queryString);
        }
        public JSON.Node nodeFromGetRequest(String endpoint) throws DokpoolRuntimeException {
            return service.nodeFromGetRequest(endpoint);
        }
        public JSON.Node patchRequestWithNode(String endpoint, JSON.Node patchNode) throws DokpoolRuntimeException {
            return service.patchRequestWithNode(endpoint, patchNode);
        }
        public JSON.Node postRequestWithNode(String endpoint, JSON.Node patchNode) throws DokpoolRuntimeException {
            return service.postRequestWithNode(endpoint, patchNode);
        }
        public JSON.Node deleteRequest(String endpoint) throws DokpoolRuntimeException {
            return service.deleteRequest(endpoint);
        }
        public void throwCreateDRE(String message, Throwable cause, String policy) {
            service.throwCreateDRE(message, cause, policy);
        }
        public void throwCreateDRE(DokpoolRuntimeException dre, String policy) {
            service.throwCreateDRE(dre, policy);
        }
    }

}
