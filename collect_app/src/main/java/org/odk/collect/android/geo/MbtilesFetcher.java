package org.odk.collect.android.geo;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.odk.collect.android.openrosa.HttpGetResult;
<<<<<<< HEAD

=======
import org.odk.collect.android.openrosa.OpenRosaHttpInterface;
>>>>>>> e8f8a73d5b (feat: configured file download)
import org.odk.collect.android.utilities.MbTilesFetchResult;
import org.odk.collect.android.utilities.WebCredentialsUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import timber.log.Timber;

public class MbtilesFetcher {
<<<<<<< HEAD
    private static final String HTTP_CONTENT_TYPE_TEXT_ZIP = "application/x-sqlite3";
=======

    private static final String HTTP_CONTENT_TYPE_TEXT_ZIP = "application/octet-stream";
>>>>>>> e8f8a73d5b (feat: configured file download)

    private final OpenRosaHttpInterface httpInterface;
    private final WebCredentialsUtils webCredentialsUtils;

    public MbtilesFetcher(OpenRosaHttpInterface httpInterface, WebCredentialsUtils webCredentialsUtils) {
        this.httpInterface = httpInterface;
        this.webCredentialsUtils = webCredentialsUtils;
    }

    public MbTilesFetchResult getMbtiles(String urlString) throws Exception {


        // parse response
        String file;
        HttpGetResult inputStreamResult;

        try {
            inputStreamResult = fetch(urlString, HTTP_CONTENT_TYPE_TEXT_ZIP);
            if (inputStreamResult.getStatusCode() != HttpURLConnection.HTTP_OK) {
                String error = "getMbtilesZip failed while accessing "
                        + urlString + " with status code: " + inputStreamResult.getStatusCode();
                return new MbTilesFetchResult(error, inputStreamResult.getStatusCode(),null);
            }

            try (InputStream resultInputStream = inputStreamResult.getInputStream();
                 InputStreamReader streamReader = new InputStreamReader(resultInputStream, "UTF-8")) {
                 file = "";
            }
        } catch (Exception e) {
            Timber.i(e);
            throw e;
        }

        return new MbTilesFetchResult(file);
    }

    @NonNull
    private HttpGetResult fetch(@NonNull String downloadUrl, @Nullable final String contentType) throws Exception {
        URI uri;

        try {
            // assume the downloadUrl is escaped properly
            URL url = new URL(downloadUrl);
            uri = url.toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            Timber.e(e, "Unable to get a URI for download URL : %s  due to %s : ", downloadUrl, e.getMessage());
            throw e;
        }

        if (uri.getHost() == null) {
            Timber.e(new Error("Invalid server URL (no hostname): " + downloadUrl));
            throw new Exception("Invalid server URL (no hostname): " + downloadUrl);
        }
<<<<<<< HEAD
=======


>>>>>>> e8f8a73d5b (feat: configured file download)
        return httpInterface.executeGetRequest(uri, contentType, webCredentialsUtils.getCredentials(uri));
    }
}
