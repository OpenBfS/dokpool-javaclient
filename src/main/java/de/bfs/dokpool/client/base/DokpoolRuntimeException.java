/* Copyright (C) 2015-2025 by Bundesamt fuer Strahlenschutz
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 * See LICENSE for details.
 */

package de.bfs.dokpool.client.base;

public class DokpoolRuntimeException extends RuntimeException {
    DokpoolRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
