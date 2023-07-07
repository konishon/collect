package org.odk.collect.android.geo;

import org.jetbrains.annotations.NotNull;

/**
 * A place where forms live (outside the app's storage). Ideally in future this would be
 * a common interface for getting forms from a server, Google Drive or even the disk.
 */
public interface MapLayerSource {


    @NotNull
    String fetch() throws MapLayerSourceException;


}
