package org.odk.collect.android.geo
import org.odk.collect.android.openrosa.OpenRosaFormSource
import org.odk.collect.android.openrosa.OpenRosaHttpInterface
import org.odk.collect.android.openrosa.OpenRosaResponseParserImpl
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.FormSource
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
class MapLayerSourceProvider(
    private val mbTileHttpInterface: MbTilesHttpInterface
) {

    @JvmOverloads
    fun get(serverURL: String? = null): MapLayerSource {
        return MbTilesSource(
            serverURL,
            mbTileHttpInterface,
            null,
        )
    }
}
