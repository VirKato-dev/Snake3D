package my.virkato.snake3d;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class GLRenderer implements GLSurfaceView.Renderer {
    private static final float[] VERTICES = {160, 0, -160, 0, 0, 160, 90, -160, 1, 0, -160, 0, -160, 0, 16, -160, 90, -160, 1, 16, -10, 0, -10, 0, 0, -10, 0, 10, 0, 1, 10, 0, -10, 1, 0, 10, 0, 10, 1, 1, -160, 0, -160, 0, 0, -160, 0, 160, 0, 16, 160, 0, -160, 16, 0, 160, 0, 160, 16, 16, 3, 3, -6, 1, 0, -3, 3, -6, 0, 0, 3, 3, 6, 1, 1, -3, 3, 6, 0, 1, 3, 3, 6, 1, 0, -3, 3, 6, 0, 0, 6, 0, 9, 1, 1, -6, 0, 9, 0, 1, -6, 0, 9, 1, 0, -3, 3, 6, 0, 0, -6, 0, -9, 1, 1, -3, 3, -6, 0, 1, 4, 4, 4, 0, 0, -4, 4, 4, 1, 0, 4, -4, 4, 0, 1, -4, -4, 4, 1, 1, -160, 0, -160, 0, 0, 0, 0, 0, 8, 8, 160, 0, -160, 16, 0, 20, 0, 0, 9, 8, 160, 0, 160, 16, 16, 20, 0, 20, 9, 9, -160, 0, 160, 0, 16, 0, 0, 20, 8, 9, -160, 0, -160, 0, 0, 0, 0, 0, 8, 8, 256, 64, 0, 0, 0, 0, 64, 0, 0, 1, 256, 0, 0, 1, 0, 0, 0, 0, 1, 1};
    private static final FloatBuffer VERTICES_FLOAT_BUFFER = ByteBuffer.allocateDirect(VERTICES.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();

    //    private boolean BeginGame;
//    private long BeginTicks;
    private char rCamMode = 'F';
    private long rCurrTicks;
    private long rDeltaTicks;
    private float rDepth;
    private char rDieMode;
    private float rFDownX;
    private float rFDownY;
    private int rFHeight;
    private int rFWidth;
    //    private long Frames;
    private long rGameTicks = 0;
    //private boolean rHighScores = false;
    private long rLastTicks;
    //private boolean rMainScreen = true;
    private char rOldMode;
    private boolean rPause = false;
    private long rPauseTicks;
    private char rRenderState = 'L';
    private long rStartTicks;
    private final int[] rTextures = new int[7];
    //private long rWelcome;
    private final Context context;
    private GLFont font;
    //private int frames = 0;
    //private long nano;
    private float px;
    private float py;
    private float rx;
    private float ry;
    private final Snake snake;

    static {
        VERTICES_FLOAT_BUFFER.put(VERTICES);
    }

    private static class GLFont {
        private final String fChars;
        private final float fHeight;
        private int fHeight2;
        private final int fTexture;
        private final FloatBuffer fVertices;
        private float fWidth;
        private int fWidth2;
        private final float[] fWidths;
        private boolean open = false;

        public GLFont(GL10 gl, String chars) {
            Paint pen = new Paint();
            pen.setAntiAlias(true);
            pen.setTextSize(30);
            pen.setFakeBoldText(true);
            pen.setStyle(Paint.Style.FILL_AND_STROKE);
            pen.setColor(-1);
            fChars = chars;
            Paint.FontMetrics fm = pen.getFontMetrics();
            fHeight = Math.abs(fm.bottom) + Math.abs(fm.top);
            fWidths = new float[chars.length()];
            pen.getTextWidths(chars, fWidths);
            fWidth = 0;
            for (int i = 0; i < chars.length(); i++) {
                fWidth += fWidths[i];
            }
            fWidth2 = 1;
            while (fWidth2 < fWidth) {
                fWidth2 *= 2;
            }
            fHeight2 = 1;
            while (fHeight2 < fHeight) {
                fHeight2 *= 2;
            }
            Bitmap bitmap = Bitmap.createBitmap(fWidth2, fHeight2, Bitmap.Config.ALPHA_8);
            Canvas canvas = new Canvas(bitmap);
            bitmap.eraseColor(0);
            canvas.drawText(chars, 0, fHeight, pen);
            int[] textures = new int[1];
            gl.glGenTextures(1, textures, 0);
            fTexture = textures[0];
            gl.glBindTexture(GL10.GL_TEXTURE_2D, fTexture); // target 3553 (0xDE1)
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); // pname 10240 (0x2800) param 9728 (0x2601)
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST); // pname 10241 (0x2801) param 9728 (0x2600)
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT); // pname 10242 (0x2802) param 10497 (0x2901)
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT); // pname 10243 (0x2803) param 10497 (0x2901)
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            fVertices = ByteBuffer.allocateDirect(80)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
        }

        public float textWidth(String Str) {
            float width = 0;
            for (int i = 0; i < Str.length(); i++) {
                int index = fChars.indexOf(Str.charAt(i));
                if (index >= 0) {
                    width += fWidths[index];
                }
            }
            return width;
        }

        public float charWidth(char ch) {
            int index = fChars.indexOf(ch);
            if (index >= 0) {
                return fWidths[index];
            }
            return 0;
        }

        private void drawChar(GL10 gl, char ch, float scale) {
            int index = fChars.indexOf(ch);
            if (index >= 0) {
                float Left = 0;
                for (int i = 0; i < index; i++) {
                    Left += fWidths[i];
                }
                float width = fWidths[index];
                fVertices.position(0);
                fVertices.put(0);
                fVertices.put(0);
                fVertices.put(0);
                fVertices.put((Left + 1) / fWidth2);
                fVertices.put(fHeight / fHeight2);
                fVertices.put(width * scale);
                fVertices.put(0);
                fVertices.put(0);
                fVertices.put((Left + width) / fWidth2);
                fVertices.put(fHeight / fHeight2);
                fVertices.put(0);
                fVertices.put(fHeight * scale);
                fVertices.put(0);
                fVertices.put((Left + 1) / fWidth2);
                fVertices.put(0);
                fVertices.put(width * scale);
                fVertices.put(fHeight * scale);
                fVertices.put(0);
                fVertices.put((Left + width) / fWidth2);
                fVertices.put(0);
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4); // mode 5 (0x5)
                gl.glTranslatef((width + 1) * scale, 0, 0);
            }
        }

        public void beginDraw(GL10 gl) {
            fVertices.position(0);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 20, fVertices); // type 5126 (0x1406)
            fVertices.position(3);
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 20, fVertices);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, fTexture); // target 3553 (0xDE1)
            gl.glEnable(GL10.GL_BLEND); // cap 3042 (0xBE2)
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA); // sfactor 770 (0x302) dfactor 771 (0x303)
            gl.glPushMatrix();
            open = true;
        }

        public void endDraw(GL10 gl) {
            gl.glDisable(GL10.GL_BLEND); // type 3042
            gl.glPopMatrix();
            open = false;
        }

        public void renderText(GL10 gl, float x, float y, String text, float scale) {
            gl.glPushMatrix();
            gl.glTranslatef(x, y, 0);
            for (int i = 0; i < text.length(); i++) {
                drawChar(gl, text.charAt(i), scale);
            }
            gl.glPopMatrix();
        }

        public void drawText(GL10 gl, String text, float scale) {
            boolean wasOpen = open;
            if (!open) {
                beginDraw(gl);
            }
            for (int i = 0; i < text.length(); i++) {
                drawChar(gl, text.charAt(i), scale);
            }
            if (!wasOpen) {
                endDraw(gl);
            }
        }
    }

    public GLRenderer(Context context) {
        this.context = context;
        snake = new Snake(context);
    }

    private int SetTextureColor(GL10 gl, int index, int width, int height, int color, int borderSize) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        bitmap.eraseColor(color);
        Paint pen = new Paint();
        pen.setColor(-1);
        pen.setStrokeWidth((float) borderSize);
        pen.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, width - 1, height - 1, pen);
        if (color == 0) {
            pen.setColor(0xFF72EAFF);
            pen.setStrokeWidth(borderSize / 2f);
            canvas.drawRect(borderSize / 2f, borderSize / 2f, width - (borderSize / 2f), height - (borderSize / 2f), pen);
        }
        gl.glBindTexture(GL10.GL_TEXTURE_2D, rTextures[index]); // target 3553 (0xDE1)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); // pname 10240 param 9729
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR); // pname 10241 param 9729
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        return rTextures[index];
    }

    private void SetGradientTexture(GL10 gl, int index, int width, int height, int color1, int color2) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        Paint brush = new Paint();
        LinearGradient gradient = new LinearGradient(0, 0, (float) width, 0, color1, color2, Shader.TileMode.REPEAT);
        brush.setStyle(Paint.Style.FILL);
        brush.setAntiAlias(true);
        brush.setShader(gradient);
        canvas.drawRect(0, 0, (float) width, (float) height, brush);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, rTextures[index]); // target 3553 (0xDE1)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); // pname 10240 param 9729
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR); // pname 10241 param 9729
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE); // pname 10242 param 33071
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT); // pname 10243 param 10497
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
    }

    private void BorderTexture(GL10 gl, int index) {
        Bitmap bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        Paint brush = new Paint();
        LinearGradient gradient = new LinearGradient(0, 0, 32, 0, -1, 0xFF72EAFF, Shader.TileMode.REPEAT);
        brush.setStyle(Paint.Style.FILL);
        brush.setAntiAlias(true);
        brush.setShader(gradient);
        brush.setColor(0xFF0000FF);
        canvas.drawRect(0, 0, 32, 32, brush);
        Paint pen = new Paint();
        pen.setColor(-1);
        pen.setStrokeWidth(1);
        pen.setStyle(Paint.Style.STROKE);
        canvas.drawLine(0, 1, 32, 1, pen);
        canvas.drawLine(0, 31, 32, 31, pen);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, rTextures[index]); // target 3553 (0xDE1)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); // pname 10240 param 9729
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR); // pname 10241 param 9729
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE); // pname 10242 param 33071
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT); // pname 10243 param 10497
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0, 0, 0, 0);
        gl.glClearDepthf(1);
        gl.glEnable(GL10.GL_CULL_FACE); // cap 2884
        gl.glEnable(GL10.GL_DEPTH_TEST); // cap 2929
        gl.glDepthFunc(GL10.GL_LEQUAL); // func 515
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); // array 32884
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); // array 32888
        gl.glShadeModel(GL10.GL_SMOOTH); // mode 7425
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); // target 3152 mode 4354
        font = new GLFont(gl, "GET READY!MOVSC0123456789:IPUHBNLFWK()");
        gl.glGenTextures(7, rTextures, 0);
        gl.glEnable(GL10.GL_TEXTURE_2D); // target 3553 (0xDE1)
        gl.glBindTexture(GL10.GL_TEXTURE_2D, rTextures[0]); // target 3553 (0xDE1)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); // pname 10240 param 9729
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR); // pname 10241 param 9729
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE); // pname 10242 param 33071
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE); // pname 10243 param 33071
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0); // target 3553 (0xDE1)
        bitmap.recycle();
        SetGradientTexture(gl, 1, 16, 512, 0xFF72EAFF, 0);
        snake.RedTexture = SetTextureColor(gl, 2, 32, 32, 0xFFF01020, 4);
        snake.GreenTexture = SetTextureColor(gl, 3, 32, 32, 0xFF10F020, 3);
        SetTextureColor(gl, 4, 32, 32, 0, 2);
        BorderTexture(gl, 5);
        SetTextureColor(gl, 6, 64, 256, 0, 2);
        //rWelcome = SystemClock.uptimeMillis();
        //nano = System.nanoTime();
        rStartTicks = SystemClock.uptimeMillis();
        rLastTicks = rStartTicks;
    }

    private void Restart() {
        snake.Reset();
        rGameTicks = 0;
        rRenderState = 'R';
        rOldMode = rCamMode;
        rCamMode = 'G';
        rDieMode = ' ';
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        rFWidth = width;
        rFHeight = height;
        if (height == 0) {
            height = 1;
        }
        gl.glViewport(0, 0, width, height);
        View3D(gl);
    }

    private void View3D(GL10 gl) {
        gl.glMatrixMode(GL10.GL_PROJECTION); // mode 5889
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45, (float) rFWidth / rFHeight, 0.5f, 1000);
        gl.glMatrixMode(GL10.GL_MODELVIEW); // mode 5888
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (255 & motionEvent.getAction()) {
            case 0: {
                switch (rRenderState) {
                    case 'L': {
                        return true;
                    }
                    case 'H': {
                        Restart();
                        return true;
                    }
                    case 'S': {
                        rRenderState = (char) 77; // 'M'
                    }
                }
                rFDownX = motionEvent.getX();
                rFDownY = motionEvent.getY();
                if (rRenderState != 'M') return true;
                float f = 600 * rFDownY / rFHeight;
                if (f < 520 || f > 580) return true;
                float f2 = 800 * rFDownX / rFWidth;
                if (f2 < 8 || f2 > 762) return true;
                if (f2 % 264 > 256) return true;
                float f3 = f2 / 264;
                if (f3 >= 2) {
                    rRenderState = (char) 83; // 'S'
                    return true;
                }
                if (!(f3 >= 1)) {
                    Restart();
                    return true;
                }
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setData(Uri.parse("market://details?id=re.execute.snake3d"));
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException activityNotFoundException) {
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?re.execute.snake3d"));
                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException activityNotFoundException2) {
                        Toast.makeText(context, "Could not open Android market, please install the market app.", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
            case 5: {
                if (rRenderState != 'G') return true;
                if (motionEvent.getPointerCount() != 2) return true;
                rPause = !rPause;
                if (rPause) {
                    rOldMode = rCamMode;
                    rPauseTicks = rCurrTicks;
                    return true;
                }
                rCamMode = rOldMode;
                return true;
            }
            case 2: {
                if (rFDownX == 0) return true;
                if (rRenderState != 'R' && rRenderState != 'G') {
                    return true;
                }
                float f = motionEvent.getX() - rFDownX;
                float f4 = motionEvent.getY() - rFDownY;
                if (rPause) {
                    rCamMode = (char) 77; // 'M'
                    rx = FixAngle(f + rx);
                    ry = f4 + ry;
                    if (ry > 75) ry = 75;
                    if (ry < 0) ry = 0;
                    rFDownX = motionEvent.getX();
                    rFDownY = motionEvent.getY();
                    return true;
                }
                if (!(Math.sqrt(f * f + f4 * f4) > 25)) return true;
                if (Math.abs(2 * f) > Math.abs(f4)) {
                    snake.Move = f > 0 ? (char) 82 : (char) 76; // 'R' : 'L'
                } else {
                    rCamMode = f4 > 0 ? (char) 70 : (char) 71; // 'F' : 'G'
                }
                rFDownX = 0;
                return true;
            }
        }
        return true;
    }


    private void RenderCells(GL10 gl, boolean Hole) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, rTextures[4]); // target 3553 (0xDE1)
        if (Hole) {
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 28, 10); // mode 5 (0x5)
        } else {
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 8, 4); // mode 5 (0x5)
        }
    }

    private float FixAngle(float a) {
        while (a > 360) {
            a -= 360;
        }
        while (a < 0) {
            a += 360;
        }
        return a;
    }

    private float rotate(float r, float f, float t) {
        float a = t - r;
        float b = (t + 360) - r;
        if (Math.abs(b) < Math.abs(a)) {
            a = b;
        }
        b = (t - 360) - r;
        if (Math.abs(b) < Math.abs(a)) {
            a = b;
        }
        return FixAngle(f * a + r);
    }

    private void RenderCube(GL10 gl, boolean Hole) {
        int i;
        gl.glEnable(GL10.GL_BLEND); // cap 3042 (0xBE2)
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA); // sfactor 770 (0x302) dfactor 771 (0x303)
        RenderCells(gl, Hole);
        gl.glDisable(GL10.GL_BLEND); // cap 3042 (0xBE2)
        gl.glPushMatrix();
        gl.glBindTexture(GL10.GL_TEXTURE_2D, rTextures[1]); // target 3553 (0xDE1)
        for (i = 0; i < 4; i++) {
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4); // mode 5 (0x5)
            gl.glRotatef(90, 0, 1, 0);
        }
        gl.glBindTexture(GL10.GL_TEXTURE_2D, rTextures[5]); // target 3553 (0xDE1)
        gl.glTranslatef(0, -90, 0);
        gl.glCullFace(GL10.GL_FRONT); // mode 1028 (0x404)
        for (i = 0; i < 4; i++) {
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4); // mode 5 (0x5)
            gl.glRotatef(90, 0, 1, 0);
        }
        gl.glCullFace(GL10.GL_BACK); // mode 1029 (0x405)
        gl.glPopMatrix();
    }

    private void RenderButton(GL10 gl, float x, float y) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, 0);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 38, 4); // mode 5 (0x5)
        gl.glPopMatrix();
    }

    private void BindVertices(GL10 gl) {
        VERTICES_FLOAT_BUFFER.position(0);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 20, VERTICES_FLOAT_BUFFER); //type 5126 (0x1406)
        VERTICES_FLOAT_BUFFER.position(3);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 20, VERTICES_FLOAT_BUFFER); //type 5126 (0x1406)
    }

    private void FPS(GL10 gl) {
        if (rDeltaTicks < 25) {
            try {
                Thread.sleep(25 - rDeltaTicks);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void RenderLogo(GL10 gl) {
        gl.glTranslatef(0, 0, -75);
        gl.glRotatef(90, 1, 0, 0);
        rGameTicks += rDeltaTicks;
        if (rGameTicks < 500) {
            gl.glTranslatef((rGameTicks / 2f) - 250, 0, 0);
        }
        if (rGameTicks > 2000) {
            gl.glTranslatef((rGameTicks - 2000) / 2f, 0, 0);
        }
        gl.glBindTexture(GL10.GL_TEXTURE_2D, rTextures[0]); // target 3553 (0xDE1)
        gl.glDrawArrays(5, 4, 4);
        if (rGameTicks > 2500) {
            rRenderState = 'M';
        }
        FPS(gl);
    }

    private void backgroundCube(GL10 gl) {
        gl.glTranslatef(0, 10, -450);
        gl.glRotatef(40, 1, 0, 0);
        gl.glRotatef((rCurrTicks - rStartTicks) / 100f, 0, 1, 0);
        gl.glColor4f(0, 0.5f, 0.5f, 1);
        RenderCube(gl, false);
        gl.glColor4f(1, 1, 1, 1);
    }

    private void RenderMenu(GL10 gl) {
        backgroundCube(gl);
        View2D(gl);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, rTextures[6]); // target 3553 (0xDE1)
        RenderButton(gl, 8, 30);
        RenderButton(gl, 272, 30);
        RenderButton(gl, 536, 30);
        font.beginDraw(gl);
        font.renderText(gl, 400 - font.textWidth("SNAKE 3D"), 500, "SNAKE 3D", 2);
        font.renderText(gl, 55, 400, "TURN LEFT", 1);
        font.renderText(gl, 250, 400, ": SLIDE TO THE LEFT", 1);
        font.renderText(gl, 54, 350, "TURN RIGHT", 1);
        font.renderText(gl, 250, 350, ": SLIDE TO THE RIGHT", 1);
        font.renderText(gl, 55, 300, "TOP VIEW", 1);
        font.renderText(gl, 250, 300, ": SLIDE UP", 1);
        font.renderText(gl, 55, 250, "3D VIEW", 1);
        font.renderText(gl, 250, 250, ": SLIDE DOWN (DEFAULT)", 1);
        font.renderText(gl, 55, 200, "PAUSE", 1);
        font.renderText(gl, 250, 200, ": TAP WITH 2 FINGERS", 1);
        font.renderText(gl, 250, 150, ": THEN TURN AROUND THE SNAKE", 1);
        font.renderText(gl, 40, 55, "START GAME", 1);
        font.renderText(gl, 285, 55, "RATE THIS APP", 1);
        font.renderText(gl, 565, 55, "HIGH SCORES", 1);
        FPS(gl);
        font.endDraw(gl);
    }

    private void RenderScores(GL10 gl) {
        backgroundCube(gl);
        View2D(gl);
        font.beginDraw(gl);
        gl.glTranslatef(400 - font.textWidth("HIGH SCORE !"), 595 - font.fHeight * 2, 0);
        font.renderText(gl, 0, 0, "HIGH SCORE !", 2);
        gl.glTranslatef(0, -font.fHeight, 0);
        for (int i = 0; i < 5; i++) {
            gl.glTranslatef(0, -2 * font.fHeight, 0);
            if (i == snake.NewScore) {
                gl.glColor4f(1, 0, 0, 1);
            } else {
                gl.glColor4f(1, 1, 1, 1);
            }
            font.renderText(gl, 0, 0, snake.bestScore(i), 1);
        }
        FPS(gl);
        font.endDraw(gl);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        try {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); // mask 16640 (0x4100)
            View3D(gl);
            gl.glLoadIdentity();
            BindVertices(gl);
            rCurrTicks = android.os.SystemClock.uptimeMillis();
            rDeltaTicks = rCurrTicks - rLastTicks;
            rLastTicks = rCurrTicks;
            switch (rRenderState) {
                case 'H':
                case 'S':
                    RenderScores(gl);
                    return;
                case 'L':
                    RenderLogo(gl);
                    return;
                case 'M':
                    RenderMenu(gl);
                    return;
                default:
                    float dx;
                    float dy;
                    if (!rPause) {
                        rGameTicks += rDeltaTicks;
                        snake.Animate(rDeltaTicks);
                    }
                    float tx = 0;
                    float ty = 0;
                    if (rCamMode == 'G' && rRenderState == 'R') {
                        tx = rGameTicks / 100f - 50;
                        ty = 60 - 60 * (float) Math.sin(rGameTicks / 3000f);
                    }
                    if (rRenderState == 'R') {
                        rDepth = rGameTicks / 50f;
                        if (rDepth > 50) {
                            rDepth = 0;
                            rRenderState = 'G';
                            snake.MoveFrog();
                        } else if (rDepth > 25) {
                            rDepth = 50 - rDepth;
                        } else if (rDepth > 20) {
                            rCamMode = rOldMode;
                        }
                    }
                    if (rCamMode == 'F') {
                        tx = snake.Head.RotY;
                        ty = 45;
                        dx = -snake.Head.PosX;
                        dy = -snake.Head.PosZ;
                    } else {
                        dx = 0;
                        dy = 0;
                    }
                    if (rCamMode != 'M') {
                        float f = rDeltaTicks / 250f;
                        rx = rotate(rx, f, tx);
                        ry = rotate(ry, f, ty);
                        px += (dx - px) * f;
                        py += (dy - py) * f;
                    }
                    gl.glTranslatef(0, 10, (4 * ry) - 450);
                    gl.glRotatef((-ry) + 80, 1, 0, 0);
                    gl.glRotatef(-rx, 0, 1, 0);
                    gl.glTranslatef(px, 0, py);
                    gl.glPushMatrix();
                    gl.glTranslatef(10, -5, 10);
                    gl.glScalef(1, -1, 1);
                    gl.glCullFace(GL10.GL_FRONT); // mode 1028 (0x404)
                    gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
                    snake.Render(gl, true);
                    snake.RenderFrog(gl);
                    gl.glColor4f(1, 1, 1, 1);
                    gl.glCullFace(GL10.GL_BACK); // mode 1029 (0x405)
                    gl.glPopMatrix();
                    RenderCube(gl, rRenderState == 'R');
                    if (rRenderState == 'R') {
                        RenderTrap(gl);
                    }
                    gl.glTranslatef(10, 5, 10);
                    snake.Render(gl, false);
                    snake.RenderFrog(gl);
                    if (snake.Die > 0) {
                        if (rDieMode == ' ') {
                            rDieMode = rCamMode;
                            rCamMode = 'G';
                        }
                        TextFX(gl, 2, "SCORE " + snake.Score, snake.Die);
                        TextFX(gl, 0, "GAME OVER !", snake.Die);
                        if (snake.Die > 2000) {
                            rCamMode = rDieMode;
                            if (snake.NewScore >= 0) {
                                rRenderState = 'H';
                            } else {
                                Restart();
                            }
                        }
                    }
                    if (rPause && rCamMode != 'M') {
                        TextFX(gl, 0, "PAUSE", rCurrTicks - rPauseTicks);
                    }
                    if (snake.ScoreTicks > 0) {
                        TextFX(gl, 0, snake.NewScore == 0 ? "BEST SCORE !" : "HIGH SCORE !", rCurrTicks - snake.ScoreTicks);
                        if (rCurrTicks - snake.ScoreTicks > 2000) {
                            snake.ScoreTicks = 0;
                        }
                    }
                    if (rRenderState == 'R') {
                        TextFX(gl, 0, "GET READY !", rGameTicks);
                    }
                    ScoreAndTime(gl);
                    FPS(gl);
            }
        } catch (Exception ignored) {
        }

    }

    private void ScoreAndTime(GL10 gl) {
        View2D(gl);
        gl.glTranslatef(10, 595 - font.fHeight, 0);
        gl.glPushMatrix();
        font.beginDraw(gl);
        font.drawText(gl, String.format(Locale.getDefault(), "SCORE %.0f", snake.Score), 1);
        gl.glPopMatrix();
        int iTime = (int) snake.Time;
        String str = String.format(Locale.getDefault(), "TIME %d%d:%d%d", iTime / 60 / 10, iTime / 60 % 10, iTime % 60 / 10, iTime % 60 % 10);
        gl.glTranslatef(800 - font.textWidth(str) - 30, 0, 0);
        font.drawText(gl, str, 1);
        font.endDraw(gl);
        View3D(gl);
    }

    private void View2D(GL10 gl) {
        gl.glMatrixMode(GL10.GL_PROJECTION); // mode 5889 (0x1701)
        gl.glLoadIdentity();
        gl.glOrthof(0, 800, 0, 600, -1, 1);
        gl.glMatrixMode(GL10.GL_MODELVIEW); // mode 5888 (0x1700)
        gl.glLoadIdentity();
        gl.glClear(GL10.GL_DEPTH_BUFFER_BIT); // mask 256 (0x100)
    }

    private void TextFX(GL10 gl, float Top, String Str, long Ticks) {
        gl.glClear(GL10.GL_DEPTH_BUFFER_BIT); // mask 256 (0x100)
        gl.glLoadIdentity();
        gl.glTranslatef(0, 20 * (Top - 2), -300);
        gl.glColor4f(1, 1, 1, 1);
        gl.glTranslatef(-font.textWidth(Str) / 2, 0, 0.25f);
        font.beginDraw(gl);
        float x = 0;
        for (int i = 0; i < Str.length(); i++) {
            char ch = Str.charAt(i);
            float w = font.charWidth(ch);
            if (Ticks < 500) {
                x += 8 - 8 * (Ticks / 500f);
            }
            gl.glTranslatef(x, 0, -0.25f);
            gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
            font.drawChar(gl, ch, 1);
            gl.glTranslatef(1 - w, 0, 0.25f);
            gl.glColor4f(1, 1, 1, 1);
            font.drawChar(gl, ch, 1);
        }
        font.endDraw(gl);
    }

    private void RenderTrap(GL10 gl) {
        gl.glPushMatrix();
        gl.glTranslatef(10, -rDepth, 10);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4); // mode 5
        gl.glPopMatrix();
    }
}