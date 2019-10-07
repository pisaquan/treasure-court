package com.sancai.oa.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 压缩Excel工具类
 *
 * @author fanjing
 * @date 2019/8/22
 */
public class ZipUtil {

    private static final Log log = LogFactory.getLog(ZipUtil.class);


    /**
     * 压缩文件
     *
     * @param srcfile File[] 需要压缩的文件列表
     * @param zipfile File 压缩后的文件
     */
    public static void zipFiles(List<File> srcfile, File zipfile) {

        byte[] buf = new byte[1024];
        try {
            // Create the ZIP file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
            // Compress the files
            for (int i = 0; i < srcfile.size(); i++) {
                File file = srcfile.get(i);
                FileInputStream in = new FileInputStream(file);
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(file.getName()));
                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                // Complete the entry
                out.closeEntry();
                in.close();
            }
            // Complete the ZIP file
            out.close();
        } catch (IOException e) {
            log.error("ZipUtil zipFiles exception:" + e);
        }
    }

    /**
     * 删除目录中的Excel文件
     *
     * @param fileNames 文件路径集合
     * @return 返回是否删除成功
     */
    public static boolean deleteExcel(List<String> fileNames) {
        boolean flag = false;
        if (CollectionUtils.isEmpty(fileNames)) {
            flag = false;
        }
        for (int i = 0; i < fileNames.size(); i++) {
            String filePath = fileNames.get(i);
            if (StringUtils.isNotBlank(filePath)) {
                new File(filePath).delete();
                flag = true;
            }
        }
        return flag;
    }


    /**
     * Excel模板路径转换
     *
     * @param path
     * @return
     */
    public static String convertTemplatePath(String path) {
        // 如果是windows 则直接返回
//        if (System.getProperties().getProperty("os.name").contains("Windows")) {
//            return path;
//        }

        Resource resource = new ClassPathResource(path);
        FileOutputStream fileOutputStream = null;
        // 将模版文件写入到 tomcat临时目录
        String folder = ZipUtil.class.getResource("/").getPath();
        File tempFile = new File(folder + File.separator + path);
        // System.out.println("文件路径：" + tempFile.getPath());
        // 文件存在时 不再写入
        if (tempFile.exists()) {
            return tempFile.getPath();
        }
        File parentFile = tempFile.getParentFile();
        // 判断父文件夹是否存在
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(resource.getInputStream());
            fileOutputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[10240];
            int len = 0;
            while ((len = bufferedInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tempFile.getPath();
    }
}
