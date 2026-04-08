package com.ww.ort.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ITextPdf {
    public static void main(String[] args) {
        List<String> ddd=new ArrayList<>();
        ddd.add("D:\\try3.png");
        ddd.add("D:\\try5.png");
        ddd.add("D:\\try68.png");
//        pdf(ddd);
    }

    public static String pdf(List<String> paths,String savePath) {
        PdfContentByte waterMar;
        try {
            //文档对象  实现A4纸页面
            Document document = new Document(PageSize.A1);
            //document.setMarginMirroring(true);
            //设置文档的页边距就是距离页面边上的距离，分别为：左边距，右边距，上边距，下边距
            document.setMargins(70, 70, 20, 10);
            //这个是生成pdf的位置以及名称
            String fn= System.currentTimeMillis() + ".pdf";
            String filename = savePath+"\\" +fn;
            PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(filename));
            //打开文档
            document.open();
            // 加载字体，因为我们标题和正文可能用的不是同一种字体，所以我们创建两种字体
            //有其他需求的就可以创建更多的字体
            //字体都是电脑自带的，如果你想要的电脑没有，就去网上下载
            //创建标题字体
            BaseFont title = BaseFont.createFont("c://windows//fonts//simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            //创建正文字体
            BaseFont bf = BaseFont.createFont("c://windows//fonts//simfang.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            //上面是基础的字体，代表使用哪一种字体，下面设置的是字体的字号，粗细等等属性
            //使用上面的title 字体 加粗，这个是标题字体
            Font titleFont = new Font(title, 22, Font.BOLD);
            //使用字体，正文字体
            Font font = new Font(bf, 16);
            // 这个是我们动态填充的字体，Font.UNDERLINE带下划线的
            Font underlineFont = new Font(bf, 16, Font.UNDERLINE);

            float pt=1700;
            for(String d:paths){
                // 通过文件名创建图像
                Image image = Image.getInstance(d);
//            image.scaleToFit(500,600);
                System.out.println(image.getHeight());
                System.out.println(image.getWidth());
                System.out.println(document.getPageSize().getWidth()*25.4/72);
                System.out.println(document.getPageSize().getWidth());

                image.setAbsolutePosition(((document.getPageSize().getWidth()-image.getWidth())/2),pt);
                document.add(image);
                pt=pt-image.getHeight()-50;
            }

            // -------------------设置 段落 ----------------
            //这里面可以设置段落和短语（块），如果是那种固定文字的段落就可以用段落
            //如果我们需要生成的段落里有需要动态填充的就用块，一点一点的拼起来，直到达到我们的效果
            //段落
            Paragraph p = null;
            p = new Paragraph("ORT报告", titleFont);
            p.setLeading(30);

            p.setAlignment(Element.ALIGN_CENTER);//设置对齐方式，这个是居中对齐
            document.add(p);

            p = new Paragraph();
            //短语
            Phrase ph = new Phrase();



            //设置和上行的间隔
            p.setSpacingBefore(10);
            //p.setLeading(20);
            document.add(p);



            p = new Paragraph();
            ph = new Phrase();
            String date = TimeUtil.getNowTimeByNormal();
            ph.add(new Chunk(date.substring(0, 4) + " ", underlineFont));//年
            ph.add(new Chunk("年", font));
            ph.add(new Chunk(date.substring(5, 7) + " ", underlineFont));//月
            ph.add(new Chunk("月", font));
            ph.add(new Chunk(date.substring(8, 10) + " ", underlineFont));//日
            ph.add(new Chunk("日", font));
            p.add(ph);
//            p.setPaddingTop(800);
            p.setSpacingBefore(5);
            p.setAlignment(Element.ALIGN_RIGHT);//设置对齐方式
            p.setLeading(30);
            document.add(p);

            document.close();
            pdfWriter.close();
            System.out.println("创建成功！");
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
