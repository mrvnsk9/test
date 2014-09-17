package com.badlogic.test.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.x.LibXUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class Test implements ApplicationListener {

    private BitmapFont font;
    private SpriteBatch batch;

    float w = 720;
    float h = 480;

    private BufferedImage image;

    private Texture texture;
    private Pixmap pixmap;

    @Override
    public void create() {
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();

        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);
        camera.update();

        font = new BitmapFont();
        batch = new SpriteBatch();

        LibXUtil.initialise();
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files\\VideoLAN\\VLC");

        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);

        image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage((int) w, (int) h);
        image.setAccelerationPriority(1.0f);

        String[] args = {"--no-video-title-show", "--verbose=3"};
        String media = "D:\\3 Days To Kill (2014).m4v";

        MediaPlayerFactory factory = new MediaPlayerFactory(args);
        DirectMediaPlayer mediaPlayer = factory.newDirectMediaPlayer(new TestBufferFormatCallback(), new TestRenderCallback());
        mediaPlayer.playMedia(media);

        System.out.println(LibVlc.INSTANCE.libvlc_get_version());
    }

    @Override
    public void dispose() {
        Gdx.app.exit();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
//        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
/*
        if (pixmap != null) {
            Texture texture = new Texture(pixmap);
            batch.draw(texture, 0, 0, w, h);
        }
*/

        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
        batch.end();
    }

    private final class TestRenderCallback extends RenderCallbackAdapter {

        public TestRenderCallback() {
            super(((DataBufferInt) image.getRaster().getDataBuffer()).getData());
        }

        @Override
        public void onDisplay(DirectMediaPlayer mediaPlayer, int[] data) {

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(data.length * 4).order(ByteOrder.nativeOrder());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(data);

            try {

//                long[] nativeData = new long[]{0, (long) w, (long) h, Gdx2DPixmap.GDX2D_FORMAT_RGBA8888};
                long[] nativeData = new long[]{0, (long) w, (long) h, Gdx2DPixmap.GDX2D_FORMAT_RGB888};
                Gdx2DPixmap pixmapData = new Gdx2DPixmap(byteBuffer, nativeData);
                pixmap = new Pixmap(pixmapData);
            } catch (Exception e) {
                pixmap = null;
                throw new GdxRuntimeException("Couldn't load pixmap from image data", e);
            }

        }
    }

    private final class TestBufferFormatCallback implements BufferFormatCallback {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
//            return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[] { sourceWidth * 4 }, new int[] { sourceHeight });
            return new RV32BufferFormat((int) w, (int) h);
        }

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}
