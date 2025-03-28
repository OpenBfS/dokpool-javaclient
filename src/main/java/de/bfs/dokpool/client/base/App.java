/* Copyright (C) 2015-2025 by Bundesamt fuer Strahlenschutz
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 * See LICENSE for details.
 */

package de.bfs.dokpool.client.base;

public enum App {
    ELAN,
    DOKSYS,
    RODOS,
    REI;

    public static App fromString(String name) {
        return App.valueOf(name.toUpperCase());
    }
}
