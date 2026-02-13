package org.cookpro.utils;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * 图片验证码生成工具类
 */
public class ValidateCodeUtils {
    // 验证码图片宽度
    private static final int WIDTH = 100;
    // 验证码图片高度
    private static final int HEIGHT = 40;
    // 验证码字符个数
    private static final int CODE_COUNT = 4;
    // 干扰线数量
    private static final int LINE_COUNT = 5;
    // 随机数对象
    private static final Random RANDOM = new Random();

    /**
     * 生成随机验证码字符串（数字+字母）
     */
    public static String generateCode() {
        // 验证码字符库
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_COUNT; i++) {
            code.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * 根据验证码字符串生成图片流
     */
    public static byte[] generateCodeImage(String code) throws IOException {
        // 1. 创建图片缓冲区
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        // 2. 获取画笔
        Graphics g = image.getGraphics();
        // 3. 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        // 4. 设置边框
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);
        // 5. 绘制干扰线
        for (int i = 0; i < LINE_COUNT; i++) {
            g.setColor(getRandomColor());
            g.drawLine(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT),
                    RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT));
        }
        // 6. 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 25));
        // 7. 绘制验证码字符（每个字符随机颜色、位置）
        for (int i = 0; i < CODE_COUNT; i++) {
            g.setColor(getRandomColor());
            // 字符x坐标：15 + i*20（分散显示），y坐标：随机偏移避免垂直对齐
            g.drawString(String.valueOf(code.charAt(i)),
                    15 + i * 20,
                    28 + RANDOM.nextInt(6));
        }
        // 8. 绘制噪点（可选，增强安全性）
        for (int i = 0; i < 20; i++) {
            g.setColor(getRandomColor());
            g.fillRect(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 2, 2);
        }
        // 9. 释放画笔
        g.dispose();
        // 10. 将图片转为字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "JPEG", bos);
        return bos.toByteArray();
    }

    /**
     * 生成随机颜色
     */
    private static Color getRandomColor() {
        return new Color(RANDOM.nextInt(200), RANDOM.nextInt(200), RANDOM.nextInt(200));
    }
}
