package my.virkato.snake3d;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

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
}