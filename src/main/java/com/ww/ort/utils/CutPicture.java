package com.ww.ort.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CutPicture {
    @Autowired
    static Environment env;

    /**
     *
     * @param filePath 切割的图片路径
     * @param outPutPath 保存的路径
     * @param top 顶部切割比例(0.00-1.00)
     * @param bottom 底部切割比例(0.00-1.00)
     * @param left 左边切割比例(0.00-1.00)
     * @param right 左边切割比例(0.00-1.00)
     * @return true(切割成功)
     */
    public static Boolean cutPicture(String filePath,String outPutPath,Double top,Double bottom,Double left,Double right){
        File preview = new File(filePath);
        if (preview.exists()) {
            BufferedImage bufImage = null;
            try {
                bufImage = ImageIO.read(preview);
                // 获取图片的宽高
                final int width = bufImage.getWidth();
                final int height = bufImage.getHeight();

                BufferedImage bufImage2 = bufImage.getSubimage((int)(width*left), (int)(height*top), (int)(width-(width*left)-(width*right)), (int)(height-(height*top)-(height*bottom)));

                String fileType="PNG";
                if(filePath.indexOf(".jpg")!=-1){
                    fileType="JPEG";
                }
              return   ImageIO.write(bufImage2, fileType, new File(outPutPath));


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }


    public static void main(String[] args) {
      System.out.println(cutPicture("D:\\try66.jpg","D:\\t1.jpg",0.1,0.05,0.06,0.01));
    }
}
