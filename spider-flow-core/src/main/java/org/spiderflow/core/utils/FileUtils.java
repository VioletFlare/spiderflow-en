package org.spiderflow.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.*;

/**
 * File Management Tools
 * 
 * @author ruoyi
 */
public class FileUtils
{
    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static String FILENAME_PATTERN = "[a-zA-Z0-9_\\-\\|\\.\\u4e00-\\u9fa5]+";

    /**
     * Output version informationbyteThe following text is a sample answer to the question "What is your name?":My name is John Doe.
     * 
     * @param filePath File Path
     * @param os Output stream
     * @return
     */
    public static void writeBytes(String filePath, OutputStream os) throws IOException
    {
        FileInputStream fis = null;
        try
        {
            File file = new File(filePath);
            if (!file.exists())
            {
                throw new FileNotFoundException(filePath);
            }
            fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int length;
            while ((length = fis.read(b)) > 0)
            {
                os.write(b, 0, length);
            }
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            if (os != null)
            {
                try
                {
                    os.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * Remove Files
     * 
     * @param filePath Files
     * @return
     */
    public static boolean deleteFile(String filePath)
    {
        boolean flag = false;
        File file = new File(filePath);
        // Delete the given paths if they are not already deleted.
        if (file.isFile() && file.exists())
        {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * File name verification
     * 
     * @param filename File name
     * @return true Normal false Illegal
     */
    public static boolean isValidFilename(String filename)
    {
        return filename.matches(FILENAME_PATTERN);
    }

    /**
     * Download file name recode
     * 
     * @param request Request Object
     * @param fileName File name
     * @return File name after encoding
     */
    public static String setFileDownloadHeader(HttpServletRequest request, String fileName)
            throws UnsupportedEncodingException
    {
        final String agent = request.getHeader("USER-AGENT");
        String filename = fileName;
        if (agent.contains("MSIE"))
        {
            // IE浏览器
            filename = URLEncoder.encode(filename, "utf-8");
            filename = filename.replace("+", " ");
        }
        else if (agent.contains("Firefox"))
        {
            // Firefox
            filename = new String(fileName.getBytes(), "ISO8859-1");
        }
        else if (agent.contains("Chrome"))
        {
            // google浏览器
            filename = URLEncoder.encode(filename, "utf-8");
        }
        else
        {
            // Other Browsers
            filename = URLEncoder.encode(filename, "utf-8");
        }
        return filename;
    }

    /**
     * Downloading started
     */
    public enum DownloadStatus {
        URL_ERROR(1, "URLThe text you entered is not valid. Please correct it and try again."),
        FILE_EXIST(2,"File Exists"),
        TIME_OUT(3,"Connection timed out"),
        DOWNLOAD_FAIL(4,"Download failed"),
        DOWNLOAD_SUCCESS(5,"Download started");

        private int code;

        private String name;

        DownloadStatus(int code, String name){
            this.code = code;
            this.name = name;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static DownloadStatus downloadFile(String savePath, String fileUrl, boolean downNew) {
        URL urlfile = null;
        HttpURLConnection httpUrl = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        if (fileUrl.startsWith("//")) {
            fileUrl = "http:" + fileUrl;
        }
        String fileName;
        try {
            urlfile = new URL(fileUrl);
            String urlPath = urlfile.getPath();
            fileName = urlPath.substring(urlPath.lastIndexOf("/") + 1);
        } catch (MalformedURLException e) {
            logger.error("URL1 hour before appointment", e);
            return DownloadStatus.URL_ERROR;
        }
        File path = new File(savePath);
        if (!path.exists()) {
            path.mkdirs();
        }
        File file = new File(savePath + File.separator + fileName);
        if (file.exists()) {
            if (downNew) {
                file.delete();
            } else {
                logger.info("The file already exists. Do not download it again.！");
                return DownloadStatus.FILE_EXIST;
            }
        }
        try {
            httpUrl = (HttpURLConnection) urlfile.openConnection();
            httpUrl.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0");
            //Accessibility Support
            httpUrl.setReadTimeout(60000);
            //Connection timed out
            httpUrl.setConnectTimeout(60000);
            httpUrl.connect();
            bis = new BufferedInputStream(httpUrl.getInputStream());
            bos = new BufferedOutputStream(new FileOutputStream(file));
            int len = 2048;
            byte[] b = new byte[len];
            long readLen = 0;
            while ((len = bis.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            logger.info("Remote file download successful:" + fileUrl);
            bos.flush();
            bis.close();
            httpUrl.disconnect();
            return DownloadStatus.DOWNLOAD_SUCCESS;
        } catch (SocketTimeoutException e) {
            logger.error("Read file timed out", e);
            return DownloadStatus.TIME_OUT;
        } catch (Exception e) {
            logger.error("Failed to download remote file.", e);
            return DownloadStatus.DOWNLOAD_FAIL;
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                logger.error("Download error", e);
            }
        }
    }
}
