/* Copyright (C) 2015-2025 by Bundesamt fuer Strahlenschutz
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 * See LICENSE for details.
 */

package de.bfs.dokpool.client.content;

import de.bfs.dokpool.client.base.BaseObject;
import de.bfs.dokpool.client.base.DokpoolBaseService;

import java.util.Map;

/**
 * Wraps the DPEvent
 *
 */
public class Event extends BaseObject {
    @Deprecated
    public Event(DokpoolBaseService service, String path, Object[] data) {
        super(service, path, data);
    }

    public Event(DokpoolBaseService service, String path, Map<String,Object> data) {
        super(service, path, data);
    }
}
