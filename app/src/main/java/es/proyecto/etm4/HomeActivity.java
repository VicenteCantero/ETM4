package es.proyecto.etm4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    private Button botonUsuario, botonAdministrador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        botonUsuario= (Button) findViewById(R.id.botonUsuario);
        botonAdministrador= (Button) findViewById(R.id.botonAdministrador);

        botonUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        botonAdministrador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent (HomeActivity.this, LoginAdminActivity.class);
                startActivity(intent);
            }
        });
    }
}