package es.proyecto.etm4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginAdminActivity extends AppCompatActivity {

    private EditText numero, codigo;
    private Button enviarNumero, enviarCodigo;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String verificacionId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_admin);

        numero=(EditText) findViewById(R.id.numeroAdmin);
        codigo= (EditText) findViewById(R.id.codigoAdmin);
        enviarCodigo= (Button) findViewById(R.id.enviarCodigoAdmin);
        enviarNumero= (Button) findViewById(R.id.enviarNumeroAdmin);

        auth= FirebaseAuth.getInstance();
        dialog= new ProgressDialog(this);

        enviarNumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumber= numero.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(LoginAdminActivity.this, "Ingrese su tel??fono", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.setTitle("Validando tel??fono");
                    dialog.setMessage("Por favor, espere...");
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(true);

                    PhoneAuthOptions options= PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(LoginAdminActivity.this)
                            .setCallbacks(callbacks)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        enviarCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numero.setVisibility(View.GONE);
                enviarNumero.setVisibility(View.GONE);

                String verificacionCode = codigo.getText().toString();
                if (TextUtils.isEmpty(verificacionCode)) {
                    Toast.makeText(LoginAdminActivity.this, "Ingresa el c??digo recibido", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.setTitle("Verificando c??digo");
                    dialog.setMessage("Por favor, espere...");
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(true);

                    PhoneAuthCredential credential= PhoneAuthProvider.getCredential(verificacionId, verificacionCode);

                }
            }
        });

        callbacks =new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                IngresadoConExito(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                dialog.dismiss();
                Toast.makeText(LoginAdminActivity.this, "Error, posibles causas: /n 1. No hay conexi??n a Internet /n 2. N??mero inv??lido /n 3.Sin c??digo regional", Toast.LENGTH_LONG).show();
                numero.setVisibility(View.VISIBLE);
                enviarNumero.setVisibility(View.VISIBLE);
                codigo.setVisibility(View.GONE);
                enviarCodigo.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                verificacionId =s;
                resendingToken= token;
                dialog.dismiss();
                Toast.makeText(LoginAdminActivity.this, "C??digo enviado, revise su tel??fono", Toast.LENGTH_SHORT).show();
                numero.setVisibility(View.GONE);
                enviarNumero.setVisibility(View.GONE);
                codigo.setVisibility(View.VISIBLE);
                enviarCodigo.setVisibility(View.VISIBLE);
            }
        };
    }

    private void IngresadoConExito(PhoneAuthCredential phoneAuthCredential){
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    dialog.dismiss();
                    Toast.makeText(LoginAdminActivity.this, "Login exitoso", Toast.LENGTH_SHORT).show();
                    EnviarALaPrincipal();
                }else{
                    String err= task.getException().toString();
                    Toast.makeText(LoginAdminActivity.this, "Error: "+err, Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser=auth.getCurrentUser();
        if(firebaseUser != null){
            EnviarALaPrincipal();
        }
    }

    private void EnviarALaPrincipal() {
        Intent intent= new Intent(LoginAdminActivity.this, PrincipalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("phone", phoneNumber);
        intent.putExtra("rol", "Administrador");
        startActivity(intent);
        finish();
    }
}