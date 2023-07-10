package org.odk.collect.android.geo;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloader extends AsyncTask<String, Void, Boolean> {

    private static final String FOLDER_NAME = "layers";



    private final FileDownloadListener listener;

    public interface FileDownloadListener {
        void onFileDownloaded(boolean success);
    }

    public FileDownloader(FileDownloadListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);

        // Update UI on the main (UI) thread
        new Handler(Looper.getMainLooper()).post(() -> {
            if (listener != null) {
                listener.onFileDownloaded(success);
            }
        });
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String fileUrl = params[0];
        String fileName = params[1];

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return false; // Failed to download file
            }

            InputStream inputStream = new BufferedInputStream(connection.getInputStream());


            String path = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.LAYERS);
            // Create the "layers" folder in the external storage directory
            File folder = new File(path);


            // Create the output file
            File outputFile = new File(folder, fileName);

            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.close();
            inputStream.close();

            return true; // File downloaded successfully
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Failed to download file
        }


    }
}
