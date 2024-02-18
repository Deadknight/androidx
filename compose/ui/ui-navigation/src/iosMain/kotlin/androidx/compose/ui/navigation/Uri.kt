/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.compose.ui.navigation

import kotlin.concurrent.Volatile

/**
 * Immutable URI reference. A URI reference includes a URI and a fragment, the
 * component of the URI following a '#'. Builds and parses URI references
 * which conform to
 * [RFC 2396](http://www.faqs.org/rfcs/rfc2396.html).
 *
 *
 * In the interest of performance, this class performs little to no
 * validation. Behavior is undefined for invalid input. This class is very
 * forgiving--in the face of invalid input, it will return garbage
 * rather than throw an exception unless otherwise specified.
 */
abstract class Uri
/**
 * Prevents external subclassing.
 */
private constructor() : Comparable<Uri> {
    /**
     *
     * Holds a placeholder for strings which haven't been cached. This enables us
     * to cache null. We intentionally create a new String instance so we can
     * compare its identity and there is no chance we will confuse it with
     * user data.
     *
     * NOTE This value is held in its own Holder class is so that referring to
     * [NotCachedHolder.NOT_CACHED] does not trigger `Uri.<clinit>`.
     * For example, `PathPart.<init>` uses `NotCachedHolder.NOT_CACHED`
     * but must not trigger `Uri.<clinit>`: Otherwise, the initialization of
     * `Uri.EMPTY` would see a `null` value for `PathPart.EMPTY`!
     *
     * @hide
     */
    internal object NotCachedHolder {
        const val NOT_CACHED = "NOT CACHED"
    }

    /**
     * Returns true if this URI is hierarchical like "http://google.com".
     * Absolute URIs are hierarchical if the scheme-specific part starts with
     * a '/'. Relative URIs are always hierarchical.
     */
    abstract fun isHierarchical(): Boolean
    val isOpaque: Boolean
        /**
         * Returns true if this URI is opaque like "mailto:nobody@google.com". The
         * scheme-specific part of an opaque URI cannot start with a '/'.
         */
        get() = !isHierarchical()

    /**
     * Returns true if this URI is relative, i.e.&nbsp;if it doesn't contain an
     * explicit scheme.
     *
     * @return true if this URI is relative, false if it's absolute
     */
    abstract fun isRelative(): Boolean
    val isAbsolute: Boolean
        /**
         * Returns true if this URI is absolute, i.e.&nbsp;if it contains an
         * explicit scheme.
         *
         * @return true if this URI is absolute, false if it's relative
         */
        get() = !isRelative()

    /**
     * Gets the scheme of this URI. Example: "http"
     *
     * @return the scheme or null if this is a relative URI
     */
    abstract fun getScheme(): String?

    /**
     * Gets the scheme-specific part of this URI, i.e.&nbsp;everything between
     * the scheme separator ':' and the fragment separator '#'. If this is a
     * relative URI, this method returns the entire URI. Decodes escaped octets.
     *
     *
     * Example: "//www.google.com/search?q=android"
     *
     * @return the decoded scheme-specific-part
     */
    abstract fun getSchemeSpecificPart(): String?

    /**
     * Gets the scheme-specific part of this URI, i.e.&nbsp;everything between
     * the scheme separator ':' and the fragment separator '#'. If this is a
     * relative URI, this method returns the entire URI. Leaves escaped octets
     * intact.
     *
     *
     * Example: "//www.google.com/search?q=android"
     *
     * @return the encoded scheme-specific-part
     */
    abstract fun getEncodedSchemeSpecificPart(): String?

    /**
     * Gets the decoded authority part of this URI. For
     * server addresses, the authority is structured as follows:
     * `[ userinfo '@' ] host [ ':' port ]`
     *
     *
     * Examples: "google.com", "bob@google.com:80"
     *
     * @return the authority for this URI or null if not present
     */
    abstract fun getAuthority(): String?

    /**
     * Gets the encoded authority part of this URI. For
     * server addresses, the authority is structured as follows:
     * `[ userinfo '@' ] host [ ':' port ]`
     *
     *
     * Examples: "google.com", "bob@google.com:80"
     *
     * @return the authority for this URI or null if not present
     */
    abstract fun getEncodedAuthority(): String?

    /**
     * Gets the decoded user information from the authority.
     * For example, if the authority is "nobody@google.com", this method will
     * return "nobody".
     *
     * @return the user info for this URI or null if not present
     */
    abstract fun getUserInfo(): String?

    /**
     * Gets the encoded user information from the authority.
     * For example, if the authority is "nobody@google.com", this method will
     * return "nobody".
     *
     * @return the user info for this URI or null if not present
     */
    abstract fun getEncodedUserInfo(): String?

    /**
     * Gets the encoded host from the authority for this URI. For example,
     * if the authority is "bob@google.com", this method will return
     * "google.com".
     *
     * @return the host for this URI or null if not present
     */
    abstract fun getHost(): String?

    /**
     * Gets the port from the authority for this URI. For example,
     * if the authority is "google.com:80", this method will return 80.
     *
     * @return the port for this URI or -1 if invalid or not present
     */
    abstract fun getPort(): Int

    /**
     * Gets the decoded path.
     *
     * @return the decoded path, or null if this is not a hierarchical URI
     * (like "mailto:nobody@google.com") or the URI is invalid
     */
    abstract fun getPath(): String?

    /**
     * Gets the encoded path.
     *
     * @return the encoded path, or null if this is not a hierarchical URI
     * (like "mailto:nobody@google.com") or the URI is invalid
     */
    abstract fun getEncodedPath(): String?

    /**
     * Gets the decoded query component from this URI. The query comes after
     * the query separator ('?') and before the fragment separator ('#'). This
     * method would return "q=android" for
     * "http://www.google.com/search?q=android".
     *
     * @return the decoded query or null if there isn't one
     */
    abstract fun getQuery(): String?

    /**
     * Gets the encoded query component from this URI. The query comes after
     * the query separator ('?') and before the fragment separator ('#'). This
     * method would return "q=android" for
     * "http://www.google.com/search?q=android".
     *
     * @return the encoded query or null if there isn't one
     */
    abstract fun getEncodedQuery(): String?

    /**
     * Gets the decoded fragment part of this URI, everything after the '#'.
     *
     * @return the decoded fragment or null if there isn't one
     */
    abstract fun getFragment(): String?

    /**
     * Gets the encoded fragment part of this URI, everything after the '#'.
     *
     * @return the encoded fragment or null if there isn't one
     */
    abstract fun getEncodedFragment(): String?

    /**
     * Gets the decoded path segments.
     *
     * @return decoded path segments, each without a leading or trailing '/'
     */
    abstract fun getPathSegments(): List<String?>

    /**
     * Gets the decoded last segment in the path.
     *
     * @return the decoded last segment or null if the path is empty
     */
    abstract fun getLastPathSegment(): String?

    /**
     * Compares this Uri to another object for equality. Returns true if the
     * encoded string representations of this Uri and the given Uri are
     * equal. Case counts. Paths are not normalized. If one Uri specifies a
     * default port explicitly and the other leaves it implicit, they will not
     * be considered equal.
     */
    override fun equals(o: Any?): Boolean {
        if (o !is Uri) {
            return false
        }
        return toString() == o.toString()
    }

    /**
     * Hashes the encoded string represention of this Uri consistently with
     * [.equals].
     */
    override fun hashCode(): Int {
        return toString().hashCode()
    }

    /**
     * Compares the string representation of this Uri with that of
     * another.
     */
    override fun compareTo(other: Uri): Int {
        return toString().compareTo(other.toString())
    }

    /**
     * Returns the encoded string representation of this URI.
     * Example: "http://google.com/"
     */
    abstract override fun toString(): String

    /**
     * Return a string representation of this URI that has common forms of PII redacted,
     * making it safer to use for logging purposes.  For example, `tel:800-466-4411` is
     * returned as `tel:xxx-xxx-xxxx` and `http://example.com/path/to/item/` is
     * returned as `http://example.com/...`. For all other uri schemes, only the scheme,
     * host and port are returned.
     * @return the common forms PII redacted string of this URI
     * @hide
     */
    fun toSafeString(): String {
        val scheme = getScheme()
        val ssp = getSchemeSpecificPart()
        val builder = StringBuilder(64)
        if (scheme != null) {
            builder.append(scheme)
            builder.append(":")
            if (scheme.equals("tel", ignoreCase = true) || scheme.equals("sip", ignoreCase = true)
                || scheme.equals("sms", ignoreCase = true) || scheme.equals(
                    "smsto",
                    ignoreCase = true
                )
                || scheme.equals("mailto", ignoreCase = true) || scheme.equals(
                    "nfc",
                    ignoreCase = true
                )
            ) {
                if (ssp != null) {
                    for (i in 0 until ssp.length) {
                        val c = ssp[i]
                        if (c == '-' || c == '@' || c == '.') {
                            builder.append(c)
                        } else {
                            builder.append('x')
                        }
                    }
                }
            } else {
                // For other schemes, let's be conservative about
                // the data we include -- only the host and port, not the query params, path or
                // fragment, because those can often have sensitive info.
                val host = getHost()
                val port = getPort()
                val path = getPath()
                val authority = getAuthority()
                if (authority != null) builder.append("//")
                if (host != null) builder.append(host)
                if (port != -1) builder.append(":").append(port)
                if (authority != null || path != null) builder.append("/...")
            }
        }
        return builder.toString()
    }

    /**
     * Constructs a new builder, copying the attributes from this Uri.
     */
    abstract fun buildUpon(): Builder

    /**
     * An implementation which wraps a String URI. This URI can be opaque or
     * hierarchical, but we extend AbstractHierarchicalUri in case we need
     * the hierarchical functionality.
     */
    private class StringUri(uriString: String) : AbstractHierarchicalUri() {
        /** URI string representation.  */
        private val uriString: String

        init {
            this.uriString = uriString
        }

        /** Cached scheme separator index.  */
        //@Volatile
        private var cachedSsi = NOT_CALCULATED

        /** Finds the first ':'. Returns -1 if none found.  */
        private fun findSchemeSeparator(): Int {
            return if (cachedSsi == NOT_CALCULATED) uriString.indexOf(':')
                .also { cachedSsi = it } else cachedSsi
        }

        /** Cached fragment separator index.  */
        //@Volatile
        private var cachedFsi = NOT_CALCULATED

        /** Finds the first '#'. Returns -1 if none found.  */
        private fun findFragmentSeparator(): Int {
            return if (cachedFsi == NOT_CALCULATED) uriString.indexOf('#', findSchemeSeparator())
                .also { cachedFsi = it } else cachedFsi
        }

        override fun isHierarchical(): Boolean {
            val ssi = findSchemeSeparator()
            if (ssi == NOT_FOUND) {
                // All relative URIs are hierarchical.
                return true
            }
            return if (uriString.length == ssi + 1) {
                // No ssp.
                false
            } else uriString[ssi + 1] == '/'
            // If the ssp starts with a '/', this is hierarchical.
        }

        override fun isRelative(): Boolean {
            // Note: We return true if the index is 0
            return findSchemeSeparator() == NOT_FOUND
        }

        //@Volatile
        private var scheme = NotCachedHolder.NOT_CACHED

        override fun getScheme(): String? {
            val cached = scheme !== NotCachedHolder.NOT_CACHED
            return if (cached) scheme else parseScheme().also { scheme = it!! }
        }

        private fun parseScheme(): String? {
            val ssi = findSchemeSeparator()
            return if (ssi == NOT_FOUND) null else uriString.substring(0, ssi)
        }

        private var ssp: Part? = null

        private fun getSsp(): Part {
            return if (ssp == null) Part.fromEncoded(parseSsp()).also {
                ssp = it
            } else ssp!!
        }

        override fun getEncodedSchemeSpecificPart(): String? {
            return getSsp().getEncoded()
        }

        override fun getSchemeSpecificPart(): String? {
            return getSsp().getDecoded()
        }

        private fun parseSsp(): String {
            val ssi = findSchemeSeparator()
            val fsi = findFragmentSeparator()
            // Return everything between ssi and fsi.
            return if (fsi == NOT_FOUND) uriString.substring(ssi + 1) else uriString.substring(
                ssi + 1,
                fsi
            )
        }

        private var authority: Part? = null

        private fun getAuthorityPart(): Part {
            if (authority == null) {
                val encodedAuthority = parseAuthority(uriString, findSchemeSeparator())
                return Part.fromEncoded(encodedAuthority).also {
                    authority = it
                }
            }
            return authority!!
        }

        override fun getEncodedAuthority(): String? {
            return getAuthorityPart().getEncoded()
        }

        override fun getAuthority(): String? {
            return getAuthorityPart().getDecoded()
        }

        private var path: PathPart? = null
        private fun getPathPart(): PathPart {
            return if (path == null) PathPart.fromEncoded(parsePath()).also {
                path = it
            } else path!!
        }

        override fun getPath(): String? {
            return getPathPart().getDecoded()
        }

        override fun getEncodedPath(): String? {
            return getPathPart().getEncoded()
        }

        override fun getPathSegments(): List<String?> {
            return getPathPart().getPathSegments().segments?.toList() ?: listOf()
        }

        private fun parsePath(): String? {
            val uriString = uriString
            val ssi = findSchemeSeparator()
            // If the URI is absolute.
            if (ssi > -1) {
                // Is there anything after the ':'?
                val schemeOnly = ssi + 1 == uriString.length
                if (schemeOnly) {
                    // Opaque URI.
                    return null
                }
                // A '/' after the ':' means this is hierarchical.
                if (uriString[ssi + 1] != '/') {
                    // Opaque URI.
                    return null
                }
            } else {
                // All relative URIs are hierarchical.
            }
            return parsePath(uriString, ssi)
        }

        private var query: Part? = null
        private fun getQueryPart(): Part {
            return if (query == null) Part.fromEncoded(parseQuery()).also {
                query = it
            } else query!!
        }

        override fun getEncodedQuery(): String? {
            return getQueryPart().getEncoded()
        }

        private fun parseQuery(): String? {
            // It doesn't make sense to cache this index. We only ever
            // calculate it once.
            val qsi = uriString.indexOf('?', findSchemeSeparator())
            if (qsi == NOT_FOUND) {
                return null
            }
            val fsi = findFragmentSeparator()
            if (fsi == NOT_FOUND) {
                return uriString.substring(qsi + 1)
            }
            return if (fsi < qsi) {
                // Invalid.
                null
            } else uriString.substring(qsi + 1, fsi)
        }

        override fun getQuery(): String? {
            return getQueryPart().getDecoded()
        }

        private var fragment: Part? = null
        private fun getFragmentPart(): Part {
            return if (fragment == null) Part.fromEncoded(parseFragment()).also {
                fragment = it
            } else fragment!!
        }

        override fun getEncodedFragment(): String? {
            return getFragmentPart().getEncoded()
        }

        private fun parseFragment(): String? {
            val fsi = findFragmentSeparator()
            return if (fsi == NOT_FOUND) null else uriString.substring(fsi + 1)
        }

        override fun getFragment(): String? {
            return getFragmentPart().getDecoded()
        }

        override fun toString(): String {
            return uriString
        }

        override fun buildUpon(): Builder {
            return if (isHierarchical()) {
                Builder()
                    .scheme(getScheme())
                    .authority(getAuthorityPart())
                    .path(getPathPart())
                    .query(getQueryPart())
                    .fragment(getFragmentPart())
            } else {
                Builder()
                    .scheme(getScheme())
                    .opaquePart(getSsp())
                    .fragment(getFragmentPart())
            }
        }

        companion object {
            /** Used in parcelling.  */
            const val TYPE_ID = 1

            /**
             * Parses an authority out of the given URI string.
             *
             * @param uriString URI string
             * @param ssi scheme separator index, -1 for a relative URI
             *
             * @return the authority or null if none is found
             */
            fun parseAuthority(uriString: String, ssi: Int): String? {
                val length = uriString.length
                // If "//" follows the scheme separator, we have an authority.
                return if (length > ssi + 2 && uriString[ssi + 1] == '/' && uriString[ssi + 2] == '/') {
                    // We have an authority.
                    // Look for the start of the path, query, or fragment, or the
                    // end of the string.
                    var end = ssi + 3
                    LOOP@ while (end < length) {
                        when (uriString[end]) {
                            '/', '\\', '?', '#' -> break@LOOP
                        }
                        end++
                    }
                    uriString.substring(ssi + 3, end)
                } else {
                    null
                }
            }

            /**
             * Parses a path out of this given URI string.
             *
             * @param uriString URI string
             * @param ssi scheme separator index, -1 for a relative URI
             *
             * @return the path
             */
            fun parsePath(uriString: String, ssi: Int): String {
                val length = uriString.length
                // Find start of path.
                var pathStart: Int
                if (length > ssi + 2 && uriString[ssi + 1] == '/' && uriString[ssi + 2] == '/') {
                    // Skip over authority to path.
                    pathStart = ssi + 3
                    LOOP@ while (pathStart < length) {
                        when (uriString[pathStart]) {
                            '?', '#' -> return "" // Empty path.
                            '/', '\\' ->                             // Per http://url.spec.whatwg.org/#host-state, the \ character
                                // is treated as if it were a / character when encountered in a
                                // host
                                break@LOOP
                        }
                        pathStart++
                    }
                } else {
                    // Path starts immediately after scheme separator.
                    pathStart = ssi + 1
                }
                // Find end of path.
                var pathEnd = pathStart
                LOOP@ while (pathEnd < length) {
                    when (uriString[pathEnd]) {
                        '?', '#' -> break@LOOP
                    }
                    pathEnd++
                }
                return uriString.substring(pathStart, pathEnd)
            }
        }
    }

    /**
     * Opaque URI.
     */
    private class OpaqueUri(scheme: String, ssp: Part, fragment: Part?) : Uri() {
        private val scheme: String?
        private val ssp: Part?
        private val fragment: Part?

        init {
            this.scheme = scheme
            this.ssp = ssp
            this.fragment = fragment
        }

        override fun isHierarchical(): Boolean {
            return false
        }

        override fun isRelative(): Boolean {
            return scheme == null
        }

        override fun getScheme(): String? {
            return scheme
        }

        override fun getEncodedSchemeSpecificPart(): String? {
            return ssp?.getEncoded()
        }

        override fun getSchemeSpecificPart(): String? {
            return ssp?.getDecoded()
        }

        override fun getAuthority(): String? {
            return null
        }

        override fun getEncodedAuthority(): String? {
            return null
        }

        override fun getPath(): String? {
            return null
        }

        override fun getEncodedPath(): String? {
            return null
        }

        override fun getQuery(): String? {
            return null
        }

        override fun getEncodedQuery(): String? {
            return null
        }

        override fun getFragment(): String? {
            return fragment?.getDecoded()
        }

        override fun getEncodedFragment(): String? {
            return fragment?.getEncoded()
        }

        override fun getPathSegments(): List<String?> {
            return emptyList<String>()
        }

        override fun getLastPathSegment(): String? {
            return null
        }

        override fun getUserInfo(): String? {
            return null
        }

        override fun getEncodedUserInfo(): String? {
            return null
        }

        override fun getHost(): String? {
            return null
        }

        override fun getPort(): Int {
            return -1
        }

        //@Volatile
        private var cachedString = NotCachedHolder.NOT_CACHED

        override fun toString(): String {
            val cached = cachedString !== NotCachedHolder.NOT_CACHED
            if (cached) {
                return cachedString
            }
            val sb = StringBuilder()
            sb.append(scheme).append(':')
            sb.append(getEncodedSchemeSpecificPart())
            if (fragment?.isEmpty() == false) {
                sb.append('#').append(fragment.getEncoded())
            }
            return sb.toString().also { cachedString = it }
        }

        override fun buildUpon(): Builder {
            return Builder()
                .scheme(scheme)
                .opaquePart(ssp)
                .fragment(fragment)
        }

        companion object {
            /** Used in parcelling.  */
            const val TYPE_ID = 2
        }
    }

    /**
     * Wrapper for path segment array.
     */
    class PathSegments(val segments: Array<String?>?, override val size: Int) :
        AbstractList<String?>(), RandomAccess {
        override fun get(index: Int): String? {
            if (index >= size) {
                throw IndexOutOfBoundsException()
            }
            return segments!![index]
        }

        companion object {
            val EMPTY = PathSegments(null, 0)
        }
    }

    /**
     * Builds PathSegments.
     */
    internal class PathSegmentsBuilder {
        var segments: Array<String?>? = null
        var size = 0
        fun add(segment: String?) {
            if (segments == null) {
                segments = arrayOfNulls(4)
            } else if (size + 1 == segments!!.size) {
                val expanded = arrayOfNulls<String>(
                    segments!!.size * 2
                )
                Arrays.arraycopy(segments!!, 0, expanded, 0, segments!!.size)
                segments = expanded
            }
            segments!![size++] = segment
        }

        fun build(): PathSegments {
            return if (segments == null) {
                PathSegments.EMPTY
            } else try {
                PathSegments(segments, size)
            } finally {
                // Makes sure this doesn't get reused.
                segments = null
            }
        }
    }

    /**
     * Support for hierarchical URIs.
     */
    private abstract class AbstractHierarchicalUri : Uri() {
        override fun getLastPathSegment(): String? {
            // TODO: If we haven't parsed all of the segments already, just
            // grab the last one directly so we only allocate one string.
            val segments = getPathSegments()
            val size = segments.size
            return if (size == 0) {
                null
            } else segments[size - 1]
        }

        private var userInfo: Part? = null
        private fun getUserInfoPart(): Part {
            return if (userInfo == null) Part.fromEncoded(parseUserInfo()).also {
                userInfo = it
            } else userInfo!!
        }

        override fun getEncodedUserInfo(): String? {
            return getUserInfoPart().getEncoded()
        }

        private fun parseUserInfo(): String? {
            val authority = getEncodedAuthority() ?: return null
            val end = authority.lastIndexOf('@')
            return if (end == NOT_FOUND) null else authority.substring(0, end)
        }

        override fun getUserInfo(): String? {
            return getUserInfoPart().getDecoded()
        }

        //@Volatile
        private var host = NotCachedHolder.NOT_CACHED

        override fun getHost(): String {
            val cached = host !== NotCachedHolder.NOT_CACHED
            return if (cached) host else parseHost().also { host = it!! }!!
        }

        private fun parseHost(): String? {
            val authority = getEncodedAuthority() ?: return null
            // Parse out user info and then port.
            val userInfoSeparator = authority.lastIndexOf('@')
            val portSeparator = findPortSeparator(authority)
            val encodedHost =
                if (portSeparator == NOT_FOUND) authority.substring(userInfoSeparator + 1) else authority.substring(
                    userInfoSeparator + 1,
                    portSeparator
                )
            return decode(encodedHost)
        }

        //@Volatile
        private var port = NOT_CALCULATED
        override fun getPort(): Int {
            return if (port == NOT_CALCULATED) parsePort().also {
                port = it
            } else port
        }

        private fun parsePort(): Int {
            val authority = getEncodedAuthority()
            val portSeparator = findPortSeparator(authority)
            if (portSeparator == NOT_FOUND) {
                return -1
            }
            val portString = decode(authority!!.substring(portSeparator + 1))
            return try {
                portString!!.toInt()
            } catch (e: NumberFormatException) {
                println("Error parsing port string., $e")
                -1
            }
        }

        private fun findPortSeparator(authority: String?): Int {
            if (authority == null) {
                return NOT_FOUND
            }
            // Reverse search for the ':' character that breaks as soon as a char that is neither
            // a colon nor an ascii digit is encountered. Thanks to the goodness of UTF-16 encoding,
            // it's not possible that a surrogate matches one of these, so this loop can just
            // look for characters rather than care about code points.
            for (i in authority.length - 1 downTo 0) {
                val character = authority[i].code
                if (':'.code == character) return i
                // Character.isDigit would include non-ascii digits
                if (character < '0'.code || character > '9'.code) return NOT_FOUND
            }
            return NOT_FOUND
        }
    }

    /**
     * Hierarchical Uri.
     */
    private class HierarchicalUri(// can be null
        scheme: String?, authority: Part?, path: PathPart?,
        query: Part?, fragment: Part?
    ) : AbstractHierarchicalUri() {
        /** Used in parcelling.  */
        private val scheme: String? // can be null
        private val authority: Part?
        private val path: PathPart?
        private val query: Part?
        private val fragment: Part?

        init {
            this.scheme = scheme
            this.authority = authority
            this.path = path
            this.query = query
            this.fragment = fragment
        }

        override fun isHierarchical(): Boolean {
            return true
        }

        override fun isRelative(): Boolean {
            return scheme == null
        }

        override fun getScheme(): String {
            return scheme!!
        }

        private var ssp: Part? = null
        private fun getSsp(): Part {
            return if (ssp == null) Part.fromEncoded(makeSchemeSpecificPart()).also {
                ssp = it
            } else ssp!!
        }

        override fun getEncodedSchemeSpecificPart(): String? {
            return getSsp().getEncoded()
        }

        override fun getSchemeSpecificPart(): String? {
            return getSsp().getDecoded()
        }

        /**
         * Creates the encoded scheme-specific part from its sub parts.
         */
        private fun makeSchemeSpecificPart(): String {
            val builder = StringBuilder()
            appendSspTo(builder)
            return builder.toString()
        }

        private fun appendSspTo(builder: StringBuilder) {
            val encodedAuthority = authority!!.getEncoded()
            if (encodedAuthority != null) {
                // Even if the authority is "", we still want to append "//".
                builder.append("//").append(encodedAuthority)
            }
            val encodedPath = path!!.getEncoded()
            if (encodedPath != null) {
                builder.append(encodedPath)
            }
            if (query?.isEmpty() == false) {
                builder.append('?').append(query.getEncoded())
            }
        }

        override fun getAuthority(): String? {
            return authority?.getDecoded()
        }

        override fun getEncodedAuthority(): String? {
            return authority?.getEncoded()
        }

        override fun getEncodedPath(): String? {
            return path?.getEncoded()
        }

        override fun getPath(): String? {
            return path?.getDecoded()
        }

        override fun getQuery(): String? {
            return query?.getDecoded()
        }

        override fun getEncodedQuery(): String? {
            return query?.getEncoded()
        }

        override fun getFragment(): String? {
            return fragment?.getDecoded()
        }

        override fun getEncodedFragment(): String? {
            return fragment?.getEncoded()
        }

        override fun getPathSegments(): List<String?> {
            return path?.getPathSegments()?.segments?.toList() ?: listOf()
        }

        //@Volatile
        private var uriString = NotCachedHolder.NOT_CACHED

        override fun toString(): String {
            val cached = uriString !== NotCachedHolder.NOT_CACHED
            return if (cached) uriString else makeUriString().also { uriString = it }
        }

        private fun makeUriString(): String {
            val builder = StringBuilder()
            if (scheme != null) {
                builder.append(scheme).append(':')
            }
            appendSspTo(builder)
            if (fragment?.isEmpty() == false) {
                builder.append('#').append(fragment.getEncoded())
            }
            return builder.toString()
        }

        override fun buildUpon(): Builder {
            return Builder()
                .scheme(scheme)
                .authority(authority)
                .path(path)
                .query(query)
                .fragment(fragment)
        }

        companion object {
            /** Used in parcelling.  */
            const val TYPE_ID = 3
        }
    }

    /**
     * Helper class for building or manipulating URI references. Not safe for
     * concurrent use.
     *
     *
     * An absolute hierarchical URI reference follows the pattern:
     * `<scheme>://<authority><absolute path>?<query>#<fragment>`
     *
     *
     * Relative URI references (which are always hierarchical) follow one
     * of two patterns: `<relative or absolute path>?<query>#<fragment>`
     * or `//<authority><absolute path>?<query>#<fragment>`
     *
     *
     * An opaque URI follows this pattern:
     * `<scheme>:<opaque part>#<fragment>`
     *
     *
     * Use [Uri.buildUpon] to obtain a builder representing an existing URI.
     */
    class Builder
    /**
     * Constructs a new Builder.
     */
    {
        private var scheme: String? = null
        private var opaquePart: Part? = null
        private var authority: Part? = null
        private var path: PathPart? = null
        private var query: Part? = null
        private var fragment: Part? = null

        /**
         * Sets the scheme.
         *
         * @param scheme name or `null` if this is a relative Uri
         */
        fun scheme(scheme: String?): Builder {
            this.scheme = scheme
            return this
        }

        fun opaquePart(opaquePart: Part?): Builder {
            this.opaquePart = opaquePart
            return this
        }

        /**
         * Encodes and sets the given opaque scheme-specific-part.
         *
         * @param opaquePart decoded opaque part
         */
        fun opaquePart(opaquePart: String?): Builder {
            return opaquePart(Part.fromDecoded(opaquePart))
        }

        /**
         * Sets the previously encoded opaque scheme-specific-part.
         *
         * @param opaquePart encoded opaque part
         */
        fun encodedOpaquePart(opaquePart: String?): Builder {
            return opaquePart(Part.fromEncoded(opaquePart))
        }

        fun authority(authority: Part?): Builder {
            // This URI will be hierarchical.
            opaquePart = null
            this.authority = authority
            return this
        }

        /**
         * Encodes and sets the authority.
         */
        fun authority(authority: String?): Builder {
            return authority(Part.fromDecoded(authority))
        }

        /**
         * Sets the previously encoded authority.
         */
        fun encodedAuthority(authority: String?): Builder {
            return authority(Part.fromEncoded(authority))
        }

        fun path(path: PathPart?): Builder {
            // This URI will be hierarchical.
            opaquePart = null
            this.path = path
            return this
        }

        /**
         * Sets the path. Leaves '/' characters intact but encodes others as
         * necessary.
         *
         *
         * If the path is not null and doesn't start with a '/', and if
         * you specify a scheme and/or authority, the builder will prepend the
         * given path with a '/'.
         */
        fun path(path: String?): Builder {
            return path(PathPart.fromDecoded(path))
        }

        /**
         * Sets the previously encoded path.
         *
         *
         * If the path is not null and doesn't start with a '/', and if
         * you specify a scheme and/or authority, the builder will prepend the
         * given path with a '/'.
         */
        fun encodedPath(path: String?): Builder {
            return path(PathPart.fromEncoded(path))
        }

        /**
         * Encodes the given segment and appends it to the path.
         */
        fun appendPath(newSegment: String?): Builder {
            return path(PathPart.appendDecodedSegment(path, newSegment))
        }

        /**
         * Appends the given segment to the path.
         */
        fun appendEncodedPath(newSegment: String): Builder {
            return path(PathPart.appendEncodedSegment(path, newSegment))
        }

        fun query(query: Part?): Builder {
            // This URI will be hierarchical.
            opaquePart = null
            this.query = query
            return this
        }

        /**
         * Encodes and sets the query.
         */
        fun query(query: String?): Builder {
            return query(Part.fromDecoded(query))
        }

        /**
         * Sets the previously encoded query.
         */
        fun encodedQuery(query: String?): Builder {
            return query(Part.fromEncoded(query))
        }

        fun fragment(fragment: Part?): Builder {
            this.fragment = fragment
            return this
        }

        /**
         * Encodes and sets the fragment.
         */
        fun fragment(fragment: String?): Builder {
            return fragment(Part.fromDecoded(fragment))
        }

        /**
         * Sets the previously encoded fragment.
         */
        fun encodedFragment(fragment: String?): Builder {
            return fragment(Part.fromEncoded(fragment))
        }

        /**
         * Encodes the key and value and then appends the parameter to the
         * query string.
         *
         * @param key which will be encoded
         * @param value which will be encoded
         */
        fun appendQueryParameter(key: String?, value: String?): Builder {
            // This URI will be hierarchical.
            opaquePart = null
            val encodedParameter = (encode(key, null) + "="
                + encode(value, null))
            if (query == null) {
                query = Part.fromEncoded(encodedParameter)
                return this
            }
            val oldQuery = query!!.getEncoded()
            query = if (oldQuery == null || oldQuery.length == 0) {
                Part.fromEncoded(encodedParameter)
            } else {
                Part.fromEncoded("$oldQuery&$encodedParameter")
            }
            return this
        }

        /**
         * Clears the the previously set query.
         */
        fun clearQuery(): Builder {
            return query(null as Part?)
        }

        /**
         * Constructs a Uri with the current attributes.
         *
         * @throws UnsupportedOperationException if the URI is opaque and the
         * scheme is null
         */
        fun build(): Uri {
            return if (opaquePart != null) {
                if (scheme == null) {
                    throw UnsupportedOperationException(
                        "An opaque URI must have a scheme."
                    )
                }
                OpaqueUri(scheme!!, opaquePart!!, fragment)
            } else {
                // Hierarchical URIs should not return null for getPath().
                var path = path
                if (path == null || path === PathPart.NULL) {
                    path = PathPart.EMPTY
                } else {
                    // If we have a scheme and/or authority, the path must
                    // be absolute. Prepend it with a '/' if necessary.
                    if (hasSchemeOrAuthority()) {
                        path = PathPart.makeAbsolute(path)
                    }
                }
                HierarchicalUri(
                    scheme, authority, path, query, fragment
                )
            }
        }

        private fun hasSchemeOrAuthority(): Boolean {
            return scheme != null || authority != null && authority !== Part.NULL
        }

        override fun toString(): String {
            return build().toString()
        }
    }

    val queryParameterNames: Set<String?>
        /**
         * Returns a set of the unique names of all query parameters. Iterating
         * over the set will return the names in order of their first occurrence.
         *
         * @throws UnsupportedOperationException if this isn't a hierarchical URI
         *
         * @return a set of decoded names
         */
        get() {
            if (isOpaque) {
                throw UnsupportedOperationException(NOT_HIERARCHICAL)
            }
            val query = getEncodedQuery() ?: return emptySet<String>()
            val names: MutableSet<String?> = LinkedHashSet()
            var start = 0
            do {
                val next = query.indexOf('&', start)
                val end = if (next == -1) query.length else next
                var separator = query.indexOf('=', start)
                if (separator > end || separator == -1) {
                    separator = end
                }
                val name = query.substring(start, separator)
                names.add(decode(name))
                // Move start to end of name.
                start = end + 1
            } while (start < query.length)
            return names.toSet()
        }

    /**
     * Searches the query string for parameter values with the given key.
     *
     * @param key which will be encoded
     *
     * @throws UnsupportedOperationException if this isn't a hierarchical URI
     * @throws NullPointerException if key is null
     * @return a list of decoded values
     */
    fun getQueryParameters(key: String?): List<String?> {
        if (isOpaque) {
            throw UnsupportedOperationException(NOT_HIERARCHICAL)
        }
        if (key == null) {
            throw NullPointerException("key")
        }
        val query = getEncodedQuery() ?: return emptyList<String>()
        val encodedKey: String
        encodedKey = try {
            UrlEncoderUtil.encode(key, DEFAULT_ENCODING)
        } catch (e: Exception) {
            throw AssertionError(e)
        }
        val values = ArrayList<String?>()
        var start = 0
        do {
            val nextAmpersand = query.indexOf('&', start)
            val end = if (nextAmpersand != -1) nextAmpersand else query.length
            var separator = query.indexOf('=', start)
            if (separator > end || separator == -1) {
                separator = end
            }
            if (separator - start == encodedKey.length
                && query.regionMatches(start, encodedKey, 0, encodedKey.length)
            ) {
                if (separator == end) {
                    values.add("")
                } else {
                    values.add(decode(query.substring(separator + 1, end)))
                }
            }
            // Move start to end of name.
            start = if (nextAmpersand != -1) {
                nextAmpersand + 1
            } else {
                break
            }
        } while (true)
        return values.toList()
    }

    /**
     * Searches the query string for the first value with the given key.
     *
     *
     * **Warning:** Prior to Jelly Bean, this decoded
     * the '+' character as '+' rather than ' '.
     *
     * @param key which will be encoded
     * @throws UnsupportedOperationException if this isn't a hierarchical URI
     * @throws NullPointerException if key is null
     * @return the decoded value or null if no parameter is found
     */
    fun getQueryParameter(key: String?): String? {
        if (isOpaque) {
            throw UnsupportedOperationException(NOT_HIERARCHICAL)
        }
        if (key == null) {
            throw NullPointerException("key")
        }
        val query = getEncodedQuery() ?: return null
        val encodedKey = encode(key, null)
        val length = query.length
        var start = 0
        do {
            val nextAmpersand = query.indexOf('&', start)
            val end = if (nextAmpersand != -1) nextAmpersand else length
            var separator = query.indexOf('=', start)
            if (separator > end || separator == -1) {
                separator = end
            }
            if (separator - start == encodedKey!!.length
                && query.regionMatches(start, encodedKey, 0, encodedKey.length)
            ) {
                return if (separator == end) {
                    ""
                } else {
                    val encodedValue = query.substring(separator + 1, end)
                    UriCodec.decode(encodedValue, true, false)
                }
            }
            // Move start to end of name.
            start = if (nextAmpersand != -1) {
                nextAmpersand + 1
            } else {
                break
            }
        } while (true)
        return null
    }

    /**
     * Searches the query string for the first value with the given key and interprets it
     * as a boolean value. "false" and "0" are interpreted as `false`, everything
     * else is interpreted as `true`.
     *
     * @param key which will be decoded
     * @param defaultValue the default value to return if there is no query parameter for key
     * @return the boolean interpretation of the query parameter key
     */
    fun getBooleanQueryParameter(key: String?, defaultValue: Boolean): Boolean {
        var flag = getQueryParameter(key) ?: return defaultValue
        flag = flag.lowercase()
        return "false" != flag && "0" != flag
    }

    /**
     * Return an equivalent URI with a lowercase scheme component.
     * This aligns the Uri with Android best practices for
     * intent filtering.
     *
     *
     * For example, "HTTP://www.android.com" becomes
     * "http://www.android.com"
     *
     *
     * All URIs received from outside Android (such as user input,
     * or external sources like Bluetooth, NFC, or the Internet) should
     * be normalized before they are used to create an Intent.
     *
     *
     * This method does *not* validate bad URI's,
     * or 'fix' poorly formatted URI's - so do not use it for input validation.
     * A Uri will always be returned, even if the Uri is badly formatted to
     * begin with and a scheme component cannot be found.
     *
     * @return normalized Uri (never null)
     * @see android.content.Intent.setData
     *
     * @see android.content.Intent.setDataAndNormalize
     */
    fun normalizeScheme(): Uri {
        val scheme = getScheme() ?: return this
        // give up
        val lowerScheme = scheme.lowercase()
        return if (scheme == lowerScheme) this else buildUpon().scheme(lowerScheme)
            .build() // no change
    }

    /**
     * Support for part implementations.
     */
    abstract class AbstractPart(encoded: String?, decoded: String?) {
        //@Volatile
        var encoded: String? = null

        //@Volatile
        var decoded: String? = null
        private var mCanonicalRepresentation = 0

        init {
            if (encoded !== NotCachedHolder.NOT_CACHED) {
                mCanonicalRepresentation = REPRESENTATION_ENCODED
                this.encoded = encoded
                this.decoded = NotCachedHolder.NOT_CACHED
            } else if (decoded !== NotCachedHolder.NOT_CACHED) {
                mCanonicalRepresentation = REPRESENTATION_DECODED
                this.encoded = NotCachedHolder.NOT_CACHED
                this.decoded = decoded
            } else {
                throw IllegalArgumentException("Neither encoded nor decoded")
            }
        }

        abstract fun getEncoded(): String?
        fun getDecoded(): String? {
            val hasDecoded = decoded !== NotCachedHolder.NOT_CACHED
            return if (hasDecoded) decoded else decode(encoded).also { decoded = it }
        } /*final void writeTo(Parcel parcel) {
            final String canonicalValue;
            if (mCanonicalRepresentation == REPRESENTATION_ENCODED) {
                canonicalValue = encoded;
            } else if (mCanonicalRepresentation == REPRESENTATION_DECODED) {
                canonicalValue = decoded;
            } else {
                throw new IllegalArgumentException("Unknown representation: "
                        + mCanonicalRepresentation);
            }
            if (canonicalValue == NotCachedHolder.NOT_CACHED) {
                throw new AssertionError("Canonical value not cached ("
                        + mCanonicalRepresentation + ")");
            }
            parcel.writeInt(mCanonicalRepresentation);
            parcel.writeString8(canonicalValue);
        }*/

        companion object {
            // Possible values of mCanonicalRepresentation.
            const val REPRESENTATION_ENCODED = 1
            const val REPRESENTATION_DECODED = 2
        }
    }

    /**
     * Immutable wrapper of encoded and decoded versions of a URI part. Lazily
     * creates the encoded or decoded version from the other.
     */
    open class Part private constructor(encoded: String?, decoded: String?) :
        AbstractPart(encoded, decoded) {

        open fun isEmpty(): Boolean {
            return false
        }

        override fun getEncoded(): String? {
            val hasEncoded = encoded !== NotCachedHolder.NOT_CACHED
            return if (hasEncoded) encoded else encode(decoded).also { encoded = it }
        }

        private class EmptyPart(value: String?) : Part(value, value) {
            init {
                require(!(value != null && !value.isEmpty())) { "Expected empty value, got: $value" }
                // Avoid having to re-calculate the non-canonical value.
                decoded = value
                encoded = decoded
            }

            override fun isEmpty(): Boolean {
                return true
            }
        }

        companion object {
            /** A part with null values.  */
            val NULL: Part = EmptyPart(null)

            /** A part with empty strings for values.  */
            val EMPTY: Part = EmptyPart("")
            /**
             * Returns given part or [.NULL] if the given part is null.
             */
            fun nonNull(part: Part?): Part {
                return part ?: NULL
            }

            /**
             * Creates a part from the encoded string.
             *
             * @param encoded part string
             */
            fun fromEncoded(encoded: String?): Part {
                return from(encoded, NotCachedHolder.NOT_CACHED)
            }

            /**
             * Creates a part from the decoded string.
             *
             * @param decoded part string
             */
            fun fromDecoded(decoded: String?): Part {
                return from(NotCachedHolder.NOT_CACHED, decoded)
            }

            /**
             * Creates a part from the encoded and decoded strings.
             *
             * @param encoded part string
             * @param decoded part string
             */
            fun from(encoded: String?, decoded: String?): Part {
                // We have to check both encoded and decoded in case one is
                // NotCachedHolder.NOT_CACHED.
                if (encoded == null) {
                    return NULL
                }
                if (encoded.length == 0) {
                    return EMPTY
                }
                if (decoded == null) {
                    return NULL
                }
                return if (decoded.length == 0) {
                    EMPTY
                } else Part(encoded, decoded)
            }
        }
    }

    /**
     * Immutable wrapper of encoded and decoded versions of a path part. Lazily
     * creates the encoded or decoded version from the other.
     */
    class PathPart private constructor(encoded: String?, decoded: String?) :
        AbstractPart(encoded, decoded) {
        override fun getEncoded(): String? {
            val hasEncoded = encoded !== NotCachedHolder.NOT_CACHED
            // Don't encode '/'.
            return if (hasEncoded) encoded else encode(decoded, "/").also { encoded = it }
        }

        /**
         * Cached path segments. This doesn't need to be volatile--we don't
         * care if other threads see the result.
         */
        private var pathSegments: PathSegments? = null

        /**
         * Gets the individual path segments. Parses them if necessary.
         *
         * @return parsed path segments or null if this isn't a hierarchical
         * URI
         */
        fun getPathSegments(): PathSegments {
            if (pathSegments != null) {
                return pathSegments!!
            }
            val path = getEncoded() ?: return PathSegments.EMPTY.also { pathSegments = it }
            val segmentBuilder = PathSegmentsBuilder()
            var previous = 0
            var current: Int
            while (path.indexOf('/', previous).also { current = it } > -1) {
                // This check keeps us from adding a segment if the path starts
                // '/' and an empty segment for "//".
                if (previous < current) {
                    val decodedSegment = decode(path.substring(previous, current))
                    segmentBuilder.add(decodedSegment)
                }
                previous = current + 1
            }
            // Add in the final path segment.
            if (previous < path.length) {
                segmentBuilder.add(decode(path.substring(previous)))
            }
            return segmentBuilder.build().also { pathSegments = it }
        }

        companion object {
            /** A part with null values.  */
            val NULL = PathPart(null, null)

            /** A part with empty strings for values.  */
            val EMPTY = PathPart("", "")
            fun appendEncodedSegment(
                oldPart: PathPart?,
                newSegment: String
            ): PathPart {
                // If there is no old path, should we make the new path relative
                // or absolute? I pick absolute.
                if (oldPart == null) {
                    // No old path.
                    return fromEncoded("/$newSegment")
                }
                var oldPath: String? = oldPart.getEncoded()
                if (oldPath == null) {
                    oldPath = ""
                }
                val oldPathLength = oldPath.length
                val newPath: String
                newPath = if (oldPathLength == 0) {
                    // No old path.
                    "/$newSegment"
                } else if (oldPath[oldPathLength - 1] == '/') {
                    oldPath + newSegment
                } else {
                    "$oldPath/$newSegment"
                }
                return fromEncoded(newPath)
            }

            fun appendDecodedSegment(
                oldPart: PathPart?,
                decoded: String?
            ): PathPart {
                val encoded = encode(decoded)
                // TODO: Should we reuse old PathSegments? Probably not.
                return appendEncodedSegment(oldPart, encoded!!)
            }

            /**
             * Creates a path from the encoded string.
             *
             * @param encoded part string
             */
            fun fromEncoded(encoded: String?): PathPart {
                return from(encoded, NotCachedHolder.NOT_CACHED)
            }

            /**
             * Creates a path from the decoded string.
             *
             * @param decoded part string
             */
            fun fromDecoded(decoded: String?): PathPart {
                return from(NotCachedHolder.NOT_CACHED, decoded)
            }

            /**
             * Creates a path from the encoded and decoded strings.
             *
             * @param encoded part string
             * @param decoded part string
             */
            fun from(encoded: String?, decoded: String?): PathPart {
                if (encoded == null) {
                    return NULL
                }
                return if (encoded.length == 0) {
                    EMPTY
                } else PathPart(encoded, decoded)
            }

            /**
             * Prepends path values with "/" if they're present, not empty, and
             * they don't already start with "/".
             */
            fun makeAbsolute(oldPart: PathPart): PathPart {
                val encodedCached = oldPart.encoded !== NotCachedHolder.NOT_CACHED
                // We don't care which version we use, and we don't want to force
                // unneccessary encoding/decoding.
                val oldPath = if (encodedCached) oldPart.encoded else oldPart.decoded
                if (oldPath == null || oldPath.length == 0 || oldPath.startsWith("/")) {
                    return oldPart
                }
                // Prepend encoded string if present.
                val newEncoded =
                    if (encodedCached) "/" + oldPart.encoded else NotCachedHolder.NOT_CACHED
                // Prepend decoded string if present.
                val decodedCached = oldPart.decoded !== NotCachedHolder.NOT_CACHED
                val newDecoded =
                    if (decodedCached) "/" + oldPart.decoded else NotCachedHolder.NOT_CACHED
                return PathPart(newEncoded, newDecoded)
            }
        }
    }

    fun withAppendedPath(baseUri: Uri, pathSegment: String): Uri {
        var builder = baseUri.buildUpon()
        builder = builder.appendEncodedPath(pathSegment)
        return builder.build()
    }

    fun getCanonicalUri(): Uri {
        return this
        //TODO
        /*return if ("file" == getScheme()) {
            val canonicalPath: String
            canonicalPath = try {
                File(getPath()).getCanonicalPath()
            } catch (e: IOException) {
                return this
            }
            if (Environment.isExternalStorageEmulated()) {
                val legacyPath: String = Environment.getLegacyExternalStorageDirectory()
                    .toString()
                // Splice in user-specific path when legacy path is found
                if (canonicalPath.startsWith(legacyPath)) {
                    return fromFile(
                        File(
                            Environment.getExternalStorageDirectory().toString(),
                            canonicalPath.substring(legacyPath.length + 1)
                        )
                    )
                }
            }
            fromFile(File(canonicalPath))
        } else {
            this
        }*/
    }

    /**
     * If this is a `file://` Uri, it will be reported to
     * [StrictMode].
     *
     * @hide
     */
    fun checkFileUriExposed(location: String?) {
        /*if ("file" == getScheme() && getPath() != null && !getPath()!!.startsWith("/system/")) {
            StrictMode.onFileUriExposed(this, location)
        }*/
    }

    /**
     * If this is a `content://` Uri without access flags, it will be
     * reported to [StrictMode].
     *
     * @hide
     */
    fun checkContentUriWithoutPermission(location: String?, flags: Int) {
        /*if ("content" == getScheme() && !Intent.isAccessUriMode(flags)) {
            StrictMode.onContentUriWithoutPermission(this, location)
        }*/
    }

    /**
     * Test if this is a path prefix match against the given Uri. Verifies that
     * scheme, authority, and atomic path segments match.
     *
     * @hide
     */
    fun isPathPrefixMatch(prefix: Uri): Boolean {
        if (getScheme() != prefix.getScheme()) return false
        if (getAuthority() != prefix.getAuthority()) return false
        val seg = getPathSegments()
        val prefixSeg = prefix.getPathSegments()
        val prefixSize = prefixSeg.size
        if (seg.size < prefixSize) return false
        for (i in 0 until prefixSize) {
            if (seg[i] != prefixSeg[i]) {
                return false
            }
        }
        return true
    }

    companion object {
        /*
    This class aims to do as little up front work as possible. To accomplish
    that, we vary the implementation depending on what the user passes in.
    For example, we have one implementation if the user passes in a
    URI string (StringUri) and another if the user passes in the
    individual components (OpaqueUri).
    *Concurrency notes*: Like any truly immutable object, this class is safe
    for concurrent use. This class uses a caching pattern in some places where
    it doesn't use volatile or synchronized. This is safe to do with ints
    because getting or setting an int is atomic. It's safe to do with a String
    because the internal fields are final and the memory model guarantees other
    threads won't see a partially initialized instance. We are not guaranteed
    that some threads will immediately see changes from other threads on
    certain platforms, but we don't mind if those threads reconstruct the
    cached result. As a result, we get thread safe caching with no concurrency
    overhead, which means the most common case, access from a single thread,
    is as fast as possible.
    From the Java Language spec.:
    "17.5 Final Field Semantics
    ... when the object is seen by another thread, that thread will always
    see the correctly constructed version of that object's final fields.
    It will also see versions of any object or array referenced by
    those final fields that are at least as up-to-date as the final fields
    are."
    In that same vein, all non-transient fields within Uri
    implementations should be final and immutable so as to ensure true
    immutability for clients even when they don't use proper concurrency
    control.
    For reference, from RFC 2396:
    "4.3. Parsing a URI Reference
       A URI reference is typically parsed according to the four main
       components and fragment identifier in order to determine what
       components are present and whether the reference is relative or
       absolute.  The individual components are then parsed for their
       subparts and, if not opaque, to verify their validity.
       Although the BNF defines what is allowed in each component, it is
       ambiguous in terms of differentiating between an authority component
       and a path component that begins with two slash characters.  The
       greedy algorithm is used for disambiguation: the left-most matching
       rule soaks up as much of the URI reference string as it is capable of
       matching.  In other words, the authority component wins."
    The "four main components" of a hierarchical URI consist of
    <scheme>://<authority><path>?<query>
    */

        /**
         * The empty URI, equivalent to "".
         */
        val EMPTY: Uri = HierarchicalUri(
            null, Part.NULL,
            PathPart.EMPTY, Part.NULL, Part.NULL
        )

        /** Index of a component which was not found.  */
        private const val NOT_FOUND = -1

        /** Placeholder value for an index which hasn't been calculated yet.  */
        private const val NOT_CALCULATED = -2

        /**
         * Error message presented when a user tries to treat an opaque URI as
         * hierarchical.
         */
        private const val NOT_HIERARCHICAL = "This isn't a hierarchical URI."

        /** Default encoding.  */
        private const val DEFAULT_ENCODING = "UTF-8"

        /**
         * Creates a Uri which parses the given encoded URI string.
         *
         * @param uriString an RFC 2396-compliant, encoded URI
         * @throws NullPointerException if uriString is null
         * @return Uri for this given uri string
         */
        fun parse(uriString: String): Uri {
            return StringUri(uriString)
        }

        /**
         * Creates a Uri from a file. The URI has the form
         * "file://<absolute path>". Encodes path characters with the exception of
         * '/'.
         *
         *
         * Example: "file:///tmp/android.txt"
         *
         * @throws NullPointerException if file is null
         * @return a Uri for the given file
        </absolute> */
        //TODO
        /*fun fromFile(file: File?): Uri {
            if (file == null) {
                throw NullPointerException("file")
            }
            val path = PathPart.fromDecoded(file.absolutePath)
            return HierarchicalUri(
                "file", Part.EMPTY, path, Part.NULL, Part.NULL
            )
        }*/

        /**
         * Creates an opaque Uri from the given components. Encodes the ssp
         * which means this method cannot be used to create hierarchical URIs.
         *
         * @param scheme of the URI
         * @param ssp scheme-specific-part, everything between the
         * scheme separator (':') and the fragment separator ('#'), which will
         * get encoded
         * @param fragment fragment, everything after the '#', null if undefined,
         * will get encoded
         *
         * @throws NullPointerException if scheme or ssp is null
         * @return Uri composed of the given scheme, ssp, and fragment
         *
         * @see Builder if you don't want the ssp and fragment to be encoded
         */
        fun fromParts(
            scheme: String?, ssp: String?,
            fragment: String?
        ): Uri {
            if (scheme == null) {
                throw NullPointerException("scheme")
            }
            if (ssp == null) {
                throw NullPointerException("ssp")
            }
            return OpaqueUri(
                scheme, Part.fromDecoded(ssp),
                Part.fromDecoded(fragment)
            )
        }

        /** Identifies a null parcelled Uri.  */
        private const val NULL_TYPE_ID = 0
        /**
         * Reads Uris from Parcels.
         */

        /**
         * Writes a Uri to a Parcel.
         *
         * @param out parcel to write to
         * @param uri to write, can be null
         */
        /*public static void writeToParcel(Parcel out, Uri uri) {
        if (uri == null) {
            out.writeInt(NULL_TYPE_ID);
        } else {
            uri.writeToParcel(out, 0);
        }
    }*/
        private val HEX_DIGITS = "0123456789ABCDEF".toCharArray()
        /**
         * Encodes characters in the given string as '%'-escaped octets
         * using the UTF-8 scheme. Leaves letters ("A-Z", "a-z"), numbers
         * ("0-9"), and unreserved characters ("_-!.~'()*") intact. Encodes
         * all other characters with the exception of those specified in the
         * allow argument.
         *
         * @param s string to encode
         * @param allow set of additional characters to allow in the encoded form,
         * null if no characters should be skipped
         * @return an encoded version of s suitable for use as a URI component,
         * or null if s is null
         */
        /**
         * Encodes characters in the given string as '%'-escaped octets
         * using the UTF-8 scheme. Leaves letters ("A-Z", "a-z"), numbers
         * ("0-9"), and unreserved characters ("_-!.~'()*") intact. Encodes
         * all other characters.
         *
         * @param s string to encode
         * @return an encoded version of s suitable for use as a URI component,
         * or null if s is null
         */
        fun encode(s: String?, allow: String? = null): String? {
            if (s == null) {
                return null
            }
            // Lazily-initialized buffers.
            var encoded: StringBuilder? = null
            val oldLength = s.length
            // This loop alternates between copying over allowed characters and
            // encoding in chunks. This results in fewer method calls and
            // allocations than encoding one character at a time.
            var current = 0
            while (current < oldLength) {
                // Start in "copying" mode where we copy over allowed chars.
                // Find the next character which needs to be encoded.
                var nextToEncode = current
                while (nextToEncode < oldLength
                    && isAllowed(s[nextToEncode], allow)
                ) {
                    nextToEncode++
                }
                // If there's nothing more to encode...
                if (nextToEncode == oldLength) {
                    return if (current == 0) {
                        // We didn't need to encode anything!
                        s
                    } else {
                        // Presumably, we've already done some encoding.
                        encoded!!.append(s, current, oldLength)
                        encoded.toString()
                    }
                }
                if (encoded == null) {
                    encoded = StringBuilder()
                }
                if (nextToEncode > current) {
                    // Append allowed characters leading up to this point.
                    encoded.append(s, current, nextToEncode)
                } else {
                    // assert nextToEncode == current
                }
                // Switch to "encoding" mode.
                // Find the next allowed character.
                current = nextToEncode
                var nextAllowed = current + 1
                while (nextAllowed < oldLength
                    && !isAllowed(s[nextAllowed], allow)
                ) {
                    nextAllowed++
                }
                // Convert the substring to bytes and encode the bytes as
                // '%'-escaped octets.
                val toEncode = s.substring(current, nextAllowed)
                try {
                    val bytes = toEncode.encodeToByteArray()
                    val bytesLength = bytes.size
                    for (i in 0 until bytesLength) {
                        encoded.append('%')
                        encoded.append(HEX_DIGITS[bytes[i].toInt() and 0xf0 shr 4])
                        encoded.append(HEX_DIGITS[bytes[i].toInt() and 0xf])
                    }
                } catch (e: Exception) {
                    throw AssertionError(e)
                }
                current = nextAllowed
            }
            // Encoded could still be null at this point if s is empty.
            return encoded?.toString() ?: s
        }

        /**
         * Returns true if the given character is allowed.
         *
         * @param c character to check
         * @param allow characters to allow
         * @return true if the character is allowed or false if it should be
         * encoded
         */
        private fun isAllowed(c: Char, allow: String?): Boolean {
            return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || "_-!.~'()*".indexOf(
                c
            ) != NOT_FOUND || allow != null && allow.indexOf(c) != NOT_FOUND
        }

        /**
         * Decodes '%'-escaped octets in the given string using the UTF-8 scheme.
         * Replaces invalid octets with the unicode replacement character
         * ("\\uFFFD").
         *
         * @param s encoded string to decode
         * @return the given string with escaped octets decoded, or null if
         * s is null
         */
        fun decode(s: String?): String? {
            return if (s == null) {
                null
            } else UriCodec.decode(
                s,
                false /* convertPlus */,
                false /* throwOnFailure */
            )
        }

        /**
         * Creates a new Uri by appending an already-encoded path segment to a
         * base Uri.
         *
         * @param baseUri Uri to append path segment to
         * @param pathSegment encoded path segment to append
         * @return a new Uri based on baseUri with the given segment appended to
         * the path
         * @throws NullPointerException if baseUri is null
         */
        fun withAppendedPath(baseUri: Uri, pathSegment: String): Uri {
            var builder = baseUri.buildUpon()
            builder = builder.appendEncodedPath(pathSegment)
            return builder.build()
        }
    }
}