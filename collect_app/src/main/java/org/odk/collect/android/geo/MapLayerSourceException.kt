package org.odk.collect.android.geo

sealed class MapLayerSourceException : Exception() {
    class Unreachable(val serverUrl: String) : MapLayerSourceException()
    class AuthRequired : MapLayerSourceException()
    class FetchError : MapLayerSourceException()
    class SecurityError(val serverUrl: String) : MapLayerSourceException()
    class ServerError(val statusCode: Int, val serverUrl: String) : MapLayerSourceException()
    class ParseError(val serverUrl: String) : MapLayerSourceException()

    // Aggregate 0.9 and prior used a custom API before the OpenRosa standard was in place. Aggregate continued
    // to provide this response to HTTP requests so some custom servers tried to implement it.
    class ServerNotOpenRosaError : MapLayerSourceException()
}
