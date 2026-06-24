/* Copyright (C) 2015-2025 by Bundesamt fuer Strahlenschutz
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 * See LICENSE for details.
 */

package de.bfs.dokpool.client.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.bfs.dokpool.client.base.DokpoolBaseService;
import de.bfs.dokpool.client.base.DokpoolRuntimeException;

/**
 * Handels als list of documents consistently.
 *
 */
public class DocumentFamily {
    private List<Document> documents;

    public DocumentFamily(DokpoolBaseService service, String[] paths, Map<String,Object> data) {
        documents = new ArrayList<Document>();
        for (String path : paths) {
            documents.add(new Document(service, path, data));
        }
    }

    public DocumentFamily(DokpoolBaseService service, List<Document> documents) {
        this.documents = documents;
    }

    /**
     * Uploads a file into each document.
     * This method never replaces an exising file.
     * If the id is not null and a file exists, upload is rejected.
     * If the id is null, a new file is created in any case.
     * @param id: short name for the file
     * @param title
     * @param description
     * @param data: binary data of the file
     * @param filename (used for display and download)
     * @param mimeType
     */
    public void uploadFile(String id, String title, String description, byte[] data, String filename, String mimeType) {
        for (Document d : documents) {
            d.uploadFile(id, title, description, data, filename, mimeType);
        }
    }

    /**
     * Uploads a file into each document.
     * This method never replaces an exising file.
     * If the id is not null and a file exists, upload is rejected.
     * If the id is null, a new file is created in any case.
     * @param id: short name for the file
     * @param title
     * @param description
     * @param data: binary data of the file
     * @param filename (used for display and download)
     */
    public void uploadFile(String id, String title, String description, byte[] data, String filename) {
        uploadFile(id, title, description, data, filename, null);
    }

    /**
     * Uploads a file into each document or replaces a file with the same id.
     * If the id is not null and a file exists, the file is replaced.
     * If the id is null, a new file is created in any case.
     * @param id: short name for the file
     * @param title
     * @param description
     * @param data: binary data of the file
     * @param filename (used for display and download)
     * @param mimeType
     */
    public void uploadOrReplaceFile(String id, String title, String description, byte[] data, String filename, String mimeType) {
        for (Document d : documents) {
            d.uploadOrReplaceFile(id, title, description, data, filename, mimeType);
        }
    }

    /**
     * Uploads an image into each document.
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
    public void uploadImage(final String id, final String title, final String description, final byte[] data, final String filename, String mimeType) {
        for (Document d : documents) {
            d.uploadImage(id, title, description, data, filename, mimeType);
        }
    }

    /**
     * Uploads an image into each document
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
    public void uploadImage(final String id, final String title, final String description, final byte[] data, final String filename) {
        uploadImage(id, title, description, data, filename, Document.mimeTypeFromFilename(filename));
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
    public void uploadOrReplaceImage(String id, String title, String description, byte[] data, String filename, String mimeType) {
        for (Document d : documents) {
            d.uploadOrReplaceImage(id, title, description, data, filename, mimeType);
        }
    }

    /**
     * Update each document's attributes with the given map.
     * Any attribute that is not explicitly set will keep its value;
     * @param attributes attribute map
     * @return true, if the update succeeded; false otherwise
     */
    public boolean update(Map<String, Object> attributes) {
        boolean allOk = true;
        for (Document d : documents) {
            allOk = allOk && d.update(attributes);
        }
        return allOk;
    }

    /**
     * Update a single attribute with a given value in each document.
     * To unset a value set, set it to null.
     * @param name the name of the attribute
     * @param value its designated value
     * @return true, if the update succeeded; false otherwise
     */
    public boolean setAttribute(String name, Object value) {
        boolean allOk = true;
        for (Document d : documents) {
            allOk = allOk && d.setAttribute(name, value);
        }
        return allOk;
    }

    /**
     * Attempts to execute a transition to set a new workflow status for each document.
     * @param transition: the name of the transition
     * @return true iff Dokpool returns no error.
     */
    public boolean setWorkflowStatus(String transition) {
        boolean allOk = true;
        for (Document d : documents) {
            allOk = allOk && d.setWorkflowStatus(transition);
        }
        return allOk;
    }

    /**
     * Assigns the given events to the documents of the family one-to-one.
     * @param evIdsUids: list of event ids and/or uids (can be mixed)
     * @return true iff this worked for all events.
     */
    public boolean mapEventsToDocuments(List<String> evIdsUids) throws RuntimeException {
        if (evIdsUids.size() != documents.size()) {
            //TODO: proper Exception (DRE breaks visibility)
            throw new RuntimeException("Number of events and documents do not match",null);
        }

        boolean allOk = true;
        for (int i = 0; i < documents.size(); i++) {
            Document d = documents.get(i);
            allOk = allOk && d.assignEventIdUid(evIdsUids.get(i));
        }
        return allOk;
    }

}
