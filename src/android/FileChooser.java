package com.cesidiodibenedetto.filechooser;

import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CordovaActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;

import com.ipaulpro.afilechooser.utils.FileUtils;
import fr.matmut.app.mamatmut.R;

/**
 * FileChooser is a PhoneGap plugin that acts as polyfill for Android KitKat and web
 * applications that need support for <input type="file">
 * 
 */
public class FileChooser extends CordovaPlugin {

    private CallbackContext callbackContext = null;
    private static final String TAG = "FileChooser";
    private static final int REQUEST_CODE = 6666; // onActivityResult request code

    private void showFileChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, this.cordova.getActivity().getString(R.string.chooser_title));
        try {
            this.cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode == REQUEST_CODE) {
                // If the file selection was successful
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        JSONObject obj = new JSONObject();
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this.cordova.getActivity(), uri);
							final boolean isGoogleDriveDocument = FileUtils.isGoogleDriveUri(uri);
							final String mimeType;
							final long size;
							final String filename;
							if (isGoogleDriveDocument) {
								mimeType = FileUtils.getDataMimeType(this.cordova.getActivity(), uri, null, null);
								size = FileUtils.getDataSize(this.cordova.getActivity(), uri, null, null);
								filename = FileUtils.getDataName(this.cordova.getActivity(), uri, null, null);
							} else {
								mimeType = FileUtils.getMimeType(this.cordova.getActivity(), uri);
								size = FileUtils.getSize(this.cordova.getActivity(), uri);
								filename = FileUtils.getDataName(this.cordova.getActivity(), uri, null, null);
							}                            
                            obj.put("filepath", path);
                            obj.put("mimeType", mimeType);
                            obj.put("size", size);
							obj.put("filename", filename);
							obj.put("isRemoteFile", isGoogleDriveDocument);
                            this.callbackContext.success(obj);
                        } catch (Exception e) {
                            Log.e("FileChooser", "File select error", e);
                            this.callbackContext.error(e.getMessage());
                        }
                    }
                }
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
            if (action.equals("open")) {
                showFileChooser();
                return true;
            }
            else {
                return false;
            }
    }

}
