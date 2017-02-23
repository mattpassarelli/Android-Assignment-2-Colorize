package com.example.solution_color;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.style.MetricAffectingSpan;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.library.bitmap_utilities.BitMap_Helpers;

import java.io.File;
import java.io.StringReader;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    private File picture;
    private String path;
    private int width, height;
    private ImageView background;
    private Bitmap imageBitmap;
    private String[] perms = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("");

        requestPermissions(perms, 200);
        DisplayMetrics display = this.getResources().getDisplayMetrics();
        width = display.widthPixels;
        height = display.heightPixels;

        background = (ImageView) findViewById(R.id.picture);
        background.setBackgroundResource(R.drawable.gutters);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.getBackground().setAlpha(80);
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
                Camera_Helpers.delSavedImage(path);
                //TODO: imageBitmap = (Bitmap) R.drawable.gutters;
                background = (ImageView) findViewById(R.id.picture);
                background.setImageResource(R.drawable.gutters);
                background.setScaleType(ImageView.ScaleType.FIT_CENTER);
                background.setScaleType(ImageView.ScaleType.FIT_XY);
                break;
            case R.id.action_B_W:
                getBWImage();
                break;

        }
        return true;
    }

    public void launchCamera(View view) {

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        picture = new File(external, "temp.jpg");
        Uri file = Uri.fromFile(picture);
        path = picture.getAbsolutePath();

        camera.putExtra(MediaStore.EXTRA_OUTPUT, file);
        startActivityForResult(camera, CONSTANTS.REQUEST_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONSTANTS.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            imageBitmap = BitmapFactory.decodeFile(path, options);

            Camera_Helpers.saveProcessedImage(imageBitmap, picture.getAbsolutePath());
            imageBitmap = Camera_Helpers.loadAndScaleImage(picture.getAbsolutePath(), height, width);
            background = (ImageView) findViewById(R.id.picture);
            background.setImageBitmap(imageBitmap);
        } else {
            Toast.makeText(this, "result code is " + resultCode + " request code is " + requestCode, Toast.LENGTH_LONG).show();
        }
    }

    //I personally need this (I think) because of Nougat's permissions settings
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        switch (permsRequestCode) {
            case 200:
                boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    public void getBWImage() {
        //SharedPreferences pref = getActivity().getPref

        //TODO: the 50 will be changed when I figure out how to get the int from preferences
        Bitmap BW = BitMap_Helpers.thresholdBmp(imageBitmap, 50);
        background = (ImageView) findViewById(R.id.picture);
        background.setImageBitmap(BW);
    }
}


