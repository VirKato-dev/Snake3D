package my.virkato.snake3d;

import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private GLRenderer mFRender;
    private GLSurfaceView mFSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        mFSurface = new GLSurfaceView(this);
        mFRender = new GLRenderer(this);
        mFSurface.setRenderer(mFRender);
        setContentView(mFSurface);
    }

    @Override
    public void onResume() {
        super.onResume();
        mFSurface.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mFSurface.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mFRender.onTouchEvent(event);
    }

    private void showNotification() {
        // Идентификатор уведомления
        int NOTIFY_ID = 101;

        // Идентификатор канала
        String CHANNEL_ID = "Cat channel";

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        //.setSmallIcon(R.drawable.ic_pets_black_24dp)
                        .setContentTitle("Напоминание")
                        .setContentText("Пора покормить кота")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFY_ID, builder.build());
    }
}