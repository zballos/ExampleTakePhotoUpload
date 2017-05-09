package br.com.zballos.examplephoto;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.zballos.examplephoto.adapters.MyImageAdapter;
import br.com.zballos.examplephoto.model.MyImage;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity
{
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView mRecyclerView;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    // TODO: Capture video
    // private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    //public static final int MEDIA_TYPE_VIDEO = 2;

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 1234;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Photos";

    private Uri fileUri;
    private AlertDialog dialog;
    private List<MyImage> mList;

    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        FloatingActionButton fabCapturePhoto = (FloatingActionButton) findViewById(R.id.fabCapturePhoto);
        fabCapturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rvImages);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(llm);

        Realm realm = Realm.getDefaultInstance();
        mList = realm.where(MyImage.class).findAll();
        MyImageAdapter adapter = new MyImageAdapter(this, mList);
        mRecyclerView.setAdapter( adapter );

        checkPermissions();
    }

    private void checkPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                        dialog.dismiss();
                    }
                });
                builder.setMessage("É necessário ativar a sobreposição a outros apps.");
                dialog = builder.create();
                dialog.setTitle("Atenção");
                dialog.show();
            } else {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA)) {
                    }  else {
                        ActivityCompat.requestPermissions(this, permissions, 1);
                    }
                }
            }
        }
    }

    /**
     * Verify if device has camera hardware or not
     * */
    private boolean isDeviceSupportCamera() {
        return getApplicationContext()
                .getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * Capturing image of the camera app
     */
    private void captureImage() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // start the image capture Intent
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                afterCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Snackbar.make(coordinatorLayout, "Usuário cancelou a captura de imagem.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                // failed to capture image
                Snackbar.make(coordinatorLayout, "Desculpe! Falha ao capturar imagem.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        } else if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted...
                Snackbar.make(coordinatorLayout, "Permissão de sobreposição ainda está desativada!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private void afterCapturedImage() {
        try {
            saveImage(fileUri.getPath());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private File getOutputMediaFile(int type) {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } /*else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        }*/ else {
            return null;
        }

        return mediaFile;
    }

    /**
     * saveImage method
     *
     * Save image info to object MyImage. Only save path of the image.
     *
     * @param path of the image, for example: /storage/Picture/IMG_9988888.jpg
     */
    private void saveImage(String path) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        MyImage image = realm.createObject(MyImage.class);
        image.setPathName(path);
        image.setSyncronized(false);
        image.setTitle("Image ");

        realm.commitTransaction();
        realm.close();
    }
}
