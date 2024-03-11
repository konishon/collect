package org.odk.collect.android.geo
import org.odk.collect.android.openrosa.OpenRosaHttpInterface
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.settings.SettingsProvider
class MapLayerSourceProvider(
    private val settingsProvider: SettingsProvider,
    private val mbTileHttpInterface: OpenRosaHttpInterface
) {

    @JvmOverloads
    fun get(serverURL: String? = null, projectId: String? = null): MapLayerSource {
        val generalSettings = settingsProvider.getUnprotectedSettings(projectId)

        return MbTilesSource(
            serverURL,
            mbTileHttpInterface,
            WebCredentialsUtils(generalSettings)
        )
    }
}
