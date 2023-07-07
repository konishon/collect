package org.odk.collect.android.utilities;



public class MbTilesFetchResult {

    public final String errorMessage;
    public final int responseCode;
    public final String file;

    public MbTilesFetchResult(String msg, int response,String file) {
        responseCode = response;
        errorMessage = msg;
        this.file = file;

    }

    public MbTilesFetchResult(String file) {
        this.file = file;
        responseCode = 0;
        errorMessage = null;

    }


}
