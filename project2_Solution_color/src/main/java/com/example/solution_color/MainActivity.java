package com.example.solution_color;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
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

import java.io.File;
import java.io.StringReader;

public class MainActivity extends AppCompatActivity {

    private ImageView camera;
    private File external;
    private File picture;
    Bitmap imageBitmap;
    private int width, height;
    private ImageView background;
    //private String[] perms = {"android.permission.CAMERA"};

    // @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // requestPermissions(perms, 200);
        DisplayMetrics display = this.getResources().getDisplayMetrics();
        width = display.widthPixels;
        height = display.heightPixels;

        camera = (ImageView) findViewById(R.id.camera);
        background = (ImageView) findViewById(R.id.picture);
        background.setBackgroundResource(R.drawable.gutters);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
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

        }
        return true;
    }

    public void launchCamera(View view) {

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        picture = new File(external, "temp.jpg");

        camera.putExtra(MediaStore.EXTRA_OUTPUT, picture);
        startActivityForResult(camera, CONSTANTS.REQUEST_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            imageBitmap = (Bitmap) bundle.get("data");
            Camera_Helpers.saveProcessedImage(imageBitmap, picture.getAbsolutePath());
            imageBitmap = Camera_Helpers.loadAndScaleImage(picture.getAbsolutePath(), height, width);
            background = (ImageView) findViewById(R.id.picture);
            background.setImageBitmap(imageBitmap);
        } else {

        }
    }


    /*public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        switch (permsRequestCode) {

            case 200:

                boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                break;

        }


    }*/

}


