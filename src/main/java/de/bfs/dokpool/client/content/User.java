/* Copyright (C) 2015-2025 by Bundesamt fuer Strahlenschutz
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 * See LICENSE for details.
 */

package de.bfs.dokpool.client.content;


import java.util.Map;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DocpoolBaseService;

public class User extends BaseObject {
    private String userId = "";
    private String fullname = "";
    private String dp = "";

    protected User(DocpoolBaseService service, String path, String userId, String password, String fullname, String dp) {
        super(service, path, (Map<String,Object>) null);
        this.userId = userId;
        this.fullname = fullname;
        this.dp = dp;
    }

    public void addToGroup(Group group) {
        group.addUser(this);
    }

    public String getUserId() {
        return this.userId;
    }

    public String getFullname() {
        return this.fullname;
    }

    /**
     * Returns the Dokpool set while creating this User instance.
     * Note that the REST-API cannot currently set or retrieve Dokpools for a User,
     * so this field might be empty or wrong.
     * @return
     */
    public String getEsd() {
        return  this.dp;
    }
}
