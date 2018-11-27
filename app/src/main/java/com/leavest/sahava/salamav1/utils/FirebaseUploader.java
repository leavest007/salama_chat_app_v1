package com.leavest.sahava.salamav1.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.net.URISyntaxException;

public class FirebaseUploader {

    private UploadListener uploadListener;
    private UploadTask uploadTask;
    private AsyncTask<File, Void, String> compressionTask;
    private boolean replace;
    private StorageReference uploadRef;
    private Uri fileUri;

    public FirebaseUploader(UploadListener uploadListener) {
        this.uploadListener = uploadListener;
    }


    public FirebaseUploader(UploadListener uploadListener, StorageReference storageReference) {
        this.uploadListener = uploadListener;
        this.uploadRef = storageReference;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public void uploadImage(Context context, File file) {
        compressAndUpload(context, "images", file);
    }

    public void uploadAudio(Context context, File file) {
        compressAndUpload(context, "audios", file);
    }

    public void uploadVideo(Context context, File file) {
        compressAndUpload(context, "videos", file);
    }

    public void uploadOthers(Context context, File file) {
        compressAndUpload(context, "others", file);
    }

    @SuppressLint("StaticFieldLeak")
    private void compressAndUpload(final Context context, final String child, final File file) {
       compressionTask = new AsyncTask<File, Void, String>() {
           @Override
           protected String doInBackground(File... files) {
               String filePathCompressed = null;
               Uri originalFileUri = Uri.fromFile(files[0]);
               File tempFile = new File(context.getCacheDir(), originalFileUri.getLastPathSegment());

               if (child.equals("images")) {
                   filePathCompressed = SiliCompressor.with(context).compress(originalFileUri.toString(), tempFile);
               } else {
                   // Jika perlu untuk compress video bisa aktif coding dibawah ini
                   try {
                       filePathCompressed = SiliCompressor.with(context).compressVideo(files[0].getPath(), context.getCacheDir().getPath());
                   } catch (URISyntaxException e) {
                       e.printStackTrace();
                   }
               }
               if (filePathCompressed == null)
                   filePathCompressed = "";
               return filePathCompressed;
           }

           @Override
           protected void onPostExecute(String s) {
               super.onPostExecute(s);
               File compressed = new File(s);
               fileUri = Uri.fromFile(compressed.length() > 0 ? compressed : file);
               FirebaseStorage storage = FirebaseStorage.getInstance();
               if (uploadRef == null)
                   uploadRef = storage.getReference().child(child).child(fileUri.getLastPathSegment());

           }
       }
    }


    public abstract static class UploadListener {
        public abstract void onUploadFail(String message);

        public abstract void onUploadSuccess(String downloadUrl);

        public abstract void onUploadProgress(int progress);

        public abstract void onUploadCancelled();
    }
}
