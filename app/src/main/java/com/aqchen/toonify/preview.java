package com.aqchen.toonify;

import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import androidx.annotation.ColorInt;
import androidx.constraintlayout.solver.widgets.Rectangle;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.opencv.dnn.Model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class preview extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int PERMISSIONS_REQUEST_READSTORAGE = 2;
    private static final int PERMISSIONS_REQUEST_WRITESTORAGE = 3;
    private static final int LOAD_IMAGE_RESULTS = 4;
    private static final int LOAD_NEW_PREFERENCES = 13;
    private static final int RESULT_LOAD_IMG = 12;
    private ImageView imagePreview;

    boolean saveOrig;
    Bitmap finalBitmapImage;
    Bitmap origBitmapImage;
    String currentPhotoPath;
    String origPhotoPath = "";
    String imageSource;
    boolean darkMode = false;

    boolean photoTaken;
    String manipulationType;
    Uri imgUri;

    byte[] origBytes;

    Mat imgMat;
    Mat bilateralMat;
    Mat tempMat1;
    Mat tempMat2;
    Mat grayMat;
    Mat medianMat;
    Mat edgeMat;

    int seekBarVal1;
    int seekBarVal2;
    int seekBarVal3;
    int seekBarVal4;
    int seekBarVal5;
    int seekBarVal6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();

        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.AppDarkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        if (savedInstanceState != null) {
            byte[] bytes = savedInstanceState.getByteArray("imageBytes");
            //byte[] bytes = data.getByteArrayExtra("BMP");
            //if(bytes != null) {
                //finalBitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            try {
                finalBitmapImage = BitmapFactory.decodeStream(preview.this.openFileInput("finalBMI"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //origBytes = savedInstanceState.getByteArray("origImageBytes");
            //origBitmapImage = BitmapFactory.decodeByteArray(origBytes, 0, origBytes.length);

            try {
                origBitmapImage = BitmapFactory.decodeStream(preview.this.openFileInput("origBMI"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //} else {
            //    SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
            //    SharedPreferences.Editor editor = sharedPref.edit();
            //    editor.putBoolean("photoTaken", false);
            //    editor.commit();
            //}
        } else {
            SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("photoTaken", false);
            editor.commit();
        }

        //imgMat = new Mat(finalBitmapImage.getHeight(), finalBitmapImage.getWidth(), CvType.CV_8UC3);

        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        photoTaken = prefs.getBoolean("photoTaken", false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        setTitle("Preview");
        //setTitle(String.valueOf(OpenCVLoader.initDebug()));

        Toolbar toolbar = findViewById(R.id.toolbarPreview);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initSpeedDial();

        // or other values
        if(bundle != null) {
            manipulationType = bundle.getString("manipulationType");
            saveOrig = bundle.getBoolean("saveOrig");
            imageSource = bundle.getString("imageSource", "camera");
        } else {
            exit();
            return;
        }
        /*
        if(photoTaken && finalBitmapImage != null) {
            updatePreview(finalBitmapImage);
        }
        */
        //delayRecreate();

        if(imageSource.equals("camera") && !photoTaken) {
            //photoTaken = true;
            //SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
            //SharedPreferences.Editor editor = sharedPref.edit();
            //editor.putBoolean("photoTaken", photoTaken);
            //editor.commit();
            dispatchTakePictureIntent();
        }
        else if(imageSource.equals("camera")){
            updatePreview(finalBitmapImage);
        } else if(imageSource.equals("gallery") && !photoTaken) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
        } else {
            updatePreview(finalBitmapImage);
        }


        View progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        photoTaken = true;
        SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("photoTaken", photoTaken);
        editor.commit();

    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(finalBitmapImage != null && photoTaken) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            finalBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();
            //outState.putByteArray("imageBytes", bytes);
            try {
                FileOutputStream fo = openFileOutput("finalBMI", Context.MODE_PRIVATE);
                fo.write(bytes);
                fo.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(origBitmapImage != null && photoTaken) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            origBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytes = stream.toByteArray();
            //outState.putByteArray("origImageBytes", bytes);
            try {
                FileOutputStream fo = openFileOutput("origBMI", Context.MODE_PRIVATE);
                fo.write(bytes);
                fo.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //outState.putString("imagePath",currentPhotoPath);
        //outState.putParcelable("imageBitmap", finalBitmapImage);
    }

    private void initSpeedDial() {

        SpeedDialView speedDialView = findViewById(R.id.speedDial);
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_save_action, R.drawable.ic_save_white_24dp)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, getTheme()))
                        .setLabel(getString(R.string.txtSave))
                        .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.colorTextDark, getTheme()))
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, getTheme()))
                        .setLabelClickable(false)
                        .create()
        );

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_edit_action, R.drawable.ic_edit_white_24dp)
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorAccent, getTheme()))
                        .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, getTheme()))
                        .setLabel(getString(R.string.txtEdit))
                        .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.colorTextDark, getTheme()))
                        .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, getTheme()))
                        .setLabelClickable(false)
                        .create()
        );

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem speedDialActionItem) {
                switch (speedDialActionItem.getId()) {
                    case R.id.fab_save_action:
                        try {
                            saveImage(finalBitmapImage);
                            Toast.makeText(preview.this, "Image saved to gallery!", Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            Toast.makeText(preview.this, "Failed to save image", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        return false;
                    case  R.id.fab_edit_action:
                        if(manipulationType.substring(0, 2).equals("nn")) {
                            Toast.makeText(preview.this, "This filter has no settings", Toast.LENGTH_LONG).show();
                            return false;
                        }

                        SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("applied", false);
                        editor.commit();
                        Intent intent = new Intent(preview.this, preferences.class);
                        Bundle bundle = new Bundle();
                        if(seekBarVal1 == 0) {
                            switch (manipulationType) {
                                case "cartoon":
                                    seekBarVal1 = 7;
                                    seekBarVal2 = 9;
                                    seekBarVal3 = 9;
                                    seekBarVal4 = 7;
                                    seekBarVal5 = 7;
                                    seekBarVal6 = 9;
                                    break;

                                case "pencilColor":
                                    seekBarVal1 = 60;
                                    seekBarVal2 = 7;
                                    seekBarVal3 = 20;
                                    break;
                                case "pencilBW":
                                    seekBarVal1 = 25;
                                    seekBarVal2 = 25;
                                    seekBarVal3 = 20;
                                    break;

                                case "watercolor":
                                    seekBarVal1 = 60;
                                    seekBarVal2 = 25;
                                    break;

                                default:
                                    break;
                            }
                        }
                        bundle.putInt("seekBarVal1", seekBarVal1);
                        bundle.putInt("seekBarVal2", seekBarVal2);
                        bundle.putInt("seekBarVal3", seekBarVal3);
                        bundle.putInt("seekBarVal4", seekBarVal4);
                        bundle.putInt("seekBarVal5", seekBarVal5);
                        bundle.putInt("seekBarVal6", seekBarVal6);
                        bundle.putString("manipulationType", manipulationType);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, LOAD_NEW_PREFERENCES);
                        return false;
                    default:
                        return false;
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.preview_menu, menu);
        MenuItem mItem = menu.getItem(0);
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            //setTheme(R.style.AppDarkTheme);
            mItem.setChecked(true);
        } else {
            //setTheme(R.style.AppTheme);
            mItem.setChecked(false);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuDarkMode:
                if (item.isChecked()) {
                    item.setChecked(false);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("nightMode", false);
                    editor.commit();
                    recreate();
                }
                else {
                    item.setChecked(true);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("nightMode", true);
                    editor.commit();
                    recreate();
                }
                return true;
            case android.R.id.home:
                exit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void exit() {
        SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("photoTaken", false);
        editor.commit();
        finish();
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void saveImage(Bitmap finalBitmap) throws IOException {
        OutputStream fos;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName + ".jpg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
        } else {
            File image = createImageFile();
            fos = new FileOutputStream(image);
        }
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        Objects.requireNonNull(fos).close();
    }


    private void dispatchTakePictureIntent() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID + ".provider",
                                    photoFile));
                startActivityForResult(takePictureIntent, PERMISSIONS_REQUEST_CAMERA);
            } else {
                Toast.makeText(this, "Failed to open new picture", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 3000;
        int MAX_WIDTH = 3000;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSIONS_REQUEST_CAMERA && resultCode == RESULT_OK && resultCode != RESULT_CANCELED) {
            //Intent mediaScanIntent;
            File f = new File(currentPhotoPath);
            Uri contentUri = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider",
                            f);
            photoTaken = true;
            if (saveOrig) {
                File origPhotoFile = new File(currentPhotoPath);
                Uri origUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                                origPhotoFile);
                try {
                    Bitmap imageBitmap = handleSamplingAndRotationBitmap(preview.this, origUri);
                    saveImage(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Bitmap imageBitmap = handleSamplingAndRotationBitmap(preview.this, contentUri);
                updatePreview(imageBitmap);
                transformChooser();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (manipulationType.equals("cartoon")) {
                seekBarVal1 = 7;
                seekBarVal2 = 9;
                seekBarVal3 = 9;
                seekBarVal4 = 7;
                seekBarVal5 = 7;
                seekBarVal6 = 9;
            } else if (manipulationType.equals("pencilColor")) {
                seekBarVal1 = 60;
                seekBarVal2 = 7;
                seekBarVal3 = 20;
            } else if (manipulationType.equals("pencilBW")) {
                seekBarVal1 = 20;
                seekBarVal2 = 15;
                seekBarVal3 = 20;
            } else if (manipulationType.equals("watercolor")) {
                seekBarVal1 = 60;
                seekBarVal2 = 25;
            }
        } else if (requestCode == PERMISSIONS_REQUEST_CAMERA) { // Result was a failure
            Toast.makeText(this, "No picture taken", Toast.LENGTH_SHORT).show();
            exit();
            return;
        } else if (requestCode == LOAD_IMAGE_RESULTS && resultCode == RESULT_OK && data != null) {
            // Read picked image data - its URI
            Uri pickedImage = data.getData();
            // Read picked image path using content resolver
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

            cursor.close();
        } else if (requestCode == LOAD_NEW_PREFERENCES) {
            SharedPreferences sharedPref = getSharedPreferences("settings", Context.MODE_PRIVATE);
            boolean applied = sharedPref.getBoolean("applied", false);
            if (!applied) {
                Toast.makeText(this, "Changes not applied", Toast.LENGTH_SHORT).show();
            } else if (manipulationType.equals("cartoon")) {
                int numBilateral = sharedPref.getInt("seekBar1", 7);
                int bDiameter = sharedPref.getInt("seekBar2", 9);
                int sigmaColor = sharedPref.getInt("seekBar3", 9);
                int sigmaSpace = sharedPref.getInt("seekBar4", 7);
                int mDiameter = sharedPref.getInt("seekBar5", 7);
                int eDiameter = sharedPref.getInt("seekBar6", 9);
                System.gc();

                seekBarVal1 = numBilateral;
                seekBarVal2 = bDiameter;
                seekBarVal3 = sigmaColor;
                seekBarVal4 = sigmaSpace;
                seekBarVal5 = mDiameter;
                seekBarVal6 = eDiameter;
                Bitmap tempBitmap;
                if (origBitmapImage != null && photoTaken && origBytes == null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    origBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    origBytes = stream.toByteArray();
                }
                tempBitmap = BitmapFactory.decodeByteArray(origBytes, 0, origBytes.length);
                cartoonTransform(tempBitmap, numBilateral, bDiameter, sigmaColor, sigmaSpace, mDiameter, eDiameter);
                tempBitmap.recycle();
                Toast.makeText(this, "Changes applied", Toast.LENGTH_SHORT).show();
            } else if (manipulationType.equals("pencilColor")) {
                int sigmaSpace = sharedPref.getInt("seekBar1", 60);
                int sigmaColor = sharedPref.getInt("seekBar2", 7);
                int shadeFactor = sharedPref.getInt("seekBar3", 20);
                System.gc();

                seekBarVal1 = sigmaSpace;
                seekBarVal2 = sigmaColor;
                seekBarVal3 = shadeFactor;
                Bitmap tempBitmap;
                if (origBitmapImage != null && photoTaken && origBytes == null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    origBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    origBytes = stream.toByteArray();
                }
                tempBitmap = BitmapFactory.decodeByteArray(origBytes, 0, origBytes.length);
                pencilColorTransform(tempBitmap, sigmaSpace, sigmaColor / (float)100.0, shadeFactor / (float)1000.0);
                tempBitmap.recycle();
                Toast.makeText(this, "Changes applied", Toast.LENGTH_SHORT).show();
            } else if (manipulationType.equals("pencilBW")) {
                int sigmaSpace = sharedPref.getInt("seekBar1", 20);
                int sigmaColor = sharedPref.getInt("seekBar2", 15);
                int shadeFactor = sharedPref.getInt("seekBar3", 20);
                System.gc();

                seekBarVal1 = sigmaSpace;
                seekBarVal2 = sigmaColor;
                seekBarVal3 = shadeFactor;
                Bitmap tempBitmap;
                if (origBitmapImage != null && photoTaken && origBytes == null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    origBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    origBytes = stream.toByteArray();
                }
                tempBitmap = BitmapFactory.decodeByteArray(origBytes, 0, origBytes.length);
                pencilBWTransform(tempBitmap, sigmaSpace, sigmaColor / (float)100.0, shadeFactor / (float)1000.0);
                tempBitmap.recycle();
                Toast.makeText(this, "Changes applied", Toast.LENGTH_SHORT).show();
            } else if (manipulationType.equals("watercolor")) {
                int sigmaSpace = sharedPref.getInt("seekBar1", 60);
                int sigmaColor = sharedPref.getInt("seekBar2", 25);
                System.gc();

                seekBarVal1 = sigmaSpace;
                seekBarVal2 = sigmaColor;
                Bitmap tempBitmap;
                if (origBitmapImage != null && photoTaken && origBytes == null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    origBitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    origBytes = stream.toByteArray();
                }
                tempBitmap = BitmapFactory.decodeByteArray(origBytes, 0, origBytes.length);
                watercolorTransform(tempBitmap, sigmaSpace, sigmaColor / (float)100.0);
                tempBitmap.recycle();
                Toast.makeText(this, "Changes applied", Toast.LENGTH_SHORT).show();
            } else if (manipulationType.substring(0, 2).equals("nn")) {
                Bitmap tempBitmap = BitmapFactory.decodeByteArray(origBytes, 0, origBytes.length);
                neuralnetworkTransform(tempBitmap, manipulationType);
                tempBitmap.recycle();
                Toast.makeText(this, "Changes applied", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    origBitmapImage = BitmapFactory.decodeStream(imageStream);
                    try {
                        currentPhotoPath = getPath(this.getApplicationContext(), imageUri);
                        origBitmapImage = handleSamplingAndRotationBitmap(preview.this, imageUri);
                        //Bitmap imageBitmap= MediaStore.Images.Media.getBitmap(getContentResolver(), contentUri);
                        updatePreview(origBitmapImage);
                        transformChooser();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    photoTaken = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to find file", Toast.LENGTH_LONG).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void updatePreview(Bitmap imageBitmap) {
        //int newHeight = (int) ( imageBitmap.getHeight() * (800.0 / imageBitmap.getWidth()) );
        //Bitmap scaled = Bitmap.createScaledBitmap(imageBitmap, 600, newHeight, true);
        imagePreview = findViewById(R.id.imgPreview);
        imagePreview.setImageBitmap(imageBitmap);
    }

    private void transformChooser() {
        //Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        MediaScannerConnection.scanFile(this,
                new String[]{f.toString()},
                new String[]{f.getName()},null);
        //mediaScanIntent.setData(contentUri);
        Bitmap imageBitmap = null;
        try {
            imageBitmap = handleSamplingAndRotationBitmap(preview.this, contentUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(manipulationType.equals("cartoon") && imageBitmap != null) {
            origBitmapImage = imageBitmap.copy(imageBitmap.getConfig(), true);

            cartoonTransform(imageBitmap, 7, 9, 9, 7, 7, 9);
            imageBitmap.recycle();
        } else if (manipulationType.equals("pencilColor") && imageBitmap != null) {
            origBitmapImage = imageBitmap.copy(imageBitmap.getConfig(), true);

            pencilColorTransform(imageBitmap, 60, (float)0.07, (float)0.02);
            imageBitmap.recycle();
        } else if (manipulationType.equals("pencilBW") && imageBitmap != null) {
            origBitmapImage = imageBitmap.copy(imageBitmap.getConfig(), true);

            pencilBWTransform(imageBitmap, 20, (float)0.15, (float)0.02);
            imageBitmap.recycle();
        } else if (manipulationType.equals("watercolor") && imageBitmap != null) {
            origBitmapImage = imageBitmap.copy(imageBitmap.getConfig(), true);

            watercolorTransform(imageBitmap, (float)60, (float)0.45);
            imageBitmap.recycle();
        } else if (manipulationType.substring(0, 2).equals("nn") && imageBitmap != null) {
            origBitmapImage = imageBitmap.copy(imageBitmap.getConfig(), true);

            neuralnetworkTransform(imageBitmap, manipulationType);
            imageBitmap.recycle();
        }
        View progressBar = findViewById(R.id.progressBar);
        if(progressBar != null)
            progressBar.setVisibility(View.INVISIBLE);
    }

    private void cartoonTransform(Bitmap imageBitmap, int numBilateral, int bDiameter, double sigmaColor, double sigmaSpace, int mDiameter, int eDiameter) {
        imgMat = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);
        tempMat1 = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);
        tempMat2 = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);

        Utils.bitmapToMat(imageBitmap, imgMat);

        imgMat.copyTo(tempMat1);
        imgMat.copyTo(tempMat2);
        Imgproc.cvtColor(tempMat1, tempMat1, Imgproc.COLOR_BGRA2RGB); //tempMat1 RGB
        Imgproc.cvtColor(tempMat2, tempMat2, Imgproc.COLOR_BGRA2RGB); //tempMat2 RGB
        for(int i=0; i < 2; i++) {
            Imgproc.pyrDown(tempMat1, tempMat1);
        }
        for(int i=0; i < numBilateral; i++) {
            Imgproc.bilateralFilter(tempMat1, tempMat2, bDiameter, sigmaColor, sigmaSpace);
            System.gc();
            tempMat2.copyTo(tempMat1);
        }
        for(int i=0; i < 2; i++) {
            Imgproc.pyrUp(tempMat1, tempMat1);
        }
        Imgproc.resize(tempMat1, tempMat1, imgMat.size());
        Imgproc.cvtColor(tempMat2, tempMat2, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(imgMat, tempMat2, Imgproc.COLOR_RGB2GRAY); //tempMat2 Gray
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.medianBlur(tempMat2, imgMat, mDiameter); //

        Imgproc.adaptiveThreshold(imgMat, tempMat2, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, eDiameter, 2);
        Imgproc.cvtColor(tempMat2, tempMat2, Imgproc.COLOR_GRAY2RGB);
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_GRAY2RGB);
        Core.bitwise_and(tempMat1, tempMat2, imgMat);
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2BGRA);
        Utils.matToBitmap(imgMat, imageBitmap);
        finalBitmapImage = imageBitmap.copy(imageBitmap.getConfig(), true);
        updatePreview(finalBitmapImage);
        imgMat.release();
        tempMat1.release();
        tempMat2.release();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void pencilColorTransform(Bitmap imageBitmap, float sigmaSpace, float sigmaColor, float shadeFactor) {
        imgMat = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);
        tempMat1 = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC1);
        tempMat2 = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);

        Utils.bitmapToMat(imageBitmap, imgMat);

        Size origSize = imgMat.size();

        // MUST CONVERT INPUT TO RGB TO FIX DIMENSION/OVERLAP ISSUES
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGRA2RGB); //imgMat RGB

        for(int i=0; i < 1; i++) {
            Imgproc.pyrDown(imgMat, imgMat);
        }
        Photo.pencilSketch(imgMat, tempMat1, tempMat2, sigmaSpace, sigmaColor, shadeFactor);
        for(int i=0; i < 1; i++) {
            Imgproc.pyrUp(tempMat2, tempMat2);
        }
        Imgproc.resize(tempMat2, tempMat2, origSize);
        Imgproc.cvtColor(tempMat2, tempMat2, Imgproc.COLOR_RGB2BGRA);
        Utils.matToBitmap(tempMat2, imageBitmap);
        finalBitmapImage = imageBitmap.copy(imageBitmap.getConfig(), true);
        updatePreview(finalBitmapImage);

        imgMat.release();
        tempMat1.release();
        tempMat2.release();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void pencilBWTransform(Bitmap imageBitmap, float sigmaSpace, float sigmaColor, float shadeFactor) {
        imgMat = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);
        tempMat1 = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC1);
        tempMat2 = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);

        Utils.bitmapToMat(imageBitmap, imgMat);

        Size origSize = imgMat.size();

        // MUST CONVERT INPUT TO RGB TO FIX DIMENSION/OVERLAP ISSUES
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGRA2RGB); //imgMat RGB

        for(int i=0; i < 1; i++) {
            Imgproc.pyrDown(imgMat, imgMat);
        }
        Photo.pencilSketch(imgMat, tempMat1, tempMat2, sigmaSpace, sigmaColor, shadeFactor);
        for(int i=0; i < 1; i++) {
            Imgproc.pyrUp(tempMat1, tempMat1);
        }
        Imgproc.resize(tempMat1, tempMat1, origSize);
        Imgproc.cvtColor(tempMat1, tempMat1, Imgproc.COLOR_GRAY2BGRA);
        Utils.matToBitmap(tempMat1, imageBitmap);
        finalBitmapImage = imageBitmap.copy(imageBitmap.getConfig(), true);
        updatePreview(finalBitmapImage);

        imgMat.release();
        tempMat1.release();
        tempMat2.release();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void watercolorTransform(Bitmap imageBitmap, float sigmaSpace, float sigmaColor) {
        imgMat = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);

        Utils.bitmapToMat(imageBitmap, imgMat);

        Size origSize = imgMat.size();

        // MUST CONVERT INPUT TO RGB TO FIX DIMENSION/OVERLAP ISSUES
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGRA2RGB); //imgMat RGB

        for(int i=0; i < 1; i++) {
            Imgproc.pyrDown(imgMat, imgMat);
        }
        Photo.stylization(imgMat, imgMat, sigmaSpace, sigmaColor);
        for(int i=0; i < 1; i++) {
            Imgproc.pyrUp(imgMat, imgMat);
        }
        Imgproc.resize(imgMat, imgMat, origSize);
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2BGRA);
        Utils.matToBitmap(imgMat, imageBitmap);
        finalBitmapImage = imageBitmap.copy(imageBitmap.getConfig(), true);
        updatePreview(finalBitmapImage);

        imgMat.release();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    private void neuralnetworkTransform(Bitmap imageBitmap, String manipulationType) {
        imgMat = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);
        Utils.bitmapToMat(imageBitmap, imgMat);

        Size origSize = new Size(imgMat.width(), imgMat.height());

        // MUST CONVERT INPUT TO RGB TO FIX DIMENSION/OVERLAP ISSUES
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_BGRA2RGB); //imgMat RGB

        Size newSize = new Size(800.0, origSize.height/(origSize.width / 800.0));
        Imgproc.resize(imgMat, imgMat, newSize, 0, 0, Imgproc.INTER_AREA);

        String modelName = "starry_night.t7";
        switch(manipulationType) {
            case "nnSN":
                modelName = "starry_night.t7";
                break;

            case "nnMosaic":
                modelName = "mosaic.t7";
                break;

            case "nnUdnie":
                modelName = "udnie.t7";
                break;

            case "nnTheScream":
                modelName = "the_scream.t7";
                break;

            case "nnCandy":
                modelName = "candy.t7";
                break;

            case "nnFeathers":
                modelName = "feathers.t7";
                break;

            default:
                modelName = "starry_night.t7";
                break;
        }

        String modelPath = "";
        try {
            modelPath = fetchModelFile(this, modelName);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to apply filter", Toast.LENGTH_SHORT).show();
            exit();
            return;
        }

        Mat blob = Dnn.blobFromImage(imgMat, 1.0, newSize, new Scalar(103.939, 116.779, 123.680), false, false);

        // Free imgMat
        imgMat.release();

        Net net = Dnn.readNetFromTorch(modelPath);
        net.setInput(blob);

        Mat out = net.forward();

        // Free blob
        blob.release();

        // Split into layers
        Mat layerR = out.submat(0, 1, 0, 1);
        Mat layerG = out.submat(0, 1, 1, 2);
        Mat layerB = out.submat(0, 1, 2, 3);
        // Reshape from 4D tensor to 2D matrix
        layerR = layerR.reshape(1, out.size(2));
        layerG = layerG.reshape(1, out.size(2));
        layerB = layerB.reshape(1, out.size(2));

        // Add back in mean
        Core.add(layerR, new Scalar(103.939), layerR);
        Core.add(layerG, new Scalar(116.779), layerG);
        Core.add(layerB, new Scalar(123.680), layerB);

        Mat finalMat = new Mat(out.size(2), out.size(3), CvType.CV_8UC3);

        for(int i = 0; i < out.size(2); i++) {
            for(int j = 0; j < out.size(3); j++) {
                byte[] data  = new byte[3];
                double r = layerR.get(i, j)[0];
                double g = layerG.get(i, j)[0];
                double b = layerB.get(i, j)[0];
                // Clamp r,g,b values
                if(r > 255) {
                    r = 255.0;
                } else if (r < 0) {
                    r = 0;
                }
                if(g > 255) {
                    g = 255.0;
                } else if (g < 0) {
                    g = 0;
                }
                if(b > 255) {
                    b = 255.0;
                } else if (b < 0) {
                    b = 0;
                }
                data[0] = (byte)r;
                data[1] = (byte)g;
                data[2] = (byte)b;
                finalMat.put(i, j, data);
            }
        }
        // Free out
        out.release();

        Imgproc.resize(finalMat, finalMat, new Size(origSize.width, origSize.height), 0, 0, Imgproc.INTER_CUBIC);

        Imgproc.cvtColor(finalMat, finalMat, Imgproc.COLOR_RGB2BGRA);


        Utils.matToBitmap(finalMat, imageBitmap);
        finalBitmapImage = imageBitmap.copy(imageBitmap.getConfig(), true);
        updatePreview(finalBitmapImage);

        // Free finalMat
        finalMat.release();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public static String getPath( Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }

    public static String fetchModelFile(Context context, String modelName) throws IOException {
        File file = new File(context.getFilesDir(), modelName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(modelName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }
}
