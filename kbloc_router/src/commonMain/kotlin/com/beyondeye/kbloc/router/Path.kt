package com.beyondeye.kbloc.router
//original code from https://github.com/hfhbd/routing-compose

//custom implementation of critical path method
//it could be have been implemented with string.removePrefix("/").takeWhile { it != '/' }
private fun String.getFirstPathSegment():String {
    if(length==0) return this
    val hasSlashAtStart=this[0].equals('/')
    val start=if(hasSlashAtStart) 1 else 0
    for (index in start until length)
        if (get(index)=='/') {
            return substring(start, index)
        }
    return if(!hasSlashAtStart) this else substring(1)
}
/**
 * *DARIO* a html destination path, with potentially multiple levels and optional parameters
 */
public data class Path(val path: String, val parameters: Parameters?) {
    internal val firstSegment = path.getFirstPathSegment() //path.removePrefix("/").takeWhile { it != '/' }

    internal fun newPath(currentPath: String) = Path(path = path.removePrefix("/$currentPath"), parameters)

    /**
     * https://datatracker.ietf.org/doc/html/rfc1808
     * *DARIO* define a new path relative to the current one as defined in [path]
     * each "./" component in the relative path will drop the last level of the current path
     * for example: for
     *  val base = Path.from("/a/b/c/d")
     * base.relative("./") -> "/a/b/c"
     * base.relative("././") -> "/a/b"
     * base.relative("././g") -> "/a/b/g"
     * base.relative("././g?foo=bar") -> "/a/b/g?foo=bar"
     */
    internal fun relative(to: String): Path {
        val paths = path.split("/")
        val split = to.split("./")
        val result = split.last().let {
            if (it.isNotEmpty()) "/$it" else it
        }
        val number = split.count() - 1
        return from(
            paths.dropLast(number).joinToString(postfix = result, separator = "/") {
                it
            }
        )
    }

    internal companion object {
        /**
         * *DARIO* factory method for a [Path] object that take a http path string
         * and split it in the actual paths and the path parameters while also parsing the path parameters
         * and setting the [parameters] property in the created [Path] object
         * note that a maximum of a single parameter is allowed by this factory method
         * but the [parameters] field that is of type [Parameters] can actually support multiple parameters
         */
        fun from(rawPath: String): Path {
            val pathAndQuery = rawPath.split("?")
            val (path, rawParameters) = when (pathAndQuery.size) {
                1 -> { //no parameters
                    pathAndQuery.first() to null
                }

                2 -> { //one parameter
                    pathAndQuery.first() to pathAndQuery.last().let { Parameters.from(it) }
                }

                else -> { //more than one parameter
                    error("path contains more than 1 '?' delimiter: $rawPath")
                }
            }
            return Path(path.addPrefix("/"), rawParameters)
        }

        /**
         * add some specific prefix the specified String only if the prefix is not already present
         * this method is actually used only for adding the "/" prefix when creating a
         * [Path] instance with [from]
         */
        private fun String.addPrefix(prefix: String) = if (startsWith(prefix)) this else "$prefix$this"
    }

    override fun toString(): String = if (parameters == null) {
        path
    } else {
        "$path?$parameters"
    }

}
