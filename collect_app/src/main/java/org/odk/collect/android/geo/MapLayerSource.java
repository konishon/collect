package org.odk.collect.android.geo;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.forms.FormListItem;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.forms.ManifestFile;

import java.io.InputStream;
import java.util.List;

/**
 * A place where forms live (outside the app's storage). Ideally in future this would be
 * a common interface for getting forms from a server, Google Drive or even the disk.
 */
public interface MapLayerSource {


    @NotNull
    String fetchZip(String url) throws MapLayerSourceException;


}
