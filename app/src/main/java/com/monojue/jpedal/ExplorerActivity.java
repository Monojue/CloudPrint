package com.monojue.jpedal;

import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.monojue.jpedal.R.mipmap.ic_launcher_round;

public class ExplorerActivity extends AppCompatActivity {

    private List<String> item = null;
    private TextView myPath;
    SharedPreferences myPrefs;
    private List<String> path = null;
    private List<Integer> type = null;
    private String root = Environment.getExternalStorageDirectory().getAbsolutePath();
    public Integer[] icon = new Integer[]{R.drawable.ic_folder,R.drawable.ic_doc,R.drawable.ic_image,R.drawable.ic_pdf,R.drawable.ic_pp};
    public ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(ic_launcher_round);

        this.listView = (ListView) findViewById(R.id.list);
        this.myPath = (TextView) findViewById(R.id.path);
        getDir(this.root);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onListItemClick(view, i);
            }
        });
    }

    private void getDir(String dirPath) {
        this.myPath.setText("Location: " + dirPath);
        this.item = new ArrayList();
        this.path = new ArrayList();
        this.type = new ArrayList();

        File f = new File(dirPath);
        File[] files = f.listFiles();
        if (!dirPath.equals(this.root)) {
            this.item.add(this.root);
            this.path.add(this.root);
            this.item.add("../");
            this.path.add(f.getParent());
        }
        for (File file : files) {
            this.path.add(file.getPath());
            if (file.isDirectory()) {
                this.item.add(file.getName() + "/");
            } else {
                this.item.add(file.getName());
            }
        }

        ArrayAdapter<String> myAdapter = new ArrayAdapter <String>(this,
                R.layout.row, R.id.rowtext, item);
        /*ImageView imageView = (ImageView) findViewById(R.id.row_img);
        imageView.setBackgroundResource(icon[1]);*/
        listView.setAdapter(myAdapter);


    }

    protected void onListItemClick(final View v, final int position) {

        Animation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(200);
        v.startAnimation(animation);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final File file = new File((String) path.get(position));
                final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString()));
                final MainActivity mainActivity = new MainActivity();
                if (!file.isDirectory()) {
                    final String s = (String) path.get(position);
                    new AlertDialog.Builder(v.getContext()).setIcon(R.mipmap.ic_launcher).setTitle("Are you Sure?").setMessage("[" + file.getName() + "]").setPositiveButton("Print", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        public void onClick(DialogInterface dialog, int which) {
                   /* AndroidExplorer.this.myPrefs = AndroidExplorer.this.getSharedPreferences("myPrefs", 0);
                    SharedPreferences.Editor prefsEditor = AndroidExplorer.this.myPrefs.edit();
                    prefsEditor.putString("path", s);
                    prefsEditor.commit();*/
                            //mainActivity.submitPrintJob(AndroidExplorer.this, Uri.fromFile(file), mimeType, file.getName());
                            printDocument(v, file.getName(), file.getAbsolutePath());
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    }).setCancelable(false).show();
                } else if (file.canRead()) {
                    getDir((String) path.get(position));
                } else {
                    new AlertDialog.Builder(v.getContext()).setIcon(R.mipmap.ic_launcher).setTitle("[" + file.getName() + "] folder can't be read!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
                }
            }
        },300);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void printDocument(View view, final String jobName, final String fileName)
    {
//        PrintManager printManager = (PrintManager) this
//                .getSystemService(Context.PRINT_SERVICE);
//
//        //String jobName = "Document";
//
//        printManager.print(jobName, new MainActivity.MyPrintDocumentAdapter(this), null);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);

            PrintDocumentAdapter pda = new PrintDocumentAdapter() {



                @Override
                public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                                    CancellationSignal cancellationSignal, WriteResultCallback callback) {
                    InputStream input = null;
                    OutputStream output = null;



                    try {
//                        AssetManager assetManager = getAssets();
                        File file = new File(fileName);
//                        input = assetManager.open("Korea.pdf");
                        input = new FileInputStream(file);
                        output = new FileOutputStream(destination.getFileDescriptor());
                        byte[] buf = new byte[1024];
                        int bytesRead;

                        while ((bytesRead = input.read(buf)) > 0) {
                            output.write(buf, 0, bytesRead);
                        }

                        callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

                    } catch (FileNotFoundException ee) {
                        //Catch exception
                    } catch (Exception e) {
                        //Catch exception
                    } finally {
                        try {
                            input.close();
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                     CancellationSignal cancellationSignal, LayoutResultCallback callback,
                                     Bundle extras) {

                    if (cancellationSignal.isCanceled()) {
                        callback.onLayoutCancelled();
                        return;
                    }

//                    int pages = computePageCount(newAttributes);

                    PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(jobName).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();

                    callback.onLayoutFinished(pdi, true);
                }

//                private int computePageCount(PrintAttributes printAttributes){
//                    int itemsPerPage = 4;
//
//                    PrintAttributes.MediaSize pageSize = printAttributes.getMediaSize();
//                    if(!pageSize.isPortrait()){
//                        itemsPerPage =6;
//                    }
//
//                    int printItemCount =52;
//                    return  (int) Math.ceil(printItemCount/itemsPerPage);
//                }
            };
            printManager.print(jobName, pda, null);
        }
    }




    @Override
    public void onBackPressed() {

        if (this.item.get(1) =="../"){
            getDir((String) this.path.get(1));
        }else{
            super.onBackPressed();
        }

    }
}
