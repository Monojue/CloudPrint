package com.monojue.jpedal;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.print.PrinterCapabilitiesInfo.Builder;
import android.print.PrintAttributes.MediaSize;
import android.print.PrintAttributes.Resolution;
import android.print.PrintAttributes.Margins;
import android.util.Printer;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kaung Myat on 8/4/2018.
 */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class Service extends PrintService {


    @Override
    protected void onConnected() {
        super.onConnected();
        try {
            List<PrintJob> pjl = getActivePrintJobs();
            for (int i = 0; i < pjl.size(); i++) {
                ((PrintJob) pjl.get(i)).cancel();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDisconnected() {
        super.onDisconnected();
    }

    @Nullable
    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        return new PrinterDiscoverySession() {
            @Override
            public void onStartPrinterDiscovery(@NonNull List<PrinterId> list) {
                List<PrinterInfo> pl = new ArrayList();
                PrinterId pid = Service.this.generatePrinterId("default");
                PrinterInfo pi = null;
//                Printer prn = ActivityCore.getPrinter();
//                if (prn != null) {
//                    try {
//                        Builder pcb = new Builder(pid);
//                        boolean fl = false;
//                        for (int i = 0; i < prn.paper_list.size(); i++) {
//                            Paper p = (Paper) prn.paper_list.get(i);
//                            if (!(p.id == null || p.id.length() == 0 || p.name == null || p.name.length() == 0)) {
//                                pcb.addMediaSize(new MediaSize(p.id, p.name, (p.width * 1000) / DnsConstants.TYPE_MAILB, (p.height * 1000) / DnsConstants.TYPE_MAILB), p.id.equals(prn.paper_default));
//                                fl = true;
//                            }
//                        }
//                        if (fl) {
//                            pcb.addResolution(new Resolution("default", "Default", 300, 300), true);
//                            pcb.setColorModes(3, 2);
//                            pcb.setMinMargins(Margins.NO_MARGINS);
//                            pi = new PrinterInfo.Builder(pid, prn.title, 1).setCapabilities(pcb.build()).build();
//                        }
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                        App.reportThrowable(ex, prn.model + " | " + prn.drv_name);
//                    }
//                }
                if (pi == null) {
                    pi = new PrinterInfo.Builder(pid, "CloudPrint", 1).setCapabilities(new Builder(pid).addMediaSize(new MediaSize("Letter", "Letter", MediaSize.NA_LETTER.getWidthMils(), MediaSize.NA_LETTER.getHeightMils()), true).addMediaSize(new MediaSize("A4", "A4", MediaSize.ISO_A4.getWidthMils(), MediaSize.ISO_A4.getHeightMils()), false).addResolution(new Resolution("default", "Default", 150, 150), true).setColorModes(3, 2).setMinMargins(Margins.NO_MARGINS).build()).build();
                }
                pl.add(pi);
                addPrinters(pl);
            }

            @Override
            public void onStopPrinterDiscovery() {

            }

            @Override
            public void onValidatePrinters(@NonNull List<PrinterId> list) {

            }

            @Override
            public void onStartPrinterStateTracking(@NonNull PrinterId printerId) {

            }

            @Override
            public void onStopPrinterStateTracking(@NonNull PrinterId printerId) {

            }

            @Override
            public void onDestroy() {

            }
        };

    }

    @Override
    protected void onRequestCancelPrintJob(PrintJob printJob) {
        printJob.cancel();
    }

    @Override
    protected void onPrintJobQueued(PrintJob printJob) {
        printJob.start();

//        try {
//            File dt = new File("/storage/emulated/0/cloudPrint/", "cloudPrint_temp.pdf");
//            FileOutputStream fos = new FileOutputStream(dt);
//            FileInputStream fis = new FileInputStream(printJob.getDocument().getData().getFileDescriptor());
//            byte[] buf = new byte[NtlmFlags.NTLMSSP_NEGOTIATE_OEM_DOMAIN_SUPPLIED];
//            while (true) {
//                int z = fis.read(buf);
//                if (z == -1) {
//                    break;
//                }
//                fos.write(buf, 0, z);
//            }
//            fos.close();
//            fis.close();
//            Intent i = new Intent("android.intent.action.VIEW");
//            i.putExtra("temp_file", dt.getAbsolutePath());
//            i.putExtra("job_info", printJob.getInfo());
//            i.setFlags(268435456);
//            i.setClass(getApplicationContext(), GCP.class);
//            i.setDataAndType(Uri.fromFile(dt), "application/pdf");
//            Toast.makeText(this, dt.getAbsolutePath() , Toast.LENGTH_SHORT).show();
//            startActivity(i);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//
//        }

        if (printJob.isQueued()) {
            printJob.start();
        }
        final PrintJobInfo info = printJob.getInfo();
        final File file = new File(getFilesDir(), info.getLabel() + ".pdf");


        InputStream in = null;
        FileOutputStream out = null;

        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString()));
        Uri uri = Uri.fromFile(file);

        try {
            in = new FileInputStream(printJob.getDocument().getData().getFileDescriptor());
            out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            out.flush();
            out.close();


//            Intent printPreview = new Intent(this, PrintDialogActivity.class);
//            printPreview.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            printPreview.putExtra("FILE", file.getPath());
//            Toast.makeText(this, file.getAbsolutePath().toString(), Toast.LENGTH_SHORT).show();
//            startActivity(printPreview);

            Intent printIntent = new Intent(this, PrintDialogActivity.class);
            printIntent.setDataAndType(uri, mimeType);
            printIntent.putExtra("title", file.getName());
            startActivity(printIntent);

//            submitPrintJob.submit(this, Uri.fromFile(file), mimeType, file.getName()  );

        } catch (IOException ioe) {

        }
        printJob.complete();

    }
}
