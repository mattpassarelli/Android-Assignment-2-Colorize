package com.example.solution_color;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.library.bitmap_utilities.BitMap_Helpers;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private File unedited;
    private String path = "/storage/emulated/0/Pictures/temp.jpg";
    private Uri file;
    private int width, height;
    private ImageView background;
    private Bitmap imageBitmap;
    private SharedPreferences prefs;
    private boolean cameraAccepted, readWriteAccepted;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("");

        requestPermissions(CONSTANTS.PERMISSIONS, CONSTANTS.PERMISSIONS_INT);
        DisplayMetrics display = this.getResources().getDisplayMetrics();
        width = display.widthPixels;
        height = display.heightPixels;


        background = (ImageView) findViewById(R.id.picture);

        try {
            imageBitmap = Camera_Helpers.loadAndScaleImage(path, height, width);
            if (imageBitmap != null) {
                background.setImageBitmap(imageBitmap);
            } else {
                background.setBackgroundResource(R.drawable.gutters);
                imageBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.gutters);
            }

        } catch (Exception e) {
            // ¯\_(ツ)_/¯
            // eh
        }


        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.getBackground().setAlpha(CONSTANTS.APP_BAR_ALPHA);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                break;
            case R.id.action_undo:
                undo();
                break;
            case R.id.action_B_W:
                getBWImage();
                break;
            case R.id.action_colorize:
                getColorImage();
                break;
            case R.id.action_share:
                shareImage();
                break;
        }
        return true;
    }

    private void shareImage() {
        File sent = new File(path);
        Uri uri = Uri.fromFile(sent);

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("image/plain");
        String body = prefs.getString("edit_text_preference_2", getString(R.string.DEFAULT_BODY));
        String subject = prefs.getString("edit_text_preference_1", getString(R.string.DEFAULT_SUBJECT));
        send.putExtra(Intent.EXTRA_SUBJECT, subject);
        send.putExtra(Intent.EXTRA_TEXT, body);
        send.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(send, getString(R.string.Share_Via)));

    }

    private void undo() {
        Camera_Helpers.delSavedImage(path);
        background = (ImageView) findViewById(R.id.picture);
        background.setImageResource(R.drawable.gutters);
        background.setScaleType(ImageView.ScaleType.FIT_CENTER);
        background.setScaleType(ImageView.ScaleType.FIT_XY);
        imageBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.gutters);
    }

    public void launchCamera(View view) {

        if (Build.VERSION.SDK_INT >= 23 && (!cameraAccepted || !readWriteAccepted)) {
            Toast.makeText(this, R.string.Permissions_Denied, Toast.LENGTH_LONG).show();
        } else {

            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            unedited = new File(external, "temp.jpg");
            file = Uri.fromFile(unedited);
            path = unedited.getAbsolutePath();

            camera.putExtra(MediaStore.EXTRA_OUTPUT, file);
            startActivityForResult(camera, CONSTANTS.REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONSTANTS.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            imageBitmap = BitmapFactory.decodeFile(path, options);

            Camera_Helpers.saveProcessedImage(imageBitmap, unedited.getAbsolutePath());

            //Toast.makeText(this, "File made" + file.toString(), Toast.LENGTH_LONG).show();

            imageBitmap = Camera_Helpers.loadAndScaleImage(unedited.getAbsolutePath(), height, width);
            background = (ImageView) findViewById(R.id.picture);
            background.setImageBitmap(imageBitmap);
        } else {
            Toast thisIsLiterallySoThisIsNotBlank = new Toast(this);
            thisIsLiterallySoThisIsNotBlank.cancel();
        }
    }


    public void getBWImage() {

        int sketch = prefs.getInt("sketch", CONSTANTS.DEFAULT_SKETCHINESS);

        //Toast.makeText(this, "" + sketch, Toast.LENGTH_LONG).show();

        Bitmap BW = BitMap_Helpers.thresholdBmp(imageBitmap, sketch);
        background = (ImageView) findViewById(R.id.picture);
        background.setImageBitmap(BW);
        
        File temp = new File(path);
        file = Uri.fromFile(temp);
        Camera_Helpers.saveProcessedImage(BW, path);

        //Toast.makeText(this, "File made " + file.toString(), Toast.LENGTH_LONG).show();
    }

    public void getColorImage() {
        //By redoing the Black and White sketch, it means I don't have to worry about checking if the user
        //pressed sketch to begin with because people can be idiots and rush

        int sketch = prefs.getInt("sketch", CONSTANTS.DEFAULT_SKETCHINESS);
        int sat = prefs.getInt("saturation", CONSTANTS.DEFAULT_SATURATION);

        //take the int sat from settings and convert it to a float for the colorBmp method
        float saturation = ((float) sat / 100f) * 255f;

        //Toast.makeText(this, "Sketch " + sketch + " Saturation " +  saturation, Toast.LENGTH_LONG).show();

        Bitmap BW = BitMap_Helpers.thresholdBmp(imageBitmap, sketch);
        Bitmap CLR = BitMap_Helpers.colorBmp(imageBitmap, saturation);

        background = (ImageView) findViewById(R.id.picture);

        BitMap_Helpers.merge(CLR, BW);

        background.setImageBitmap(CLR);

        File temp = new File(path);
        file = Uri.fromFile(temp);
        Camera_Helpers.saveProcessedImage(CLR, path);
    }

    //I personally need this (I think) because of Nougat's permissions settings
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (permsRequestCode) {
            case CONSTANTS.PERMISSIONS_INT:
                cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                readWriteAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                break;
        }
    }
}


