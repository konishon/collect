package org.odk.collect.android.geo;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.openrosa.OpenRosaResponseParser;
import org.odk.collect.android.utilities.MbTilesFetchResult;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLException;

public class MbTilesSource implements MapLayerSource {

    private final MbtilesFetcher mbtilesFetcher;

    private final WebCredentialsUtils webCredentialsUtils;

    private String serverURL;

    public MbTilesSource(String serverURL, MbTilesHttpInterface mbTilesHttpInterface, WebCredentialsUtils webCredentialsUtils) {
        this.webCredentialsUtils = webCredentialsUtils;
        this.serverURL = serverURL;
        this.mbtilesFetcher = new MbtilesFetcher(mbTilesHttpInterface, this.webCredentialsUtils);
    }


    @Override
    public @NotNull String fetchZip(String url) throws MapLayerSourceException {
        MbTilesFetchResult result = mapException(() -> mbtilesFetcher.getMbtiles(url));

        if (result.errorMessage == null) {
            throw new MapLayerSourceException.ServerError(result.responseCode, serverURL);
        } else {
            return result.file;
        }
    }

    public void updateUrl(String url) {
        this.serverURL = url;
    }


    @NotNull
    private <T> T mapException(Callable<T> callable) throws MapLayerSourceException {
        try {
            T result = callable.call();

            if (result != null) {
                return result;
            } else {
                throw new MapLayerSourceException.FetchError();
            }
        } catch (UnknownHostException e) {
            throw new MapLayerSourceException.Unreachable(serverURL);
        } catch (SSLException e) {
            throw new MapLayerSourceException.SecurityError(serverURL);
        } catch (Exception e) {
            throw new MapLayerSourceException.FetchError();
        }
    }

    public WebCredentialsUtils getWebCredentialsUtils() {
        return webCredentialsUtils;
    }

}
