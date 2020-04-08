package com.monojue.jpedal;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.RequiresApi;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.R.attr.bitmap;
import static com.monojue.jpedal.R.mipmap.ic_launcher_round;


public class MainActivity extends AppCompatActivity {

    private static final int GALLERY_IMAGE_PICK = 1;
    private String[] name = {"Browser","Image","Documents"};
   // private Integer[] drawble = {R.drawable.btn_ic_web,R.drawable.btn_ic_pictures,R.drawable.btn_ic_docs};

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == GALLERY_IMAGE_PICK && resultCode == RESULT_OK && null!=data) {

            try {
//                Uri selectedImage = data.getData();
//                String picturePath = getRealPathFromURI(selectedImage, this);
//                String filename = getFileNameformURI(picturePath);

//                String picturePath = filePath.getUriRealPath(this, selectedImage);
//                String filename = getFileNameformURI(picturePath);

                final Uri imageUri = data.getData();
                final InputStream imageStream;

                imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                String filename = getFileName(imageUri);

//                String mimetype = getContentResolver().getType(imageUri);

                PrintHelper printHelper = new PrintHelper(this);
                printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
//            Bitmap bitmap = BitmapFactory(picturePath);
                printHelper.printBitmap(filename, bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }




        }

    }

    private String getFileNameformURI(String path) {
        String file;

        String imageName = path.substring(path.lastIndexOf("/")+1);
//        if (imageName.indexOf(".")>0){
//            file = imageName.substring(0,imageName.lastIndexOf("."));
//        }else {
//            file = imageName;
//        }
        Toast.makeText(this, imageName, Toast.LENGTH_SHORT).show();

        return imageName;
    }

    private String getFileName(Uri contextURI) {
        Cursor cursor = getContentResolver().query(contextURI, null, null, null, null);
        int name_index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        return cursor.getString(name_index);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(ic_launcher_round);

        findViewById(R.id.btn_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent gallerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(gallerIntent, GALLERY_IMAGE_PICK);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent.createChooser(intent,"Select Picture"),GALLERY_IMAGE_PICK);
                    }
                },300);

            }
        });

        findViewById(R.id.btn_web).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(view.getContext(), WebBrowserActivity.class));
                    }
                },300);

            }
        });

        findViewById(R.id.btn_docs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(view.getContext(), ExplorerActivity.class));
                    }
                },300);

            }
        });

//        button = (Button) findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//            @Override
//            public void onClick(View view) {
//                printDocument(view);
//            }
//        });


    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void printDocument(View view, String jobName)
    {
        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        printManager.print(jobName, new MyPrintDocumentAdapter(this), null);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static class MyPrintDocumentAdapter extends PrintDocumentAdapter
    {
        Context context;
        private int pageHeight;
        private int pageWidth;
        public PdfDocument myPdfDocument;
        public int totalpages = 4;

        public MyPrintDocumentAdapter(Context context)
        {
            this.context = context;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes,
                             PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback,
                             Bundle metadata) {

            myPdfDocument = new PrintedPdfDocument(context, newAttributes);

            pageHeight =
                    newAttributes.getMediaSize().getHeightMils()/1000 * 72;
            pageWidth =
                    newAttributes.getMediaSize().getWidthMils()/1000 * 72;

            if (cancellationSignal.isCanceled() ) {
                callback.onLayoutCancelled();
                return;
            }



            if (totalpages > 0) {
                PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                        .Builder("print_output.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(totalpages);

                PrintDocumentInfo info = builder.build();
                callback.onLayoutFinished(info, true);
            } else {
                callback.onLayoutFailed("Page count is zero.");
            }
        }


        @Override
        public void onWrite(final PageRange[] pageRanges,
                            final ParcelFileDescriptor destination,
                            final CancellationSignal
                                    cancellationSignal,
                            final WriteResultCallback callback) {


            for (int i = 0; i < totalpages; i++) {
                if (pageInRange(pageRanges, i))
                {
                    PdfDocument.PageInfo newPage = new PdfDocument.PageInfo.Builder(pageWidth,
                            pageHeight, i).create();

                    PdfDocument.Page page =
                            myPdfDocument.startPage(newPage);

                    if (cancellationSignal.isCanceled()) {
                        callback.onWriteCancelled();
                        myPdfDocument.close();
                        myPdfDocument = null;
                        return;
                    }
//                    drawPage(page, i);
                    myPdfDocument.finishPage(page);
                }
            }

            try {
                myPdfDocument.writeTo(new FileOutputStream(
                        destination.getFileDescriptor()));
            } catch (IOException e) {
                callback.onWriteFailed(e.toString());
                return;
            } finally {
                myPdfDocument.close();
                myPdfDocument = null;
            }

            callback.onWriteFinished(pageRanges);
        }

//        private void drawPage(PdfDocument.Page page,
//                              int pagenumber) {
//
//            Canvas canvas = page.getCanvas();
//
//            pagenumber++; // Make sure page numbers start at 1
//
//            int titleBaseLine = 72;
//            int leftMargin = 54;
//
//            Paint paint = new Paint();
//            paint.setColor(Color.BLACK);
//            paint.setTextSize(40);
//            canvas.drawText(
//                    "Test Print Document Page " + pagenumber,
//                    leftMargin,
//                    titleBaseLine,
//                    paint);
//
//            paint.setTextSize(14);
//            canvas.drawText("This is some test content to verify that custom document printing works", leftMargin, titleBaseLine + 35, paint);
//
//            if (pagenumber % 2 == 0)
//                paint.setColor(Color.RED);
//            else
//                paint.setColor(Color.GREEN);
//
//            PdfDocument.PageInfo pageInfo = page.getInfo();
//
//
//            canvas.drawCircle(pageInfo.getPageWidth()/2,
//                    pageInfo.getPageHeight()/2,
//                    150,
//                    paint);
//        }

        private boolean pageInRange(PageRange[] pageRanges, int page)
        {

            for (int i = 0; i<pageRanges.length; i++)
            {
                if ((page >= pageRanges[i].getStart()) &&
                        (page <= pageRanges[i].getEnd()))
                    return true;
            }
            return false;
        }

    }

    boolean doublePressed = false;
    @Override
    public void onBackPressed() {

        if (doublePressed){
            super.onBackPressed();
            return;
        }

        this.doublePressed = true;
        Toast.makeText(MainActivity.this, "Press again to Exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doublePressed = false;
            }
        },2000);

    }



}
