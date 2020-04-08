package com.monojue.jpedal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * Created by Kaung Myat on 8/5/2018.
 */

public class SubmitPrintJob {

    public static void submit(Service activity, Uri uri, String type, String title){

        if (type != null) {
            Intent printIntent = new Intent(activity, PrintDialogActivity.class);
            printIntent.setDataAndType(uri, type);
            printIntent.putExtra("title", title);
            activity.startActivity(printIntent);
        }else {
            Toast.makeText(activity, "File Type Wrong!", Toast.LENGTH_SHORT).show();
        }
    }
}
