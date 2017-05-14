package br.com.zballos.examplephoto;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
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
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import br.com.zballos.examplephoto.adapters.MyImageAdapter;
import br.com.zballos.examplephoto.model.MyImage;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity
{
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView mRecyclerView;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int SELECT_PICTURE = 1;

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 1234;

    // directory name to store captured images
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
        toolbar.setTitle(R.string.pictures);
        setSupportActionBar(toolbar);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        FloatingActionButton fabCapturePhoto = (FloatingActionButton) findViewById(R.id.fabCapturePhoto);
        fabCapturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectMethodPickImage();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rvImages);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(llm);

        MyImage.checkInvalidAndDelete();

        Realm realm = Realm.getDefaultInstance();
        mList = realm.where(MyImage.class).findAll();
        Log.e("count2", mList.size() + "");
        //realm.close();

        MyImageAdapter adapter = new MyImageAdapter(this, mList);
        mRecyclerView.setAdapter(adapter);

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
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }
    }

    private void getImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
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
        } else if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //String[] projection = { MediaStore.Images.Media.DATA };
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor cursor = getContentResolver().query(data.getData(), projection, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
            String picturePath = cursor.getString(columnIndex); // returns null
            cursor.close();
            if (picturePath != null) {
                saveImage(picturePath);
            } else {
                Snackbar.make(coordinatorLayout, "Não foi possível salvar a imagem!", Snackbar.LENGTH_LONG)
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
        }  else {
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
        final MyImage image = new MyImage();
        image.setUUID(UUID.randomUUID().toString());
        image.setPathName(path);
        image.setSyncronized(false);
        Random random = new Random();
        int randomNumber = random.nextInt(500);
        image.setTitle("Image:: " + randomNumber);

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(image);
            }
        });
        realm.close();
    }

    private void selectMethodPickImage() {
        View inflater = getLayoutInflater().inflate(R.layout.dialog_type_pick_image, null);

        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(this, R.style.AppTheme_DialogPreview);
        AlertDialog.Builder builder =
                new AlertDialog.Builder(
                        contextWrapper
                );
        builder.setView(inflater);
        builder.setCancelable(false);

        ImageView ivGallery = (ImageView) inflater.findViewById(R.id.ivGallery);
        ImageView ivPick = (ImageView) inflater.findViewById(R.id.ivCamera);

        ivGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallery();
                dialog.dismiss();
            }
        });

        ivPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.setTitle("");
        dialog.show();
    }
}
