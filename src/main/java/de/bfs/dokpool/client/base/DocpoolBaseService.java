/* Copyright (C) 2015-2025 by Bundesamt fuer Strahlenschutz
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 * See LICENSE for details.
 */

package de.bfs.dokpool.client.base;

/**
 * This class has been renamed to DokpoolBaseService with k.
 * In general, variables like docPool refer to a single document pool,
 * whereas Dokpool refers to the software as a whole.
 */
@Deprecated
public class DocpoolBaseService extends DokpoolBaseService {

    public DocpoolBaseService(String url, String username, String password) {
        super(url, username, password, true);
    }

    public DocpoolBaseService(String url, String username, String password, boolean caching) {
        super(url, username, password, caching);
    }

    public DocpoolBaseService(String proto, String host, String port, String plonesite, String username, String password){
        super(proto, host, port, plonesite, username, password);
    }
    public DocpoolBaseService(String proto, String host, String port, String plonesite, String username, String password, boolean caching){
        super(proto, host, port, plonesite, username, password, caching);
    }
}
