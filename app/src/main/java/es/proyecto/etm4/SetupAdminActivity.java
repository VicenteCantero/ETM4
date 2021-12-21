package es.proyecto.etm4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupAdminActivity extends AppCompatActivity {

    private EditText nombre, direccion, telefono, ciudad;
    private Button guardar;
    private String phone= " ";
    private CircleImageView imagen;
    private FirebaseAuth auth;
    private DatabaseReference UserRef;
    private ProgressDialog dialog;
    private String CurrentUserId;
    private static int Galery_Pick=1;
    private StorageReference UserImagenPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_admin);

        auth= FirebaseAuth.getInstance();
        CurrentUserId= auth.getCurrentUser().getUid();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Admin");
        dialog= new ProgressDialog(this);
        UserImagenPerfil= FirebaseStorage.getInstance().getReference().child("Perfil");
        nombre= (EditText) findViewById(R.id.asetup_nombre);
        direccion= (EditText) findViewById(R.id.asetup_direccion);
        telefono= (EditText) findViewById(R.id.asetup_telefono);
        ciudad= (EditText) findViewById(R.id.asetup_ciudad);
        guardar= (Button) findViewById(R.id.asetup_boton);
        imagen= (CircleImageView)findViewById(R.id.asetup_imagen);

        Bundle bundle= getIntent().getExtras();
        if(bundle!=null){
            phone= bundle.getString("phone");
        }

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GuardarInformacion();
            }
        });

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/");
                startActivityForResult(intent,
                        Galery_Pick);
            }
        });

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                    if(snapshot.hasChild("imagen")){
                        String imagestr= snapshot.child("imagen").getValue().toString();
                        Picasso.get().load(imagestr).placeholder(R.drawable.check).into(imagen);

                    }else{
                        Toast.makeText(SetupAdminActivity.this, "Seleccione una imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Galery_Pick && resultCode== RESULT_OK && data != null){
            Uri imagenUri= data.getData();

            CropImage.activity(imagenUri).setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1).start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result= CropImage.getActivityResult(data);
            if(resultCode== RESULT_OK){
                dialog.setTitle("Imagen de perfil");
                dialog.setMessage("Espere...");
                dialog.show();
                dialog.setCanceledOnTouchOutside(true);

                final Uri resultUri= result.getUri();
                StorageReference filePath= UserImagenPerfil.child(CurrentUserId+".jpg");
                final File url= new File(resultUri.getPath());
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            UserImagenPerfil.child(CurrentUserId+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri =uri.toString();
                                    UserRef.child(CurrentUserId).child("imagen").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Picasso.get().load(downloadUri).into(imagen);
                                                dialog.dismiss();
                                            }else{
                                                String mensaje= task.getException().getMessage();
                                                Toast.makeText(SetupAdminActivity.this, "Error: "+mensaje, Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }else{
                Toast.makeText(SetupAdminActivity.this, "Imagen no soportada", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }
    }

    private void GuardarInformacion() {
        String nombres= nombre.getText().toString().toUpperCase();
        String direcciones= direccion.getText().toString().toUpperCase();
        String telefonos= telefono.getText().toString();
        String ciudades= ciudad.getText().toString().toUpperCase();

        if(TextUtils.isEmpty(nombres)){
            Toast.makeText(SetupAdminActivity.this, "Ingrese el nombre", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(direcciones)){
            Toast.makeText(SetupAdminActivity.this, "Ingrese la dirección", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(ciudades)){
            Toast.makeText(SetupAdminActivity.this, "Ingrese su ciudad", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(telefonos)){
            Toast.makeText(SetupAdminActivity.this, "Ingrese el teléfono", Toast.LENGTH_SHORT).show();
        }else{
            dialog.setTitle("Guardar");
            dialog.setMessage("Espere...");
            dialog.show();
            dialog.setCanceledOnTouchOutside(true);

            HashMap map =new HashMap();
            map.put("nombre", nombres );
            map.put("dirección", direcciones );
            map.put("ciudad", ciudades );
            map.put("teléfono", telefonos );
            map.put("uid", CurrentUserId );

            UserRef.child(CurrentUserId).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        EnviarAlInicio();
                        dialog.dismiss();
                    }else {
                        String mensaje = task.getException().getMessage();
                        Toast.makeText(SetupAdminActivity.this, "Error: " + mensaje, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    private void EnviarAlInicio() {

        Intent intent= new Intent(SetupAdminActivity.this, AdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

