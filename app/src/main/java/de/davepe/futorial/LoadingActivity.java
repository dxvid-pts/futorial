package de.davepe.futorial;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class LoadingActivity extends AppCompatActivity {

    public static LoadingActivity i;

    TextView info;
    Button retry;
    ProgressBar check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        i = this;
        this.info = (TextView) findViewById(R.id.infoOffline);
        this.retry = (Button) findViewById(R.id.buttonOffline);
        this.check = (ProgressBar) findViewById(R.id.checkBar);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                check.setVisibility(View.VISIBLE);
                retry.setVisibility(View.GONE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            public void run() {
                                handler.post(new Runnable() {
                                    public void run() {
                                        if (!isOnline()) {
                                            check.setVisibility(View.GONE);
                                            retry.setVisibility(View.VISIBLE);
                                            Snackbar.make(view, "Du bist offline.", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        } else {
                                            check.setVisibility(View.GONE);
                                            retry.setVisibility(View.VISIBLE);
                                            startActivity(new Intent(i, MainActivity.class));
                                            finish();
                                        }
                                    }
                                });
                            }
                        }, 1000);
                    }
                }).start();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    public void run() {
                        handler.post(new Runnable() {
                            public void run() {
                                if (isOnline())
                                    cancel();
                            }
                        });
                    }
                }, 2000);
            }
        }).start();

        if (!isOnline()) {
            oflline();
        } else
            check4Updates();
    }

    public void displayUpdates(final String newVersion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        MainActivity.getMainactivity());

                // set title
                LinearLayout l = new LinearLayout(MainActivity.getMainactivity());
                l.setPadding(40, 30, 0, 10);
                l.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(MainActivity.getMainactivity());
                title.setTextSize(20);
                title.setText("Updatate verfügbar");
                title.setTextColor(MainActivity.getMainactivity().getResources().getColor(R.color.font));
                title.setPadding(0, 0, 0, 20);
                l.addView(title);

                TextView msg = new TextView(MainActivity.getMainactivity());
                msg.setTextSize(15);
                msg.setText("Version: " + newVersion);
                msg.setTextColor(MainActivity.getMainactivity().getResources().getColor(R.color.dark_font));
                msg.setPadding(20, 0, 0, 0);
                l.addView(msg);

                alertDialogBuilder.setCustomTitle(l);


                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                download();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                alertDialog.getWindow().setBackgroundDrawableResource(R.color.colorPrimaryDark);


            }
        });
    }

    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    retry.setVisibility(View.INVISIBLE);
                    download();

                } else {
                    Toast.makeText(LoadingActivity.this, "Die App benötigt die berechtigung, um das Update durchführen zu können.", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    public void check4Updates() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("checking...");
                    String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                    System.out.println("checking...");
                    String version = getVersion();
                    System.out.println("checking...");

                    if (version.equals("")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoadingActivity.this, "failed.", Toast.LENGTH_LONG).show();
                                return;
                            }
                        });
                    }

                    if (!version.equals(versionName)) {
                        System.out.println("Update gefunden.");
                        displayUpdates(version);
                    } else {
                        System.out.println("Kein Update gefunden.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public String getVersion() {
        // Document document = Jsoup.connect("https://dl.dropbox.com/s/pnw9slnpcut9o1g/version.txt?dl=0").get();
        // return document.text();
        try {
            Scanner scanner = new Scanner(new URL("https://dl.dropbox.com/s/pnw9slnpcut9o1g/version.txt?dl=0").openStream());

            return scanner.nextLine();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void cancel() {
        finish();
    }

    public void oflline() {
        info.setVisibility(View.VISIBLE);
        retry.setVisibility(View.VISIBLE);
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void download() {
        createProzessDialog();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    URL url = new URL("https://dl.dropbox.com/s/gwssfvzpbg3aldp/futorial.apk?dl=0");

                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("GET");
                    c.setDoOutput(true);
                    c.connect();

                    int lenghtOfFile = c.getContentLength();

                    String PATH = Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(PATH);
                    File output = new File(file, "update.apk");

                    System.out.println(output.getPath());
                    FileOutputStream fos = new FileOutputStream(output);

                    InputStream is = c.getInputStream();

                    byte[] buffer = new byte[1024];
                    int len1 = 0;

                    int total = 0;
                    while ((len1 = is.read(buffer)) != -1) {
                        total += len1;

                        progressStatus = (total * 100) / lenghtOfFile;

                        fos.write(buffer, 0, len1);
                    }
                    fos.close();
                    is.close();
                    System.out.println("Download complete!");
                    progressDialog.dismiss();

                    progressStatus = 111;
                    install(output);

                } catch (Exception e) {
                    Log.e("UpdateAPP", "Update error! " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private static ProgressDialog progressDialog;
    private static int progressStatus = 0;
    private static Handler handler = new Handler();

    private void createProzessDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog = new ProgressDialog(MainActivity.getMainactivity());

                TextView title = new TextView(MainActivity.getMainactivity());
                title.setTextSize(20);
                title.setText("Updatate verfügbar");
                title.setTextColor(MainActivity.getMainactivity().getResources().getColor(R.color.dark_font));
                title.setPadding(40, 30, 0, 00);

                progressDialog.setCustomTitle(title);

                //"Downloading Update ..."
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.setMax(100);
                progressDialog.show();
                progressDialog.getWindow().setBackgroundDrawableResource(R.color.colorPrimaryDark);
            }
        });

        new Thread(new Runnable() {
            public void run() {
                while (progressStatus <= 100) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                public void run() {
                                    handler.post(new Runnable() {
                                        public void run() {
                                            progressDialog.setProgress(progressStatus);
                                        }
                                    });
                                }
                            }, 10);
                        }
                    }).start();
                }
            }

        }).start();
    }

    private void install(final File f) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Build.VERSION.SDK_INT >= 23 ? FileProvider.getUriForFile(LoadingActivity.this, getPackageName() + ".provider", f) : Uri.fromFile(f), "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);// without this flag android returned a intent error!
        startActivity(intent);
    }
}
