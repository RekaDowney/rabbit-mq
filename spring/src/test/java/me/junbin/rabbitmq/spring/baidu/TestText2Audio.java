package me.junbin.rabbitmq.spring.baidu;

import javazoom.jl.player.Player;
import org.junit.Test;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:zhongjunbin@chinamaincloud.com">发送邮件</a>
 * @createDate : 2018/5/30 16:27
 * @description :
 */
public class TestText2Audio {

    @Test
    public void test01() throws Exception {
        byte[] data = Text2AudioUtils.text2audio("一行白鹭上青天", Text2AudioUtils.Language.ZH, 100, 3);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            Player player = new Player(inputStream);
            player.play();
        }
    }

    @Test
    public void test02() throws Exception {
        Path path = Paths.get("M:/Downloaded/Browser/Chrome/MP3/尽头 - 赵方婧.mp3");
        try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            Player player = new Player(inputStream);
            player.play();
        }
    }

    @Test
    public void testJmf() throws Exception {
        Path path = Paths.get("M:/Downloaded/Browser/Chrome/MP3/尽头 - 赵方婧.mp3");
        try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
            final AudioInputStream in = AudioSystem.getAudioInputStream(inputStream);
            AudioFormat outFormat = getOutFormat(in.getFormat());
            final DataLine.Info info = new DataLine.Info(SourceDataLine.class, outFormat);

            final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);

            if (line != null) {
                line.open(outFormat);
                line.start();
                stream(AudioSystem.getAudioInputStream(outFormat, in), line);
                line.drain();
                line.stop();
            }
        }
    }

    private AudioFormat getOutFormat(AudioFormat inFormat) {
        final int ch = inFormat.getChannels();
        final float rate = inFormat.getSampleRate();
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false);
    }

    private void stream(AudioInputStream in, SourceDataLine line)
            throws IOException {
        final byte[] buffer = new byte[65536];
        for (int n = 0; n != -1; n = in.read(buffer, 0, buffer.length)) {
            line.write(buffer, 0, n);
        }
    }


}