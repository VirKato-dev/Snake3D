package my.virkato.snake3d;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import java.util.Locale;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

public class Snake {
    public boolean Dead;
    public int Die;
    private int FrogX;
    private int FrogY;
    public int GreenTexture;
    private final int[] Grid = new int[256];
    public Snakie Head;
    private Snakie Last;
    public char Move;
    public int NewScore = -1;
    public int RedTexture;
    public float Score;
    public long ScoreTicks = 0;
    public float Time;
    private final float[] bestScore = new float[5];
    private final float[] bestTimes = new float[5];
    private final Context context;

    public class Snakie {
        private float Angle = 0;
        private int CellX = 0;
        private int CellY = 0;
        private int MoveX = 0;
        private int MoveY;
        private int MoveZ = 0;
        private Snakie Next;
        private int NextX = 0;
        private int NextY = 0;
        private final Snake Owner;
        private final Snakie Parent;
        public int PosX = 0;
        private int PosY = 0;
        public int PosZ = 0;
        private float RotX = 0;
        public float RotY = 0;
        private float RotZ = 0;
        private float StepX = 0;
        private float StepY = 0;
        private float StepZ = 0;
        //        private final long TICKS = 250;
        private long Ticks = 0;
        private float Turn = 0;

        public Snakie(Snake snake, Snakie parent) {
            Owner = snake;
            Parent = parent;
            snake.Last = this;
            if (parent == null) {
                MoveY = 20;
                PosY = -100;
                CellX = 8;
                CellY = 8;
                return;
            }
            parent.Next = this;
            PosX = parent.PosX;
            PosY = parent.PosY;
            PosZ = parent.PosZ;
            Ticks = parent.Ticks;
            CellX = parent.CellX;
            CellY = parent.CellY;
            Angle = parent.Angle;
            StepX = parent.StepX;
            StepY = parent.StepY;
            StepZ = parent.StepZ;
            RotY = parent.RotY;
            RotZ = parent.RotZ;
        }

        private float FixAngle(float value) {
            while (value > 360) {
                value -= 360;
            }
            while (value < 0) {
                value += 360;
            }
            return value;
        }

        private void Animate(long ticks) {
            if (Next != null) {
                Next.Animate(ticks);
            }
            Ticks += ticks;
            while (Ticks > 250) {
                if (MoveY == 0 && !Owner.Dead) {
                    if (Next == null) {
                        Owner.Grid[(CellY * 16) + CellX] = 0;
                    }
                    NextCell();
                    CellX = NextX;
                    CellY = NextY;
                    Owner.Grid[(CellY * 16) + CellX] = 1;
                }
                PosX += MoveX;
                PosY += MoveY;
                PosZ += MoveZ;
                Angle = FixAngle(Angle + Turn);
                if (Parent != null) {
                    MoveX = Parent.MoveX;
                    MoveY = Parent.MoveY;
                    MoveZ = Parent.MoveZ;
                    Turn = Parent.Turn;
                } else if (MoveY <= 0) {
                    Turn = 0;
                    switch (Owner.Move) {
                        case 'L':
                            if (MoveX > 0) {
                                MoveX = 0;
                                MoveZ = -20;
                            } else if (MoveX < 0) {
                                MoveX = 0;
                                MoveZ = 20;
                            } else if (MoveZ > 0) {
                                MoveZ = 0;
                                MoveX = 20;
                            } else if (MoveZ < 0) {
                                MoveZ = 0;
                                MoveX = -20;
                            }
                            Turn = 90;
                            break;
                        case 'R':
                            if (MoveX > 0) {
                                MoveX = 0;
                                MoveZ = 20;
                            } else if (MoveX < 0) {
                                MoveX = 0;
                                MoveZ = -20;
                            } else if (MoveZ > 0) {
                                MoveZ = 0;
                                MoveX = -20;
                            } else if (MoveZ < 0) {
                                MoveZ = 0;
                                MoveX = 20;
                            }
                            Turn = -90;
                            break;
                    }
                    Owner.Move = ' ';
                } else if (PosY == 0) {
                    MoveY = 0;
                    MoveZ = -20;
                }
                Ticks -= 250;
            }
            float f = Ticks / 250f;
            if (Parent == null && f > 0 && MoveY == 0) {
                NextCell();
                if (NextX < 0 || NextY < 0 || NextX >= 16 || NextY >= 16 || Grid[(NextY * 16) + NextX] == 1) {
                    Owner.Dies();
                } else if (Grid[(NextY * 16) + NextX] == 2) {
                    Owner.Grow();
                }
            }
            StepX = PosX + MoveX * f;
            StepY = PosY + MoveY * f;
            StepZ = PosZ + MoveZ * f;
            RotY = FixAngle(Angle + (Turn * f));
            if (f > 0) {
                RotZ = (1 - f) * Turn;
            } else {
                RotZ = Turn * f;
            }
            if (MoveY > 0) {
                f = -PosY - MoveY * f;
                if (f > 20) {
                    f = 1;
                } else {
                    f /= 20;
                }
                RotX = 90 * f;
                return;
            }
            RotX = 0.0f;
        }

        private void NextCell() {
            NextX = CellX;
            NextY = CellY;
            if (MoveX > 0) {
                NextX++;
            } else if (MoveX < 0) {
                NextX--;
            }
            if (MoveZ > 0) {
                NextY++;
            } else if (MoveZ < 0) {
                NextY--;
            }
        }

        private void Render(GL10 gl, boolean shadow) {
            if (!shadow || MoveY == 0) {
                if (Next != null) {
                    Next.Render(gl, shadow);
                }
                if (Parent == null) {
                    gl.glBindTexture(GL10.GL_TEXTURE_2D, RedTexture); // target 3553 (0xDE1)
                }
                gl.glPushMatrix();
                gl.glTranslatef(StepX, StepY, StepZ);
                gl.glRotatef(RotY, 0, 1, 0);
                gl.glRotatef(RotZ, 0, 0, 1);
                gl.glRotatef(RotX, 1, 0, 0);
                Snake.HalfSnake(gl);
                gl.glRotatef(180, 1, 0, 0);
                Snake.HalfSnake(gl);
                gl.glPopMatrix();
            }
        }
    }

    public Snake(Context context) {
        this.context = context;
        SharedPreferences settings = context.getSharedPreferences("re.execute.minimal", 0);
        for (int i = 0; i < 5; i++) {
            bestScore[i] = settings.getFloat("score" + i, 0);
            bestTimes[i] = settings.getFloat("times" + i, 0);
            Log.d("S3D", bestScore(i));
        }
    }

    public static void HalfSnake(GL10 gl) {
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 12, 4); // mode 5 (0x5)
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4); // mode 5 (0x5)
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 20, 4); // mode 5 (0x5)
        gl.glRotatef(180, 0, 1, 0);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4); // mode 5 (0x5)
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 20, 4); // mode 5 (0x5)
    }

    public String bestScore(int index) {
        int time = (int) bestTimes[index];
        return String.format(Locale.getDefault(), "%09d %d%d:%d%d",
                (int) bestScore[index], time / 60 / 10, time / 60 % 10, time % 60 / 10, time % 60 % 10);
    }

    public void Reset() {
        int i;
        Head = new Snakie(this, null);
        for (i = 0; i < 4; i++) {
            /*Snakie snakie =*/
            new Snakie(this, Last);
        }
        Dead = false;
        Die = 0;
        for (i = 0; i < 256; i++) {
            Grid[i] = 0;
        }
        FrogX = -1;
        Score = 0;
        Time = 0;
        Move = ' ';
    }

    void Grow() {
        /*Snakie snakie =*/
        new Snakie(this, Last);
        Score += 50;
        MoveFrog();
        int i = 0;
        while (i < 5) {
            if (bestScore[i] >= Score && (bestScore[i] > Score || bestTimes[i] <= Time)) {
                i++;
            } else if (NewScore != i) {
                NewScore = i;
                ScoreTicks = SystemClock.uptimeMillis();
                return;
            } else {
                return;
            }
        }
    }

    void Dies() {
        Dead = true;
        NewScore = -1;
        if (Score >= 50) {
            int i = 0;
            while (i < 5) {
                if (bestScore[i] < Score || (bestScore[i] <= Score && bestTimes[i] > Time)) {
                    int j;
                    for (j = 4; j > i; j--) {
                        bestScore[j] = bestScore[j - 1];
                        bestTimes[j] = bestTimes[j - 1];
                    }
                    bestScore[i] = Score;
                    bestTimes[i] = Time;
                    NewScore = i;
                    SharedPreferences.Editor editor = context.getSharedPreferences("re.execute.minimal", Activity.MODE_PRIVATE).edit();
                    for (j = 0; j < 5; j++) {
                        editor.putFloat("score" + j, bestScore[j]);
                        editor.putFloat("times" + j, bestTimes[j]);
                    }
                    editor.apply(); //.commit();
                    return;
                }
                i++;
            }
        }
    }

    public void MoveFrog() {
        if (FrogX >= 0) {
            Grid[(FrogY * 16) + FrogX] = 0;
        }
        if (Dead) {
            FrogX = -1;
            return;
        }
        Random r = new Random();
        do {
            FrogX = r.nextInt(16);
            FrogY = r.nextInt(16);
        } while (Grid[(FrogY * 16) + FrogX] != 0);
        Grid[(FrogY * 16) + FrogX] = 2;
    }

    public void Animate(long ticks) {
        if (Dead) {
            Die += ticks;
            return;
        }
        Score += ticks / 2000f;
        Time += ticks / 1000f;
        Head.Animate(ticks);
    }

    public void Render(GL10 gl, boolean shadow) {
        if (Dead) {
            gl.glTranslatef(0, -Die / 200f, 0);
        }
        gl.glBindTexture(3553, GreenTexture);
        Head.Render(gl, shadow);
    }

    public void RenderFrog(GL10 gl) {
        if (FrogX >= 0) {
            gl.glPushMatrix();
            gl.glTranslatef((FrogX - 8) * 20f, 0, (FrogY - 8) * 20f);
            gl.glRotatef(45, 0, 1, 0);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, GreenTexture); // target 3553 (0xDE1)
            for (int i = 0; i < 5; i++) {
                gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 24, 4); // mode 5 (0x5)
                gl.glRotatef(90, 0, 1, 0);
                if (i == 3) {
                    gl.glRotatef(-90, 1, 0, 0);
                }
            }
            gl.glPopMatrix();
        }
    }
}
