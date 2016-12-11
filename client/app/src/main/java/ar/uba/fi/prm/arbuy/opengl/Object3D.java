package ar.uba.fi.prm.arbuy.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.util.List;

import ar.uba.fi.prm.arbuy.R;

/**
 * Created by pablo on 10/12/16.
 */
public class Object3D {
    private static final String TAG = "Object3D";

    private OpenGlMesh mMesh;
    private int[] mTextures;
    private int mProgram;

    private float[] mModelMatrix = new float[16];

    private float mNearPlane;
    private float mFarPlane;
    private float mTextureWidth;
    private float mTextureHeight;

    // Material
    private String mTextureName;
    private int mTextureId;
    private float[] mColor = new float[4];;

    public void setData(float[] vertices, float[] textureCoords, short[] indices){
        mMesh = new OpenGlMesh(vertices, textureCoords, indices, GLES20.GL_TRIANGLES);
    }


    public void setUpProgramAndBuffers(Context context) {
        mMesh.createVbos();
        if(mTextureName != null){
            Log.d(TAG, "Bitmap file " + mTextureName);
            Bitmap bitmap = BitmapFactory.decodeFile(mTextureName);

            createTexture(bitmap);
        }else if(mTextureId != 0){
            Log.d(TAG, "Bitmap id " + mTextureId);
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), mTextureId);
            createTexture(bitmap);
        }
        mProgram = OpenGlHelper.createProgram(context, R.raw.sphere_vertex_shader, R.raw
                .sphere_fragment_shader);
    }

    private void createTexture(Bitmap texture) {
        mTextures = new int[1];
        GLES20.glGenTextures(1, mTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);
        texture.recycle();
    }

    public void drawSphere(float[] vpMatrix, int depthTexture) {
        GLES20.glUseProgram(mProgram);
        // Enable depth write for AR.
        int sph = GLES20.glGetAttribLocation(mProgram, "a_Position");
        int sth = GLES20.glGetAttribLocation(mProgram, "a_TexCoord");

        int um = GLES20.glGetUniformLocation(mProgram, "u_MvpMatrix");
        int ut = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        int dh = GLES20.glGetUniformLocation(mProgram, "u_depthTexture");
        int un = GLES20.glGetUniformLocation(mProgram, "u_NearPlane");
        int uf = GLES20.glGetUniformLocation(mProgram, "u_FarPlane");
        int uw = GLES20.glGetUniformLocation(mProgram, "u_Width");
        int uh = GLES20.glGetUniformLocation(mProgram, "u_Height");
        int uht = GLES20.glGetUniformLocation(mProgram, "u_hasTexture");
        int uch = GLES20.glGetUniformLocation(mProgram, "u_Color");

        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, mModelMatrix, 0);

        if(mTextures != null) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
            GLES20.glUniform1i(ut, 0);
            GLES20.glUniform1i(uht, GLES20.GL_TRUE);
        }else{
            GLES20.glUniform1i(uht, GLES20.GL_FALSE);
            GLES20.glUniform4fv(uch, 1, mColor, 0);
        }


        GLES20.glUniformMatrix4fv(um, 1, false, mvpMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, depthTexture);
        GLES20.glUniform1i(dh, 1);

        GLES20.glUniform1f(un, mNearPlane);
        GLES20.glUniform1f(uf, mFarPlane);

        GLES20.glUniform1f(uw, mTextureWidth);
        GLES20.glUniform1f(uh, mTextureHeight);

        mMesh.drawMesh(sph, sth);
    }

    public void setModelMatrix(float[] modelMatrix) {
        System.arraycopy(modelMatrix, 0, mModelMatrix, 0, 16);
    }

    public void configureCamera(float nearPlane, float farPlane){
        mNearPlane = nearPlane;
        mFarPlane = farPlane;
    }

    public void setDepthTextureSize(float width, float height){
        mTextureWidth = width;
        mTextureHeight = height;
    }

    public void setTexturePath(String textureName){
        mTextureName = textureName;
    }

    public void setColor(int color){
        mColor = new float[4];
        mColor[0] = (float) Color.red(color) / 255.f;
        mColor[1] = (float) Color.green(color) / 255.f;
        mColor[2] = (float) Color.blue(color) / 255.f;
        //mColor[3] = (float) Color.alpha(color) / 255.f;
        mColor[3] = 1f;
        Log.d(TAG, "Setting color r: " + mColor[0] + " g: " + mColor[1] + " b: " + mColor[2] + " a: " + mColor[3]);
    }

    public void setTextureId(int resourceId){
        mTextureId = resourceId;
    }
}
