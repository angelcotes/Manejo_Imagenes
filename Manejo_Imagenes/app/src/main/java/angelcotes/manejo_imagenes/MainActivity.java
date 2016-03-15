package angelcotes.manejo_imagenes;

import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import angelcotes.Libreria_Estilos.BitmapFilter;

public class MainActivity extends AppCompatActivity {


    private String APP_DIRECTORY = "MyPhoto/";
    private String MEDIA_DIRECTORY = APP_DIRECTORY + "media";
    private String NOMBRE_TEMPORAL_IMAGEN;

    private final int PHOTO_CODE = 100;
    private final int SELECT_PICTURE = 200;

    private Bitmap image;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.img_ver);
        Button btn = (Button) findViewById(R.id.btn_opciones);

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Tomar Foto", "Buscar en Galeria", "Cancelar"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("Elegir opcion");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int eleccion) {
                        if (options[eleccion] == "Tomar Foto") {
                            openCamera();
                        } else if (options[eleccion] == "Buscar en Galeria") {
                            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Seleccionar imagen"), SELECT_PICTURE);
                        } else {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Grayscale" , "Relief", "Oil Painting", "Neon", "Pixelate", "Old TV", "Invert Color", "Block", "Old Photo", "Sharpen", "Light"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("Estilos");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int eleccion) {
                        if (image != null) {
                            if (options[eleccion] == "Grayscale") {
                                Bitmap newBitmap = BitmapFilter.changeStyle(image, BitmapFilter.MOTION_BLUR_STYLE);
                                imageView.setImageBitmap(newBitmap);
                            }
                        } else {
                            //mensaje de error por estar vacio el imageView
                        }
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case PHOTO_CODE:
                if (resultCode == RESULT_OK){
                    String dir = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY + File.separator + NOMBRE_TEMPORAL_IMAGEN;

                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(dir);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    this.sendBroadcast(mediaScanIntent);
                    decodeBitmap(dir);
                }
                break;
            case SELECT_PICTURE:
                if (resultCode == RESULT_OK){
                    Uri path = data.getData();
                    imageView.setImageURI(path);
                }
                break;
        }
    }

    private void decodeBitmap(String dir) {
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(dir);
        imageView.setImageBitmap(bitmap);
        image = bitmap;
    }

    private void openCamera() {


        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Img" + timeStamp;

        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        file.mkdirs();
        NOMBRE_TEMPORAL_IMAGEN = imageFileName + ".jpg";
        String path = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY + File.separator + NOMBRE_TEMPORAL_IMAGEN;
        File newFile = new File(path);
        Log.d("tag", path);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
        startActivityForResult(intent, PHOTO_CODE);
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }
}
